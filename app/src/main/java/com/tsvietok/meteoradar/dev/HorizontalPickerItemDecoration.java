package com.tsvietok.meteoradar.dev;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class HorizontalPickerItemDecoration extends RecyclerView.ItemDecoration {
    private final int padding;

    HorizontalPickerItemDecoration(int padding) {
        this.padding = padding;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        outRect.left = padding;
        outRect.right = padding;
    }
}
