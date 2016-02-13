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

package de.vanita5.twittnuker.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableNoThanks;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import de.vanita5.twittnuker.api.twitter.model.MediaEntity;
import de.vanita5.twittnuker.api.twitter.model.MediaEntity.Size;
import de.vanita5.twittnuker.util.TwitterContentUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

@JsonObject
@ParcelablePlease
public class ParcelableMedia implements Parcelable {
    @SuppressWarnings("NullableProblems")
    @NonNull
    @JsonField(name = "url")
    @ParcelableThisPlease
    public String url;
    @Nullable
    @JsonField(name = "media_url")
    @ParcelableThisPlease
    public String media_url;
    @Nullable
    @JsonField(name = "preview_url")
    @ParcelableThisPlease
    public String preview_url;
    @JsonField(name = "start")
    @ParcelableThisPlease
    public int start;
    @JsonField(name = "end")
    @ParcelableThisPlease
    public int end;
    @Type
    @JsonField(name = "type")
    @ParcelableThisPlease
    public int type;
    @JsonField(name = "width")
    @ParcelableThisPlease
    public int width;
    @JsonField(name = "height")
    @ParcelableThisPlease
    public int height;
    @JsonField(name = "video_info")
    @ParcelableThisPlease
    public VideoInfo video_info;
    @ParcelableNoThanks
    public ParcelableCardEntity card;
    @JsonField(name = "page_url")
    public String page_url;
    @ParcelableThisPlease
    @JsonField(name = "open_browser")
    public boolean open_browser;
    public static final Creator<ParcelableMedia> CREATOR = new Creator<ParcelableMedia>() {
        public ParcelableMedia createFromParcel(Parcel source) {
            ParcelableMedia target = new ParcelableMedia();
            ParcelableMediaParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public ParcelableMedia[] newArray(int size) {
            return new ParcelableMedia[size];
        }
    };

    public ParcelableMedia() {

    }


    public ParcelableMedia(ParcelableMediaUpdate update) {
        media_url = update.uri;
        url = update.uri;
        preview_url = update.uri;
        type = update.type;
    }


    @OnJsonParseComplete
    void onParseComplete() {
        if (this.page_url != null) {
            this.url = this.page_url;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParcelableMedia media = (ParcelableMedia) o;

        if (start != media.start) return false;
        if (end != media.end) return false;
        if (type != media.type) return false;
        if (width != media.width) return false;
        if (height != media.height) return false;
        if (!url.equals(media.url)) return false;
        if (media_url != null ? !media_url.equals(media.media_url) : media.media_url != null)
            return false;
        if (preview_url != null ? !preview_url.equals(media.preview_url) : media.preview_url != null)
            return false;
        if (video_info != null ? !video_info.equals(media.video_info) : media.video_info != null)
            return false;
        if (card != null ? !card.equals(media.card) : media.card != null) return false;
        return !(page_url != null ? !page_url.equals(media.page_url) : media.page_url != null);

    }

    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + (media_url != null ? media_url.hashCode() : 0);
        result = 31 * result + (preview_url != null ? preview_url.hashCode() : 0);
        result = 31 * result + start;
        result = 31 * result + end;
        result = 31 * result + type;
        result = 31 * result + width;
        result = 31 * result + height;
        result = 31 * result + (video_info != null ? video_info.hashCode() : 0);
        result = 31 * result + (card != null ? card.hashCode() : 0);
        result = 31 * result + (page_url != null ? page_url.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ParcelableMedia{" +
                "url='" + url + '\'' +
                ", media_url='" + media_url + '\'' +
                ", preview_url='" + preview_url + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", type=" + type +
                ", width=" + width +
                ", height=" + height +
                ", video_info=" + video_info +
                ", card=" + card +
                ", page_url='" + page_url + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableMediaParcelablePlease.writeToParcel(this, dest, flags);
    }

    @IntDef({Type.UNKNOWN, Type.IMAGE, Type.VIDEO, Type.ANIMATED_GIF, Type.CARD_ANIMATED_GIF,
            Type.EXTERNAL_PLAYER, Type.VARIABLE_TYPE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {

        @Type
        int UNKNOWN = 0;
        @Type
        int IMAGE = 1;
        @Type
        int VIDEO = 2;
        @Type
        int ANIMATED_GIF = 3;
        @Type
        int CARD_ANIMATED_GIF = 4;
        @Type
        int EXTERNAL_PLAYER = 5;
        @Type
        int VARIABLE_TYPE = 6;
    }

    @ParcelablePlease
    @JsonObject
    public static class VideoInfo implements Parcelable {

        @ParcelableThisPlease
        @JsonField(name = "variants")
        public Variant[] variants;
        @ParcelableThisPlease
        @JsonField(name = "duration")
        public long duration;
        public static final Creator<VideoInfo> CREATOR = new Creator<VideoInfo>() {
            public VideoInfo createFromParcel(Parcel source) {
                VideoInfo target = new VideoInfo();
                ParcelableMedia$VideoInfoParcelablePlease.readFromParcel(target, source);
                return target;
            }

            public VideoInfo[] newArray(int size) {
                return new VideoInfo[size];
            }
        };

        public VideoInfo() {

        }

        public VideoInfo(MediaEntity.VideoInfo videoInfo) {
            variants = Variant.fromMediaEntityVariants(videoInfo.getVariants());
            duration = videoInfo.getDuration();
        }

        public static VideoInfo fromMediaEntityInfo(MediaEntity.VideoInfo videoInfo) {
            if (videoInfo == null) return null;
            return new VideoInfo(videoInfo);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            VideoInfo videoInfo = (VideoInfo) o;

            if (duration != videoInfo.duration) return false;
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            return Arrays.equals(variants, videoInfo.variants);

        }

        @Override
        public int hashCode() {
            int result = variants != null ? Arrays.hashCode(variants) : 0;
            result = 31 * result + (int) (duration ^ (duration >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return "VideoInfo{" +
                    "variants=" + Arrays.toString(variants) +
                    ", duration=" + duration +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            ParcelableMedia$VideoInfoParcelablePlease.writeToParcel(this, dest, flags);
        }

        @ParcelablePlease
        @JsonObject
        public static class Variant implements Parcelable {
            @ParcelableThisPlease
            @JsonField(name = "content_type")
            public String content_type;
            @ParcelableThisPlease
            @JsonField(name = "url")
            public String url;
            @ParcelableThisPlease
            @JsonField(name = "bitrate")
            public long bitrate;
            public static final Creator<Variant> CREATOR = new Creator<Variant>() {
                public Variant createFromParcel(Parcel source) {
                    Variant target = new Variant();
                    ParcelableMedia$VideoInfo$VariantParcelablePlease.readFromParcel(target, source);
                    return target;
                }

                public Variant[] newArray(int size) {
                    return new Variant[size];
                }
            };

            public Variant() {
            }

            public Variant(MediaEntity.VideoInfo.Variant entityVariant) {
                content_type = entityVariant.getContentType();
                url = entityVariant.getUrl();
                bitrate = entityVariant.getBitrate();
            }

            public static Variant[] fromMediaEntityVariants(MediaEntity.VideoInfo.Variant[] entityVariants) {
                if (entityVariants == null) return null;
                final Variant[] variants = new Variant[entityVariants.length];
                for (int i = 0, j = entityVariants.length; i < j; i++) {
                    variants[i] = new Variant(entityVariants[i]);
                }
                return variants;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                Variant variant = (Variant) o;

                if (bitrate != variant.bitrate) return false;
                if (content_type != null ? !content_type.equals(variant.content_type) : variant.content_type != null)
                    return false;
                return !(url != null ? !url.equals(variant.url) : variant.url != null);

            }

            @Override
            public int hashCode() {
                int result = content_type != null ? content_type.hashCode() : 0;
                result = 31 * result + (url != null ? url.hashCode() : 0);
                result = 31 * result + (int) (bitrate ^ (bitrate >>> 32));
                return result;
            }

            @Override
            public String toString() {
                return "Variant{" +
                        "content_type='" + content_type + '\'' +
                        ", url='" + url + '\'' +
                        ", bitrate=" + bitrate +
                        '}';
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                ParcelableMedia$VideoInfo$VariantParcelablePlease.writeToParcel(this, dest, flags);
            }
        }
    }
}