package com.josfloy.magicmirror.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.net.Uri;
import android.provider.Settings;
import android.view.WindowManager;

/**
 * Created by Jos on 2018/8/3 0003.
 * You can copy it anywhere you want
 */
public class SetBrightness {
    public static boolean isAutoBrightness(ContentResolver aContentResolver) {
        boolean automicBrightness = false;
        try {
            //获取系统设置的亮度调节模式
            automicBrightness = Settings.System.getInt(aContentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE) ==
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        return automicBrightness;
    }

    public static int getScreenBrightness(Activity activity) {
        int nowBrightnessValue = 0;
        //通过ContentResolver接口访问ContentProviders数据,获得ContentResolver实例
        ContentResolver resolver = activity.getContentResolver();
        try {
            nowBrightnessValue = android.provider.Settings.System
                    .getInt(resolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return nowBrightnessValue;
    }

    public static void setScreenBrightness(Activity activity, int brightness) {
        WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();
        layoutParams.screenBrightness = (float) brightness * (1f / 255f);
        activity.getWindow().setAttributes(layoutParams);
    }

    public static void startAutoBrightness(Activity activity) {
        Settings.System.putInt(activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
    }

    public static void stopAutoBrightness(Activity activity) {
        Settings.System.putInt(activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

    public static void saveBrightness(ContentResolver resolver, int brightness) {
        //获取屏幕亮度
        Uri uri = android.provider.Settings.System.getUriFor("screen_brightness");
        //设置屏幕亮度
        android.provider.Settings.System.putInt(resolver,
                "screen_brightness",
                brightness);
        resolver.notifyChange(uri, null);
    }

    public static ContentResolver getResolver(Activity activity) {
        return activity.getContentResolver();
    }

}
