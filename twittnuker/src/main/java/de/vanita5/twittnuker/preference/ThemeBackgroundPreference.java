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

package de.vanita5.twittnuker.preference;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.preference.iface.IDialogPreference;

public class ThemeBackgroundPreference extends DialogPreference implements Constants,
        IDialogPreference {

    public final static int MAX_ALPHA = 0xFF;
    public final static int MIN_ALPHA = 0x40;

    private final String[] mBackgroundEntries, mBackgroundValues;
    private String mValue;
    private String mDefaultValue;


    public ThemeBackgroundPreference(Context context) {
        this(context, null);
    }

    public ThemeBackgroundPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setKey(KEY_THEME_BACKGROUND);
        final Resources resources = context.getResources();
        mBackgroundEntries = resources.getStringArray(R.array.entries_theme_background);
        mBackgroundValues = resources.getStringArray(R.array.values_theme_background);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        mDefaultValue = (String) defaultValue;
        setValue(restorePersistedValue ? getPersistedString(null) : mDefaultValue);
        updateSummary();
    }

    private void updateSummary() {
        final int valueIndex = getValueIndex();
        setSummary(valueIndex != -1 ? mBackgroundEntries[valueIndex] : null);
    }

    private void persistValue(String value) {
        // Always persist/notify the first time.
        if (!TextUtils.equals(getPersistedString(null), value)) {
            persistString(value);
            callChangeListener(value);
            notifyChanged();
        }
        updateSummary();
    }

    public String getValue() {
        return mValue;
    }

    private void setValue(String value) {
        mValue = value;
    }

    private int getValueIndex() {
        return findIndexOfValue(mValue);
    }

    public int findIndexOfValue(String value) {
        if (value != null && mBackgroundValues != null) {
            for (int i = mBackgroundValues.length - 1; i >= 0; i--) {
                if (mBackgroundValues[i].equals(value)) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public void displayDialog(PreferenceFragmentCompat fragment) {
        InternalDialogFragment df = InternalDialogFragment.newInstance(getKey());
        df.setTargetFragment(fragment, 0);
        df.show(fragment.getFragmentManager(), getKey());
    }

    private void saveValue() {
        persistValue(mValue);
    }

    private void setSelectedOption(int which) {
        if (which < 0) {
            setValue(mDefaultValue);
        } else {
            setValue(mBackgroundValues[which]);
        }
    }

    public static class InternalDialogFragment extends PreferenceDialogFragmentCompat {

        private View mAlphaContainer;
        private SeekBar mAlphaSlider;

        public static InternalDialogFragment newInstance(String key) {
            final InternalDialogFragment df = new InternalDialogFragment();
            final Bundle args = new Bundle();
            args.putString(PreferenceDialogFragmentCompat.ARG_KEY, key);
            df.setArguments(args);
            return df;
        }

        private int getSliderAlpha() {
            return mAlphaSlider.getProgress() + MIN_ALPHA;
        }

        @Override
        public void onDialogClosed(boolean positive) {
            if (!positive) return;
            final ThemeBackgroundPreference preference = (ThemeBackgroundPreference) getPreference();
            final SharedPreferences preferences = preference.getSharedPreferences();
            final SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(KEY_THEME_BACKGROUND_ALPHA, getSliderAlpha());
            editor.apply();
            preference.saveValue();
        }

        private void updateAlphaVisibility() {
            if (mAlphaContainer == null) return;
            final ThemeBackgroundPreference preference = (ThemeBackgroundPreference) getPreference();
            final boolean isTransparent = VALUE_THEME_BACKGROUND_TRANSPARENT.equals(preference.getValue());
            mAlphaContainer.setVisibility(isTransparent ? View.VISIBLE : View.GONE);
        }


        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(getContext());
            final ThemeBackgroundPreference preference = (ThemeBackgroundPreference) getPreference();
            final SharedPreferences preferences = preference.getSharedPreferences();
            preference.setValue(preference.getPersistedString(null));
            builder.setTitle(preference.getDialogTitle());
            builder.setSingleChoiceItems(preference.mBackgroundEntries, preference.getValueIndex(), new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    preference.setSelectedOption(which);
                    updateAlphaVisibility();
                }
            });
            builder.setPositiveButton(android.R.string.ok, this);
            builder.setNegativeButton(android.R.string.cancel, this);
            final Dialog dialog = builder.create();
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    if (preferences != null) {
                        final MaterialDialog materialDialog = (MaterialDialog) dialog;
                        final LayoutInflater inflater = materialDialog.getLayoutInflater();
                        final ListView listView = materialDialog.getListView();
                        assert listView != null;
                        final ViewGroup listViewParent = (ViewGroup) listView.getParent();
                        listViewParent.removeView(listView);
                        final View view = inflater.inflate(R.layout.dialog_theme_background_preference, listViewParent);
                        ((ViewGroup) view.findViewById(R.id.list_container)).addView(listView);
                        mAlphaContainer = view.findViewById(R.id.alpha_container);
                        mAlphaSlider = (SeekBar) view.findViewById(R.id.alpha_slider);
                        mAlphaSlider.setMax(MAX_ALPHA - MIN_ALPHA);
                        mAlphaSlider.setProgress(preferences.getInt(KEY_THEME_BACKGROUND_ALPHA, DEFAULT_THEME_BACKGROUND_ALPHA) - MIN_ALPHA);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                materialDialog.onItemClick(parent, view, position, id);
                                preference.setSelectedOption(position);
                                updateAlphaVisibility();
                            }
                        });
                        updateAlphaVisibility();
                    }
                }
            });
            return dialog;
        }
    }
}