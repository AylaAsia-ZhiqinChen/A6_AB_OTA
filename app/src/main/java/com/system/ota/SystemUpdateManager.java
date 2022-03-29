package com.system.ota;

import android.content.Context;
import android.os.Looper;
import android.os.PowerManager;
import android.os.UpdateEngine;
import android.os.UpdateEngineCallback;
import android.util.Log;

import java.net.MalformedURLException;
import java.text.DecimalFormat;

public class SystemUpdateManager {
    final String TAG = "A6_OTA " + this.getClass().getSimpleName();

    UpdateEngine mUpdateEngine;
    private Context mContext;

    public SystemUpdateManager(Context context) throws MalformedURLException {
        mUpdateEngine = new UpdateEngine();
        mContext = context;
    }

    UpdateEngineCallback mUpdateEngineCallback = new UpdateEngineCallback() {
        @Override
        public void onStatusUpdate(int status, float percent) {
            int otaProgress = 0;

            Log.d(TAG, "onStatusUpdate  status: " + status);

            if (status < 5) {
                otaProgress = status * 10;
                MQTTClient.getInstance().setOTAProgressValue(otaProgress);
                Log.d(TAG, "status < 5, otaProgress = " + otaProgress);
                MQTTClient.getInstance().reportOTAInstallProgress(0, otaProgress);
            } else if (status == 5) {
                otaProgress = MQTTClient.getInstance().getOTAProgressValue();
                if (otaProgress < 100) {
                    otaProgress++;
                } else {
                    otaProgress = 100;
                }
                Log.d(TAG, "status = 5, otaProgress = " + otaProgress);
                MQTTClient.getInstance().reportOTAInstallProgress(0, otaProgress);
            } else if (status == 6) {
                Log.d(TAG, "status = 6");
                MQTTClient.getInstance().reportOTAInstallProgress(1, 100);
            }

            switch (status) {
                case UpdateEngine.UpdateStatusConstants.UPDATED_NEED_REBOOT:
                    FileUtils.getInstance().deleteFile(FileUtils.OTA_LOCK_FILE_PATH);
                    rebootNow();
                    break;
                // 回调状态，升级进度
                case UpdateEngine.UpdateStatusConstants.DOWNLOADING:
                    //mProgressBar.setProgress((int) (percent * 100));
                    DecimalFormat df = new DecimalFormat("#");
                    if (percent > 1.0) {
                        percent = 1.0f;
                    }
                    String progress = df.format(percent * 100);
                    Log.d(TAG, "update progress: " + progress);

                    break;
                default:
                    // noop
            }

        }

        @Override
        public void onPayloadApplicationComplete(int errorCode) {
            int otaProgress = 0;

            Log.d(TAG, "onPayloadApplicationComplete errorCode=" + errorCode);

            if (errorCode == UpdateEngine.ErrorCodeConstants.SUCCESS) {// 回调状态
                Log.d(TAG, "UPDATE SUCCESS!");
            } else {
                FileUtils.getInstance().deleteFile(FileUtils.OTA_LOCK_FILE_PATH);
                Looper.prepare();
                Log.d(TAG, "升级包错误");
                otaProgress = MQTTClient.getInstance().getOTAProgressValue();
                MQTTClient.getInstance().reportOTAInstallProgress(-1, otaProgress);
                Looper.loop();
            }
        }
    };

    public void startUpdateSystem(UpdateParser.ParsedUpdate parsedUpdate) {
        mUpdateEngine.bind(mUpdateEngineCallback);// 绑定callback

        mUpdateEngine.applyPayload(
                parsedUpdate.mUrl, parsedUpdate.mOffset, parsedUpdate.mSize, parsedUpdate.mProps);
    }

    /**
     * Reboot the system.
     */
    private void rebootNow() {
        Looper.prepare();
        Log.d(TAG, "升级完成，马上将重启设备，完成初始化!");
        Log.d(TAG, "rebootNow");
        PowerManager pManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        pManager.reboot("reboot-ab-update");
        Looper.loop();
    }
}