/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.task

import android.content.Context
import android.content.SharedPreferences
import com.squareup.otto.Bus
import com.twitter.Extractor
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.kpreferences.KPreferences
import org.mariotaku.restfu.http.RestHttpClient
import de.vanita5.twittnuker.model.DefaultFeatures
import de.vanita5.twittnuker.util.AsyncTwitterWrapper
import de.vanita5.twittnuker.util.ErrorInfoStore
import de.vanita5.twittnuker.util.ReadStateManager
import de.vanita5.twittnuker.util.UserColorNameManager
import de.vanita5.twittnuker.util.cache.JsonCache
import de.vanita5.twittnuker.util.dagger.GeneralComponent
import de.vanita5.twittnuker.util.media.MediaPreloader
import de.vanita5.twittnuker.util.premium.ExtraFeaturesService
import de.vanita5.twittnuker.util.schedule.StatusScheduleProvider
import de.vanita5.twittnuker.util.sync.SyncPreferences
import de.vanita5.twittnuker.util.sync.TimelineSyncManager
import javax.inject.Inject


abstract class BaseAbstractTask<Params, Result, Callback>(val context: Context) : AbstractTask<Params, Result, Callback>() {

    @Inject
    lateinit var bus: Bus
    @Inject
    lateinit var microBlogWrapper: AsyncTwitterWrapper
    @Inject
    lateinit var mediaPreloader: MediaPreloader
    @Inject
    lateinit var preferences: SharedPreferences
    @Inject
    lateinit var kPreferences: KPreferences
    @Inject
    lateinit var manager: UserColorNameManager
    @Inject
    lateinit var errorInfoStore: ErrorInfoStore
    @Inject
    lateinit var readStateManager: ReadStateManager
    @Inject
    lateinit var userColorNameManager: UserColorNameManager
    @Inject
    lateinit var extraFeaturesService: ExtraFeaturesService
    @Inject
    lateinit var restHttpClient: RestHttpClient
    @Inject
    lateinit var defaultFeatures: DefaultFeatures
    @Inject
    lateinit var scheduleProviderFactory: StatusScheduleProvider.Factory
    @Inject
    lateinit var extractor: Extractor
    @Inject
    lateinit var syncPreferences: SyncPreferences
    @Inject
    lateinit var timelineSyncManagerFactory: TimelineSyncManager.Factory
    @Inject
    lateinit var jsonCache: JsonCache

    val scheduleProvider: StatusScheduleProvider?
        get() = scheduleProviderFactory.newInstance(context)

    init {
        injectMembers()
    }

    private fun injectMembers() {
        @Suppress("UNCHECKED_CAST")
        GeneralComponent.get(context).inject(this as BaseAbstractTask<Any, Any, Any>)
    }
}