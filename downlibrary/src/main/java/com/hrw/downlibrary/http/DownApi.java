package com.hrw.downlibrary.http;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * @version 1.0.0
 * @author:hrw
 * @date:2018/12/27 10:51
 * @desc:
 */
public interface DownApi {
    String BASE_URL = "http://dldir1.qq.com";
    @GET
    @Streaming
    Observable<ResponseBody> down(@Header("range") String range, @Url String url);
}
