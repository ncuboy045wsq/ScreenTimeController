package com.motorola.screentimecontroller.database;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.motorola.screentimecontroller.database.config.TaskUsageInfoTable;

public class ScreenUsageDBOpenHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "ScreenUsage.db";


    private static final String SQL_CREATE_TASK_USAGE_INFO_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + TaskUsageInfoTable.TABLE_NAME + " (" +
                    TaskUsageInfoTable.PACKAGE_NAME + " TEXT," +
                    TaskUsageInfoTable.UID + " TEXT," +
                    TaskUsageInfoTable.START_TIME + " INTEGER," +
                    TaskUsageInfoTable.DURATION + " INTEGER," +
                    "PRIMARY KEY (" + TaskUsageInfoTable.PACKAGE_NAME + ", " + TaskUsageInfoTable.UID + "))";

    private static final String SQL_DELETE_TASK_USAGE_INFO_ENTRIES = "DROP TABLE IF EXISTS " + TaskUsageInfoTable.TABLE_NAME;

    public ScreenUsageDBOpenHelper(@Nullable Context context) {
        this(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private ScreenUsageDBOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    private ScreenUsageDBOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, @Nullable DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    private ScreenUsageDBOpenHelper(@Nullable Context context, @Nullable String name, int version, @NonNull SQLiteDatabase.OpenParams openParams) {
        super(context, name, version, openParams);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TASK_USAGE_INFO_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (newVersion == oldVersion) {
            return;
        }

        db.execSQL(SQL_DELETE_TASK_USAGE_INFO_ENTRIES);

        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
        onUpgrade(db, oldVersion, newVersion);
    }

}