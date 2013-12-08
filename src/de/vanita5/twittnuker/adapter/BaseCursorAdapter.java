/*
 *			Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2013 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.adapter;

import static de.vanita5.twittnuker.util.Utils.getLinkHighlightOptionInt;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;

import de.vanita5.twittnuker.adapter.iface.IBaseAdapter;
import de.vanita5.twittnuker.util.OnLinkClickHandler;
import de.vanita5.twittnuker.util.TwidereLinkify;

public class BaseCursorAdapter extends SimpleCursorAdapter implements IBaseAdapter, OnSharedPreferenceChangeListener {

	private final TwidereLinkify mLinkify;

	private float mTextSize;

	private int mLinkHighlightOption, mLinkHighlightColor;

	private boolean mDisplayProfileImage, mNicknameOnly, mDisplayNameFirst, mShowAccountColor;

	private final SharedPreferences mNicknamePrefs, mColorPrefs;

	public BaseCursorAdapter(final Context context, final int layout, final Cursor c, final String[] from,
			final int[] to) {
		this(context, layout, c, from, to, 0);
	}

	public BaseCursorAdapter(final Context context, final int layout, final Cursor c, final String[] from,
			final int[] to, final int flags) {
		super(context, layout, c, from, to, flags);
		mLinkify = new TwidereLinkify(new OnLinkClickHandler(context));
		mNicknamePrefs = context.getSharedPreferences(USER_NICKNAME_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mColorPrefs = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mNicknamePrefs.registerOnSharedPreferenceChangeListener(this);
		mColorPrefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public final int getLinkHighlightColor() {
		return mLinkHighlightColor;
	}

	@Override
	public final int getLinkHighlightOption() {
		return mLinkHighlightOption;
	}

	public final TwidereLinkify getLinkify() {
		return mLinkify;
	}

	@Override
	public final float getTextSize() {
		return mTextSize;
	}

	@Override
	public final boolean isDisplayNameFirst() {
		return mDisplayNameFirst;
	}

	@Override
	public final boolean isDisplayProfileImage() {
		return mDisplayProfileImage;
	}

	@Override
	public final boolean isNicknameOnly() {
		return mNicknameOnly;
	}

	@Override
	public final boolean isShowAccountColor() {
		return mShowAccountColor;
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
		notifyDataSetChanged();
	}

	@Override
	public final void setDisplayNameFirst(final boolean name_first) {
		if (mDisplayNameFirst == name_first) return;
		mDisplayNameFirst = name_first;
		notifyDataSetChanged();
	}

	@Override
	public final void setDisplayProfileImage(final boolean display) {
		if (display == mDisplayProfileImage) return;
		mDisplayProfileImage = display;
		notifyDataSetChanged();
	}

	@Override
	public final void setLinkHighlightColor(final int color) {
		if (color == mLinkHighlightColor) return;
		mLinkHighlightColor = color;
		mLinkify.setHighlightColor(color);
		notifyDataSetChanged();
	}

	@Override
	public final void setLinkHighlightOption(final String option) {
		final int option_int = getLinkHighlightOptionInt(option);
		if (option_int == mLinkHighlightOption) return;
		mLinkHighlightOption = option_int;
		mLinkify.setHighlightOption(option_int);
		notifyDataSetChanged();
	}

	@Override
	public final void setNicknameOnly(final boolean nickname_only) {
		if (mNicknameOnly == nickname_only) return;
		mNicknameOnly = nickname_only;
		notifyDataSetChanged();
	}

	@Override
	public final void setShowAccountColor(final boolean show) {
		if (show == mShowAccountColor) return;
		mShowAccountColor = show;
		notifyDataSetChanged();
	}

	@Override
	public final void setTextSize(final float text_size) {
		if (text_size == mTextSize) return;
		mTextSize = text_size;
		notifyDataSetChanged();
	}

}
