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

package de.vanita5.twittnuker.util.dagger

import android.content.Context
import android.content.SharedPreferences
import com.twitter.Validator
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.Dns
import org.mariotaku.kpreferences.KPreferences
import org.mariotaku.restfu.http.RestHttpClient
import de.vanita5.twittnuker.model.DefaultFeatures
import de.vanita5.twittnuker.util.*
import de.vanita5.twittnuker.util.media.MediaPreloader
import de.vanita5.twittnuker.util.premium.ExtraFeaturesService
import de.vanita5.twittnuker.util.sync.TimelineSyncManager
import javax.inject.Inject

class DependencyHolder internal constructor(context: Context) {
    @Inject
    lateinit var readStateManager: ReadStateManager
        internal set
    @Inject
    lateinit var restHttpClient: RestHttpClient
        internal set
    @Inject
    lateinit var externalThemeManager: ExternalThemeManager
        internal set
    @Inject
    lateinit var activityTracker: ActivityTracker
        internal set
    @Inject
    lateinit var dns: Dns
        internal set
    @Inject
    lateinit var validator: Validator
        internal set
    @Inject
    lateinit var preferences: SharedPreferences
        internal set
    @Inject
    lateinit var connectionPool: ConnectionPool
        internal set
    @Inject
    lateinit var cache: Cache
        internal set
    @Inject
    lateinit var defaultFeatures: DefaultFeatures
        internal set
    @Inject
    lateinit var mediaPreloader: MediaPreloader
        internal set
    @Inject
    lateinit var userColorNameManager: UserColorNameManager
        internal set
    @Inject
    lateinit var kPreferences: KPreferences
        internal set
    @Inject
    lateinit var timelineSyncManagerFactory: TimelineSyncManager.Factory
        internal set

    @Inject
    lateinit var extraFeaturesService: ExtraFeaturesService
        internal set
    @Inject
    lateinit var notificationManager: NotificationManagerWrapper
        internal set

    init {
        GeneralComponent.get(context).inject(this)
    }

    /**
     * For Twittnuker NotificationActionReceiver
     */
    @Inject
    lateinit var asyncTwitterWrapper: AsyncTwitterWrapper
        internal set

    companion object {

        private var sInstance: DependencyHolder? = null

        fun get(context: Context): DependencyHolder {
            if (sInstance != null) return sInstance!!
            sInstance = DependencyHolder(context)
            return sInstance!!
        }
    }
}