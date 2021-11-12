package com.tsvietok.meteoradar.test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.tsvietok.meteoradar.R;

import java.util.ArrayList;

public class ColorTestListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<ColorTest> mColorTestList;

    ColorTestListAdapter(Context context, ArrayList<ColorTest> colorTestList) {
        mColorTestList = colorTestList;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mColorTestList.size();
    }

    @Override
    public ColorTest getItem(int position) {
        return mColorTestList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(R.layout.color_test_item, parent, false);
        }

        ColorTest testColors = getInfo(position);

        ImageView originalColorView = view.findViewById(R.id.originalColor);
        ImageView darkColorView = view.findViewById(R.id.darkColor);
        ImageView invertedColorView = view.findViewById(R.id.invertedColor);

        originalColorView.setImageBitmap(testColors.getOriginalColorBitmap());
        darkColorView.setImageBitmap(testColors.getDarkColorBitmap());
        invertedColorView.setImageBitmap(testColors.getInvertedColorBitmap());

        return view;
    }

    private ColorTest getInfo(int position) {
        return getItem(position);
    }
}