package com.josfloy.magicmirror.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.josfloy.magicmirror.R;
import com.josfloy.magicmirror.utils.AudioRecordManager;
import com.josfloy.magicmirror.utils.CameraManager;
import com.josfloy.magicmirror.utils.MyBrokenCallback;
import com.josfloy.magicmirror.utils.SetBrightness;
import com.josfloy.magicmirror.view.DrawView;
import com.josfloy.magicmirror.view.FunctionView;
import com.josfloy.magicmirror.view.PictureView;
import com.zys.brokenview.BrokenCallback;
import com.zys.brokenview.BrokenTouchListener;
import com.zys.brokenview.BrokenView;

import java.io.IOException;

/**
 * 声明： 原程序来来自于明日科技的Android项目开发实战入门
 * 但 源程序写的太烂，在代码荣誉 设计 思路方面都存在各种冗余
 * 改动如下：
 * 一、添加 对权限的申请和验证
 * 二、修改主类 主类挂载太多多余的接口不符合设计原则
 * 三、增加各个功能类，不让单个类做过多的事情
 * 四、对于应用内的各个组件的通信 重新修改，其中表现为对相框的选择
 * 这些只是基础的东西，对系统的兼容性有很大的挑战
 * 未来计划改动的部分：
 * 增加框架的利用，重构代码 让其更能符合设计原则
 * 增加新的功能
 * 目前能用的框架也只有 注解和事件总线，还有就是对性能的分析
 */
public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback
        , View.OnTouchListener, View.OnClickListener {
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

    //起雾操作相关属性
    private AudioRecordManager mAudioRecordManager;
    private static final int RECORD = 2;

    //碎屏操作属性
    private BrokenView mBrokenView;
    private boolean isBroken;
    private BrokenCallback mCallback;
    private BrokenTouchListener mBrokenTouchListener;
    private Paint brokenPaint;

    private GestureDetector mGestureDetector;
    private MySimpleGestureListener mySimpleGestureListener;    //手势自定义子类

    class MySimpleGestureListener extends GestureDetector.SimpleOnGestureListener {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public void onLongPress(MotionEvent e) {        //开启碎屏效果
            super.onLongPress(e);
            Log.e("手势", "长按");
            isBroken = true;//碎屏
            mBrokenView.setEnable(isBroken);            //碎屏控件可用
            pictureView.setOnTouchListener(mBrokenTouchListener);//设置碎屏长按监听
            hideView();                    //隐藏控件
            mAudioRecordManager.isGetVoiceRun = false;        //设置话筒不启动
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setViews();

        //设置默认镜框ID数组漕
        frame_index = 0;
        frame_index_ID = new int[]{R.drawable.mag_0001, R.drawable.mag_0003, R.drawable.mag_0005,
                R.drawable.mag_0006, R.drawable.mag_0007, R.drawable.mag_0008, R.drawable.mag_0009,
                R.drawable.mag_0011, R.drawable.mag_0012, R.drawable.mag_0014};

        //camera = CameraManager.getCamera(this);
        requestCameraPermission();

        //设置屏幕亮度
        getBrightnessFromWindow();

        //实例化话筒并开启录音
        mAudioRecordManager = new AudioRecordManager(mHandler, RECORD);
        mAudioRecordManager.getNoiseLevel();

        //实现碎屏效果
        mySimpleGestureListener = new MySimpleGestureListener();   //创建手势识别监听对象
        mGestureDetector = new GestureDetector(this, mySimpleGestureListener);
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

        //为控件设置监听事件
        add.setOnClickListener(this);
        minus.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
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
        });
        functionView.setOnFunctionViewItemClickListener(new FunctionView.onFunctionViewItemClickListener() {
            @Override
            public void hint() {
                Intent intent = new Intent(MainActivity.this, HintActivity.class);
                startActivity(intent);
            }

            @Override
            public void choose() {
                Intent intent = new Intent(MainActivity.this, PhotoFrameActivity.class);
                startActivityForResult(intent, PHOTO);
                Toast.makeText(MainActivity.this, "选择！", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void down() {
                downCurrentActivityBrightnessValues();
            }

            @Override
            public void up() {
                upCurrentActivityBrightnessValues();
            }
        });
        pictureView.setOnTouchListener(this);
        drawView.setOnCaYiCaCompleteListener(new DrawView.OnCaYiCaCompleteListener() {
            @Override
            public void complete() {
                showView();
                mAudioRecordManager.getNoiseLevel();
                drawView.setVisibility(View.GONE);
            }
        });
        //设置碎屏的相关属性
        setToBrokenTheView();
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

    private void getCameraAndParams() {
        camera = CameraManager.getCamera(this);
        getCameraParams();
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
        //释放相机资源
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add:
                addZoomValues();
                break;
            case R.id.minus:
                minusZoomValues();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (view.getId()) {
            case R.id.picture:
                mGestureDetector.onTouchEvent(motionEvent);
                break;
            default:
                break;
        }
        return true;
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

    private void hideView() {
        bottom.setVisibility(View.INVISIBLE);            //底部焦距缩放不可见
        functionView.setVisibility(View.GONE);            //顶部亮度、帮助、选择镜框不可见
    }

    private void showView() {
        pictureView.setImageBitmap(null);                //设置图片为null
        bottom.setVisibility(View.VISIBLE);            //底部焦距缩放可见
        functionView.setVisibility(View.VISIBLE);        //顶部亮度、帮助、选择镜框可见
    }

    private void getSoundValues(double values) {
        //话筒分贝大于50，屏幕起雾
        if (values > 50) {
            hideView();
            drawView.setVisibility(View.VISIBLE);
            //设置补间动画
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.in_window);
            drawView.setAnimation(animation);
            mAudioRecordManager.isGetVoiceRun = false; //停止话筒录音
            Log.e("玻璃显示", "执行");
        }
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case RECORD:                     //检测话筒
                    double soundValues = (double) message.obj;
                    getSoundValues(soundValues);    //获得话筒声音后，屏幕重绘起雾
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    private void setToBrokenTheView() {
        brokenPaint = new Paint();
        brokenPaint.setStrokeWidth(5);
        brokenPaint.setColor(Color.BLACK);
        brokenPaint.setAntiAlias(true);
        mBrokenView = BrokenView.add2Window(this);
        mBrokenTouchListener = new BrokenTouchListener
                .Builder(mBrokenView)
                .setPaint(brokenPaint)
                .setBreakDuration(2000)
                .setFallDuration(5000)
                .build();
        mBrokenView.setEnable(true);
        mCallback = new MyBrokenCallback();
        mBrokenView.setCallback(mCallback);
    }
}
