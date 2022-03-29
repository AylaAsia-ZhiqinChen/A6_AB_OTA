package com.system.ota;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class OTAReportService extends IntentService {
    final String TAG = "A6_OTA " + this.getClass().getSimpleName();

    private String networkOtaFilePath = "/data/data/com.aylaasia.a6_gateway/ota/ota_gateway.zip";
    private String localOtaFilePath = "/sdcard/Download/ota_gateway.zip";

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.system.ota.action.FOO";
    private static final String ACTION_BAZ = "com.system.ota.action.BAZ";
    private static final String ACTION_RUN_NETWORK_OTA_TASK = "com.system.ota.action.RunNetWorkOTATask";
    private static final String ACTION_RUN_LOCAL_OTA_TASK = "com.system.ota.action.RunLocalOTATask";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.system.ota.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.system.ota.extra.PARAM2";

    public OTAReportService() {
        super("OTAReportService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, OTAReportService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, OTAReportService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    public static void startNetWorkOTATask(Context context) {
        Intent intent = new Intent(context, OTAReportService.class);
        intent.setAction(ACTION_RUN_NETWORK_OTA_TASK);
        context.startService(intent);
    }

    public static void startLocalOTATask(Context context) {
        Intent intent = new Intent(context, OTAReportService.class);
        intent.setAction(ACTION_RUN_LOCAL_OTA_TASK);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            } else if (ACTION_RUN_NETWORK_OTA_TASK.equals(action)) {
                StartOTATask.getInstance().otaTask(getApplicationContext(), networkOtaFilePath);
            } else if (ACTION_RUN_LOCAL_OTA_TASK.equals(action)) {
                StartOTATask.getInstance().otaTask(getApplicationContext(), localOtaFilePath);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}