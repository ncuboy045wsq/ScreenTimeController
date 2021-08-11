package com.motorola.screentimecontroller.database.config;

import android.os.Parcelable;

public abstract class DatabaseBaseEntry implements Parcelable {

    // id
    private Long _id;

    public Long get_id() {
        return _id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

}