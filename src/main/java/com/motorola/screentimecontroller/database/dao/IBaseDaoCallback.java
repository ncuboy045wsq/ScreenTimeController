package com.motorola.screentimecontroller.database.dao;

import android.net.Uri;

import java.util.List;

public interface IBaseDaoCallback<T> {

    void onInsert(Uri uri);

    void onQuery(T t);

    void onQueryAll(List<T> result);

    void onUpdate(int count);

    void onDelete(int count);
}