package com.josfloy.magicmirror.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.josfloy.magicmirror.R;

/**
 * Created by Jos on 2018/8/1 0001.
 * You can copy it anywhere you want
 */
public class DrawView extends View {

    private Canvas mCanvas;                            // 画布
    private Path mPath;                                // 路径
    private Paint mPaint;                                // 画笔
    private float moveX, moveY;                        //移动坐标
    private Bitmap mBitmap;                            //图片变量
    private Bitmap bitmap;                            //图片变量
    private volatile boolean mComplete = false;        // 判断遮盖层区域是否消除达到阈值
    private Xfermode mXfermode;
    private OnCaYiCaCompleteListener mListener;

    public interface OnCaYiCaCompleteListener {
        void complete();

    }

    public DrawView(Context context) {
        this(context, null);
    }

    public DrawView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.glasses)
                .copy(Bitmap.Config.ARGB_8888, true);    //初始化图片加载
        mPaint = new Paint();            //新建画笔
        mPaint.setColor(Color.RED);            //设置画笔颜色
        mPaint.setStyle(Paint.Style.STROKE);        //设置画笔样式
        mPaint.setStrokeJoin(Paint.Join.ROUND);    //设置结合处样子
        mPaint.setStrokeCap(Paint.Cap.ROUND);        //设置画笔笔触风格
        mPaint.setDither(true);            // 设置递色
        mPaint.setAntiAlias(true);            //设置抗锯齿
        mPaint.setStrokeWidth(100);            //设置空心线宽
        mPath = new Path();                //创建新路径
        mXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.TRANSPARENT);

        if (!mComplete) {
            //设置目标图模式
            mPaint.setXfermode(mXfermode);
            canvas.drawBitmap(mBitmap, 0, 0, null);
            mCanvas.drawPath(mPath, mPaint);
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }

        //如果擦除干净，则进行资源释放操作
        if (mComplete) {
            if (mListener != null) {
                mListener.complete();        //监听结束
                setEndValues();            //变量重置
            }
        }
    }

    /**
     * 避免在 OnMeasure 和OnDraw这样高度重复调用的函数中创建对象
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        //基于原bitmap 创建一个新的宽高的bitmap
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(Color.TRANSPARENT);
        mCanvas.drawBitmap(bitmap, 0, 0, null);
    }

    public void setOnCaYiCaCompleteListener(OnCaYiCaCompleteListener mListener) {
        this.mListener = mListener;
    }

    /**
     * 变量重置
     */

    public void setEndValues() {
        moveX = 0;
        moveY = 0;
        mPath.reset();
        mComplete = false;
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            int w = getWidth();            //获取宽度
            int h = getHeight();            //获取高度

            float wipeArea = 0;
            float totalArea = w * h;            //面积计算
            Bitmap bitmap = mBitmap;
            int[] mPixels = new int[w * h];        //像素数组

            // 获得Bitmap上所有的像素信息
            bitmap.getPixels(mPixels, 0, w, 0, 0, w, h);

            //计算擦除区域
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    int index = i + j * w;
                    if (mPixels[index] == 0) {
                        wipeArea++;
                    }
                }
            }
            //计算擦除区域所占百分比
            if (wipeArea > 0 && totalArea > 0) {
                int percent = (int) (wipeArea * 100 / totalArea);
                Log.e("TAG", percent + "");

                if (percent > 50)            //如果擦除区域大于50%，则清除图层
                {
                    mComplete = true;            // 清除掉图层区域
                    postInvalidate();
                }
            }
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();                        //获得X坐标
        float y = event.getY();                        //获得Y坐标

        //获得触屏事件
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:                //按键按下
                moveX = x;
                moveY = y;
                mPath.moveTo(moveX, moveY);                //移动到坐标moveX、moveY处
                break;
            case MotionEvent.ACTION_MOVE:                //滑动事件
                int dx = (int) Math.abs(moveX - x);
                int dy = (int) Math.abs(moveY - y);
                if (dx > 1 || dy > 1) {
                    mPath.quadTo(x, y, (moveX + x) / 2, (moveY + y) / 2);
                }
                moveX = x;
                moveY = y;
                break;
            case MotionEvent.ACTION_UP:                //按键抬起
                if (!mComplete) {                        //如果擦除未完成，则新线程开始
                    new Thread(mRunnable).start();
                }
                break;

            default:
                break;
        }
        if (!mComplete) {
            invalidate();                                //刷新View控件
        }
        return true;
    }

}
