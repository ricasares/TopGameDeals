package com.ricardocasarez.topgamedeals.utils;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.net.URL;

import com.squareup.okhttp.Callback;

/**
 * Wrapper class for OkHttpClient
 */
public class HttpRequest {

    public static final int STATUS_FAILED = -1;
    public static final int STATUS_SUCCESS = 0;

    private static OkHttpClient sOkHttpClient;

    /**
     * Performs a synchronous HTTP request.
     * @param   url         URL to do the request.
     * @return  Response    okhttp.Response.
     * @throws IOException
     */
    public static Response doHTTPRequest(URL url) throws IOException {
        if (sOkHttpClient == null)
            sOkHttpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = sOkHttpClient.newCall(request).execute();
        return response;
    }

    /**
     * Performs a asynchronous HTTP request.
     * @param url       URL to do the request.
     * @param callback  okhttp.Callback with the Response object if success.
     */
    public static void doAsyncHTTPRequest(URL url, Callback callback) {
        if (sOkHttpClient == null)
            sOkHttpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        sOkHttpClient.newCall(request).enqueue(callback);
    }
}
