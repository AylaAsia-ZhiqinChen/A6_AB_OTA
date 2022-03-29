package com.system.ota;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/*
    注意 注意 注意：
    一、pubic void applyPayload(String url,long offset,long size,String[] headerKeyValuePairs)
    url:升级包(Payload)的路径，ab升级只能使用内置存储，必须在目录 /data/ota_package/xxx
    而且需要以file://开头，比如file://data/ota_package/update.zip

    update engine只接受固定的路径/data/ota_package/update.zip，其他路径和sdcard不支持
    offset:这是payload 在update.zip中的偏移量，需要从升级包文件中计算出来
    Size:这是payload文件的大小，可以在payload_properties.txt中找到
    headerKeyValuePairs:这是metadata,可以在升级包中的payload_properties.txt中找到

    二、 如果adb push /data/ota_package/update.zip 目录下可以升级成功，而下载下来不可以，一般应该是seLiunx权限问题 可以ls -lZ查看下
        需要在
        /system/sepolicy/private/system_app.te
        /system/sepolicy/prebuilts/api/28.0/private/system_app.te 加上下面两句

        allow system_app ota_package_file:dir { read open write create remove_name search rename add_name getattr };
        allow system_app ota_package_file:file { read write create open rename setattr getattr unlink };

	==========================================================================================================
    相关代码接口说明如下

    一、framwork层 应用接口
    源代码位置：framwork/base/core/java/android/os/UpdateEngine.java
    framwork/base/core/java/android/os/UpdateEngineCallback.java

    二、app应用调取applyUpdate方法我这里只说一下大致流程
    当然得系统权限的App了，需要系统签名，这些Api也是@SystemApi的

    1、创建 UpdateEngineCallback 的对象 mUpdateEngineCallback
    2、创建 UpdateEngine 的对象 mUpdateEngine, 创建后服务开启
    3、使用mUpdateEngine.bind(mUpdateEngineCallback) 因为bind方法时接受的callback对象，而我们创建的类继承了callback,传入当前类的对象即可
    4、调用 applyPayload(String url,long offset,long size,String[] headerKeyValuePairs) 方法具体执行升级
    5、在重写的onStatusUpdate(int status, float percent)方法中根据拿到的状态执行进度逻辑
    在重写的onPayloadApplicationComplete(int errorCode);方法中执行升级完成后的逻辑
 */

public class MainActivity extends Activity {
    final String TAG = "A6_OTA " + this.getClass().getSimpleName();

    // private Button abOTA;
    private static final int STORAGE_PERMISSIONS_REQUEST_CODE = 0;
    private static final String[] REQUIRED_STORAGE_PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "checkSelfPermission = false");
            ActivityCompat.requestPermissions(this, REQUIRED_STORAGE_PERMISSIONS, STORAGE_PERMISSIONS_REQUEST_CODE);
        } else {
            Log.d(TAG, "checkSelfPermission = true");
        }

        FileUtils.createInstance(getApplicationContext());
        FileUtils.getInstance().saveTxt2Public(getApplicationContext(), "ota_install.lock", "", "Documents");
        OTAReportService.startNetWorkOTATask(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "onDestroy");

        super.onDestroy();
    }

    @Override
    protected void onStop() {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "onStop");

        super.onStop();
    }

    @Override
    protected void onResume() {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "onResume");

        finish();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
