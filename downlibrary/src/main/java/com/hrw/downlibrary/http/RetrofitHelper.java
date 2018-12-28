package com.hrw.downlibrary.http;

import android.content.Context;
import android.os.Environment;

import com.hrw.downlibrary.dao.FileDownDao;
import com.hrw.downlibrary.db.DownDataBase;
import com.hrw.downlibrary.entity.DownBean;
import com.hrw.downlibrary.entity.DownEnumType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @version 1.0.0
 * @author:hrw
 * @date:2018/12/27 10:54
 * @desc:
 */
public class RetrofitHelper {
    private static RetrofitHelper retrofitHelper;
    private static Map<String, Disposable> disposableMap = new HashMap<>();
    private static Map<String, DownBean> downBeanMap = new HashMap<>();
    private FileDownDao fileDownDao;
    private Context mContext;

    private RetrofitHelper(Context context) {
        fileDownDao = DownDataBase.instance(context).getFileDownDao();
        mContext = context;
    }

    public static RetrofitHelper instance(Context context) {
        if (retrofitHelper == null) {
            synchronized (RetrofitHelper.class) {
                if (retrofitHelper == null) {
                    retrofitHelper = new RetrofitHelper(context);
                }
            }
        }
        return retrofitHelper;
    }


    /**
     * 开始下载任务
     *
     * @param url          下载地址
     * @param downEnumType 保存类型
     */
    public void down(DownEnumType downEnumType, final String url) {
        String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + mContext.getPackageName() + downEnumType.getTypePath();
        down(downEnumType, url, savePath);
    }

    /**
     * 开始下载任务
     *
     * @param url          下载地址
     * @param savePath     保存地址
     * @param downEnumType 保存类型
     */
    public void down(DownEnumType downEnumType, final String url, String savePath) {
        String saveName = url.substring(url.lastIndexOf("/") + 1);
        down(downEnumType, url, savePath, saveName);
    }


    /**
     * 暂停请求
     *
     * @param url
     */
    public void stop(String url) {
        Disposable disposable = disposableMap.get(url);
        if (disposable != null) {
            disposable.dispose();
        }
    }

    /**
     * 恢复下载
     *
     * @param url
     */
    public void resume(String url) {
        DownBean downBean = downBeanMap.get(url);
        if (downBean != null) {
            down(DownEnumType.getDownEnumType(downBean.getFileType()), downBean.getDownUrl(), downBean.getSavePath(), downBean.getSaveName());
        }
    }

    /**
     * 开始下载任务
     *
     * @param url          下载地址
     * @param savePath     保存地址
     * @param saveName     保存名称
     * @param downEnumType 保存类型
     */
    public void down(final DownEnumType downEnumType, final String url, final String savePath, final String saveName) {
//        System.out.println("down:" + Thread.currentThread().getName());
        Observable.create(new ObservableOnSubscribe<DownBean>() {
            @Override
            public void subscribe(ObservableEmitter<DownBean> emitter) throws Exception {
//                System.out.println("create:" + Thread.currentThread().getName());
                DownBean downBean = fileDownDao.getDownBean(url);
                if (downBean == null) {
                    downBean = new DownBean();
                    downBean.setDownUrl(url);
                    downBean.setSavePath(savePath);
                    downBean.setSaveName(saveName);
                    downBean.setCurrentFileSize(0);
                    downBean.setFileSize(0);
                    downBean.setFileType(downEnumType.getTypeValue());
                    fileDownDao.insertDownBean(downBean);
                }
                downBeanMap.put(url, downBean);
                emitter.onNext(downBean);
            }
        }).flatMap(new Function<DownBean, Observable<ResponseBody>>() {
            @Override
            public Observable<ResponseBody> apply(DownBean downBean) throws Exception {
//                System.out.println("flatMap:" + Thread.currentThread().getName());
                String rangeStr;
                if (downBean.getCurrentFileSize() == 0) {
                    rangeStr = "bytes=" + downBean.getCurrentFileSize() + "-";
                } else {
                    rangeStr = "bytes=" + downBean.getCurrentFileSize() + "-" + downBean.getFileSize();
                }
//                getService(DownApi.class).down(rangeStr, url);
                return getService(DownApi.class).down(rangeStr, url);
            }
        }).map(new Function<ResponseBody, ResponseBody>() {
            @Override
            public ResponseBody apply(ResponseBody responseBody) throws Exception {
                DownBean bean = fileDownDao.getDownBean(url);
                RandomAccessFile accessFile = null;
                InputStream inputStream = null;
                long currentSize = 0;
                long total = 0;
                int progress;

                File fileDir = new File(bean.getSavePath());
                if (!fileDir.exists()) fileDir.mkdirs();

                try {
                    File file = new File(bean.getSavePath(), bean.getSaveName());
                    inputStream = responseBody.byteStream();
                    accessFile = new RandomAccessFile(file, "rwd");
                    long contentLength = responseBody.contentLength();
                    accessFile.setLength(contentLength);
                    if (bean.getCurrentFileSize() == 0) {//没有开始下载
                        bean.setFileSize(contentLength);
                        accessFile.seek(0);
                        total = contentLength;
                        currentSize = 0;
                    } else {
                        currentSize = bean.getCurrentFileSize();
                        accessFile.seek(currentSize);
                        total = bean.getFileSize();
                    }
                    byte[] buf = new byte[2048];
                    int len;
                    int lastProgress = 0;
                    while ((len = inputStream.read(buf)) != -1) {
                        accessFile.write(buf, 0, len);
                        currentSize += len;
                        progress = (int) (currentSize * 100 / total);
                        if (lastProgress != progress) {
                            System.out.println("当前进度:" + progress);
                            bean.setCurrentFileSize(currentSize);
                            fileDownDao.updateDownBean(bean);
                        }
                        lastProgress = progress;
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (accessFile != null) accessFile.close();
                        if (inputStream != null) inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return responseBody;
            }
        }).subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribe(new Observer<ResponseBody>() {
            Disposable disposable;

            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
                disposableMap.put(url, d);
            }

            @Override
            public void onNext(ResponseBody responseBody) {

            }

            @Override
            public void onError(Throwable e) {
                disposable.dispose();
            }

            @Override
            public void onComplete() {
                disposable.dispose();
            }
        });


    }

    private <T> T getService(Class<T> aClass) {
        return getRetrofit().create(aClass);
    }

    private Retrofit getRetrofit() {
        return new Retrofit.Builder()
                .client(getOkHttpClient())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(DownApi.BASE_URL)
                .build();
    }

    private OkHttpClient getOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
//                .addInterceptor(interceptor)
//                .readTimeout(8, TimeUnit.SECONDS)
//                .readTimeout(8, TimeUnit.SECONDS)
                .build();
    }

    Interceptor interceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Response response = chain.proceed(chain.request());
            return response.newBuilder().body(new DownResponseBody(response.body())).build();
        }
    };

}
