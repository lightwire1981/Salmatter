package kr.go.gn.salmatter.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class PreferenceSetting {

    private static final String TAG = "PreferenceSetting";

    public enum PREFERENCE_KEY {
        USER_ID
    }

    public static String LoadPreference(Context context, PREFERENCE_KEY category) {
        String returnValue;
        SharedPreferences preferences = context.getSharedPreferences("prefInfo", Activity.MODE_PRIVATE);

        if (category == PREFERENCE_KEY.USER_ID) {
            returnValue = preferences.getString(PREFERENCE_KEY.USER_ID.name(), "");
        } else {
            returnValue = "";
        }
        return returnValue;
    }

    public static void SavePreference(Context context, PREFERENCE_KEY category, String value) {
        SharedPreferences preferences = context.getSharedPreferences("prefInfo", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(category.name(), value);
        editor.apply();
        Log.i(TAG, "정보 반영 됨");
    }
}
