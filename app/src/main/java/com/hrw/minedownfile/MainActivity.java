package com.hrw.minedownfile;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.hrw.downlibrary.DownFileHelper;
import com.hrw.downlibrary.entity.DownEnumType;

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
                DownFileHelper.instance(this).start(DownEnumType.APP,url2);
                break;
            case R.id.btn02:
                DownFileHelper.instance(this).stop(url2);
                break;
            case R.id.btn03:
                DownFileHelper.instance(this).resume(url2);
                break;
        }
    }
}
