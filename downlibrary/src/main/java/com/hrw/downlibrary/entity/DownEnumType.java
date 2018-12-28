package com.hrw.downlibrary.entity;

import android.support.annotation.IntRange;

/**
 * @version 1.0.0
 * @author:hrw
 * @date:2018/12/28 11:24
 * @desc:
 */
public enum DownEnumType {
    APP(0, "下载APP", "/app/"),
    MUSIC(1, "下载音乐", "/music/"),
    MOVIE(2, "下载电影", "/movie/"),
    IMAGE(3, "下载图片", "/image/"),
    FILE(4, "下载文件", "/files/");

    private String typePath;
    private int typeValue;
    private String typeName;

    DownEnumType(int typeValue, String type, String typePath) {
        this.typeValue = typeValue;
        this.typeName = type;
        this.typePath = typePath;
    }

    public String getTypeName() {
        return typeName;
    }

    public int getTypeValue() {
        return typeValue;
    }

    public String getTypePath() {
        return typePath;
    }

    public static DownEnumType getDownEnumType(@IntRange(from = 0L, to = 4L) int typeValue) {
        if (typeValue == 0) {
            return APP;
        } else if (typeValue == 1) {
            return MUSIC;
        } else if (typeValue == 2) {
            return MOVIE;
        } else if (typeValue == 3) {
            return IMAGE;
        } else {
            return FILE;
        }
    }
}
