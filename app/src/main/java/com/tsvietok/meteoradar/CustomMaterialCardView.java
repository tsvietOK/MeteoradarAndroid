package com.tsvietok.meteoradar;

import android.content.Context;
import android.util.AttributeSet;

import com.google.android.material.card.MaterialCardView;

public class CustomMaterialCardView extends MaterialCardView {

    public CustomMaterialCardView(Context context) {
        super(context);
    }

    public CustomMaterialCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomMaterialCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        this.getLayoutParams().height = this.getWidth();
    }
}
