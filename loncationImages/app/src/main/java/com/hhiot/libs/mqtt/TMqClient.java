package com.hhiot.libs.mqtt;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.HashMap;
import java.util.Map;

public class TMqClient {
    public static  final  String TAG="TMqClient";

    public static final int MsgWhat_OnOpen = 1000;
    public static final int MsgWhat_OnClose = 1010;
    public static final int MsgWhat_OnReceiveMsg = 1020;

    Context context;
    Handler handler;
    TMqClientConfig config = new TMqClientConfig();
    MqttAndroidClient client;
    String clientId="";

    public TMqClient( Context _context , Handler _handler , TMqClientConfig _config){
        context = _context;
        handler = _handler;
        config = _config;
    }
    public boolean isConnected(){
        boolean flag = false;
        try{
            if( client!=null ){
                flag = client.isConnected();
            }
        }
        catch (Exception er){
            Log.e(TAG, "isConnected: "+ er.getMessage());
        }
        return  flag;
    }
    public void open(){
        try{
            clientId = "cid"+ System.currentTimeMillis();
            client = new MqttAndroidClient( context , config.serverUri , clientId);
            Thread ath = new Thread(){
                @Override
                public void run() {
                    super.run();
                    try{
                        startMqttClient();
                    }
                    catch (Exception eer){
                        Log.e(TAG, "run open mqtt: "+eer.getMessage() );
                    }
                }
            };
            ath.setDaemon(true);
            ath.start();
        }
        catch (Exception er){
            Log.e(TAG, "open: "+ er.getMessage());
        }
    }
    void startMqttClient(){
        try{
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.d(TAG, "connectionLost: ");
                    if( handler!=null){
                        Message msg = new Message();
                        msg.what = MsgWhat_OnClose;
                        msg.obj = clientId;
                        handler.sendMessage(msg);
                    }
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Log.d(TAG, "mqtt message arrived.");
                    Log.d(TAG, topic);
                    String content =  message.toString();
                    Log.d(TAG, content);
                    if( handler!=null){
                        Message msg = new Message();
                        msg.what = MsgWhat_OnReceiveMsg;
                        Map<String, String> msgData = new HashMap<String, String>();
                        msgData.put("clientId" , clientId);
                        msgData.put("topic" , topic);
                        msgData.put("content" , content);
                        msg.obj = msgData;
                        handler.sendMessage(msg);
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // send msg complete
                    Log.d(TAG, "mqtt deliveryComplete.");
                }
            });
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setKeepAliveInterval(60);
            mqttConnectOptions.setUserName(config.userName);
            mqttConnectOptions.setPassword(config.userPwd.toCharArray());
            mqttConnectOptions.setCleanSession(false);
            client.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "success: ");
                    try{
                        if( handler!=null){
                            Message msg = new Message();
                            msg.what = MsgWhat_OnOpen;
                            msg.obj = clientId;
                            handler.sendMessage(msg);
                        }
                    }
                    catch (Exception er){
                        Log.d(TAG, "onSuccess: "+er.getMessage());
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "onFailure: ");
                    close();
                }
            });
        }
        catch (Exception er){
            Log.e(TAG, "startMqttClient: "+ er.getMessage());
        }
    }
    public void close(){
        try{
            boolean flag = isConnected();
            if( flag){
                client.close();
                client = null;
            }
        }
        catch (Exception er){
            Log.e(TAG, "open: "+ er.getMessage());
        }
    }
    public void subscribe(String topic){
        try{
            boolean flag = isConnected();
            if( flag){
                client.subscribe(topic, 0);
            }
        }
        catch (Exception er){
            Log.e(TAG, "open: "+ er.getMessage());
        }
    }
    public void publish(String topic , String  msgContent){
        try{
            boolean flag = isConnected();
            if( flag){
                MqttMessage msg = new MqttMessage();
                byte[] msgBytes = msgContent.getBytes("UTF-8");
                msg.setPayload(msgBytes);
                client.publish(topic , msg);
            }
        }
        catch (Exception er){
            Log.e(TAG, "open: "+ er.getMessage());
        }
    }
    public MqttAndroidClient getClient(){
        return  client;
    }
}
