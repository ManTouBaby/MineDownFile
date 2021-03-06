package com.hrw.downlibrary.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.hrw.downlibrary.entity.DownBean;
import com.hrw.downlibrary.entity.DownEnumType;
import com.hrw.downlibrary.http.RetrofitHelper;
import com.hrw.downlibrary.listener.DownCallBack;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @version 1.0.0
 * @author:hrw
 * @date:2018/12/27 10:56
 * @desc:
 */
public class DownService extends Service implements DownCallBack {
    public static final String SERVICE_ACTION_TYPE = "service_action_type";//操作类型

    public static final String DOWN_URL = "down_url";//下载地址
    public static final String DOWN_ENUM_TYPE = "down_enum_type";//下载文件类型
    public static final String SAVE_PATH = "save_path";//保存本地地址
    public static final String SAVE_FILE_NAME = "save_file_name";//保存本地文件名称
    public static final String MAX_DOWN_COUNT = "max_down_count";//最大下载数量


    List<DownBean> waitDown = new ArrayList<>();
    Map<String, DownBean> doingDown = new LinkedHashMap<>();
    int maxDownCount = 3;

    @Override
    public void onCreate() {
        EventBus.getDefault().isRegistered(this);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        maxDownCount = intent.getIntExtra(MAX_DOWN_COUNT, 3);
        int actionType = intent.getIntExtra(SERVICE_ACTION_TYPE, -1);
        String url = intent.getStringExtra(DOWN_URL);
        switch (actionType) {
            case ServiceActionType.START:
                int downEnumType = intent.getIntExtra(DOWN_ENUM_TYPE, 0);
                String saveName = intent.getStringExtra(SAVE_FILE_NAME);
                String savePath = intent.getStringExtra(SAVE_PATH);

                DownBean bean = new DownBean();
                bean.setFileType(downEnumType);
                bean.setDownUrl(url);
                bean.setSavePath(savePath);
                bean.setSaveName(saveName);
                start(bean);
                break;
            case ServiceActionType.STOP:
                stop(url);
                break;
            case ServiceActionType.RESUME:
                resume(url);
                break;
            case ServiceActionType.DELETE:
                delete(url);
                break;
            case ServiceActionType.DELETE_ALL:
                RetrofitHelper.instance(this).deleteAll();
                break;
        }
        return START_STICKY_COMPATIBILITY;
    }

    private void start(DownBean bean) {
        if (doingDown.size() < maxDownCount) {
            if (doingDown.containsKey(bean.getDownUrl())) return;

            RetrofitHelper.instance(this).start(DownEnumType.getDownEnumType(bean.getFileType()), bean.getDownUrl(), bean.getSavePath(), bean.getSaveName(), this);
            doingDown.put(bean.getDownUrl(), bean);
        } else {
            waitDown.add(bean);
        }
    }

    private void delete(String url) {
        RetrofitHelper.instance(this).delete(url);

        DownBean bean = null;
        for (DownBean downBean : waitDown) {
            if (url.equals(downBean.getDownUrl())) {
                bean = downBean;
            }
        }

        if (bean!=null)waitDown.remove(bean);
        if (doingDown.containsKey(url))doingDown.remove(url);

    }

    private void stop(String url) {
        DownBean bean = null;
        if (doingDown.containsKey(url)) {
            bean = doingDown.get(url);
            doingDown.remove(url);
            waitDown.add(bean);
        }
        RetrofitHelper.instance(this).stop(url);
    }

    private void resume(String url) {
        DownBean bean = null;
        for (DownBean downBean : waitDown) {
            if (url.equals(downBean.getDownUrl())) {
                bean = downBean;
            }
        }
        if (bean != null) {
            waitDown.remove(bean);
            doingDown.put(bean.getDownUrl(), bean);
            RetrofitHelper.instance(this).resume(url, this);
        }

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDown(String url, DownBean downBean) {

    }

    @Override
    public void onComplete(String url) {
        if (doingDown.containsKey(url)) doingDown.remove(url);

        if (waitDown.size() > 0) {
            DownBean bean = waitDown.get(0);
            waitDown.remove(0);
            doingDown.put(bean.getDownUrl(), bean);
        }
    }

    @Override
    public void onError(String url, String errorMSG) {

    }
}
