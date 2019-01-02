package com.hrw.downlibrary.http;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.hrw.downlibrary.dao.FileDownDao;
import com.hrw.downlibrary.db.DownDataBase;
import com.hrw.downlibrary.entity.DownBean;
import com.hrw.downlibrary.entity.DownEnumType;
import com.hrw.downlibrary.entity.DownStatus;
import com.hrw.downlibrary.listener.DownCallBack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
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
    private final static String TAG = "RetrofitHelper";
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
    public void start(DownEnumType downEnumType, final String url, DownCallBack downCallBack) {
        String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + mContext.getPackageName() + downEnumType.getTypePath();
        start(downEnumType, url, savePath, downCallBack);
    }

    /**
     * 开始下载任务
     *
     * @param url          下载地址
     * @param savePath     保存地址
     * @param downEnumType 保存类型
     */
    public void start(DownEnumType downEnumType, final String url, String savePath, DownCallBack downCallBack) {
        String saveName = url.substring(url.lastIndexOf("/") + 1);
        start(downEnumType, url, savePath, saveName, downCallBack);
    }


    /**
     * 开始下载任务
     *
     * @param url          下载地址
     * @param savePath     保存地址
     * @param saveName     保存名称
     * @param downEnumType 保存类型
     */
    public void start(final DownEnumType downEnumType, final String url, final String savePath, final String saveName, final DownCallBack downCallBack) {
        System.out.println("start线程：" + Thread.currentThread().getName());
        Observable.create(new ObservableOnSubscribe<DownBean>() {
            @Override
            public void subscribe(ObservableEmitter<DownBean> emitter) throws Exception {
                DownBean downBean = fileDownDao.getDownBean(url);
                if (downBean == null) {
                    downBean = new DownBean();
                    downBean.setDownUrl(url);
                    downBean.setSavePath(savePath);
                    downBean.setSaveName(saveName);
                    downBean.setCurrentFileSize(0);
                    downBean.setFileSize(0);
                    downBean.setDownStatus(DownStatus.ON_DOWN);
                    downBean.setFileType(downEnumType.getTypeValue());
                    downBeanMap.put(url, downBean);
                    fileDownDao.insertDownBean(downBean);
                    emitter.onNext(downBean);
                } else {
                    File file = new File(downBean.getSavePath(), downBean.getSaveName());
                    long currentSize = downBean.getCurrentFileSize();
                    long fileAllSize = downBean.getFileSize();
                    if (currentSize == fileAllSize && fileAllSize > 0 && file.exists()) {
                        System.out.println("数据已下载完毕");
                        emitter.onComplete();
                    } else if (currentSize > fileAllSize || currentSize == fileAllSize && !file.exists()) {
                        downBean.setFileSize(0);
                        downBean.setCurrentFileSize(0);
                        downBean.setDownStatus(DownStatus.ON_DOWN);
                        fileDownDao.updateDownBean(downBean);
                        downBeanMap.put(url, downBean);
                        emitter.onNext(downBean);
                    } else {
                        downBean.setDownStatus(DownStatus.ON_DOWN);
                        downBeanMap.put(url, downBean);
                        emitter.onNext(downBean);
                    }
                }


            }
        }).subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).flatMap(new Function<DownBean, Observable<ResponseBody>>() {
            @Override
            public Observable<ResponseBody> apply(DownBean downBean) throws Exception {
                System.out.println("flatMap线程：" + Thread.currentThread().getName());
                String rangeStr;
                if (downBean.getCurrentFileSize() == 0) {
                    rangeStr = "bytes=" + downBean.getCurrentFileSize() + "-";
                } else {
                    rangeStr = "bytes=" + downBean.getCurrentFileSize() + "-" + downBean.getFileSize();
                }
                return getService(DownApi.class).down(rangeStr, url);
            }
        }).map(new Function<ResponseBody, ResponseBody>() {
            @Override
            public ResponseBody apply(ResponseBody responseBody) throws Exception {
                System.out.println("map线程：" + Thread.currentThread().getName());
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
                    int len = 0;
                    int lastProgress = 0;
                    while (downBeanMap.get(url).getDownStatus() == DownStatus.ON_DOWN && len != -1) {
                        System.out.println("当前状态:"+downBeanMap.get(url).getDownStatus());
                        len = inputStream.read(buf);
                        accessFile.write(buf, 0, len);
                        currentSize += len;
                        progress = (int) (currentSize * 100 / total);
                        if (lastProgress != progress) {
                            System.out.println("当前进度:" + progress);
                            bean.setCurrentFileSize(currentSize);
                            if (progress == 100) {
                                bean.setDownStatus(DownStatus.ON_COMPLETE);
                            } else {
                                bean.setDownStatus(DownStatus.ON_DOWN);
                            }
                            fileDownDao.updateDownBean(bean);
                            downBeanMap.put(url, bean);
                            downCallBack.onDown(url, bean);
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
        }).subscribe(new Observer<ResponseBody>() {
            Disposable disposable;

            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
                disposableMap.put(url, d);
            }

            @Override
            public void onNext(ResponseBody responseBody) {
                System.out.println("onNext线程：" + Thread.currentThread().getName());
                DownBean bean = downBeanMap.get(url);
                if (bean.getDownStatus() == DownStatus.ON_COMPLETE) {
                    onComplete();
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "Down Fail--" + e.toString());
                downCallBack.onError(url, e.toString());
                disposable.dispose();
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "Down Complete--" + url);
                downCallBack.onComplete(url);
                disposable.dispose();
            }
        });


    }

    /**
     * 暂停请求
     * 030932
     *
     * @param url
     */
    public void stop(final String url) {
        Disposable disposable = disposableMap.get(url);
        if (disposableMap.containsKey(url)) downBeanMap.get(url).setDownStatus(DownStatus.ON_STOP);
        if (disposable != null) {
            disposable.dispose();
            disposableMap.remove(disposable);
        }
        Observable.create(new ObservableOnSubscribe<DownBean>() {
            @Override
            public void subscribe(ObservableEmitter<DownBean> emitter) throws Exception {
                DownBean bean = fileDownDao.getDownBean(url);
                bean.setDownStatus(DownStatus.ON_STOP);
                fileDownDao.updateDownBean(bean);
                emitter.onComplete();
            }
        }).subscribe(new Consumer<DownBean>() {
            @Override
            public void accept(DownBean downBean) throws Exception {

            }
        });
    }

    /**
     * 恢复下载
     *
     * @param url
     */
    public void resume(final String url, final DownCallBack downCallBack) {
        DownBean downBean = downBeanMap.get(url);
        if (downBean != null) {
            start(DownEnumType.getDownEnumType(downBean.getFileType()), downBean.getDownUrl(), downBean.getSavePath(), downBean.getSaveName(), downCallBack);
        }
        Observable.create(new ObservableOnSubscribe<DownBean>() {
            @Override
            public void subscribe(ObservableEmitter<DownBean> emitter) throws Exception {
                DownBean downBean = fileDownDao.getDownBean(url);
                downBean.setDownStatus(DownStatus.ON_DOWN);
                fileDownDao.updateDownBean(downBean);
                emitter.onComplete();
            }
        }).subscribe(new Consumer<DownBean>() {
            @Override
            public void accept(DownBean downBean) throws Exception {

            }
        });
    }

    public void delete(final String url) {
        if (disposableMap.containsKey(url)) disposableMap.remove(url);
        Observable.create(new ObservableOnSubscribe<DownBean>() {
            @Override
            public void subscribe(ObservableEmitter<DownBean> emitter) throws Exception {
                DownBean downBean = fileDownDao.getDownBean(url);
                if (downBean != null) {
                    File file = new File(downBean.getSavePath() + downBean.getSaveName());
                    file.delete();
                    fileDownDao.deleteDownBean(url);
                }
                emitter.onComplete();
            }
        }).subscribe(new Consumer<DownBean>() {
            @Override
            public void accept(DownBean downBean) throws Exception {

            }
        });
    }

    public void deleteAll() {
        Observable.create(new ObservableOnSubscribe<List<DownBean>>() {
            @Override
            public void subscribe(ObservableEmitter<List<DownBean>> emitter) throws Exception {
                List<DownBean> downBeans = fileDownDao.getAllDownBean();
                for (DownBean bean : downBeans) {
                    File file = new File(bean.getSavePath() + bean.getSaveName());
                    file.delete();
                }
                fileDownDao.deleteAllDownBean();
                emitter.onComplete();
            }
        }).subscribe(new Consumer<List<DownBean>>() {
            @Override
            public void accept(List<DownBean> downBeans) throws Exception {

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
                .readTimeout(8, TimeUnit.SECONDS)
                .build();
    }


}
