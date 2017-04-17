/*
 *          Twittnuker - Twitter client for Android
 *
 *          This program incorporates a modified version of
 *          Twidere - Twitter client for Android
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.vanita5.twittnuker.library.mastodon.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.Arrays;
import java.util.Date;

/**
 * {@see https://github.com/tootsuite/documentation/blob/master/Using-the-API/API.md#status}
 *
 * Created by mariotaku on 2017/4/17.
 */
@JsonObject
public class Status {
    /**
     * The ID of the status
     */
    @JsonField(name = "id")
    String id;
    /**
     * A Fediverse-unique resource ID
     */
    @JsonField(name = "uri")
    String uri;
    /**
     * URL to the status page (can be remote)
     */
    @JsonField(name = "url")
    String url;
    /**
     * The {@link Account} which posted the status
     */
    @JsonField(name = "account")
    Account account;
    /**
     * {@code null} or the ID of the status it replies to
     */
    @JsonField(name = "in_reply_to_id")
    String inReplyToId;
    /**
     * {@code null} or the ID of the account it replies to
     */
    @JsonField(name = "in_reply_to_account_id")
    String inReplyToAccountId;
    /**
     * {@code null} or the reblogged {@link Status}
     */
    @JsonField(name = "reblog")
    Status reblog;
    /**
     * Body of the status; this will contain HTML (remote HTML already sanitized)
     */
    @JsonField(name = "content")
    String content;
    /**
     * The time the status was created
     */
    @JsonField(name = "created_at")
    Date createdAt;
    /**
     * The number of reblogs for the status
     */
    @JsonField(name = "reblogs_count")
    long reblogsCount;
    /**
     * The number of favourites for the status
     */
    @JsonField(name = "favourites_count")
    long favouritesCount;
    /**
     * Whether the authenticated user has reblogged the status
     */
    @JsonField(name = "reblogged")
    boolean reblogged;
    /**
     * Whether the authenticated user has favourited the status
     */
    @JsonField(name = "favourited")
    boolean favourited;
    /**
     * Whether media attachments should be hidden by default
     */
    @JsonField(name = "sensitive")
    boolean sensitive;
    /**
     * If not empty, warning text that should be displayed before the actual content
     */
    @JsonField(name = "spoiler_text")
    String spoilerText;
    /**
     * One of: {@code public}, {@code unlisted}, {@code private}, {@code direct}
     */
    @JsonField(name = "visibility")
    String visibility;
    /**
     * An array of {@link Attachment}
     */
    @JsonField(name = "media_attachments")
    Attachment[] mediaAttachments;
    /**
     * An array of {@link Mention}
     */
    @JsonField(name = "mentions")
    Mention[] mentions;
    /**
     * An array of {@link Tag}
     */
    @JsonField(name = "tags")
    Tag[] tags;
    /**
     * {@link Application} from which the status was posted
     */
    @JsonField(name = "application")
    Application application;

    public String getId() {
        return id;
    }

    public String getUri() {
        return uri;
    }

    public String getUrl() {
        return url;
    }

    public Account getAccount() {
        return account;
    }

    public String getInReplyToId() {
        return inReplyToId;
    }

    public String getInReplyToAccountId() {
        return inReplyToAccountId;
    }

    public Status getReblog() {
        return reblog;
    }

    public String getContent() {
        return content;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public long getReblogsCount() {
        return reblogsCount;
    }

    public long getFavouritesCount() {
        return favouritesCount;
    }

    public boolean isReblogged() {
        return reblogged;
    }

    public boolean isFavourited() {
        return favourited;
    }

    public boolean isSensitive() {
        return sensitive;
    }

    public String getSpoilerText() {
        return spoilerText;
    }

    public String getVisibility() {
        return visibility;
    }

    public Attachment[] getMediaAttachments() {
        return mediaAttachments;
    }

    public Mention[] getMentions() {
        return mentions;
    }

    public Tag[] getTags() {
        return tags;
    }

    public Application getApplication() {
        return application;
    }

    @Override
    public String toString() {
        return "Status{" +
                "id='" + id + '\'' +
                ", uri='" + uri + '\'' +
                ", url='" + url + '\'' +
                ", account=" + account +
                ", inReplyToId='" + inReplyToId + '\'' +
                ", inReplyToAccountId='" + inReplyToAccountId + '\'' +
                ", reblog=" + reblog +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                ", reblogsCount=" + reblogsCount +
                ", favouritesCount=" + favouritesCount +
                ", reblogged=" + reblogged +
                ", favourited=" + favourited +
                ", sensitive=" + sensitive +
                ", spoilerText='" + spoilerText + '\'' +
                ", visibility='" + visibility + '\'' +
                ", mediaAttachments=" + Arrays.toString(mediaAttachments) +
                ", mentions=" + Arrays.toString(mentions) +
                ", tags=" + Arrays.toString(tags) +
                ", application=" + application +
                '}';
    }
}