package com.notnotme.sketchup;

public interface Callback<T> {

    void success(T success);

    void failure(Throwable error);

}
