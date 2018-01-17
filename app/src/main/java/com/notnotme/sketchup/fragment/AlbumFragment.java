package com.notnotme.sketchup.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.notnotme.sketchup.Callback;
import com.notnotme.sketchup.R;
import com.notnotme.sketchup.dao.Sketch;

import java.util.List;

public final class AlbumFragment extends Fragment {

    private static final String TAG = AlbumFragment.class.getSimpleName();
    private static final String STATE_LIST_POSITION = TAG + ".list_position";

    private AlbumFragmentCallback mCallback;
    private SketchAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private View mActionLayout;
    private AlertDialog mAlertDialog;
    private Bundle mSavedInstanceState;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_album, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_fragment_album);
        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.settings:
                    if (isInEditMode()) {
                        exitEditMode();
                    }
                    mCallback.showSettings();
                    return true;
            }
            return false;
        });

        view.findViewById(R.id.btn_sketch).setOnClickListener(v -> {
                mCallback.showSketchFragment();
                exitEditMode();
            });

        view.findViewById(R.id.share).setOnClickListener(v -> {
                mCallback.shareSketches(mAdapter.getSelected());
                exitEditMode();
            });

        view.findViewById(R.id.delete).setOnClickListener(v -> mAlertDialog = new AlertDialog.Builder(getContext())
            .setMessage(R.string.ask_delete_selection)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok, (dialog, i) -> mCallback.deleteSketches(mAdapter.getSelected(),
                    new Callback<List<Sketch>>() {
                        @Override
                        public void success(List<Sketch> success) {
                            if (isDetached()) return;
                            mAdapter.getItems().removeAll(success);
                            exitEditMode();
                        }

                        @Override
                        public void failure(Throwable error) {
                            if (isDetached()) return;
                            mAlertDialog = new AlertDialog.Builder(getContext())
                                    .setMessage(error.getMessage())
                                    .setPositiveButton(android.R.string.ok, null)
                                    .show();
                        }
                    }))
            .show());

        mAdapter = new SketchAdapter(new SketchAdapter.SketchAdapterListener() {
                @Override
                public void onSketchClicked(Sketch sketch) {
                    if (!mAdapter.isInEditMode()) {
                        mCallback.showSketch(sketch);
                    } else {
                        mAdapter.setSelected(sketch, !mAdapter.isSelected(sketch));
                    }
                }

                @Override
                public void onSketchLongClick(Sketch sketch) {
                    if (!mAdapter.isInEditMode()) {
                        enterEditMode();
                        mAdapter.setSelected(sketch, true);
                    }
                }
            });

        mActionLayout = view.findViewById(R.id.action_layout);
        mRecyclerView = view.findViewById(R.id.recycler);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        mSavedInstanceState = savedInstanceState;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSketches(mSavedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (AlbumFragmentCallback) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_LIST_POSITION, mRecyclerView.getLayoutManager().onSaveInstanceState());
    }

    public void loadSketches(Bundle savedInstanceState) {
        mCallback.getAllSketches(new Callback<List<Sketch>>() {
            @Override
            public void success(List<Sketch> success) {
                if (isDetached()) return;
                mAdapter.getItems().clear();
                mAdapter.getItems().addAll(success);
                mAdapter.notifyDataSetChanged();

                if (savedInstanceState != null) {
                    mRecyclerView.getLayoutManager()
                            .onRestoreInstanceState(savedInstanceState.getParcelable(STATE_LIST_POSITION));
                }
            }

            @Override
            public void failure(Throwable error) {
                if (isDetached()) return;
                mAlertDialog = new AlertDialog.Builder(getContext())
                        .setMessage(error.getMessage())
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        });
    }

    public void enterEditMode() {
        mAdapter.setInEditMode(true);
        mActionLayout.animate()
                .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                .translationY(0);
    }

    public void exitEditMode() {
        mAdapter.setInEditMode(false);
        mActionLayout.animate()
                .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                .translationY(mActionLayout.getHeight());
    }

    public boolean isInEditMode() {
        return mAdapter.isInEditMode();
    }

    public interface AlbumFragmentCallback {
        void showSketchFragment();

        void showSketch(Sketch sketch);

        void getAllSketches(Callback<List<Sketch>> callback);

        void deleteSketches(List<Sketch> sketches, Callback<List<Sketch>> callback);

        void shareSketches(List<Sketch> selected);

        void showSettings();
    }

}
