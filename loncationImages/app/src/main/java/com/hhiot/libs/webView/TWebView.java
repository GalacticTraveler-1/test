package com.hhiot.libs.webView;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TWebView {
    public static  final  String TAG="THhuWebView";

    public static  final  int OnCallJsResult= 1000;
    public static  final  int OnJsCallNative= 1010;
    public static  final  String HhuWebViewBridge="HhuWebViewBridge";


    WebView mWebView ;
    AppCompatActivity context;
    Handler handler;
    String mWebUrl= "";
    public TWebView( AppCompatActivity _context , WebView _webView , Handler _handler){
        context = _context;
        mWebView = _webView;
        handler = _handler;
    }

    public void setContext(AppCompatActivity _context){
        context= _context;
    }
    public void setWebView(WebView _webView){
        mWebView = _webView;
    }
    public void setHandler(Handler _handler){
        handler = _handler;
    }
    public void init( ){
        try{
            String[] mediaPermissions = getMediaPermissions();
            //获取相机拍摄读写权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //版本判断
                if (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(context, mediaPermissions, 1);
                }
            }
            mWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
            mWebView.setWebViewClient(new WebViewClient(){
                //访问https
                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){
                    //handler.cancel(); 默认的处理方式，WebView变成空白页
                    handler.proceed(); //接受证书
                    //handleMessage(Message msg); 其他处理
                }
            });
            //判断页面加载的过程
            mWebView.setWebChromeClient(new WebChromeClient(){
                @Override
                public void onPermissionRequest(PermissionRequest request) {
                    context.runOnUiThread(() -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            String[] PERMISSIONS = getWebRtcPermissions();
                            List<String> ps = new ArrayList<String>();
                            for(int i=0;i<PERMISSIONS.length;i++){
                                String p = PERMISSIONS[i];
                                boolean flag = hasExternalStoragePermission(p);
                                if( !flag){
                                    ps.add(p);
                                }
                            }
                            String[] psArray=ps.toArray(new String[ps.size()]);
                            request.grant(psArray);
                        }
                    });
                }
                @Override
                public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                    callback.invoke(origin, true, false);
                    //super.onGeolocationPermissionsShowPrompt(origin, callback);
                }

            });
            WebSettings setting = mWebView.getSettings();
            setting.setMediaPlaybackRequiresUserGesture(false);
            setting.setJavaScriptEnabled(true);
            setting.setJavaScriptCanOpenWindowsAutomatically(true);
            setting.setAllowFileAccess(true);// 设置允许访问文件数据
            setting.setSupportZoom(true);
            setting.setBuiltInZoomControls(true);
            setting.setJavaScriptCanOpenWindowsAutomatically(true);
            setting.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            setting.setDomStorageEnabled(true);
            setting.setDatabaseEnabled(true);
            //设置Js调用JAVA函数的桥
            mWebView.addJavascriptInterface(this, HhuWebViewBridge);
            //启用数据库
            setting.setDatabaseEnabled(true);
            //启用地理定位，默认为true
            setting.setGeolocationEnabled(true);
            //设置定位的数据库路径
            //String dir = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();

            //开启DomStorage缓存
            setting.setDomStorageEnabled(true);
            setting.setUseWideViewPort(true);
            setting.setAllowContentAccess(true);
            setting.setSupportMultipleWindows(false);
            setting.setAppCacheEnabled(true);
        }
        catch (Exception er){
            Log.e(TAG, "open: "+er.getMessage());
        }
    }
    public void open(String url){
        try{
            String webUrl = url;
            webUrl += url.indexOf("?")>0?"&":"?";
            webUrl +="t="+Math.random();
            mWebUrl = webUrl;
            mWebView.loadUrl(webUrl);
        }
        catch (Exception er){
            Log.e(TAG, "open: "+er.getMessage());
        }
    }
    public void callJsWithoutResult(String jsCommand){
        try{
            String cmd = jsCommand.indexOf("javascript:")<0?"javascript:":"";
            cmd = cmd + jsCommand;
            mWebView.loadUrl(cmd);
        }
        catch (Exception er){
            Log.e(TAG, "open: "+er.getMessage());
        }
    }
    public void callJs(String jsCommand , String callId){
        try{
            mWebView.evaluateJavascript(jsCommand, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String s) {
                    if( handler!=null){
                        Message msg = new Message();
                        msg.what = OnCallJsResult;
                        Map<String , String> msgObj = new HashMap<String, String>();
                        msgObj.put("callId" , callId);
                        msgObj.put("jsCommand" , jsCommand);
                        msgObj.put("result", s);
                        msg.obj = msgObj;
                        handler.sendMessage(msg);
                    }
                }
            });
        }
        catch (Exception er){
            Log.e(TAG, "open: "+er.getMessage());
        }
    }
    @JavascriptInterface
    public void jsCallNative(String params){
        if( handler!=null){
            Message msg = new Message();
            msg.what = OnJsCallNative;
            msg.obj = params;
            handler.sendMessage(msg);
        }
    }
    String[] getMediaPermissions(){
        return new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_MEDIA_LOCATION,
                Manifest.permission.MEDIA_CONTENT_CONTROL,
                Manifest.permission.CAPTURE_AUDIO_OUTPUT,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.RECORD_AUDIO,
                //Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION

        };
    }
    String[] getWebRtcPermissions(){
        return new String[] {
                PermissionRequest.RESOURCE_AUDIO_CAPTURE,
                PermissionRequest.RESOURCE_VIDEO_CAPTURE,
                PermissionRequest.RESOURCE_MIDI_SYSEX ,
                PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID ,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_MEDIA_LOCATION,
                Manifest.permission.MEDIA_CONTENT_CONTROL,
                Manifest.permission.CAPTURE_AUDIO_OUTPUT,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
        };
    }
    boolean hasExternalStoragePermission(String permissionName) {
        int perm = context.checkCallingOrSelfPermission(permissionName);
        return perm == PackageManager.PERMISSION_GRANTED;
    }

}
