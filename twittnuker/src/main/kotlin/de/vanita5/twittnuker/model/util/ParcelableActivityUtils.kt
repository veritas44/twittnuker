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

package de.vanita5.twittnuker.model.util

import de.vanita5.twittnuker.model.ParcelableLiteUser
import de.vanita5.twittnuker.model.UserKey

/**
 * Processing ParcelableActivity
 */
object ParcelableActivityUtils {

    /**
     * @param activity        Activity for processing
     * *
     * @param filtered Those ids will be removed from source_ids.
     * *
     * @param followingOnly   Limit following users in sources
     * *
     * @return true if source ids changed, false otherwise
     */
    fun filterSources(sources: Array<ParcelableLiteUser>?, filtered: Array<UserKey>?,
            followingOnly: Boolean): Array<ParcelableLiteUser>? {
        return sources?.filterNot { user ->
            if (filtered != null && user.key in filtered) {
                return@filterNot true
            }

            if (followingOnly && !user.is_following) {
                return@filterNot true
            }

            return@filterNot false
        }?.toTypedArray()
    }


}