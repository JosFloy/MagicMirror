package com.josfloy.magicmirror.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;

import java.util.List;

/**
 * Created by Jos on 2018/8/1 0001.
 * You can copy it anywhere you want
 */

/**
 * 相机操作的相关工具类
 */
public class CameraManager {
    //设置摄像头方向getCameraInfo需要一个摄像头ID
    public static int mCurrentCamIndex;

    /**
     * 判断是否有摄像头
     */
    public static boolean checkCameraHardware(Context mContext) {
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /**
     * 打开前置摄像头
     */
    public static Camera openFrontFacingCameraGingerbread() {
        int cameraCount;
        Camera camera = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo(); //创建相机对象
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            //判断是否是前置摄像头
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    camera = Camera.open(camIdx);
                    mCurrentCamIndex = camIdx;
                } catch (RuntimeException e) {
                    Log.e("相机操作", "相机打开失败: " + e.getLocalizedMessage());
                }
            }
        }
        return camera;
    }

    /**
     * 设置摄像头方向
     */
    public static void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        //获得旋转角度
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:                                //旋转90度
                degrees = 90;
                break;
            case Surface.ROTATION_180:                                //旋转180度
                degrees = 180;
                break;
            case Surface.ROTATION_270:                                //旋转270度
                degrees = 270;
                break;
        }

        int result = 0;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            //前置摄像头旋转算法
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            //后置摄像头旋转算法
            result = (info.orientation - degrees + 360) % 360;
        }
        //ROTATE = result + 180;
        camera.setDisplayOrientation(result);
    }

    /**
     * 设置摄像头
     */
    public static Camera setCamera(Activity mContext) {
        if (checkCameraHardware(mContext)) {
            Camera camera = openFrontFacingCameraGingerbread();

            setCameraDisplayOrientation(mContext, mCurrentCamIndex, camera);
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPictureFormat(ImageFormat.JPEG);

            /* List<String> list = parameters.getSupportedFocusModes();
            for (String str : list) {
                Log.e("CameraManager", "支持的对焦模式: " + str);
            }*/

            //手机支持的图片尺寸集合
            List<Camera.Size> pictureList = parameters.getSupportedPictureSizes();
            //手机支持的预览尺寸集合
            List<Camera.Size> previewList = parameters.getSupportedPreviewSizes();
            //设置为当前使用手机的最大尺寸
            parameters.setPictureSize(pictureList.get(0).width, pictureList.get(0).height);
            parameters.setPreviewSize(previewList.get(0).width, previewList.get(0).height);

            camera.setParameters(parameters);
            return camera;
        } else {
            return null;
        }
    }
}
