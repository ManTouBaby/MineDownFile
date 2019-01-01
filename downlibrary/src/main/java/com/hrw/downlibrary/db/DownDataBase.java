package com.hrw.downlibrary.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

import com.hrw.downlibrary.dao.FileDownDao;
import com.hrw.downlibrary.entity.DownBean;

/**
 * @version 1.0.0
 * @author:hrw
 * @date:2018/12/28 10:24
 * @desc:
 */
@Database(entities = {DownBean.class}, version = 2, exportSchema = false)
public abstract class DownDataBase extends RoomDatabase {
    public abstract FileDownDao getFileDownDao();

    private static DownDataBase mDateBase;

    public static DownDataBase instance(Context context) {
        if (mDateBase == null) {
            synchronized (DownDataBase.class) {
                if (mDateBase == null) {
                    mDateBase = Room.databaseBuilder(context, DownDataBase.class, "DownFileDB")
                            .allowMainThreadQueries()
                            .addMigrations(migration1_2)
                            .build();
                }
            }
        }
        return mDateBase;
    }


    private static final Migration migration1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("alter table DownBean add downStatus integer default 0");
        }
    };
}
