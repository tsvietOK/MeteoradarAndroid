package com.tsvietok.meteoradar.dev;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MapInfoListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<MapInfo> mInfoList;

    MapInfoListAdapter(Context context, ArrayList<MapInfo> infoList) {
        mInfoList = infoList;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mInfoList.size();
    }

    @Override
    public MapInfo getItem(int position) {
        return mInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(R.layout.map_info_item, parent, false);
        }

        MapInfo info = getInfo(position);

        TextView textView = view.findViewById(R.id.mapInfoText);
        ImageView imageView = view.findViewById(R.id.mapInfoColorImage);
        textView.setText(info.getTitle());
        imageView.setBackgroundColor(info.getColor());

        return view;
    }

    private MapInfo getInfo(int position) {
        return getItem(position);
    }
}
