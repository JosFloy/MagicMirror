package com.josfloy.magicmirror.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Jos on 2018/8/1 0001.
 * You can copy it anywhere you want
 */
public class DrawView extends View {
    public DrawView(Context context) {
        this(context,null);
    }

    public DrawView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public DrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
