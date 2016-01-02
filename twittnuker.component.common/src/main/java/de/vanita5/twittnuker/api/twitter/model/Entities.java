/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.api.twitter.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.Arrays;

/**
 * Created by mariotaku on 15/3/31.
 */
@JsonObject
public class Entities {
    @JsonField(name = "hashtags")
    HashtagEntity[] hashtags;

    @JsonField(name = "user_mentions")
    UserMentionEntity[] userMentions;

    @JsonField(name = "urls")
    UrlEntity[] urls;

    @JsonField(name = "media")
    MediaEntity[] media;

    public de.vanita5.twittnuker.api.twitter.model.HashtagEntity[] getHashtags() {
        return hashtags;
    }

    public de.vanita5.twittnuker.api.twitter.model.UserMentionEntity[] getUserMentions() {
        return userMentions;
    }

    public UrlEntity[] getUrls() {
        return urls;
    }

    public MediaEntity[] getMedia() {
        return media;
    }

    @Override
    public String toString() {
        return "Entities{" +
                "hashtags=" + Arrays.toString(hashtags) +
                ", userMentions=" + Arrays.toString(userMentions) +
                ", urls=" + Arrays.toString(urls) +
                ", media=" + Arrays.toString(media) +
                '}';
    }
}