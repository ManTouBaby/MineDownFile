package com.hrw.downlibrary.entity;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.hrw.downlibrary.entity.DownType.APP;
import static com.hrw.downlibrary.entity.DownType.FILE;
import static com.hrw.downlibrary.entity.DownType.IMAGE;
import static com.hrw.downlibrary.entity.DownType.MOVIE;
import static com.hrw.downlibrary.entity.DownType.MUSIC;

/**
 * @version 1.0.0
 * @author:hrw
 * @date:2018/12/28 11:01
 * @desc:
 */
@IntDef({APP, MUSIC, MOVIE, IMAGE, FILE})
@Retention(RetentionPolicy.SOURCE)
public @interface DownType {
    int APP     = 0;
    int FILE    = 1;
    int MUSIC   = 2;
    int MOVIE   = 3;
    int IMAGE   = 4;
}
