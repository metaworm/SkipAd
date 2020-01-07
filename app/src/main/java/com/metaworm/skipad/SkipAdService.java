package com.metaworm.skipad;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SkipAdService extends AccessibilityService {
    public static SkipAdService instance = null;
    public static boolean log = true;
    private static JSONArray config;

    public JSONArray getConfig() { return config; }
    public static void setConfig(String json) {
        try {
            JSONArray c = new JSONArray(json);
            for (int i = 0; i < c.length(); i++) {
                if (c.getJSONObject(i) == null)
                    throw new Exception("必须为Object");
            }
            config = c;
        } catch (Exception e) {
            Toast.makeText(instance, e.toString(), Toast.LENGTH_SHORT).show();
            Log.d("EXCEPTION", e.toString());
        }
    }

    public AccessibilityNodeInfo findText(AccessibilityNodeInfo node, String text) {
        if (node == null) return null;
        CharSequence t = node.getText();
        if (t != null && t.toString().contains(text)) return node;
        if (t != null) Log.d("EVENT", "GetText: " + t);

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo n = findText(node.getChild(i), text);
            if (n != null) return n;
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent e) {
        int t = e.getEventType();
        AccessibilityNodeInfo source = e.getSource();
        if (source == null) return;
        // 获取顶级窗口
        AccessibilityNodeInfo top = source;
        while (top.getParent() != null) { top = top.getParent(); }

        switch (t) {
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
                Log.d("EVENT", "WINDOWS_CHANGED: " + e.getWindowChanges());
                if (log)
                    Log.d("EVENT", String.format("[%d] %s:%s", t, top.getPackageName(), source.getClassName()));
//                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                Log.d("EVENT", "WINDOW_STATE_CHANGED: " + e.getContentChangeTypes());
//                break;
            default:
                // 遍历配置
                try {
                    for (int i = 0; i < config.length(); i++) {
                        JSONObject c = config.getJSONObject(i);
                        String activity = top.getClassName().toString();
                        String package_ = top.getPackageName().toString();
                        String id = c.has("viewId") ? c.getString("viewId") : null;
                        String text = c.has("text") ? c.getString("text") : null;

                        boolean b = true;
                        if (c.has("activity")) b = b && activity.equals(c.getString("activity"));
                        if (c.has("package")) b = b && package_.equals(c.getString("package"));
                        if (!b) continue;

                        List<AccessibilityNodeInfo> nodes = id != null ? top.findAccessibilityNodeInfosByViewId(id)
                                                          : text != null ? top.findAccessibilityNodeInfosByText(text)
                                                          : new ArrayList<AccessibilityNodeInfo>();
                        for (AccessibilityNodeInfo n : nodes) {
                            if (n.isClickable()) {
                                String msg = "SkipAd: " + id + ":" + text + "@" + activity + " " + n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                                break;
                            }
                        }
                    }
                } catch (Exception ex) {
                    Toast.makeText(this, ex.toString(), Toast.LENGTH_LONG).show();
                }
        }
    }

    @Override
    public void onServiceConnected() {
        instance = this;

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.packageNames = null;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        info.notificationTimeout = 100;
        this.setServiceInfo(info);

        // 转为前台服务，防止被杀死
//        startForeground(1, buildForegroundNotification());
//        Toast.makeText(this, "onServiceConnected", Toast.LENGTH_SHORT).show();
    }

    private Notification buildForegroundNotification() {
        Notification.Builder builder = new Notification.Builder(this);

        builder.setOngoing(true);

        builder.setContentTitle("aaa")
                .setContentText("bbb")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("ccc");
        builder.setPriority(Notification.PRIORITY_MAX);
        return builder.build();
    }

    @Override
    public void onInterrupt() {
    }
}
