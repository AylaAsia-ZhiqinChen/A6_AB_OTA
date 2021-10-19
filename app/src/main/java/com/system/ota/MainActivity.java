package com.system.ota;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
    private static final String TAG = "AB ota MainActivity";
//    private Button abOTA;
    private static final int STORAGE_PERMISSIONS_REQUEST_CODE = 0;
    private static final String[] REQUIRED_STORAGE_PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private String otaUpdatePath = "/data/ota_package/update.zip";
    //    private String otaFilePath = "/storage/emulated/0/test/update.zip";
    private String otaFilePath = "/data/data/com.aylaasia.a6_gateway/ota/update.zip";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "checkSelfPermission ===false");
            ActivityCompat.requestPermissions(this, REQUIRED_STORAGE_PERMISSIONS, STORAGE_PERMISSIONS_REQUEST_CODE);
        } else {
            Log.d(TAG, "checkSelfPermission ===true");
        }

//        setContentView(R.layout.activity_main);
//        initView();

        startOTA();
    }

//    private void initView() {
//        abOTA = findViewById(R.id.ab_ota);
//        abOTA.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startOTA();
//            }
//        });
//    }

    private void startOTA() {
        File otaFile = new File(otaFilePath);
        if (!otaFile.exists()) {//没有找到升级包，升级失败
            Toast.makeText(getApplicationContext(), "无升级包", Toast.LENGTH_LONG).show();
            return;
        }

        if (1 == CopySdcardFile(otaFilePath, otaUpdatePath)) {
            otaFile.delete();
            execRootCmd("chmod 0666" + otaUpdatePath);
        }

        try {
            UpdateParser.ParsedUpdate mParsedUpdate = UpdateParser.parse(new File(otaUpdatePath));
            Log.d(TAG, mParsedUpdate.toString());
            SystemUpdateManager mSystemUpdateManager = new SystemUpdateManager(MainActivity.this);
            mSystemUpdateManager.startUpdateSystem(mParsedUpdate);
            Toast.makeText(getApplicationContext(), "开始升级，请勿关机", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "e=" + e.toString());
        }
    }

    //执行adb命令
    public void execRootCmd(String cmd) {
        Runtime r = Runtime.getRuntime();
        DataOutputStream dos = null;
        try {
            Process p = r.exec("su");
            OutputStream output = p.getOutputStream();
            InputStream input = p.getInputStream();

            dos = new DataOutputStream(output);
            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();

            p.waitFor();
            Log.d(TAG, "execRootCmd SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "execRootCmd ERROR" + e);
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*从内部存储指定路径下拷贝系统升级包到应用cache下*/
    public int CopySdcardFile(String fromFile, String toFile) {
        try {
            InputStream fosfrom = new FileInputStream(fromFile);
            OutputStream fosto = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c);
            }
            fosfrom.close();
            fosto.close();
            return 1;

        } catch (Exception ex) {
            Log.e(TAG, "ex=" + ex);
            return -1;
        }
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
