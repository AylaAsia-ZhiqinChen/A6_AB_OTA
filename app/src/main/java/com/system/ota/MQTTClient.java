package com.system.ota;

import android.content.Context;
import android.util.Log;

import com.zs.easy.mqtt.EasyMqttService;
import com.zs.easy.mqtt.IEasyMqttCallBack;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.json.JSONException;
import org.json.JSONObject;

public class MQTTClient {
    final String TAG = "A6_OTA " + this.getClass().getSimpleName();

    private static final MQTTClient instance = new MQTTClient();

    private MQTTClient() {
    }

    public static MQTTClient getInstance() {
        return instance;
    }

    private EasyMqttService mqttService;

    private int OTAState = 0;
    private int OTAProgress = 0;

    /**
     * 构建EasyMqttService对象
     */
    public void buildEasyMqttService(Context context) {
        mqttService = new EasyMqttService.Builder()
                //设置自动重连
                .autoReconnect(true)
                //设置不清除回话session 可收到服务器之前发出的推送消息
                .cleanSession(false)
                //唯一标示 保证每个设备都唯一就可以 建议 imei
                .clientId("test")
                //mqtt服务器地址 格式例如：tcp://10.0.261.159:1883
                .serverUrl("tcp://127.0.0.1:1883")
                //心跳包默认的发送间隔
                .keepAliveInterval(60)
                //构建出EasyMqttService 建议用application的context
                .bulid(context);
    }

    public void setOTAStateValue(int state) {
        OTAState = state;
    }

    public int getOTAStateValue() {
        return OTAState;
    }

    public void setOTAProgressValue(int progress) {
        OTAProgress = progress;
    }

    public int getOTAProgressValue() {
        return OTAProgress;
    }

    public void reportOTAInstallProgress(int state, int progress) {
        if (MQTTClient.getInstance().isConnected()) {
            try {
                mqttPublishOTAInstallProgress(state, progress);

                setOTAStateValue(state);
                setOTAProgressValue(progress);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "MQTTClient isConnected fail!");
        }
    }

    public void MQTTInit(Context context) {
        buildEasyMqttService(context);
        connect();
    }

    private void mqttPublishOTAInstallProgress(int state, int progress) throws JSONException {

        Log.d(TAG, "mqttPublishOTAInstallProgress!");

        //消息主题
        String topic = "/A6/OTA/install";
        //消息策略
        int qos = 2;
        //是否保留
        boolean retained = false;

        //消息内容
        JSONObject msgJson = new JSONObject();
        msgJson.put("state", state);
        msgJson.put("progress", progress);
        msgJson.put("stage", "install");
        String msg = msgJson.toString();

        Log.d(TAG, "msg = " + msg);

        //发布消息
        publish(msg, topic, qos, retained);
    }

    /**
     * 发布消息
     */
    private void publish(String msg, String topic, int qos, boolean retained) {
        mqttService.publish(msg, topic, qos, retained);
    }

    /**
     * 订阅主题 这里订阅三个主题分别是"a", "b", "c"
     */
    private void subscribe() {
        String[] topics = new String[]{"/A6/OTA/queryInstall"};
        //主题对应的推送策略 分别是0, 1, 2 建议服务端和客户端配置的主题一致
        // 0 表示只会发送一次推送消息 收到不收到都不关心
        // 1 保证能收到消息，但不一定只收到一条
        // 2 保证收到切只能收到一条消息
        int[] qoss = new int[]{2};
        mqttService.subscribe(topics, qoss);
    }

    /**
     * 判断服务是否连接
     */
    public boolean isConnected() {
        return mqttService.isConnected();
    }

    /**
     * 断开连接
     */
    private void disconnect() {
        mqttService.disconnect();
    }

    /**
     * 关闭连接
     */
    private void close() {
        mqttService.close();
    }

    /**
     * 连接Mqtt服务器
     */
    public void connect() {
        mqttService.connect(new IEasyMqttCallBack() {
            @Override
            public void messageArrived(String topic, String message, int qos) {
                //推送消息到达
                Log.d(TAG, "-> message = " + message);
                Log.d(TAG, "-> topic = " + topic);
                Log.d(TAG, "-> qos = " + qos);
            }

            @Override
            public void connectionLost(Throwable arg0) {
                //连接断开
                Log.d(TAG, "connectionLost!");
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken arg0) {
                Log.d(TAG, "deliveryComplete!");
            }

            @Override
            public void connectSuccess(IMqttToken arg0) {
                //连接成功 订阅一次即可 订阅状态可以保存到sp 这里简单处理了
                Log.d(TAG, "subscribe!");
            }

            @Override
            public void connectFailed(IMqttToken arg0, Throwable arg1) {
                //连接失败
                Log.d(TAG, "connectFailed!");
            }
        });
    }
}
