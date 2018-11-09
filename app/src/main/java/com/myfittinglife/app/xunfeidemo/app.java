package com.myfittinglife.app.xunfeidemo;

import android.app.Application;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

/**
 * 作者    LD
 * 时间    2018/11/6 14:38
 * 描述    讯飞SDK初始化
 */
public class app extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5bdf992a");
    }
}
