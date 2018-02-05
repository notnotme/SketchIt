package com.notnotme.sketchup.popup;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.notnotme.sketchup.R;

import java.util.ArrayList;
import java.util.List;

public final class PencilAdapter extends RecyclerView.Adapter<PencilAdapter.ViewHolder> {

    private ArrayList<Item> mItems;
    private PencilAdapterListener mPencilAdapterListener;
    private int mSelected;

    PencilAdapter(List<Item> pencilList, int selected, PencilAdapterListener listener) {
        mItems = new ArrayList<>();
        mItems.addAll(pencilList);
        mSelected = selected;
        mPencilAdapterListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_color, parent, false));
        holder.itemView.setOnClickListener(view -> {
            ViewHolder viewHolder = (ViewHolder) view.getTag();

            mSelected = viewHolder.getAdapterPosition();
            mPencilAdapterListener.onItemClick(this, mItems.get(mSelected));
            notifyDataSetChanged();
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Item item = mItems.get(position);

        int backgroundSelected = ContextCompat.getColor(holder.itemView.getContext(), R.color.lightgrey);
        holder.itemView.setBackgroundColor(position == mSelected ? backgroundSelected : Color.TRANSPARENT);

        holder.itemView.setTag(holder);
        holder.icon.setImageResource(item.getIconRes());
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    void setSelected(int selected) {
        mSelected = selected;
    }

    public interface PencilAdapterListener {
        void onItemClick(PencilAdapter adapter, Item pencil);
    }

    static class Item {
        private int iconRes;

        Item(int iconRes) {
            this.iconRes = iconRes;
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
