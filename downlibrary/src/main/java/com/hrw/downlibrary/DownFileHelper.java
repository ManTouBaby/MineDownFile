package com.hrw.downlibrary;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.hrw.downlibrary.entity.DownEnumType;
import com.hrw.downlibrary.service.DownService;
import com.hrw.downlibrary.service.ServiceActionType;

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
    private int maxDownCount = 3;

    private DownFileHelper(Context context) {
        mContext = context;
    }


    public static DownFileHelper instance(Context context) {
        if (mFileHelper == null) {
            synchronized (DownFileHelper.class) {
                if (mFileHelper == null) {
                    mFileHelper = new DownFileHelper(context);
                }
            }
        }
        return mFileHelper;
    }

    /**
     * 设置最大同时下载数量
     *
     * @param maxDownCount
     */
    public void setMaxDownCount(int maxDownCount) {
        this.maxDownCount = maxDownCount;
    }
    /**
     * 开始下载任务
     *
     * @param url          下载地址
     * @param downEnumType 保存类型
     */
    public DownFileHelper start(DownEnumType downEnumType, final String url) {
        String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + mContext.getPackageName() + downEnumType.getTypePath();
        start(downEnumType, url, savePath);
        return mFileHelper;
    }

    /**
     * 开始下载任务
     *
     * @param url          下载地址
     * @param savePath     保存地址
     * @param downEnumType 保存类型
     */
    public DownFileHelper start(DownEnumType downEnumType, final String url, String savePath) {
        String saveName = url.substring(url.lastIndexOf("/") + 1);
        start(downEnumType, url, savePath, saveName);
        return mFileHelper;
    }

    /**
     * 开始下载任务
     *
     * @param downEnumType
     * @param url
     * @param savePath
     * @param saveName
     * @return
     */
    public DownFileHelper start(DownEnumType downEnumType, String url, String savePath, String saveName) {
        startService(ServiceActionType.START, downEnumType, url, savePath, saveName);
        return mFileHelper;
    }

    public DownFileHelper stop(String url) {
        startService(ServiceActionType.STOP, url);
        return mFileHelper;
    }

    public DownFileHelper stopAll() {
        startService(ServiceActionType.STOP_ALL);
        return mFileHelper;
    }

    public DownFileHelper resume(String url) {
        startService(ServiceActionType.RESUME, url);
        return mFileHelper;
    }

    public DownFileHelper delete(String url) {
        startService(ServiceActionType.DELETE, url);
        return mFileHelper;
    }

    /**
     * 删除所有下载的文件
     *
     * @return
     */
    public DownFileHelper deleteAll() {
        startService(ServiceActionType.DELETE_ALL);
        return mFileHelper;
    }

    /**
     * 下载所有指定下载的文件
     *
     * @param urls
     * @return
     */
    public DownFileHelper deleteAll(List<String> urls) {
        return mFileHelper;
    }

    private void startService(ServiceActionType actionType, DownEnumType downEnumType, @NonNull String url, String savePath, String saveName) {
        Intent intent = new Intent(mContext, DownService.class);
        intent.putExtra(DownService.SERVICE_ACTION_TYPE, actionType);
        intent.putExtra(DownService.MAX_DOWN_COUNT, maxDownCount);
        intent.putExtra(DownService.DOWN_URL, url);

        if (downEnumType != null) intent.putExtra(DownService.DOWN_ENUM_TYPE, downEnumType);
        if (savePath != null) intent.putExtra(DownService.SAVE_PATH, savePath);
        if (saveName != null) intent.putExtra(DownService.SAVE_FILE_NAME, saveName);
        mContext.startService(intent);
    }

    private void startService(ServiceActionType actionType, @NonNull String url) {
        Intent intent = new Intent(mContext, DownService.class);
        intent.putExtra(DownService.SERVICE_ACTION_TYPE, actionType);
        intent.putExtra(DownService.MAX_DOWN_COUNT, maxDownCount);
        intent.putExtra(DownService.DOWN_URL, url);
        mContext.startService(intent);
    }

    private void startService(ServiceActionType actionType) {
        Intent intent = new Intent(mContext, DownService.class);
        intent.putExtra(DownService.SERVICE_ACTION_TYPE, actionType);
        intent.putExtra(DownService.MAX_DOWN_COUNT, maxDownCount);
        mContext.startService(intent);
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
