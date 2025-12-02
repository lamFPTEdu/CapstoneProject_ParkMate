package com.parkmate.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.recyclerview.widget.RecyclerView;
import com.parkmate.android.R;

public class MaxHeightRecyclerView extends RecyclerView {
    private int maxHeight = -1;

    public MaxHeightRecyclerView(Context context) {
        super(context);
    }

    public MaxHeightRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public MaxHeightRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);
    }

    private void initialize(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray array = null;
            try {
                array = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightRecyclerView, 0, 0);
                // getDimensionPixelSize returns pixels for a dimension attribute
                maxHeight = array.getDimensionPixelSize(R.styleable.MaxHeightRecyclerView_maxHeight, -1);
            } finally {
                if (array != null) {
                    array.recycle();
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        if (maxHeight > 0) {
            heightSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthSpec, heightSpec);
    }
}

