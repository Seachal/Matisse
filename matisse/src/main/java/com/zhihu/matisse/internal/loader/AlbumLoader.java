/*
 * Copyright (C) 2014 nohana, Inc.
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhihu.matisse.internal.loader;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;

import com.zhihu.matisse.internal.entity.Album;

public class AlbumLoader extends CursorLoader {
    public static final String COLUMN_COUNT = "count";
    private static final Uri QUERY_URI = MediaStore.Files.getContentUri("external");
    private static final String[] COLUMNS = {
            "bucket_id",
            "bucket_display_name",
            MediaStore.Files.FileColumns._ID,
            COLUMN_COUNT};
    private static final String[] PROJECTION = {
            "bucket_id",
            "bucket_display_name",
            MediaStore.Files.FileColumns._ID,
            "COUNT(*) AS " + COLUMN_COUNT};
    private static final String SELECTION =
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
            + " OR "
            + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
            + ") GROUP BY (bucket_id";
    private static final String[] SELECTION_ARGS = {
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
    };
    private static final String BUCKET_ORDER_BY = "datetaken DESC";
    private static final String MEDIA_ID_DUMMY = String.valueOf(-1);

    public AlbumLoader(Context context) {
        super(context, QUERY_URI, PROJECTION, SELECTION, SELECTION_ARGS, BUCKET_ORDER_BY);
    }

    @Override
    public Cursor loadInBackground() {
        Cursor albums = super.loadInBackground();
        MatrixCursor allAlbum = new MatrixCursor(COLUMNS);
        int totalCount = 0;
        while (albums.moveToNext()) {
            totalCount += albums.getInt(albums.getColumnIndex(COLUMN_COUNT));
        }
        String allAlbumId;
        if (albums.moveToFirst()) {
            allAlbumId = albums.getString(albums.getColumnIndex(MediaStore.Files.FileColumns._ID));
        } else {
            allAlbumId = MEDIA_ID_DUMMY;
        }
        allAlbum.addRow(new String[]{Album.ALBUM_ID_ALL, Album.ALBUM_NAME_ALL, allAlbumId, String.valueOf(totalCount)});

        return new MergeCursor(new Cursor[]{allAlbum, albums});
    }

    @Override
    public void onContentChanged() {
        // FIXME a dirty way to fix loading multiple times
    }
}