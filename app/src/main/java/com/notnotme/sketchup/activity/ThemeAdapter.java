package com.notnotme.sketchup.activity;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.notnotme.sketchup.R;
import com.notnotme.sketchup.Theme;

import java.util.ArrayList;
import java.util.List;

final class ThemeAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Theme> mItems;

    ThemeAdapter(@NonNull Context context, List<Theme> items) {
        mContext = context;
        mItems = new ArrayList<>(items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView != null) {
            viewHolder = (ViewHolder) convertView.getTag();
        } else {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_spinner, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }

        Theme theme = mItems.get(position);
        viewHolder.mColorPrimary.setBackgroundColor(ContextCompat.getColor(mContext, theme.getColorPrimary()));
        viewHolder.mColorPrimaryDark.setBackgroundColor(ContextCompat.getColor(mContext, theme.getColorPrimaryDark()));
        viewHolder.mTextColorPrimary.setText(theme.name());

        return convertView;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Theme getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static final class ViewHolder {
        View mColorPrimary;
        View mColorPrimaryDark;
        TextView mTextColorPrimary;

        ViewHolder(View itemView) {
            mColorPrimary = itemView.findViewById(R.id.primary);
            mColorPrimaryDark = itemView.findViewById(R.id.primary_dark);
            mTextColorPrimary = itemView.findViewById(R.id.text);
        }
    }

}
