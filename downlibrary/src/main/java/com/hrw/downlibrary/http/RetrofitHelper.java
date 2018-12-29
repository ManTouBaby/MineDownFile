package com.hrw.downlibrary.http;

import android.content.Context;
import android.os.Environment;

import com.hrw.downlibrary.dao.FileDownDao;
import com.hrw.downlibrary.db.DownDataBase;
import com.hrw.downlibrary.entity.DownBean;
import com.hrw.downlibrary.entity.DownEnumType;
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
    public void resume(String url, final DownCallBack downCallBack) {
        DownBean downBean = downBeanMap.get(url);
        if (downBean != null) {
            start(DownEnumType.getDownEnumType(downBean.getFileType()), downBean.getDownUrl(), downBean.getSavePath(), downBean.getSaveName(), downCallBack);
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
    public void start(final DownEnumType downEnumType, final String url, final String savePath, final String saveName, final DownCallBack downCallBack) {
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
                    downBean.setFileType(downEnumType.getTypeValue());
                    fileDownDao.insertDownBean(downBean);
                }
                downBeanMap.put(url, downBean);
                emitter.onNext(downBean);
            }
        }).flatMap(new Function<DownBean, Observable<ResponseBody>>() {
            @Override
            public Observable<ResponseBody> apply(DownBean downBean) throws Exception {
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
                downCallBack.onError(url, e.toString());
                disposable.dispose();

            }

            @Override
            public void onComplete() {
                downCallBack.onComplete(url);
                System.out.println("下载完成-----------------------");
                disposable.dispose();
            }
        });


    }

    public void delete(final String url) {
        Observable.create(new ObservableOnSubscribe<DownBean>() {
            @Override
            public void subscribe(ObservableEmitter<DownBean> emitter) throws Exception {
                DownBean downBean = fileDownDao.getDownBean(url);
                if (downBean != null) {
                    emitter.onNext(downBean);
                }
            }
        }).map(new Function<DownBean, Boolean>() {
            @Override
            public Boolean apply(DownBean downBean) throws Exception {
                File file = new File(downBean.getSavePath() + downBean.getSaveName());
                return file.delete();
            }
        }).subscribe(new Observer<Boolean>() {
            Disposable disposable;

            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(Boolean aBoolean) {

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

    public void deleteAll() {
        Observable.create(new ObservableOnSubscribe<List<DownBean>>() {
            @Override
            public void subscribe(ObservableEmitter<List<DownBean>> emitter) throws Exception {
                List<DownBean> downBeans = fileDownDao.getAllDownBean();
                if (downBeans != null) {
                    emitter.onNext(downBeans);
                }
                emitter.onComplete();
            }
        }).subscribe(new Observer<List<DownBean>>() {
            Disposable disposable;

            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(List<DownBean> downBeans) {
                for (DownBean bean : downBeans) {
                    File file = new File(bean.getSavePath() + bean.getSaveName());
                    file.delete();
                }
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
                .build();
    }


}
