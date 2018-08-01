package com.josfloy.magicmirror.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.josfloy.magicmirror.R;

/**
 * Created by Jos on 2018/8/1 0001.
 * You can copy it anywhere you want
 */
public class FunctionView extends LinearLayout implements View.OnClickListener {

    /**
     * 回调接口，调用4个按钮方法，后续将逐步实现这4个方法
     */

    private onFunctionViewItemClickListener listener;

    @Override
    public void onClick(View view) {

    }

    public interface onFunctionViewItemClickListener {
        void hint();

        void choose();

        void down();

        void up();
    }

    public FunctionView(Context context) {
        this(context, null);
    }

    public FunctionView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FunctionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.function_bar, this);
    }
}
