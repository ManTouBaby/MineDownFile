package com.hrw.downlibrary;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import com.hrw.downlibrary.service.DownService;

import java.util.List;

/**
 * @version 1.0.0
 * @author:hrw
 * @date:2018/12/27 10:34
 * @desc:
 */
public class DownFileHelper {
    private static DownFileHelper mFileHelper;
    private Context mContext;

    private DownFileHelper(Context context) {
        mContext = context;
    }


    public DownFileHelper instance(Context context) {
        if (mFileHelper == null) {
            synchronized (DownFileHelper.class) {
                if (mFileHelper == null) {
                    mFileHelper = new DownFileHelper(context);
                }
            }
        }
        return mFileHelper;
    }

    public DownFileHelper start(String url) {
        if (!isServiceRuning("com.hrw.downlibrary.service.DownService")) {
            Intent intent = new Intent(mContext, DownService.class);
            mContext.startService(intent);
        }
        return mFileHelper;
    }

    public DownFileHelper start(List<String> urls) {
        if (!isServiceRuning("com.hrw.downlibrary.service.DownService")) {
            Intent intent = new Intent(mContext, DownService.class);
            mContext.startService(intent);
        }
        return mFileHelper;
    }

    public DownFileHelper stop(String url) {
        return mFileHelper;
    }

    public DownFileHelper stopAll() {
        return mFileHelper;
    }

    public DownFileHelper resume(String url) {
        return mFileHelper;
    }

    private boolean isServiceRuning(String serviceName) {
        boolean isRunning = false;
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceInfos = manager.getRunningServices(45);
        for (ActivityManager.RunningServiceInfo serviceInfo : serviceInfos) {
            if (serviceInfo.service.getClassName().equals(serviceName)) {
                isRunning = true;
            }
        }
        return isRunning;
    }
}
