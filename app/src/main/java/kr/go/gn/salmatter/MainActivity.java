package kr.go.gn.salmatter;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.unity3d.player.UnityPlayerActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import kr.go.gn.salmatter.utils.DBRequest;
import kr.go.gn.salmatter.utils.PreferenceSetting;

public class MainActivity extends AppCompatActivity {

    private JSONObject USER_DATA;

    private ProgressBar loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingBar = findViewById(R.id.progressBar);

        if (!checkNetwork()) return;

        getUserData(PreferenceSetting.LoadPreference(getBaseContext(), PreferenceSetting.PREFERENCE_KEY.USER_ID));
    }

    private Boolean checkNetwork() {
        boolean enable = false;

        ConnectivityManager connectivityManager = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        Network network = connectivityManager.getActiveNetwork();
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);

        if(networkCapabilities == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("인터넷 접속 확인").setMessage("서비스 사용을 위해 모바일 통신 또는 WiFi 연결이 필요합니다");
            builder.setNeutralButton("확인", (dialogInterface, i) -> finish());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return enable;
        }

        if(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            enable = true;
        }

        return enable;
    }

    private void getUserData(String user_id) {
        loadingBar.setVisibility(View.VISIBLE);
        if (user_id.isEmpty()) {
//                Toast.makeText(getBaseContext(), "아이디 없음", Toast.LENGTH_SHORT).show();
            Log.d("<NO ID>", "");
            DBRequest.OnCompleteListener onCompleteListener = result -> {
                Log.d("<<<<<<<<<<< Response Data : ", result);
                if (result.equals("INSERT_FAIL")) {
                    Log.e("<<<<<<<<<<<< Load Data Error", result);
                    return;
                }
                try {
                    USER_DATA = new JSONArray(result).getJSONObject(0);
                    PreferenceSetting.SavePreference(getBaseContext(), PreferenceSetting.PREFERENCE_KEY.USER_ID, USER_DATA.getString("user_id"));
                    loadingBar.setVisibility(View.INVISIBLE);
                    Intent i = new Intent(MainActivity.this, UnityActivity.class);
                    i.putExtra("unity", user_id);
                    startActivity(i);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            };
            new DBRequest(getBaseContext(), new Handler(Looper.getMainLooper())).executeAsync(DBRequest.REQUEST_TYPE.JOIN, "", onCompleteListener);
        } else {
            DBRequest.OnCompleteListener onCompleteListener = result -> {
                Log.d("<<<<<<<<<<< Response Data : ", result);
                try {
                    USER_DATA = new JSONArray(result).getJSONObject(0);
                    loadingBar.setVisibility(View.INVISIBLE);
                    startActivity(new Intent(MainActivity.this, UnityActivity.class));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            };
            new DBRequest(getBaseContext(), new Handler(Looper.getMainLooper())).executeAsync(DBRequest.REQUEST_TYPE.GET_DATA, user_id, onCompleteListener);
        }
    }
}