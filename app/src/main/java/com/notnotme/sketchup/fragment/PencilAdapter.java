package com.notnotme.sketchup.fragment;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.notnotme.sketchup.R;

import java.util.ArrayList;
import java.util.List;

public final class PencilAdapter extends RecyclerView.Adapter<PencilAdapter.ViewHolder> {

    private ArrayList<Pencil> mItems;
    private PencilAdapterListener mPencilAdapterListener;

    PencilAdapter(List<Pencil> pencilList, PencilAdapterListener listener) {
        mItems = new ArrayList<>();
        mItems.addAll(pencilList);
        mPencilAdapterListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_color, parent, false));
        holder.itemView.setOnClickListener(view -> mPencilAdapterListener.onItemClick((Pencil) view.getTag()));
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Pencil pencil = mItems.get(position);
        holder.itemView.setTag(pencil);
        holder.icon.setImageResource(pencil.getIconRes());
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public interface PencilAdapterListener {
        void onItemClick(Pencil pencil);
    }

    static class Pencil {
        private float size;
        private int iconRes;

        Pencil(float size, int iconRes) {
            this.size = size;
            this.iconRes = iconRes;
        }

        float getSize() {
            return size;
        }

        int getIconRes() {
            return iconRes;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;

        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
        }
    }

}
