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

package de.vanita5.twittnuker.activity

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.app.DialogFragment
import android.support.v4.util.LongSparseArray
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.TextView
import nl.komponents.kovenant.combine.and
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.alwaysUi
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.ktextension.getTypedArray
import org.mariotaku.ktextension.set
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.library.twitter.model.Location
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.constant.IntentConstants.*
import de.vanita5.twittnuker.extension.applyTheme
import de.vanita5.twittnuker.fragment.BaseDialogFragment
import de.vanita5.twittnuker.fragment.ProgressDialogFragment
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.util.MicroBlogAPIFactory
import java.lang.ref.WeakReference
import java.text.Collator
import java.util.*


class TrendsLocationSelectorActivity : BaseActivity() {

    private val accountKey: UserKey?
        get() = intent.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val accountKey = this.accountKey ?: run {
            finish()
            return
        }

        if (savedInstanceState != null) return
        val weakThis = WeakReference(this)
        executeAfterFragmentResumed {
            ProgressDialogFragment.show(it.supportFragmentManager, PROGRESS_FRAGMENT_TAG).isCancelable = false
        } and task {
            val activity = weakThis.get() ?: throw InterruptedException()
            val twitter = MicroBlogAPIFactory.getInstance(activity, accountKey)
                    ?: throw MicroBlogException("No account")
            val map = LocationsMap(Locale.getDefault())
            twitter.availableTrends.forEach { location -> map.put(location) }
            return@task map.pack()
        }.successUi { result ->
            val df = TrendsLocationDialogFragment()
            df.arguments = org.mariotaku.ktextension.Bundle {
                this[EXTRA_DATA] = result
            }
            df.show(supportFragmentManager, "trends_location_selector")
        }.failUi {
            val activity = weakThis.get() ?: return@failUi
            activity.finish()
        }.alwaysUi {
            val activity = weakThis.get() ?: return@alwaysUi
            activity.executeAfterFragmentResumed { activity ->
                val fm = activity.supportFragmentManager
                val df = fm.findFragmentByTag(PROGRESS_FRAGMENT_TAG) as? DialogFragment
                df?.dismiss()
            }
        }
    }

    class TrendsLocationDialogFragment : BaseDialogFragment() {
        private val list: Array<LocationsMap.LocationsData> get() = arguments.getTypedArray(EXTRA_DATA,
                LocationsMap.LocationsData.CREATOR)

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val selectorBuilder = AlertDialog.Builder(context)
            selectorBuilder.setTitle(R.string.trends_location)
            selectorBuilder.setView(R.layout.dialog_trends_location_selector)
            selectorBuilder.setNegativeButton(android.R.string.cancel, null)
            val dialog = selectorBuilder.create()
            dialog.setOnShowListener {
                it as AlertDialog
                it.applyTheme()
                val listView = it.findViewById(R.id.expandable_list) as ExpandableListView
                val adapter = ExpandableTrendLocationsListAdapter(context)
                adapter.data = list
                listView.setAdapter(adapter)
                listView.setOnGroupClickListener(ExpandableListView.OnGroupClickListener { _, _, groupPosition, _ ->
                    val group = adapter.getGroup(groupPosition)
                    if (group.woeid.toLong() == WORLDWIDE) {
                        setActivityResult(group)
                        dismiss()
                        return@OnGroupClickListener true
                    }
                    false
                })
                listView.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
                    val child = adapter.getChild(groupPosition, childPosition)
                    setActivityResult(child)
                    dismiss()
                    return@setOnChildClickListener true
                }
            }
            dialog.show()
            return dialog
        }

        private fun setActivityResult(location: Location) {
            activity?.setResult(Activity.RESULT_OK, Intent().putExtra(EXTRA_LOCATION, location))
        }

        override fun onDismiss(dialog: DialogInterface?) {
            super.onDismiss(dialog)
            activity?.finish()
        }

        override fun onCancel(dialog: DialogInterface?) {
            super.onCancel(dialog)
            activity?.finish()
        }
    }

    internal class ExpandableTrendLocationsListAdapter(context: Context) : BaseExpandableListAdapter() {

        private val inflater = LayoutInflater.from(context)
        var data: Array<LocationsMap.LocationsData>? = null
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun getGroupCount(): Int {
            return data?.size ?: 0
        }

        override fun getChildrenCount(groupPosition: Int): Int {
            return data!![groupPosition].children.size
        }

        override fun getGroup(groupPosition: Int): Location {
            return data!![groupPosition].root
        }

        override fun getChild(groupPosition: Int, childPosition: Int): Location {
            return data!![groupPosition].children[childPosition]
        }

        override fun getGroupId(groupPosition: Int): Long {
            return getGroup(groupPosition).woeid.toLong()
        }

        override fun getChildId(groupPosition: Int, childPosition: Int): Long {
            return getChild(groupPosition, childPosition).woeid.toLong()
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup): View {
            val view: View
            if (convertView != null) {
                view = convertView
            } else {
                view = inflater.inflate(android.R.layout.simple_expandable_list_item_1, parent, false)
            }
            (view.findViewById(android.R.id.text1) as TextView).text = getGroup(groupPosition).name
            return view
        }

        override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup): View {
            val view: View
            if (convertView != null) {
                view = convertView
            } else {
                view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false)
            }
            val location = getChild(groupPosition, childPosition)
            val text1 = view.findViewById(android.R.id.text1) as TextView
            if (location.parentId == WORLDWIDE) {
                text1.setText(R.string.location_countrywide)
            } else {
                text1.text = location.name
            }
            return view
        }

        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
            return true
        }

    }


    private class LocationComparator(private val collator: Collator) : Comparator<Location> {

        private fun isCountryOrWorldwide(location: Location): Boolean {
            val parentId = location.parentId
            return parentId == EMPTY || parentId == WORLDWIDE
        }

        override fun compare(lhs: Location, rhs: Location): Int {
            if (isCountryOrWorldwide(lhs)) return Integer.MIN_VALUE
            if (isCountryOrWorldwide(rhs)) return Integer.MAX_VALUE
            return collator.compare(lhs.name, rhs.name)
        }
    }

    internal class LocationsMap(locale: Locale) {

        val map = LongSparseArray<MutableList<Location>>()
        val parents = LongSparseArray<Location>()
        private val comparator = LocationComparator(Collator.getInstance(locale))

        fun put(location: Location) {
            val parentId = location.parentId
            if (parentId == EMPTY || parentId == WORLDWIDE) {
                putParent(location)
            } else {
                putChild(parentId, location)
            }
        }

        fun putParent(location: Location) {
            val woeid = location.woeid.toLong()
            parents.put(woeid, location)
            val list = getList(woeid)
            // Don't add child for 'worldwide'
            if (woeid != WORLDWIDE) {
                addToList(list, location)
            }
        }

        fun putChild(parentId: Long, location: Location) {
            addToList(getList(parentId), location)
        }

        fun getList(parentId: Long): MutableList<Location> {
            return map.get(parentId) ?: run {
                val l = ArrayList<Location>()
                map.put(parentId, l)
                return@run l
            }
        }

        fun addToList(list: MutableList<Location>, location: Location) {
            val loc = Collections.binarySearch(list, location, comparator)
            if (loc < 0) {
                list.add(-(loc + 1), location)
            }
        }

        fun pack(): Array<LocationsData> {
            return (0 until map.size()).mapNotNull { i ->
                val parent = parents.get(map.keyAt(i)) ?: return@mapNotNull null
                return@mapNotNull LocationsData(parent, map.valueAt(i).toTypedArray())
            }.toTypedArray()
        }

        data class LocationsData(val root: Location, val children: Array<Location>) : Parcelable {
            override fun describeContents(): Int = 0

            override fun writeToParcel(dest: Parcel, flags: Int) {
                dest.writeParcelable(root, flags)
                dest.writeTypedArray(children, flags)
            }

            companion object {
                val CREATOR = object : Parcelable.Creator<LocationsData> {
                    override fun createFromParcel(source: Parcel): LocationsData {
                        val root = source.readParcelable<Location>(Location::class.java.classLoader)
                        val children = source.createTypedArray(Location.CREATOR)
                        return LocationsData(root, children)
                    }

                    override fun newArray(size: Int): Array<out LocationsData?> {
                        return arrayOfNulls<LocationsData>(size)
                    }

                }
            }
        }
    }

    companion object {
        private const val PROGRESS_FRAGMENT_TAG = "load_location_progress"
        private const val EMPTY: Long = 0
        private const val WORLDWIDE: Long = 1
    }
}