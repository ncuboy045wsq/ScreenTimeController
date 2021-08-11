package com.motorola.screentimecontroller.database.dao;

import android.net.Uri;

import java.util.List;

public interface BaseDao<T> {

    Uri insert(T t);

    T query(T t);

    List<T> query();

    int update(T t);

    int delete(T t);
}
