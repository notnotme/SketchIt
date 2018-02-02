package com.notnotme.sketchup.dao;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

@Entity(indices = {@Index(value = {"mPath"}), @Index(value = {"mCreationDate"})})
public final class Sketch implements Parcelable {

    @PrimaryKey
    @NonNull
    private String mPath;
    private long mCreationDate;

    public Sketch() {
        mPath = "";
        mCreationDate = 0;
    }

    public Sketch(@NonNull String uri, long creationDate) {
        mPath = uri;
        mCreationDate = creationDate;
    }

    protected Sketch(Parcel in) {
        mPath = in.readString();
        mCreationDate = in.readLong();
    }

    @NonNull
    public String getPath() {
        return mPath;
    }

    public void setPath(@NonNull String path) {
        mPath = path;
    }

    public long getCreationDate() {
        return mCreationDate;
    }

    public void setCreationDate(long creationDate) {
        mCreationDate = creationDate;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Sketch) {
            Sketch other = (Sketch) obj;
            return other.mPath.equals(mPath);
        }

        return super.equals(obj);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mPath);
        dest.writeLong(mCreationDate);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Sketch> CREATOR = new Creator<Sketch>() {
        @Override
        public Sketch createFromParcel(Parcel in) {
            return new Sketch(in);
        }

        @Override
        public Sketch[] newArray(int size) {
            return new Sketch[size];
        }
    };

}
