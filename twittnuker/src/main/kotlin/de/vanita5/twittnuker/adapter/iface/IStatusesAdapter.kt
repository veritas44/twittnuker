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

package de.vanita5.twittnuker.adapter.iface

import de.vanita5.twittnuker.annotation.PreviewStyle
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.util.TwidereLinkify
import de.vanita5.twittnuker.view.holder.iface.IStatusViewHolder

/**
 *
 */
interface IStatusesAdapter<in Data> : IContentAdapter, IGapSupportedAdapter {

    @TwidereLinkify.HighlightStyle
    val linkHighlightingStyle: Int

    val lightFont: Boolean

    @PreviewStyle
    val mediaPreviewStyle: Int

    val twidereLinkify: TwidereLinkify

    val mediaPreviewEnabled: Boolean

    val nameFirst: Boolean

    val sensitiveContentEnabled: Boolean

    val showAccountsColor: Boolean

    val useStarsForLikes: Boolean

    val statusClickListener: IStatusViewHolder.StatusClickListener?

    fun isCardActionsShown(position: Int): Boolean

    fun showCardActions(position: Int)

    fun isFullTextVisible(position: Int): Boolean

    fun setFullTextVisible(position: Int, visible: Boolean)

    fun setData(data: Data?): Boolean

    /**
     * @param raw Count hidden (filtered) item if `true `
     */
    fun getStatusCount(raw: Boolean = false): Int

    fun getStatus(position: Int, raw: Boolean = false): ParcelableStatus

    fun getStatusId(position: Int, raw: Boolean = false): String

    fun getStatusTimestamp(position: Int, raw: Boolean = false): Long

    fun getStatusPositionKey(position: Int, raw: Boolean = false): Long

    fun getAccountKey(position: Int, raw: Boolean = false): UserKey

    fun findStatusById(accountKey: UserKey, statusId: String): ParcelableStatus?

}