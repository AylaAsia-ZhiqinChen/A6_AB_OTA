package com.system.ota;

import android.content.Context;
import android.util.Log;

public class StartOTATask {
    final String TAG = this.getClass().getSimpleName();

    private static final StartOTATask instance = new StartOTATask();

    private StartOTATask() {
    }

    public static StartOTATask getInstance() {
        return instance;
    }

    public void otaTask(final Context mContext) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                MQTTClient.getInstance().MQTTInit(mContext);

                while (!MQTTClient.getInstance().isConnected()) {
                    Log.d(TAG, "MQTTClient isConnected fail!");
                }

                OTATask.getInstance().startOTA(mContext);
            }
        }).start();
    }
}
