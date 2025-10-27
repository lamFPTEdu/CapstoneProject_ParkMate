package com.parkmate.android.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtils {
    private static final String TAG = "FileUtils";

    /**
     * Lấy File từ Uri (hỗ trợ cả content:// và file://)
     */
    public static File getFileFromUri(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }

        try {
            // Nếu là file:// URI
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                return new File(uri.getPath());
            }

            // Nếu là content:// URI, cần copy sang temp file
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                return getFileFromContentUri(context, uri);
            }

            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error getting file from URI", e);
            return null;
        }
    }

    /**
     * Lấy File từ content:// URI bằng cách copy sang temp file
     */
    private static File getFileFromContentUri(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }

            // Tạo temp file
            String fileName = getFileName(context, uri);
            if (fileName == null) {
                fileName = "temp_image_" + System.currentTimeMillis() + ".jpg";
            }

            File tempFile = new File(context.getCacheDir(), fileName);
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            return tempFile;
        } catch (Exception e) {
            Log.e(TAG, "Error copying content URI to file", e);
            return null;
        }
    }

    /**
     * Lấy tên file từ Uri
     */
    private static String getFileName(Context context, Uri uri) {
        String fileName = null;
        try {
            String[] projection = {MediaStore.Images.Media.DISPLAY_NAME};
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                    fileName = cursor.getString(columnIndex);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting file name", e);
        }
        return fileName;
    }
}

