package io.github.lumyuan.turingbox.common.model;

import android.graphics.drawable.Drawable;

/**
 * 应用信息
 * Created by Hello on 2018/01/26.
 */

public class AppInfo extends io.github.lumyuan.turingbox.common.pojo.AppInfo {
    public Drawable icon = null;
    public CharSequence stateTags = "";
    public CharSequence path = "";
    public CharSequence dir = "";
    public Boolean enabled = false;
    public Boolean suspended = false;
    public Boolean updated = false;
    public String versionName = "";
    public int versionCode = 0;
    public AppType appType = AppType.UNKNOW;
    public SceneConfigInfo sceneConfigInfo;
    public CharSequence desc;
    public int targetSdkVersion;
    public int minSdkVersion;

    public static AppInfo getItem() {
        return new AppInfo();
    }

    public enum AppType {
        UNKNOW,
        USER,
        SYSTEM,
        BACKUPFILE
    }
}
