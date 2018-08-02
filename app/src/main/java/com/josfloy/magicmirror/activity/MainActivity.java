package com.josfloy.magicmirror.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

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
        //camera = CameraManager.getCamera(this);
        requestCameraPermission();
    }

    private void setViews() {
        holder = surfaceView.getHolder();
        holder.addCallback(this);
    }

    private void getCameraParams() {
        final Camera.Parameters parameters = camera.getParameters();
        minFocus = parameters.getZoom();
        maxFocus = parameters.getMaxZoom();
        everyFocus = 1;
        nowFocus = minFocus;
        seekBar.setMax(maxFocus);
        Log.e(TAG, "当前镜头距离： " + minFocus + "\t\t获得最大距离: " + maxFocus);
    }

    private void initViews() {
        surfaceView = findViewById(R.id.surface);
        pictureView = findViewById(R.id.picture);
        functionView = findViewById(R.id.function);
        seekBar = findViewById(R.id.seekbar);
        add = findViewById(R.id.add);
        minus = findViewById(R.id.minus);
        bottom = findViewById(R.id.bottom_bar);
        drawView = findViewById(R.id.draw_glasses);
    }

    /**
     * 有个问题，在第一次 申请权限获得成功后不能立刻显示出图像，因为此时
     * Camera类并没有获得初始化值，surfaceCreated中因为必定会出现空值
     */
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.e("surfaceCreated", "绘制开始");
        try {
            //camera = CameraManager.getCamera(this);
            //在surfaceCreate创建的时候请求权限显然不合适
            // /requestCameraPermission();
            //getCameraParams();
            if (camera != null) {
                camera.setPreviewDisplay(holder);    //设置预览显示的surfaceHolder对象
                camera.startPreview();        //开始预览
            } /*else {
                //在第一次进入surfaceCreated时候进行判断，如果不为空就显示
                //如果为空，就重新刷新surfaceView

            }*/

        } catch (IOException e) {
            camera.release();            //清空对象
            camera = null;            //相机对象赋值Null
            e.printStackTrace();        //打印错误信息
        }
    }

    private void getCameraAndParams() {
        camera = CameraManager.getCamera(this);
        getCameraParams();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.e("surfaceChanged", "绘制改变");
        try {
            if (camera != null) {
                camera.stopPreview();                    //相机停止预览
                camera.setPreviewDisplay(holder);            //设置相机预览显示区域
                camera.startPreview();                    //相机启动预览
            } else {
                surfaceCreated(surfaceHolder);
            }
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
        if (camera != null) {
            camera.setPreviewCallback(null);                //停止相机视频，这个方法必须在前面，否则出错
            camera.stopPreview();                        //停止预览
            camera.release();                            //相机释放
            camera = null;                                //清空对象
        }
    }

    private boolean hasPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        if (hasPermission()) {
            getCameraAndParams();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCameraAndParams();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        requestCameraPermission();
    }
}
