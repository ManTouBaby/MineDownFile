package com.hrw.downlibrary.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.hrw.downlibrary.dao.FileDownDao;
import com.hrw.downlibrary.entity.DownBean;

/**
 * @version 1.0.0
 * @author:hrw
 * @date:2018/12/28 10:24
 * @desc:
 */
@Database(entities = {DownBean.class}, version = 1, exportSchema = false)
public abstract class DownDataBase extends RoomDatabase {
    public abstract FileDownDao getFileDownDao();

    private static DownDataBase mDateBase;

    public static DownDataBase instance(Context context) {
        if (mDateBase == null) {
            synchronized (DownDataBase.class) {
                if (mDateBase == null) {
                    mDateBase = Room.databaseBuilder(context, DownDataBase.class, "DownFileDB").build();
                }
            }
        }
        return mDateBase;
    }
}
