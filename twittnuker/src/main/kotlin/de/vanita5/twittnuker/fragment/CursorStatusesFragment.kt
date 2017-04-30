/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.fragment

import android.accounts.AccountManager
import android.accounts.OnAccountsUpdateListener
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.Loader
import android.widget.Toast
import com.bumptech.glide.Glide
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_content_recyclerview.*
import org.mariotaku.ktextension.*
import org.mariotaku.sqliteqb.library.Columns.Column
import org.mariotaku.sqliteqb.library.Expression
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.adapter.ListParcelableStatusesAdapter
import de.vanita5.twittnuker.adapter.iface.ILoadMoreSupportAdapter
import de.vanita5.twittnuker.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_FROM_USER
import de.vanita5.twittnuker.loader.ExtendedObjectCursorLoader
import de.vanita5.twittnuker.model.ParameterizedExpression
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.model.RefreshTaskParam
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.event.*
import de.vanita5.twittnuker.model.pagination.SinceMaxPagination
import de.vanita5.twittnuker.provider.TwidereDataStore.Filters
import de.vanita5.twittnuker.provider.TwidereDataStore.Statuses
import de.vanita5.twittnuker.task.twitter.GetStatusesTask
import de.vanita5.twittnuker.util.DataStoreUtils
import de.vanita5.twittnuker.util.ErrorInfoStore
import de.vanita5.twittnuker.util.Utils
import de.vanita5.twittnuker.util.buildStatusFilterWhereClause

abstract class CursorStatusesFragment : AbsStatusesFragment() {

    override var refreshing: Boolean
        get() = swipeLayout.isRefreshing
        set(value) {
            super.refreshing = value
        }

    override val useSortIdAsReadPosition: Boolean
        get() = false

    override val accountKeys: Array<UserKey>
        get() = Utils.getAccountKeys(context, arguments) ?: DataStoreUtils.getActivatedAccountKeys(context)

    abstract val errorInfoKey: String
    abstract val isFilterEnabled: Boolean
    abstract val notificationType: Int
    abstract val contentUri: Uri

    private var contentObserver: ContentObserver? = null
    private val accountListener: OnAccountsUpdateListener = OnAccountsUpdateListener {
        reloadStatuses()
    }

    override fun onStart() {
        super.onStart()
        if (contentObserver == null) {
            contentObserver = object : ContentObserver(Handler()) {
                override fun onChange(selfChange: Boolean) {
                    reloadStatuses()
                }
            }
            context.contentResolver.registerContentObserver(Filters.CONTENT_URI, true, contentObserver)
        }
        AccountManager.get(context).addOnAccountsUpdatedListenerSafe(accountListener, updateImmediately = false)
        updateRefreshState()
        reloadStatuses()
    }

    override fun onStop() {
        if (contentObserver != null) {
            context.contentResolver.unregisterContentObserver(contentObserver)
            contentObserver = null
        }
        AccountManager.get(context).removeOnAccountsUpdatedListenerSafe(accountListener)
        super.onStop()
    }


    override fun onStatusesLoaded(loader: Loader<List<ParcelableStatus>?>, data: List<ParcelableStatus>?) {
        showContentOrError()
    }

    override fun onCreateStatusesLoader(context: Context, args: Bundle, fromUser: Boolean): Loader<List<ParcelableStatus>?> {
        val uri = contentUri
        val table = DataStoreUtils.getTableNameByUri(uri)!!
        val sortOrder = Statuses.DEFAULT_SORT_ORDER
        val accountKeys = this.accountKeys
        val accountWhere = Expression.inArgs(Column(Statuses.ACCOUNT_KEY), accountKeys.size)
        val filterWhere = getFiltersWhere(table)
        val where: Expression
        if (filterWhere != null) {
            where = Expression.and(accountWhere, filterWhere)
        } else {
            where = accountWhere
        }
        adapter.showAccountsColor = accountKeys.size > 1
        val projection = Statuses.COLUMNS
        val selectionArgs = Array(accountKeys.size) {
            accountKeys[it].toString()
        }
        val expression = processWhere(where, selectionArgs)
        return ExtendedObjectCursorLoader(context, ParcelableStatus::class.java, uri, projection,
                expression.sql, expression.parameters, sortOrder, fromUser).apply {
            isUseCache = false
        }
    }

    override fun createMessageBusCallback(): Any {
        return CursorStatusesBusCallback()
    }

    override fun hasMoreData(loader: Loader<List<ParcelableStatus>?>,
            data: List<ParcelableStatus>?): Boolean {
        return data.isNotNullOrEmpty()
    }

    override fun onCreateAdapter(context: Context): ListParcelableStatusesAdapter {
        return ListParcelableStatusesAdapter(context, Glide.with(this))
    }

    override fun onLoaderReset(loader: Loader<List<ParcelableStatus>?>) {
        adapter.setData(null)
    }

    override fun onLoadMoreContents(@IndicatorPosition position: Long) {
        // Only supports load from end, skip START flag
        if (ILoadMoreSupportAdapter.START in position) return
        super.onLoadMoreContents(position)
        if (position == 0L) return
        getStatuses(object : RefreshTaskParam {
            override val accountKeys by lazy {
                this@CursorStatusesFragment.accountKeys
            }

            override val pagination by lazy {
                val keys = accountKeys.toNulls()
                val maxIds = DataStoreUtils.getOldestStatusIds(context, contentUri, keys)
                val maxSortIds = DataStoreUtils.getOldestStatusSortIds(context, contentUri, keys)
                return@lazy Array(keys.size) { idx ->
                    SinceMaxPagination.maxId(maxIds[idx], maxSortIds[idx])
                }
            }

        })
    }

    override fun triggerRefresh(): Boolean {
        super.triggerRefresh()
        getStatuses(object : RefreshTaskParam {
            override val accountKeys: Array<UserKey> by lazy {
                this@CursorStatusesFragment.accountKeys
            }

            override val pagination by lazy {
                val keys = accountKeys.toNulls()
                val sinceIds = DataStoreUtils.getNewestStatusIds(context, contentUri, keys)
                val sinceSortIds = DataStoreUtils.getNewestStatusSortIds(context, contentUri, keys)
                return@lazy Array(keys.size) { idx ->
                    SinceMaxPagination.sinceId(sinceIds[idx], sinceSortIds[idx])
                }
            }

            override val shouldAbort: Boolean
                get() = context == null
        })
        return true
    }

    override fun saveReadPosition(position: Int) {
        super.saveReadPosition(position)
        if (position == 0) {
            clearNotifications()
        }
    }

    protected fun getFiltersWhere(table: String): Expression? {
        if (!isFilterEnabled) return null
        return buildStatusFilterWhereClause(preferences, table, null)
    }

    protected open fun processWhere(where: Expression, whereArgs: Array<String>): ParameterizedExpression {
        return ParameterizedExpression(where, whereArgs)
    }

    protected abstract fun updateRefreshState()

    protected fun reloadStatuses() {
        if (context == null || isDetached) return
        val args = Bundle()
        val fragmentArgs = arguments
        if (fragmentArgs != null) {
            args.putAll(fragmentArgs)
            args.putBoolean(EXTRA_FROM_USER, true)
        }
        loaderManager.restartLoader(loaderId, args, this)
    }

    private fun showContentOrError() {
        val accountKeys = this.accountKeys
        if (adapter.itemCount > 0) {
            showContent()
        } else if (accountKeys.isNotEmpty()) {
            val errorInfo = ErrorInfoStore.getErrorInfo(context,
                    errorInfoStore[errorInfoKey, accountKeys[0]])
            if (errorInfo != null) {
                showEmpty(errorInfo.icon, errorInfo.message)
            } else {
                showEmpty(R.drawable.ic_info_refresh, getString(R.string.swipe_down_to_refresh))
            }
        } else {
            showError(R.drawable.ic_info_accounts, getString(R.string.message_toast_no_account_selected))
        }
    }

    private fun clearNotifications() {
        if (context != null && userVisibleHint) {
            for (accountKey in accountKeys) {
                twitterWrapper.clearNotificationAsync(notificationType, accountKey)
            }
        }
    }

    protected inner class CursorStatusesBusCallback {

        @Subscribe
        fun notifyGetStatusesTaskChanged(event: GetStatusesTaskEvent) {
            if (event.uri != contentUri) return
            refreshing = event.running
            if (!event.running) {
                setLoadMoreIndicatorPosition(ILoadMoreSupportAdapter.NONE)
                refreshEnabled = true
                showContentOrError()

                val exception = event.exception
                if (exception is GetStatusesTask.GetTimelineException && userVisibleHint) {
                    Toast.makeText(context, exception.getToastMessage(context), Toast.LENGTH_SHORT).show()
                }
            }
        }


        @Subscribe
        fun notifyFavoriteTask(event: FavoriteTaskEvent) {
            if (event.isSucceeded) {
                val status = event.status
                val data = adapterData
                if (status == null || data == null || data.isEmpty()) return
                val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
                val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                if (firstVisiblePosition < 0 || lastVisiblePosition < 0) return
                val startIndex = adapter.statusStartIndex
                for (i in firstVisiblePosition..lastVisiblePosition) {
                    if (status.account_key == adapter.getAccountKey(i) && status.id == adapter.getStatusId(i)) {
                        if (data is MutableList) {
                            data[i - startIndex] = status
                        }
                        return
                    }
                }
                adapter.notifyDataSetChanged()
            }
        }

        @Subscribe
        fun notifyStatusDestroyed(event: StatusDestroyedEvent) {
        }

        @Subscribe
        fun notifyStatusListChanged(event: StatusListChangedEvent) {
            adapter.notifyDataSetChanged()
        }

        @Subscribe
        fun notifyStatusRetweeted(event: StatusRetweetedEvent) {
        }

        @Subscribe
        fun notifyAccountChanged(event: AccountChangedEvent) {

        }

    }

}