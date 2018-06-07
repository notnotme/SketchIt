package com.notnotme.sketchup.fragment;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.notnotme.sketchup.R;

import java.util.ArrayList;
import java.util.List;

public final class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ViewHolder> {

    private ArrayList<Integer> mItems;
    private ColorAdapterListener mColorAdapterListener;

    ColorAdapter(List<Integer> colorList, ColorAdapterListener listener) {
        mItems = new ArrayList<>();
        mItems.addAll(colorList);
        mColorAdapterListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_color, parent, false));
        holder.itemView.setOnClickListener(view -> {
            mColorAdapterListener.onItemClick((int) view.getTag());
            notifyDataSetChanged();
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int color = mItems.get(position);

        int backgroundSelected = ContextCompat.getColor(holder.itemView.getContext(), R.color.lightgrey);
        holder.itemView.setBackgroundColor(mColorAdapterListener.getCurrentColor() == color ? backgroundSelected : Color.TRANSPARENT);

        holder.itemView.setTag(color);
        holder.icon.setBackgroundColor(color);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public interface ColorAdapterListener {
        void onItemClick(int color);

        int getCurrentColor();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;

        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
        }
    }

}
