package com.veyndan.generic.ui;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

public class Gallery {

    public static List<String> getImagesPath(Context context) {
        final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
        final String orderBy = MediaStore.Images.Media._ID;
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);

        List<String> imagePaths = new ArrayList<>();

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isLast()) {
                cursor.moveToNext();
                int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                imagePaths.add(cursor.getString(dataColumnIndex));
            }
            cursor.close();
        }

        return imagePaths;
    }

}
