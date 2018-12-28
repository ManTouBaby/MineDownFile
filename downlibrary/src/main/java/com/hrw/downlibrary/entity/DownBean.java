package com.hrw.downlibrary.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * @version 1.0.0
 * @author:hrw
 * @date:2018/12/27 10:56
 * @desc:
 */
@Entity()
public class DownBean {
    @NonNull
    @PrimaryKey
    String downUrl;//下载地址
    String savePath;//本地文件存储地址
    String saveName;//文件名称
    int fileType;//文件类型
    long fileSize;//文件大小--长整形
    String fileSizeStr;//文件大小--字符大小
    long currentFileSize;//当前文件大小--长整形
    String currentFileSizeStr;//当前文件大小--字符串

    public String getDownUrl() {
        return downUrl;
    }

    public void setDownUrl(String downUrl) {
        this.downUrl = downUrl;
    }


    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public String getSaveName() {
        return saveName;
    }

    public void setSaveName(String saveName) {
        this.saveName = saveName;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(@DownType int fileType) {
        this.fileType = fileType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileSizeStr() {
        return fileSizeStr;
    }

    public void setFileSizeStr(String fileSizeStr) {
        this.fileSizeStr = fileSizeStr;
    }

    public long getCurrentFileSize() {
        return currentFileSize;
    }

    public void setCurrentFileSize(long currentFileSize) {
        this.currentFileSize = currentFileSize;
    }

    public String getCurrentFileSizeStr() {
        return currentFileSizeStr;
    }

    public void setCurrentFileSizeStr(String currentFileSizeStr) {
        this.currentFileSizeStr = currentFileSizeStr;
    }
}
