package com.system.ota;

import android.content.Context;
import android.os.Looper;
import android.os.PowerManager;
import android.os.UpdateEngine;
import android.os.UpdateEngineCallback;
import android.util.Log;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.text.DecimalFormat;

public class SystemUpdateManager {

    private static final String TAG = "AB ota SystemUpdateManager";

    UpdateEngine mUpdateEngine;
    private Context mContext;

    public SystemUpdateManager(Context context) throws MalformedURLException {
        mUpdateEngine = new UpdateEngine();
        mContext = context;
    }

    UpdateEngineCallback mUpdateEngineCallback = new UpdateEngineCallback() {
        @Override
        public void onStatusUpdate(int status, float percent) {

            Log.d(TAG, "onStatusUpdate  status: " + status);

            switch (status) {
                case UpdateEngine.UpdateStatusConstants.UPDATED_NEED_REBOOT:
                    rebootNow();
                    break;
                case UpdateEngine.UpdateStatusConstants.DOWNLOADING:// 回调状态，升级进度
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
            Log.d(TAG, "onPayloadApplicationComplete errorCode=" + errorCode);

            if (errorCode == UpdateEngine.ErrorCodeConstants.SUCCESS) {// 回调状态
                Log.d(TAG, "UPDATE SUCCESS!");
            } else {
                Looper.prepare();
                Toast.makeText(mContext, "升级包错误", Toast.LENGTH_LONG).show();
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
        Toast.makeText(mContext, "升级完成，马上将重启手机，完成初始化！", Toast.LENGTH_LONG).show();
        Log.d(TAG, "rebootNow");
        PowerManager pManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        pManager.reboot("reboot-ab-update");
        Looper.loop();
    }
}