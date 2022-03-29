package com.system.ota;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LocalOTABroadcastReceiver extends BroadcastReceiver {
    final String TAG = "A6_OTA " + this.getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "run LocalOTABroadcastReceiver!");

        FileUtils.createInstance(context);
        FileUtils.getInstance().saveTxt2Public(context, "ota_install.lock", "", "Documents");
        OTAReportService.startLocalOTATask(context);
    }
}