package com.hrw.downlibrary.http;

import java.io.IOException;

import io.reactivex.annotations.Nullable;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.Okio;
import okio.Source;
import okio.Timeout;

/**
 * @version 1.0.0
 * @author:hrw
 * @date:2018/12/28 16:18
 * @desc:
 */
public class DownResponseBody extends ResponseBody {
    ResponseBody mResponseBody;

    public DownResponseBody(ResponseBody mResponseBody) {
        this.mResponseBody = mResponseBody;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return mResponseBody.contentType();
    }

    @Override
    public long contentLength() {
        return mResponseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        return Okio.buffer(dealSource(mResponseBody.source()));
    }

    private Source dealSource(final Source source) {

        return new Source() {
            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                System.out.println("下载数据:" + source.toString());
                return source.read(sink, byteCount);
            }

            @Override
            public Timeout timeout() {
                return source.timeout();
            }

            @Override
            public void close() throws IOException {

            }
        };
    }
}
