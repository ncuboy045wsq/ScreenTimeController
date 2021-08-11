package com.motorola.screentimecontroller.database.dao;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.motorola.screentimecontroller.database.config.TaskUsageInfoTable;
import com.motorola.screentimecontroller.provider.ScreenUsageProviderEntry;

import java.util.ArrayList;
import java.util.List;

import motorola.core_services.screentimecontroller.bean.TaskUsageInfo;


public class TaskUsageInfoDao implements BaseDao<TaskUsageInfo> {

    private final ContentResolver mContentResolver;


    private Uri mUri = Uri.parse("content://" + ScreenUsageProviderEntry.AUTHORITY + "/" + TaskUsageInfoTable.TABLE_NAME);

    // 表名
    public static final String TABLE_NAME = TaskUsageInfoTable.TABLE_NAME;

    public TaskUsageInfoDao(ContentResolver contentResolver) {
        this.mContentResolver = contentResolver;
    }

    private ContentValues getInsertContentValues(TaskUsageInfo taskUsageInfo) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TaskUsageInfoTable.PACKAGE_NAME, taskUsageInfo.getPackageName());
        contentValues.put(TaskUsageInfoTable.UID, taskUsageInfo.getUid());
        contentValues.put(TaskUsageInfoTable.START_TIME, taskUsageInfo.getStartTime());
        contentValues.put(TaskUsageInfoTable.END_TIME, taskUsageInfo.getEndTime());
        return contentValues;
    }

    @Override
    public Uri insert(TaskUsageInfo taskUsageInfo) {
        ContentValues contentValues = getInsertContentValues(taskUsageInfo);
        return mContentResolver.insert(mUri, contentValues);
    }

    @Override
    public TaskUsageInfo query(TaskUsageInfo taskUsageInfo) {

        String selection = TaskUsageInfoTable.PACKAGE_NAME + "=? and " + TaskUsageInfoTable.UID + "=?";
        String[] selectionArgs = new String[]{taskUsageInfo.getPackageName(), taskUsageInfo.getUid() + ""};

        Cursor cursor = mContentResolver.query(mUri, null, selection, selectionArgs, null);
        if (cursor == null || cursor.getCount() <= 0) {
            return null;
        }

        TaskUsageInfo screenUsageInfo = null;
        if (cursor.moveToNext()) {
            screenUsageInfo = getTaskUsageInfo(cursor);
        }

        return screenUsageInfo;
    }

    @Override
    public List<TaskUsageInfo> query() {
        List<TaskUsageInfo> taskUsageInfoList = new ArrayList<>();
        Cursor cursor = mContentResolver.query(mUri, null, null, null, null);

        while (cursor != null && cursor.moveToNext()) {
            taskUsageInfoList.add(getTaskUsageInfo(cursor));
        }

        return taskUsageInfoList;
    }

    public List<TaskUsageInfo> query(long startTime, long endTime) {

        List<TaskUsageInfo> taskUsageInfoList = new ArrayList<>();

        String startTimeStr = startTime + "";
        String endTimeStr = endTime + "";

        String selection = TaskUsageInfoTable.START_TIME + ">=? AND " + TaskUsageInfoTable.START_TIME
                + "<? OR " + TaskUsageInfoTable.END_TIME + ">? AND " + TaskUsageInfoTable.END_TIME + "<=? OR "
                + TaskUsageInfoTable.START_TIME + " <? AND " + TaskUsageInfoTable.END_TIME + ">?";
        String[] selectionArgs = new String[]{startTimeStr, endTimeStr, startTimeStr, endTimeStr, startTimeStr, endTimeStr};
        Cursor cursor = mContentResolver.query(mUri, null, selection, selectionArgs, null);

        while (cursor != null && cursor.moveToNext()) {
            taskUsageInfoList.add(getTaskUsageInfo(cursor));
        }

        return taskUsageInfoList;
    }

    @Override
    public int delete(TaskUsageInfo taskUsageInfo) {

        String where;
        String[] selectionArgs;

        where = TaskUsageInfoTable.PACKAGE_NAME + "=? and " + TaskUsageInfoTable.UID + "=?";
        selectionArgs = new String[]{taskUsageInfo.getPackageName(), taskUsageInfo.getUid() + ""};

        return mContentResolver.delete(mUri, where, selectionArgs);
    }

    private TaskUsageInfo getTaskUsageInfo(Cursor cursor) {
        TaskUsageInfo screenUsageInfo = new TaskUsageInfo();
        screenUsageInfo.setPackageName(cursor.getString(cursor.getColumnIndex(TaskUsageInfoTable.PACKAGE_NAME)));
        screenUsageInfo.setUid(cursor.getInt(cursor.getColumnIndex(TaskUsageInfoTable.UID)));
        screenUsageInfo.setStartTime(cursor.getLong(cursor.getColumnIndex(TaskUsageInfoTable.START_TIME)));
        screenUsageInfo.setEndTime(cursor.getLong(cursor.getColumnIndex(TaskUsageInfoTable.END_TIME)));
        return screenUsageInfo;
    }

    @Override
    public int update(TaskUsageInfo taskUsageInfo) {
        String where;
        String[] selectionArgs;

        where = TaskUsageInfoTable.PACKAGE_NAME + "=? and " + TaskUsageInfoTable.UID + "=?";
        selectionArgs = new String[]{taskUsageInfo.getPackageName(), taskUsageInfo.getUid() + ""};

        return mContentResolver.update(mUri, getInsertContentValues(taskUsageInfo), where, selectionArgs);
    }
}