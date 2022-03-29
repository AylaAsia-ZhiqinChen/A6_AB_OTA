package com.system.ota;

import android.content.Context;
import android.util.Log;

public class StartOTATask {
    final String TAG = "A6_OTA " + this.getClass().getSimpleName();

    private static final StartOTATask instance = new StartOTATask();

    private StartOTATask() {
    }

    public static StartOTATask getInstance() {
        return instance;
    }

    public void otaTask(final Context mContext, final String otaFilePath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                MQTTClient.getInstance().MQTTInit(mContext);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                while (!MQTTClient.getInstance().isConnected()) {
                    Log.d(TAG, "MQTTClient isConnected fail!");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                OTATask.getInstance().startOTA(mContext, otaFilePath);
            }
        }).start();
    }
}
