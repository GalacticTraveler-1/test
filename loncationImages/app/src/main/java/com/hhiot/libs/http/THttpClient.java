package com.hhiot.libs.http;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class THttpClient {
    public static final String TAG="THttpClient";
    public String get(String urlAddress){
        String res = "";
        try{
            URL url = new URL(urlAddress);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(6000);
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream in = conn.getInputStream();
                byte[] b = new byte[1024];
                int len = 0;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ((len = in.read(b)) > -1) {
                    // in.read()返回是int类型数据，代表实际读到的数据长度
                    // 将字节数组里面的内容写入缓存流
                    // 参数1：待写入的数组   参数2：起点  参数3：长度
                    baos.write(b, 0, len);
                }
                res = new String(baos.toByteArray());
            }
        }
        catch (Exception er){
            res ="";
        }
        return  res;
    }
    public void get2Ui(String urlAddress , Handler handler , int msgWhat){
        try{
            THttpGetThread ath = new THttpGetThread( urlAddress , handler , msgWhat);
            ath.setDaemon(true);
            ath.start();
        }
        catch (Exception er){
            ;
        }
    }
    class THttpGetThread extends Thread{
        String urlAddress;
        Handler handler;
        int what;
        public THttpGetThread( String _url , Handler _handler , int _what){
            urlAddress = _url ;
            handler = _handler;
            what = _what;
        }
        @Override
        public void run() {
            super.run();
            try{
                String res = get(urlAddress);
                Message msg = new Message();
                msg.what = what;
                msg.obj = res;
                handler.sendMessage(msg);
            }
            catch (Exception eer){
                ;
            }
        }
    }

    public static void GetResult(String urlAddress , Handler handler , int msgWhat){
        try{
            (new THttpClient()).get2Ui(urlAddress , handler , msgWhat);
        }
        catch (Exception er){
            Log.e(TAG, "GetResult: "+er.getMessage() );
        }
    }
}
