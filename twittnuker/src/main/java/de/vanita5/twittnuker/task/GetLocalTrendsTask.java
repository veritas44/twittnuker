/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2016 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2016 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.task;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import de.vanita5.twittnuker.api.twitter.Twitter;
import de.vanita5.twittnuker.api.twitter.TwitterException;
import de.vanita5.twittnuker.api.twitter.model.Trends;
import de.vanita5.twittnuker.provider.TwidereDataStore.CachedTrends;

import java.util.List;

public class GetLocalTrendsTask extends GetTrendsTask {

    private final int woeid;

    public GetLocalTrendsTask(final Context context, final long accountId, final int woeid) {
        super(context, accountId);
        this.woeid = woeid;
    }

    @Override
    public List<Trends> getTrends(@NonNull final Twitter twitter) throws TwitterException {
        return twitter.getLocationTrends(woeid);
    }

    @Override
    protected Uri getContentUri() {
        return CachedTrends.Local.CONTENT_URI;
    }

}