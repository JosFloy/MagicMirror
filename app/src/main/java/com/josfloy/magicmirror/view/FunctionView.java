package com.josfloy.magicmirror.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.josfloy.magicmirror.R;

/**
 * Created by Jos on 2018/8/1 0001.
 * You can copy it anywhere you want
 */
public class FunctionView extends LinearLayout implements View.OnClickListener {
    public static final int HINT_ID = R.id.hint;        //提示控件ID
    public static final int CHOOSE_ID = R.id.choose;    //选择镜框控件ID
    public static final int DOWN_ID = R.id.light_down;    //减少亮度控件ID
    public static final int UP_ID = R.id.light_up;        //增加亮度控件ID

    private ImageView hint, choose, down, up;            //控件对象，包括：系统帮助、亮度、选择镜框
    private LayoutInflater mInflater;

    /**
     * 回调接口，调用4个按钮方法，后续将逐步实现这4个方法
     */
    private onFunctionViewItemClickListener mlistener;

    @Override
    public void onClick(View view) {
        if (mlistener != null) {                //监听不为空，表示有按钮按下
            switch (view.getId()) {
                case HINT_ID:                //帮助按钮
                    mlistener.hint();            //执行监听方法，实现功能
                    break;
                case CHOOSE_ID:                //选择镜框按钮
                    mlistener.choose();            //执行监听方法，实现功能
                    break;
                case DOWN_ID:                //减少亮度按钮
                    mlistener.down();            //执行监听方法，实现功能
                    break;
                case UP_ID:                //增加亮度
                    mlistener.up();            //执行监听方法，实现功能
                    break;
                default:
                    break;
            }
        }
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
        mInflater = LayoutInflater.from(context);
        init();
    }

    private void setView() {
        hint.setOnClickListener(this);
        choose.setOnClickListener(this);
        down.setOnClickListener(this);
        up.setOnClickListener(this);
    }

    /**
     * 初始化控件，导入布局
     */
    private void init() {
        View view = mInflater.inflate(R.layout.function_bar, this);//获取view_function布局
        hint = view.findViewById(HINT_ID);                //获取帮助按钮对象
        choose = view.findViewById(CHOOSE_ID);            //获取选择镜框按钮对象
        down = view.findViewById(DOWN_ID);                //获取减少亮度按钮对象
        up = view.findViewById(UP_ID);                    //获取增加亮度按钮对象
        setView();                                                //调用设置控件
    }

    public void setOnFunctionViewItemClickListener(onFunctionViewItemClickListener listener) {
        mlistener = listener;
    }
}
