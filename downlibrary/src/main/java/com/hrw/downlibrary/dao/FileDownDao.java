package com.hrw.downlibrary.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.hrw.downlibrary.entity.DownBean;

import java.util.List;

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

    @Query("select * from DownBean")
    List<DownBean> getAllDownBean();//获取所有对象

    @Insert
    void insertDownBean(DownBean downBean);

    @Query("delete from DownBean where downUrl= :downUrl")
    void deleteDownBean(String downUrl);//按条件删除对象

    @Query("delete from DownBean")
    void deleteAllDownBean();

    @Update
    void updateDownBean(DownBean downBean);
}
