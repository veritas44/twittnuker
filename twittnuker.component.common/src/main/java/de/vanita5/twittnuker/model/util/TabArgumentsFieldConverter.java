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

package de.vanita5.twittnuker.model.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import org.mariotaku.library.objectcursor.converter.CursorFieldConverter;

import de.vanita5.twittnuker.model.Tab;
import de.vanita5.twittnuker.model.tab.argument.TabArguments;
import de.vanita5.twittnuker.provider.TwidereDataStore.Tabs;
import de.vanita5.twittnuker.util.JsonSerializer;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

public class TabArgumentsFieldConverter implements CursorFieldConverter<TabArguments> {

    @Override
    public TabArguments parseField(Cursor cursor, int columnIndex, ParameterizedType fieldType) throws IOException {
        final String tabType = Tab.getTypeAlias(cursor.getString(cursor.getColumnIndex(Tabs.TYPE)));
        if (TextUtils.isEmpty(tabType)) return null;
        return TabArguments.parse(tabType, cursor.getString(columnIndex));
    }

    @Override
    public void writeField(ContentValues values, TabArguments object, String columnName, ParameterizedType fieldType) {
        if (object == null) return;
        try {
            values.put(columnName, JsonSerializer.serialize(object));
        } catch (IOException e) {
            // Ignore
        }
    }
}