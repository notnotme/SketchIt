package com.notnotme.sketchup.fragment;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.notnotme.sketchup.R;

import java.util.ArrayList;
import java.util.List;

public final class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ViewHolder> {

    private ArrayList<String> mItems;
    private ColorAdapterListener mColorAdapterListener;

    ColorAdapter(List<String> colorList, ColorAdapterListener listener) {
        mItems = new ArrayList<>();
        mItems.addAll(colorList);
        mColorAdapterListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_color, parent, false));
        holder.itemView.setOnClickListener(view -> mColorAdapterListener.onItemClick((int) view.getTag()));
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int color = Color.parseColor(mItems.get(position));

        holder.itemView.setTag(color);
        holder.icon.setBackgroundColor(color);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public interface ColorAdapterListener {
        void onItemClick(int color);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;

        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
        }
    }

}
