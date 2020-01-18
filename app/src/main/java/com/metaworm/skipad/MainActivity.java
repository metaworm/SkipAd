package com.metaworm.skipad;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText editLog;
    private EditText editText;
    private CheckBox checkFilter;

    public static boolean isServiceRunning(Context mContext, String className) {
        ActivityManager activityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                .getRunningServices(30);

        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!isServiceRunning(this, "com.metaworm.skipad.SkipAdService"))
            goAccess(null);

        AssetManager mgr = getAssets();
        editLog = findViewById(R.id.editLog);
        checkFilter = findViewById(R.id.checkFilter);
        editText = findViewById(R.id.editText);

        SkipAdService.editLog = editLog;

        try {
            BufferedReader b = new BufferedReader(new InputStreamReader(mgr.open("default.json")));
            SkipAdService.setConfig(b.readLine());
        } catch (Exception e) {
            Log.d("EXCEPTION", e.toString());
        }
    }

    public void toggleFilter(View view) {
        if (checkFilter.isChecked()) {
            SkipAdService.filter = editText.getText().toString();
            Toast.makeText(this, SkipAdService.filter, Toast.LENGTH_SHORT).show();
        } else {
            SkipAdService.filter = null;
        }
    }

    public void goAccess(View view) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void doConfig(View v) {
//        SkipAdService.setConfig(getString(R.string.default_config));
        SkipAdService.setConfig("[{},{}]");
    }
}
