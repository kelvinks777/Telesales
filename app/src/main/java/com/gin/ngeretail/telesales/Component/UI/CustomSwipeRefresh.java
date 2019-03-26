package com.gin.ngeretail.telesales.Component.UI;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

public class CustomSwipeRefresh extends SwipeRefreshLayout {
    public CustomSwipeRefresh(Context context) {
        super(context);
        setColor();
    }

    public CustomSwipeRefresh(Context context, AttributeSet attrs) {
        super(context, attrs);
        setColor();
    }

    private void setColor() {
        this.setColorSchemeResources(
                android.R.color.holo_orange_light,
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_red_light);
    }
}