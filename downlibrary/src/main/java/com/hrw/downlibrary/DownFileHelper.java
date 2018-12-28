package com.hrw.downlibrary;

import java.util.List;

/**
 * @version 1.0.0
 * @author:hrw
 * @date:2018/12/27 10:34
 * @desc:
 */
public class DownFileHelper {
    private static DownFileHelper mFileHelper;

    private DownFileHelper() {
    }


    public DownFileHelper instance() {
        if (mFileHelper == null) {
            synchronized (DownFileHelper.class) {
                if (mFileHelper == null) {
                    mFileHelper = new DownFileHelper();
                }
            }
        }
        return mFileHelper;
    }

    public DownFileHelper start(String url) {
        return mFileHelper;
    }

    public DownFileHelper start(List<String> urls) {
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

}
