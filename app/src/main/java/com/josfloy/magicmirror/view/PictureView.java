package com.josfloy.magicmirror.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;

import com.josfloy.magicmirror.R;

/**
 * Created by Jos on 2018/8/1 0001.
 * You can copy it anywhere you want
 */
public class PictureView extends AppCompatImageView {

    private int[] bitmap_ID_Array;            //图片资源ID的数组
    private Canvas mCanvas;                    //画布
    private int draw_Width;                    //要画的长度
    private int draw_Height;                    //要画的高度
    private Bitmap mBitmap;                    //镜框
    private int bitmap_index;                    //图片标记
    private Rect mRect;

    public PictureView(Context context) {
        this(context, null);
    }

    public PictureView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PictureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getTheWindowSize((Activity) context);
        init();
    }

    /**
     * 一段图片资源 来来回回的在(PictureView、MainActivity、PhotoFrameActivity)三个类中折腾，太低能了。
     * 解决办法：
     * 建立一个公共的能够让三个类甚至更多类读取的资源类，任何想要获取图片信息的都去这个类取
     * 封装 图片和名字，可以以类的方式，也能以Map或者HashMap的方式来封装，还要记得改写GridView
     * 的Adapter
     */
    private void initBitmaps() {
        //获取drawable资源中的图片
        bitmap_ID_Array = new int[]{R.drawable.mag_0001, R.drawable.mag_0003, R.drawable.mag_0005,
                R.drawable.mag_0006, R.drawable.mag_0007, R.drawable.mag_0008, R.drawable.mag_0009,
                R.drawable.mag_0011, R.drawable.mag_0012, R.drawable.mag_0014};
    }

    private void init() {
        initBitmaps();                        //初始化图片集合
        bitmap_index = 0;                    //图片索引
        //初始化画布
        mBitmap = Bitmap.createBitmap(draw_Width, draw_Height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);            //实例化Canvas对象
        mCanvas.drawColor(Color.TRANSPARENT);  //设置背景色为透明色
        //可能会有问题，在init的时候 不知道能不能获得到此View的 Width和Height
        mRect = new Rect(0, 0, this.getWidth(), this.getHeight());
    }

    public void setPhotoFrame(int index) {
        bitmap_index = index;
        //调用invalidate()能让窗口无效，重绘
        invalidate();
    }

    public int getPhotoFrame() {
        return bitmap_index;
    }

    private void getTheWindowSize(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();    //获取屏幕分辨率的类
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);//获取屏幕显示属性
        draw_Width = dm.widthPixels;        //宽度
        draw_Height = dm.heightPixels;        //高度
        Log.e("1、屏幕宽度：", draw_Width + "\t\t屏幕高度：" + draw_Height);
    }

    private Bitmap getNewBitmap() {
        //根据bitmap_index获取图片
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                bitmap_ID_Array[bitmap_index]).copy(Bitmap.Config.ARGB_8888, true);
        //根据长、宽设置图片
        bitmap = Bitmap.createScaledBitmap(bitmap, draw_Width, draw_Height, true);
        return bitmap;        //返回获取的图片对象
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawBitmap(mBitmap, 0, 0, null);
        canvas.drawBitmap(getNewBitmap(), null, mRect, null);
    }
}
