package com.motorola.screentimecontroller.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.motorola.screentimecontroller.database.ScreenUsageDBOpenHelper;
import com.motorola.screentimecontroller.database.config.TaskUsageInfoTable;

import motorola.core_services.screentimecontroller.database.config.ScreenBlockUpTimeTable;
import motorola.core_services.screentimecontroller.database.config.TaskBlockUpTimeTable;

public class ScreenUsageProvider extends ContentProvider {

    // Creates a UriMatcher object.
    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int INDEX_PACKAGE_USAGE_LIMIT = 0;
    private static final int INDEX_SCREEN_LIMIT_TIME = 1;
    private static final int INDEX_SCREEN_USAGE_INFO = 2;

    private ScreenUsageDBOpenHelper screenUsageDBOpenHelper = null;
    private SQLiteDatabase mScreenUsageDatabase;

    static {
        mUriMatcher.addURI(ScreenUsageProviderEntry.AUTHORITY, TaskBlockUpTimeTable.TABLE_NAME, INDEX_PACKAGE_USAGE_LIMIT);
        mUriMatcher.addURI(ScreenUsageProviderEntry.AUTHORITY, ScreenBlockUpTimeTable.TABLE_NAME, INDEX_SCREEN_LIMIT_TIME);
        mUriMatcher.addURI(ScreenUsageProviderEntry.AUTHORITY, TaskUsageInfoTable.TABLE_NAME, INDEX_SCREEN_USAGE_INFO);
    }

    @Override
    public boolean onCreate() {
        screenUsageDBOpenHelper = new ScreenUsageDBOpenHelper(getContext());
        mScreenUsageDatabase = screenUsageDBOpenHelper.getWritableDatabase();
        return mScreenUsageDatabase != null;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor = null;
        switch (mUriMatcher.match(uri)) {
            case INDEX_PACKAGE_USAGE_LIMIT:
                cursor = mScreenUsageDatabase.query(TaskBlockUpTimeTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case INDEX_SCREEN_LIMIT_TIME:
                cursor = mScreenUsageDatabase.query(ScreenBlockUpTimeTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case INDEX_SCREEN_USAGE_INFO:
                cursor = mScreenUsageDatabase.query(TaskUsageInfoTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                break;
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        String type = null;
        switch (mUriMatcher.match(uri)) {
            case INDEX_PACKAGE_USAGE_LIMIT:
            case INDEX_SCREEN_LIMIT_TIME:
            case INDEX_SCREEN_USAGE_INFO:
                type = ScreenUsageProviderEntry.CONTENT_TYPE_MULTI;
                break;
            default:
                break;
        }

        return type;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (TextUtils.isEmpty(getTableName(uri))) {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        long count = 0;
        try {
            count = mScreenUsageDatabase.insert(getTableName(uri), null, values);
        } catch (Exception e) {
            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        Uri resultUri = ContentUris.withAppendedId(uri, count > 0 ? 1 : count);
        return resultUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[]
            selectionArgs) {
        if (TextUtils.isEmpty(getTableName(uri))) {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        return mScreenUsageDatabase.delete(getTableName(uri), selection, selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String
            selection, @Nullable String[] selectionArgs) {
        if (TextUtils.isEmpty(getTableName(uri))) {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        return mScreenUsageDatabase.update(getTableName(uri), values, selection, selectionArgs);
    }

    private String getTableName(@NonNull Uri uri) {
        String tableName = null;
        switch (mUriMatcher.match(uri)) {
            case INDEX_PACKAGE_USAGE_LIMIT:
                tableName = TaskBlockUpTimeTable.TABLE_NAME;
                break;
            case INDEX_SCREEN_LIMIT_TIME:
                tableName = ScreenBlockUpTimeTable.TABLE_NAME;
                break;
            case INDEX_SCREEN_USAGE_INFO:
                tableName = TaskUsageInfoTable.TABLE_NAME;
                break;
            default:
                break;
        }
        return tableName;
    }
}