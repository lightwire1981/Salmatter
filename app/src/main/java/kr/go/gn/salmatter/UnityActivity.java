package kr.go.gn.salmatter;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;

import androidx.annotation.Nullable;

import com.unity3d.player.IUnityPlayerLifecycleEvents;
import com.unity3d.player.UnityPlayer;

import org.json.JSONException;
import org.json.JSONObject;

import kr.go.gn.salmatter.utils.DBRequest;
import kr.go.gn.salmatter.utils.PreferenceSetting;

public class UnityActivity extends Activity implements IUnityPlayerLifecycleEvents {

    private static enum MSG_TYPE {
        GameStamp,
        DoneStamp
    }

    protected UnityPlayer mUnityPlayer;
    private static String USER_ID;
    private static String STAMP_DATA;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        USER_ID = PreferenceSetting.LoadPreference(getBaseContext(), PreferenceSetting.PREFERENCE_KEY.USER_ID);
        STAMP_DATA = PreferenceSetting.LoadPreference(getBaseContext(), PreferenceSetting.PREFERENCE_KEY.STAMP_DATA);

        mUnityPlayer = new UnityPlayer(this, this);
        setContentView(mUnityPlayer);
        mUnityPlayer.requestFocus();

        sendStr(MSG_TYPE.GameStamp.name(), STAMP_DATA);
    }

    public void sendStr(String type, String code) {

        long delay = 9000;
        if (type.equals(MSG_TYPE.DoneStamp.name())) delay = 500;
        final Handler handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
//                String testCode = "1#0#0#0#0#0#0#0";
                UnityPlayer.UnitySendMessage("SuperManager", type, code);
            }
        };
        // 유니티 로딩 타임을 위해 초기 9초 대기 이후 0.5초
        handler.sendEmptyMessageDelayed(0,delay);
    }

    public void receiveStr(String str) {
        Log.i("Str From Unity >>>>>>>>>>>>", str);

        try {
            JSONObject rawData = new JSONObject(str);
            if ("SetStamp".equals(rawData.getString("type"))) {// {"type":"SetStamp","stamp_num":"1"}
                String stamp = rawData.getString("stamp_num");
                DBRequest.OnCompleteListener onCompleteListener = result -> {
                    if (result.equals("STAMP_COMPLETE")) {
                        sendStr(MSG_TYPE.DoneStamp.name(), "");
                    }
                };
                new DBRequest(getBaseContext(), new Handler(Looper.getMainLooper())).executeAsync(DBRequest.REQUEST_TYPE.UPDATE, onCompleteListener, USER_ID, stamp);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        System.exit(0);
    }

    @Override
    public void onUnityPlayerUnloaded() {

    }

    @Override
    public void onUnityPlayerQuitted() {

    }

    @Override protected void onNewIntent(Intent intent)
    {
        // To support deep linking, we need to make sure that the client can get access to
        // the last sent intent. The clients access this through a JNI api that allows them
        // to get the intent set on launch. To update that after launch we have to manually
        // replace the intent with the one caught here.
        setIntent(intent);
        mUnityPlayer.newIntent(intent);
    }

    // Quit Unity
    @Override protected void onDestroy ()
    {
        mUnityPlayer.destroy();
        super.onDestroy();
    }

    // Pause Unity
    @Override protected void onPause()
    {
        super.onPause();
        mUnityPlayer.pause();
    }

    // Resume Unity
    @Override protected void onResume()
    {
        super.onResume();
        mUnityPlayer.resume();
    }

    // Low Memory Unity
    @Override public void onLowMemory()
    {
        super.onLowMemory();
        mUnityPlayer.lowMemory();
    }

    // Trim Memory Unity
    @Override public void onTrimMemory(int level)
    {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_RUNNING_CRITICAL)
        {
            mUnityPlayer.lowMemory();
        }
    }

    // This ensures the layout will be correct.
    @Override public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mUnityPlayer.configurationChanged(newConfig);
    }

    // Notify Unity of the focus change.
    @Override public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        mUnityPlayer.windowFocusChanged(hasFocus);
    }

    // For some reason the multiple keyevent type is not supported by the ndk.
    // Force event injection by overriding dispatchKeyEvent().
    @Override public boolean dispatchKeyEvent(KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_MULTIPLE)
            return mUnityPlayer.injectEvent(event);
        return super.dispatchKeyEvent(event);
    }

    // Pass any events not handled by (unfocused) views straight to UnityPlayer
    @Override public boolean onKeyUp(int keyCode, KeyEvent event)     { return mUnityPlayer.injectEvent(event); }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event)   { return mUnityPlayer.injectEvent(event); }
    @Override public boolean onTouchEvent(MotionEvent event)          { return mUnityPlayer.injectEvent(event); }
    /*API12*/ public boolean onGenericMotionEvent(MotionEvent event)  { return mUnityPlayer.injectEvent(event); }
}
