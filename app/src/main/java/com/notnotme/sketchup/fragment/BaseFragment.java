package com.notnotme.sketchup.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.notnotme.sketchup.SettingsManager;

public class BaseFragment extends Fragment {

    private SettingsManager mSettingsManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettingsManager = new SettingsManager(getContext());
    }

    protected SettingsManager getSettingsManager() {
        return mSettingsManager;
    }

}
