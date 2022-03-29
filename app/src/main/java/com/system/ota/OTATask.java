package com.system.ota;

import android.content.Context;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class OTATask {
    final String TAG = "A6_OTA " + this.getClass().getSimpleName();

    private String otaUpdatePath = "/data/ota_package/update.zip";

    private static final OTATask instance = new OTATask();

    private OTATask() {
    }

    public static OTATask getInstance() {
        return instance;
    }

    public void startOTA(Context context, String otaFilePath) {
        int otaProgress = 0;

        Log.d(TAG, "otaFilePath = " + otaFilePath);

        MQTTClient.getInstance().setOTAProgressValue(0);
        MQTTClient.getInstance().reportOTAInstallProgress(0, 0);

        File otaFile = new File(otaFilePath);
        if (!otaFile.exists()) {
            //没有找到升级包，升级失败
            Log.d(TAG, "无升级包");

            FileUtils.getInstance().deleteFile(FileUtils.OTA_LOCK_FILE_PATH);

            otaProgress = MQTTClient.getInstance().getOTAProgressValue();
            MQTTClient.getInstance().reportOTAInstallProgress(-1, otaProgress);

            return;
        }

        if (1 == CopySdcardFile(otaFilePath, otaUpdatePath)) {
            otaFile.delete();
            execRootCmd("chmod 0666" + otaUpdatePath);
        } else {
            Log.d(TAG, "CopySdcardFile Failure!");
            FileUtils.getInstance().deleteFile(FileUtils.OTA_LOCK_FILE_PATH);
        }

        try {
            UpdateParser.ParsedUpdate mParsedUpdate = UpdateParser.parse(new File(otaUpdatePath));
            Log.d(TAG, mParsedUpdate.toString());
            SystemUpdateManager mSystemUpdateManager = new SystemUpdateManager(context);
            mSystemUpdateManager.startUpdateSystem(mParsedUpdate);
            Log.d(TAG, "开始升级，请勿关机");
        } catch (Exception e) {
            Log.e(TAG, "e=" + e.toString());
            FileUtils.getInstance().deleteFile(FileUtils.OTA_LOCK_FILE_PATH);

            otaProgress = MQTTClient.getInstance().getOTAProgressValue();
            MQTTClient.getInstance().reportOTAInstallProgress(-1, otaProgress);
        }
    }

    // 执行adb命令
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

    // 从内部存储指定路径下拷贝系统升级包到应用cache下
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
}
