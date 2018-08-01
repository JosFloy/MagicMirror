package com.josfloy.magicmirror.activity;

import android.app.ProgressDialog;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.josfloy.magicmirror.R;
import com.josfloy.magicmirror.util.CameraManager;
import com.josfloy.magicmirror.view.DrawView;
import com.josfloy.magicmirror.view.FunctionView;
import com.josfloy.magicmirror.view.PictureView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static final String TAG = MainActivity.class.getSimpleName(); //获得类名
    private SurfaceHolder holder;            //用于控制SurfaceView的显示内容
    private SurfaceView surfaceView;        //显示相机拍摄的内容
    private PictureView pictureView;        //效果自定义View
    private FunctionView functionView;    //标题栏类声明
    private SeekBar seekBar;                //控制焦距滑动条
    private ImageView add, minus;            //控制焦距按钮
    private LinearLayout bottom;            //调节焦距的按钮
    private ImageView save;                //保存图片的按钮
    private ProgressDialog dialog;        //弹窗
    private DrawView drawView;            //绘画类

    //使用摄像头操作的属性
    private Camera camera;
    private int mCurrentCamIndex;        //相机的指数
    private int minFocus;                //当前手机默认的焦距
    private int maxFocus;                //当前手机的最大焦距
    private int everyFocus;            //用于调整焦距
    private int nowFocus;                //当前的焦距值

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setViews();
        camera = CameraManager.setCamera(this);
    }

    private void initViews() {
        surfaceView = findViewById(R.id.surface); //获得布局文件中Id为surface的组件
        pictureView = findViewById(R.id.picture);//获得布局文件中picture的组件
        functionView = findViewById(R.id.function);//获得布局文件中function组件
        seekBar = findViewById(R.id.seekbar);        //获得布局文件中seekbar拖动条
        add = findViewById(R.id.add);                //获得布局文件中add焦距放大组件
        minus = findViewById(R.id.minus);            //获得布局文件中minus焦距缩小组件
        bottom = findViewById(R.id.bottom_bar); //获得布局文件中底部线性布局
        drawView = findViewById(R.id.draw_glasses); //获得布局文件中擦屏组件
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.e("surfaceCreated", "绘制开始");
        try {
            camera.setPreviewDisplay(holder);    //设置预览显示的surfaceHolder对象
            camera.startPreview();        //开始预览
        } catch (IOException e) {
            camera.release();            //清空对象
            camera = null;            //相机对象赋值Null
            e.printStackTrace();        //打印错误信息
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.e("surfaceChanged", "绘制改变");
        try {
            camera.stopPreview();                    //相机停止预览
            camera.setPreviewDisplay(holder);            //设置相机预览显示区域
            camera.startPreview();                    //相机启动预览
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.e("surfaceDestroyed", "绘制结束");
        toRelease();                                    //释放相机资源
    }

    /**
     * 释放照相机的资源
     */
    private void toRelease() {
        camera.setPreviewCallback(null);                //停止相机视频，这个方法必须在前面，否则出错
        camera.stopPreview();                        //停止预览
        camera.release();                            //相机释放
        camera = null;                                //清空对象
    }

    private void setViews() {
        holder = surfaceView.getHolder();
        holder.addCallback(this);
    }
}
