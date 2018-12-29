package com.hrw.downlibrary.listener;

import com.hrw.downlibrary.entity.DownBean;

/**
 * @version 1.0.0
 * @author:hrw
 * @date:2018/12/29 9:51
 * @desc:
 */
public interface DownCallBack {
    void onDown(String url, DownBean downBean);

    void onComplete(String url);

    void onError(String url, String errorMSG);

}
