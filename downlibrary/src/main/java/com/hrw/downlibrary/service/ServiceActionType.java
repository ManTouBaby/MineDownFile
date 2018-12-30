package com.hrw.downlibrary.service;

import android.support.annotation.IntRange;

/**
 * @version 1.0.0
 * @author:hrw
 * @date:2018/12/29 10:59
 * @desc:
 */
public enum ServiceActionType {
    START(0),
    STOP(1),
    STOP_ALL(2),
    RESUME(3),
    DELETE(4),
    DELETE_ALL(5);

    private int index;


    public int getIndex() {
        return index;
    }

    ServiceActionType(int i) {
        this.index = i;
    }

    public static ServiceActionType getActionType(@IntRange(from = 0L, to = 5L) int index) {
        if (index == 0) {
            return START;
        } else if (index == 1) {
            return STOP;
        } else if (index == 2) {
            return STOP_ALL;
        } else if (index == 3) {
            return RESUME;
        } else if (index == 4) {
            return DELETE;
        } else {
            return DELETE_ALL;
        }
    }
}
