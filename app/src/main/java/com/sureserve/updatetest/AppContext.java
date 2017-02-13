package com.sureserve.updatetest;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.sureserve.update.UpdateUtil;


public class AppContext extends Application {


    @Override
    public void onCreate() {
        super.onCreate();


        UpdateUtil.init(this);


//        PackageManager packageManager = getPackageManager();
//        // getPackageName()是你当前类的包名，0代表是获取版本信息
//        PackageInfo packInfo;
//        String pageName;
//        String version;
//        try {
//            packInfo = packageManager.getPackageInfo(getPackageName(), 0);
//            pageName = packInfo.packageName;
//            version = packInfo.versionName;
//        } catch (PackageManager.NameNotFoundException e) {
//            // TODO Auto-generated catch block
//            pageName = "com.sureserve.glasses";
//            version = "1.0.0.0";
//            e.printStackTrace();
//        }

        UpdateUtil.getInstance()
                .setAppName("com.sureserve.glasses")
                .setVersion("1.0.0.0");
    }
}
