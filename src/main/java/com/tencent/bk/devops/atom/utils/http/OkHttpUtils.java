package com.tencent.bk.devops.atom.utils.http;

import okhttp3.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * http请求工具类
 */
public class OkHttpUtils {

    private final static String CONTENT_TYPE_JSON = "application/json; charset=utf-8";

    private final static Logger logger = LoggerFactory.getLogger(OkHttpUtils.class);

    private static long finalConnectTimeout = 5L;
    private static long finalWriteTimeout = 60L;
    private static long finalReadTimeout = 60L;

    private static OkHttpClient createClient(long connectTimeout, long writeTimeout, long readTimeout) {
        return createRetryOptionClient(connectTimeout, writeTimeout, readTimeout, true);
    }

    private static OkHttpClient createRetryOptionClient(long connectTimeout, long writeTimeout, long readTimeout, boolean isRetry) {
        OkHttpClient.Builder builder = new okhttp3.OkHttpClient.Builder();
        if (connectTimeout > 0)
            finalConnectTimeout = connectTimeout;
        if (writeTimeout > 0)
            finalWriteTimeout = writeTimeout;
        if (readTimeout > 0)
            finalReadTimeout = readTimeout;
        builder.sslSocketFactory(sslSocketFactory(), trustAllCerts[0]);
        builder.writeTimeout(finalConnectTimeout, TimeUnit.SECONDS);
        builder.writeTimeout(finalWriteTimeout, TimeUnit.SECONDS);
        builder.readTimeout(finalReadTimeout, TimeUnit.SECONDS);
        builder.retryOnConnectionFailure(isRetry);
        return builder.build();
    }

    private static X509TrustManager[] trustAllCerts = new X509TrustManager[1];

    static {
        trustAllCerts[0] = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }

    private static SSLSocketFactory sslSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("fail to create sslSocketFactory");
    }

    private static Request.Builder getBuilder(String url, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        if (null != headers) {
            builder.headers(Headers.of(headers));
        }
        return builder;
    }

    /**
     * http get方式请求，返回json格式响应报文
     *
     * @param url 请求路径
     * @return json格式响应报文
     */
    public static String doGet(String url) {
        return doGet(url, null);
    }

    /**
     * http get方式请求，返回json格式响应报文
     *
     * @param url            请求路径
     * @param connectTimeout 连接超时时间
     * @param writeTimeout   写超时时间
     * @param readTimeout    读超时时间
     * @return json格式响应报文
     */
    public static String doGet(String url, long connectTimeout, long writeTimeout, long readTimeout) {
        return doGet(url, null, connectTimeout, writeTimeout, readTimeout);
    }

    /**
     * http get方式请求，返回json格式响应报文
     *
     * @param url     请求路径
     * @param headers 请求头
     * @return json格式响应报文
     */
    public static String doGet(String url, Map<String, String> headers) {
        return doGet(url, headers, -1, -1, -1);
    }

    /**
     * http get方式请求，返回json格式响应报文
     *
     * @param url            请求路径
     * @param headers        请求头
     * @param connectTimeout 连接超时时间
     * @param writeTimeout   写超时时间
     * @param readTimeout    读超时时间
     * @return json格式响应报文
     */
    public static String doGet(String url, Map<String, String> headers, long connectTimeout, long writeTimeout, long readTimeout) {
        Request.Builder builder = getBuilder(url, headers);
        Request request = builder.get().build();
        return doHttp(request, connectTimeout, writeTimeout, readTimeout);
    }

    /**
     * http post方式请求，返回json格式响应报文
     *
     * @param url       请求路径
     * @param jsonParam json格式参数
     * @return json格式响应报文
     */
    public static String doPost(String url, String jsonParam) {
        return doPost(url, jsonParam, null);
    }

    /**
     * http post方式请求，返回json格式响应报文
     *
     * @param url            请求路径
     * @param jsonParam      json格式参数
     * @param connectTimeout 连接超时时间
     * @param writeTimeout   写超时时间
     * @param readTimeout    读超时时间
     * @return json格式响应报文
     */
    public static String doPost(String url, String jsonParam, long connectTimeout, long writeTimeout, long readTimeout) {
        return doPost(url, jsonParam, null, connectTimeout, writeTimeout, readTimeout);
    }

    /**
     * http post方式请求，返回json格式响应报文
     *
     * @param url       请求路径
     * @param jsonParam json格式参数
     * @param headers   请求头
     * @return json格式响应报文
     */
    public static String doPost(String url, String jsonParam, Map<String, String> headers) {
        return doPost(url, jsonParam, headers, -1, -1, -1);
    }

    /**
     * http post方式请求，返回json格式响应报文
     *
     * @param url            请求路径
     * @param jsonParam      json格式参数
     * @param headers        请求头
     * @param connectTimeout 连接超时时间
     * @param writeTimeout   写超时时间
     * @param readTimeout    读超时时间
     * @return json格式响应报文
     */
    public static String doPost(String url, String jsonParam, Map<String, String> headers, long connectTimeout, long writeTimeout, long readTimeout) {
        Request.Builder builder = getBuilder(url, headers);
        RequestBody body = RequestBody.create(MediaType.parse(CONTENT_TYPE_JSON), jsonParam);
        Request request = builder.post(body).build();
        return doHttp(request, connectTimeout, writeTimeout, readTimeout);
    }

    /**
     * http put方式请求，返回json格式响应报文
     *
     * @param url       请求路径
     * @param jsonParam json格式参数
     * @return json格式响应报文
     */
    public static String doPut(String url, String jsonParam) {
        return doPut(url, jsonParam, null);
    }

    /**
     * http put方式请求，返回json格式响应报文
     *
     * @param url            请求路径
     * @param jsonParam      json格式参数
     * @param connectTimeout 连接超时时间
     * @param writeTimeout   写超时时间
     * @param readTimeout    读超时时间
     * @return json格式响应报文
     */
    public static String doPut(String url, String jsonParam, long connectTimeout, long writeTimeout, long readTimeout) {
        return doPut(url, jsonParam, null, connectTimeout, writeTimeout, readTimeout);
    }

    /**
     * http put方式请求，返回json格式响应报文
     *
     * @param url       请求路径
     * @param jsonParam json格式参数
     * @param headers   请求头
     * @return json格式响应报文
     */
    public static String doPut(String url, String jsonParam, Map<String, String> headers) {
        return doPut(url, jsonParam, headers, -1, -1, -1);
    }

    /**
     * http put方式请求，返回json格式响应报文
     *
     * @param url            请求路径
     * @param jsonParam      json格式参数
     * @param headers        请求头
     * @param connectTimeout 连接超时时间
     * @param writeTimeout   写超时时间
     * @param readTimeout    读超时时间
     * @return json格式响应报文
     */
    public static String doPut(String url, String jsonParam, Map<String, String> headers, long connectTimeout, long writeTimeout, long readTimeout) {
        Request.Builder builder = getBuilder(url, headers);
        RequestBody body = RequestBody.create(MediaType.parse(CONTENT_TYPE_JSON), jsonParam);
        Request request = builder.put(body).build();
        return doHttp(request, connectTimeout, writeTimeout, readTimeout);
    }

    /**
     * http delete方式请求，返回json格式响应报文
     *
     * @param url 请求路径
     * @return json格式响应报文
     */
    public static String doDelete(String url) {
        return doDelete(url, null, null);
    }

    /**
     * http delete方式请求，返回json格式响应报文
     *
     * @param url            请求路径
     * @param connectTimeout 连接超时时间
     * @param writeTimeout   写超时时间
     * @param readTimeout    读超时时间
     * @return json格式响应报文
     */
    public static String doDelete(String url, long connectTimeout, long writeTimeout, long readTimeout) {
        return doDelete(url, null, null, connectTimeout, writeTimeout, readTimeout);
    }

    /**
     * http delete方式请求，返回json格式响应报文
     *
     * @param url     请求路径
     * @param headers 请求头
     * @return json格式响应报文
     */
    public static String doDelete(String url, Map<String, String> headers) {
        return doDelete(url, headers, null, -1, -1, -1);
    }

    public static String doDelete(String url, String body, Map<String, String> headers) {
        return doDelete(url, headers, body, -1, -1, -1);
    }

    /**
     * http delete方式请求，返回json格式响应报文
     *
     * @param url            请求路径
     * @param headers        请求头
     * @param body           请求体
     * @param connectTimeout 连接超时时间
     * @param writeTimeout   写超时时间
     * @param readTimeout    读超时时间
     * @return json格式响应报文
     */
    public static String doDelete(String url, Map<String, String> headers, String body, long connectTimeout, long writeTimeout, long readTimeout) {
        Request.Builder builder = getBuilder(url, headers);
        Request request;
        if (StringUtils.isBlank(body)) request = builder.delete().build();
        else request = builder.delete(RequestBody.create(MediaType.parse(CONTENT_TYPE_JSON), body)).build();
        return doHttp(request, connectTimeout, writeTimeout, readTimeout);
    }

    /**
     * http方式请求，返回响应报文
     *
     * @param request    okhttp请求体
     * @return json格式响应报文
     */
    public static String doHttp(Request request) {
        return doHttp(request, finalConnectTimeout, finalWriteTimeout, finalReadTimeout);
    }

    public static String doHttp(Request request, long connectTimeout, long writeTimeout, long readTimeout) {
        OkHttpClient httpClient = createClient(connectTimeout, writeTimeout, readTimeout);
        Response response = null;
        String responseContent = null;
        try {
            response = httpClient.newCall(request).execute();
            assert response.body() != null;
            responseContent = response.body().string();
        } catch (IOException e) {
            logger.error("http send  throw Exception", e);
        } finally {
            if (response != null) {
                assert response.body() != null;
                response.body().close();
            }
        }
        if (response != null && !response.isSuccessful()) {
            logger.error("Fail to request(" + request + ") with code " + response.code()
                + " , message " + response.message() + " and response" + responseContent);
        }
        return responseContent;
    }

    /**
     * http方式请求，返回response响应对象
     *
     * @param request    okhttp请求体
     * @return json格式响应报文
     */
    public static Response doHttpRaw(Request request) {
        return doHttpRaw(request, finalConnectTimeout, finalWriteTimeout, finalReadTimeout, false);
    }

    public static Response doHttpRaw(Request request, boolean isRetry) {
        return doHttpRaw(request, finalConnectTimeout, finalWriteTimeout, finalReadTimeout, isRetry);
    }

    public static Response doHttpRaw(Request request, long connectTimeout, long writeTimeout, long readTimeout, boolean isRetry) {
        OkHttpClient httpClient = createRetryOptionClient(connectTimeout, writeTimeout, readTimeout, isRetry);
        try {
            return httpClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
