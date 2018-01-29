package com.waimai.baidu.performance.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Checkable;

/**
 * Created by iwm on 2018/1/24.
 * 可选择的list的item
 */

public class CheckableFrameLayout extends FrameLayout implements Checkable {

    public CheckableFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private boolean mChecked = false;

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            refreshDrawableState();
            for (int i = 0, len = getChildCount(); i < len; i++) {
                View child = getChildAt(i);
                if (child instanceof Checkable) {
                    ((Checkable) child).setChecked(checked);
                }
            }
        }
    }

}