package kr.go.gn.salmatter.utils;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class DBRequest {

    public static String SERVER_URL = "http://118.67.150.80/salmatter/res/php/request_handler.php";

    public static enum REQUEST_TYPE {
        JOIN,
        GET_DATA
    }

    private Context context;

    private final ExecutorService service = Executors.newSingleThreadExecutor();

    private final Handler resultHandler;

    public DBRequest(Context context, Handler resultHandler) {
        this.context = context;
        this.resultHandler = resultHandler;
    }

    public interface OnCompleteListener {
        void onComplete(String result);
    }

    public void executeAsync(REQUEST_TYPE type, String userID, OnCompleteListener onCompleteListener) {
        service.execute(()-> {
            try {
                final String result = new RequestData(type, userID).call();
                resultHandler.post(() -> onCompleteListener.onComplete(result));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    static class RequestData implements Callable<String> {

        REQUEST_TYPE type;
        String param;
        String URL;
        String key = "USE=";
        String id = "USER_ID=";
        final String TAG = "DBRequest";

        public RequestData(REQUEST_TYPE type, String param) {
            this.type = type;
            this.param = param;
        }
        @Override
        public String call() throws Exception {
            String response;
            switch (type) {
                case JOIN -> URL = SERVER_URL + "?" + key + REQUEST_TYPE.JOIN.name();
                case GET_DATA ->
                        URL = SERVER_URL + "?" + key + REQUEST_TYPE.GET_DATA.name() + "&" + id + param;
                default -> {
                    Log.e("<<<<<<<<<<<< URL Error : ", param);
                    return "";
                }
            }
            Log.i("<<<<<<<<<<< URL : ", URL);

            HttpURLConnection httpURLConnection = (HttpURLConnection)(new URL(URL)).openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            if (httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Http Connection Fail");
                Log.d(TAG, httpURLConnection.getResponseMessage());
                Log.d(TAG, httpURLConnection.getResponseCode()+"");
                return null;
            }
            response = (new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), StandardCharsets.UTF_8))).readLine();
            Log.d(TAG, "Response Value :: "+response);

            return response;

//            HttpsURLConnection httpsURLConnection = (HttpsURLConnection)(new URL(URL)).openConnection();
//            httpsURLConnection.setDoInput(true);
//            httpsURLConnection.setDoOutput(true);
//            httpsURLConnection.setUseCaches(false);
//            httpsURLConnection.setReadTimeout(10000);
//            httpsURLConnection.setConnectTimeout(15000);
//            httpsURLConnection.setRequestMethod("POST");
//            httpsURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//
//            if (httpsURLConnection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
//                Log.e(TAG, "Http Connection Fail");
//                Log.d(TAG, httpsURLConnection.getResponseMessage());
//                Log.d(TAG, httpsURLConnection.getResponseCode()+"");
//                return null;
//            }
//            response = (new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream(), StandardCharsets.UTF_8))).readLine();
//            Log.d(TAG, "Response Value :: "+response);
//
//            return response;
        }
    }
}
