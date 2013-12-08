package de.vanita5.twittnuker.service;

import android.content.Intent;
import android.content.res.Resources;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.TwidereConstants;
import de.vanita5.twittnuker.activity.HomeActivity;
import de.vanita5.twittnuker.provider.TweetStore.UnreadCounts;
import de.vanita5.twittnuker.util.UnreadCountUtils;

public class DashClockMentionsUnreadCountService extends DashClockExtension implements TwidereConstants {

	private static final String[] URIS = { UnreadCounts.CONTENT_URI.toString() };

	@Override
	protected void onInitialize(final boolean isReconnect) {
		super.onInitialize(isReconnect);
		addWatchContentUris(URIS);
	}

	@Override
	protected void onUpdateData(final int reason) {
		final ExtensionData data = new ExtensionData();
		final int count = UnreadCountUtils.getUnreadCount(this, TAB_TYPE_MENTIONS_TIMELINE);
		final Resources res = getResources();
		data.visible(count > 0);
		data.icon(R.drawable.ic_extension_mentions);
		data.status(Integer.toString(count));
		data.expandedTitle(res.getQuantityString(R.plurals.N_new_mentions, count, count));
		data.clickIntent(new Intent(this, HomeActivity.class));
		publishUpdate(data);
	}
}
