package com.hrw.minedownfile;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.hrw.downlibrary.entity.DownBean;
import com.hrw.downlibrary.entity.DownEnumType;
import com.hrw.downlibrary.http.RetrofitHelper;
import com.hrw.downlibrary.listener.DownCallBack;

public class MainActivity extends AppCompatActivity {
    String url2 = "http://cdn12.down.apk.gfan.net.cn/Pfiles/2018/11/20/84260_a92c2511-ae46-4d1f-af95-dd8c8bf5127b.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void downClick(View view) {
        switch (view.getId()) {
            case R.id.btn01:
                RetrofitHelper.instance(this).start(DownEnumType.APP, url2, new DownCallBack() {
                    @Override
                    public void onDown(String url, DownBean downBean) {
                        
                    }

                    @Override
                    public void onComplete(String url) {

                    }

                    @Override
                    public void onError(String url, String errorMSG) {

                    }
                });
                break;
            case R.id.btn02:
                RetrofitHelper.instance(this).stop(url2);
                break;
            case R.id.btn03:
//                RetrofitHelper.instance(this).resume(url2);
                break;
        }
    }
}
