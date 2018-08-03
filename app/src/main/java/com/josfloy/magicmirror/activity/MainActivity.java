package com.josfloy.magicmirror.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.josfloy.magicmirror.R;
import com.josfloy.magicmirror.utils.CameraManager;
import com.josfloy.magicmirror.utils.SetBrightness;
import com.josfloy.magicmirror.view.DrawView;
import com.josfloy.magicmirror.view.FunctionView;
import com.josfloy.magicmirror.view.PictureView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback
        , SeekBar.OnSeekBarChangeListener, View.OnTouchListener, View.OnClickListener,
        FunctionView.onFunctionViewItemClickListener {
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

    //镜框相关的属性
    private int frame_index;
    private int[] frame_index_ID;
    private static final int PHOTO = 1;

    //设置亮度相关属性
    private int brightnessValue;
    private boolean isAutoBrightness;
    private int SegmentLength;     //把亮度分为8段，每段为256的1/8

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setViews();

        //设置默认镜框ID数组
        frame_index = 0;
        frame_index_ID = new int[]{R.drawable.mag_0001, R.drawable.mag_0003, R.drawable.mag_0005,
                R.drawable.mag_0006, R.drawable.mag_0007, R.drawable.mag_0008, R.drawable.mag_0009,
                R.drawable.mag_0011, R.drawable.mag_0012, R.drawable.mag_0014};

        //camera = CameraManager.getCamera(this);
        requestCameraPermission();

        //设置屏幕亮度
        getBrightnessFromWindow();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        requestCameraPermission();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setViews() {
        holder = surfaceView.getHolder();
        holder.addCallback(this);
        add.setOnTouchListener(this);
        minus.setOnTouchListener(this);
        seekBar.setOnSeekBarChangeListener(this);
        functionView.setOnFunctionViewItemClickListener(this);
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

    private void setZoomValues(int want) {
        Camera.Parameters parameters = camera.getParameters();
        seekBar.setProgress(want);
        parameters.setZoom(want);
        camera.setParameters(parameters);
    }

    private int getZoomValues() {
        Camera.Parameters parameters = camera.getParameters();
        return parameters.getZoom();
    }

    private void addZoomValues() {
        if (nowFocus > maxFocus) {        //当前焦距 大于 最大焦距
            Log.e(TAG, "大于maxFocus是不可能的！");
        } else if (nowFocus != maxFocus) {                //设置焦距，当前焦距 + 每一次变化的值
            setZoomValues(getZoomValues() + everyFocus);
        }
    }

    private void minusZoomValues() {
        if (nowFocus < 0) {
            Log.e(TAG, "小于0是不可能的！");
        } else if (nowFocus != 0) {                //设置焦距，当前焦距 - 每一次变化的值
            setZoomValues(getZoomValues() - everyFocus);
        }
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //0~99  99级
        Camera.Parameters parameters = camera.getParameters();    //获取相机参数
        nowFocus = progress;                    //进度值赋值给焦距
        parameters.setZoom(progress);                //设置焦距
        camera.setParameters(parameters);                //设置相机
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    @Override
    public void onClick(View view) {

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (view.getId()) {
            case R.id.add:
                addZoomValues();
                break;
            case R.id.minus:
                minusZoomValues();
                break;
            case R.id.picture:
                //待添加手势识别事件方法
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void hint() {
        Intent intent = new Intent(this, HintActivity.class);
        startActivity(intent);
    }

    @Override
    public void choose() {
        Intent intent = new Intent(this, PhotoFrameActivity.class);
        startActivityForResult(intent, PHOTO);
        Toast.makeText(this, "选择！", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void down() {
        downCurrentActivityBrightnessValues();
    }

    @Override
    public void up() {
        upCurrentActivityBrightnessValues();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "返回值： " + resultCode + "\t\t 请求值： " + requestCode);
        if (resultCode == RESULT_OK && requestCode == PHOTO) {
            frame_index = data.getIntExtra("POSITION", 0);
            pictureView.setPhotoFrame(frame_index);
        }
    }

    private void setMyActivityBright(int brightnessValue) {
        SetBrightness.setScreenBrightness(this, brightnessValue);
        SetBrightness.saveBrightness(SetBrightness.getResolver(this), brightnessValue);
    }

    private void getAfterMySetBrightnessValues() {
        brightnessValue = SetBrightness.getScreenBrightness(this);    //获得亮度
        Log.e(TAG, "当前手机屏幕亮度值:" + brightnessValue);
    }

    public void getBrightnessFromWindow() {
        //获得是否自动调节亮度
        isAutoBrightness = SetBrightness.isAutoBrightness(SetBrightness.getResolver(this));
        Log.e(TAG, "当前手机是否是自动调节屏幕亮度: " + isAutoBrightness);

        if (isAutoBrightness) {
            SetBrightness.stopAutoBrightness(this);
            Log.e(TAG, "关闭了自动调节");
            setMyActivityBright(255 / 2 + 1);
        }

        //亮度值 0~256
        SegmentLength = (255 / 2 + 1) / 4;
        getAfterMySetBrightnessValues();
    }

    private void downCurrentActivityBrightnessValues() {
        if (brightnessValue > 0) {
            setMyActivityBright(brightnessValue - SegmentLength);  //减少亮度
        }
        getAfterMySetBrightnessValues();            //获取设置后的屏幕亮度
    }

    private void upCurrentActivityBrightnessValues() {
        if (brightnessValue < 255) {
            if (brightnessValue + SegmentLength >= 256) {        //最大值256
                return;
            }
            setMyActivityBright(brightnessValue + SegmentLength);//调高亮度
        }
        getAfterMySetBrightnessValues();            //获取设置后的屏幕亮度
    }

}
