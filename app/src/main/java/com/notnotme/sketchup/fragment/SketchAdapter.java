package com.notnotme.sketchup.fragment;

import android.content.Context;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.notnotme.sketchup.R;
import com.notnotme.sketchup.dao.Sketch;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class SketchAdapter extends RecyclerView.Adapter<SketchAdapter.ViewHolder> {

    private SketchAdapterListener mSketchAdapterListener;
    private ArrayList<Sketch> mItems;
    private SparseBooleanArray mSelected;
    private boolean mInEditMode;

    SketchAdapter(SketchAdapterListener listener) {
        mItems = new ArrayList<>();
        mSelected = new SparseBooleanArray();
        mSketchAdapterListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sketch, parent, false));
        holder.clickOverlay.setOnClickListener(view -> mSketchAdapterListener.onSketchClicked((Sketch) view.getTag()));
        holder.clickOverlay.setOnLongClickListener(view -> {
            mSketchAdapterListener.onSketchLongClick((Sketch) view.getTag());
            return true;
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        Sketch sketch = mItems.get(position);

        holder.clickOverlay.setTag(sketch);
        Picasso.with(context.getApplicationContext())
                .load(FileProvider.getUriForFile(context, context.getPackageName() + ".provider", new File(sketch.getPath())))
                .resize((int) context.getResources().getDimension(R.dimen.sketch_thumb), 0)
                .into(holder.image);

        if (mInEditMode) {
            holder.checkbox.setVisibility(View.VISIBLE);
            holder.checkbox.setChecked(mSelected.get(position, false));
        } else {
            holder.checkbox.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    boolean isInEditMode() {
        return mInEditMode;
    }

    void setInEditMode(boolean inEditMode) {
        mInEditMode = inEditMode;
        mSelected.clear();
        notifyDataSetChanged();
    }

    void setSelected(Sketch item, boolean selected) {
        int position = mItems.indexOf(item);
        mSelected.put(position, selected);
        notifyItemChanged(position);
    }

    boolean isSelected(Sketch item) {
        return mSelected.get(mItems.indexOf(item), false);
    }

    List<Sketch> getSelected() {
        ArrayList<Sketch> selected = new ArrayList<>();

        int itemSize = mItems.size();
        for (int i = 0; i < itemSize; i++) {
            if (mSelected.get(i, false)) {
                selected.add(mItems.get(i));
            }
        }

        return selected;
    }

    ArrayList<Sketch> getItems() {
        return mItems;
    }

    public interface SketchAdapterListener {
        void onSketchClicked(Sketch sketch);

        void onSketchLongClick(Sketch sketch);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        View clickOverlay;
        CheckBox checkbox;

        ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            clickOverlay = itemView.findViewById(R.id.click_overlay);
            checkbox = itemView.findViewById(R.id.check);
        }
    }

}
