package kr.go.gn.salmatter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import kr.go.gn.salmatter.utils.PreferenceSetting;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (PreferenceSetting.LoadPreference(getBaseContext(), PreferenceSetting.PREFERENCE_KEY.USER_ID).isEmpty()) {
            Toast.makeText(getBaseContext(), "아이디 없음", Toast.LENGTH_SHORT).show();
        }
    }
}