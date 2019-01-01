package com.hrw.downlibrary.entity;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.hrw.downlibrary.entity.DownStatus.ON_COMPLETE;
import static com.hrw.downlibrary.entity.DownStatus.ON_DOWN;
import static com.hrw.downlibrary.entity.DownStatus.ON_ERROR;
import static com.hrw.downlibrary.entity.DownStatus.ON_STOP;

/**
 * @version 1.0.0
 * @author:hrw
 * @date:2019/01/01 15:35
 * @desc:
 */
@IntDef({
        ON_DOWN,
        ON_STOP,
        ON_ERROR,
        ON_COMPLETE
})
@Retention(RetentionPolicy.SOURCE)
public @interface DownStatus {//0-表示正在下载  1-表示停止下载  2-表示下载失败  3-表示下载完成
    int ON_DOWN = 0;
    int ON_STOP = 1;
    int ON_ERROR = 2;
    int ON_COMPLETE = 3;
}
