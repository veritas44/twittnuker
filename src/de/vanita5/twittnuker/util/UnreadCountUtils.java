package de.vanita5.twittnuker.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.provider.TweetStore;
import de.vanita5.twittnuker.provider.TweetStore.UnreadCounts;

public class UnreadCountUtils implements Constants {

	public static int getUnreadCount(final Context context, final int position) {
		if (context == null || position < 0) return 0;
		final ContentResolver resolver = context.getContentResolver();
		final Uri.Builder builder = TweetStore.UnreadCounts.CONTENT_URI.buildUpon();
		builder.appendPath(ParseUtils.parseString(position));
		final Uri uri = builder.build();
		final Cursor c = resolver.query(uri, new String[] { UnreadCounts.COUNT }, null, null, null);
		if (c == null) return 0;
		try {
			if (c.getCount() == 0) return 0;
			c.moveToFirst();
			return c.getInt(c.getColumnIndex(UnreadCounts.COUNT));
		} finally {
			c.close();
		}
	}

	public static int getUnreadCount(final Context context, final String type) {
		if (context == null || type == null) return 0;
		final ContentResolver resolver = context.getContentResolver();
		final Uri.Builder builder = TweetStore.UnreadCounts.ByType.CONTENT_URI.buildUpon();
		builder.appendPath(type);
		final Uri uri = builder.build();
		final Cursor c = resolver.query(uri, new String[] { UnreadCounts.COUNT }, null, null, null);
		if (c == null) return 0;
		try {
			if (c.getCount() == 0) return 0;
			c.moveToFirst();
			return c.getInt(c.getColumnIndex(UnreadCounts.COUNT));
		} finally {
			c.close();
		}
	}
}
