package com.tsvietok.meteoradar;

import android.content.Context;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

public class HorizontalPickerLayoutManager extends LinearLayoutManager {
    private static final float MILLISECONDS_PER_INCH = 100f;
    private static final float SCALE_DOWN_FACTOR = 0.44f;

    HorizontalPickerLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state,
                                       int position) {
        LinearSmoothScroller linearSmoothScroller =
                new LinearSmoothScroller(recyclerView.getContext()) {
                    @Override
                    public PointF computeScrollVectorForPosition(int targetPosition) {
                        return super.computeScrollVectorForPosition(targetPosition);
                    }

                    @Override
                    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                        return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
                    }
                };
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        scaleDownView();
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler,
                                    RecyclerView.State state) {
        int orientation = getOrientation();
        if (orientation == RecyclerView.HORIZONTAL) {
            int scrolled = super.scrollHorizontallyBy(dx, recycler, state);
            scaleDownView();
            return scrolled;
        } else {
            return 0;
        }
    }

    private void scaleDownView() {
        float middle = getWidth() / 2.0f;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            float childMid;
            if (child != null) {
                childMid = (getDecoratedLeft(child) + getDecoratedRight(child)) / 2.0f;
                float distanceFromCenter = Math.abs(middle - childMid);
                float scale = 1 - (float) Math.sqrt((distanceFromCenter / getWidth())) * SCALE_DOWN_FACTOR;

                child.setScaleX(scale);
                child.setScaleY(scale);
            }
        }
    }
}
