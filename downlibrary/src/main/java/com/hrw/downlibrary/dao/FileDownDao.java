package com.hrw.downlibrary.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.hrw.downlibrary.entity.DownBean;

/**
 * @version 1.0.0
 * @author:hrw
 * @date:2018/12/28 10:05
 * @desc:
 */

@Dao
public interface FileDownDao {

    @Query("select * from DownBean where downUrl= :downUrl")
    DownBean getDownBean(String downUrl);//按条件获取对象

    @Insert
    void insertDownBean(DownBean downBean);

    @Query("delete from DownBean where downUrl= :downUrl")
    void deleteDownBean(String downUrl);//按条件删除对象

    @Update
    void updateDownBean(DownBean downBean);
}
