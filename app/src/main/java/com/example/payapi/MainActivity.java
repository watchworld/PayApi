package com.example.payapi;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.payapi.HttpRequestUtil;
import com.example.payapi.util.CookieUtil;
import com.example.payapi.util.MD5Util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint({ "HandlerLeak", "NewApi" })
public class MainActivity extends Activity  implements OnClickListener {
    static MainActivity instance;
    private Button notifyButton;
    private Button setAuthButton;
    private Button backButton;
    private TextView notifyText;
    private TextView userName;    
    private String account;
    private String password;
    private int userId = 0;
    private String userKeys = "";
    private EditText accEditor;
    private EditText pssEditor;
    private WebView webView;
    private Handler handler;
    private String handlerMsg;
    private boolean isLogined = false;
    private boolean heartBeat = false;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        initView();
        // 开启服务
        startNotificationListenService();
        handler = new Handler(){
            @SuppressLint("NewApi")
			@Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case 1:
                        System.out.println("handleMessage thread id " + Thread.currentThread().getId());
                        System.out.println("msg.arg1:" + msg.arg1);
                        System.out.println("msg.arg2:" + msg.arg2);
                        break;
                    case 0:
						String cookie = CookieUtil.GetCookie();
						Log.e("handleMsg", cookie==null?"":cookie);
						String url = "http://goddpay.com/";
						CookieManager.getInstance().setCookie(url, cookie);
						webView.setVisibility(View.VISIBLE);
						webView.loadUrl(url);
						backButton.setVisibility(View.VISIBLE);
						//应该用SendMessage或者socket发送，这种方式容易被捕获且表现不是很好
						SetNotify();
						userName.setText(account+"，你好！");
						userName.setVisibility(View.VISIBLE);
						isLogined = true;
						heartBeat = true;
						break;
                    case 2:
                        Toast.makeText(MainActivity.this, "登录票据过期，请重新登录", Toast.LENGTH_SHORT)
                                .show();
                        isLogined = false;
                        heartBeat = false;
                    	break;
                    case 3:
                        Toast.makeText(MainActivity.this, "账号不存在或密码错误，请重新登录", Toast.LENGTH_SHORT)
                                .show();
                        isLogined = false;
                        heartBeat = false;
                    	break;
                    case 4:
                    	SetNotify();
                    	Login();
                    	break;
                    case 9:
                    	if(null != handlerMsg && !handlerMsg.equals(""))
                    	{
                    		notifyText.setText(handlerMsg);
                    		//Toast.makeText(MainActivity.this, handlerMsg, Toast.LENGTH_LONG).show();
                    	}
                    	break;
                }
            }
        };

        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new HeartBeatTask(), 1, 12000);
    }

    private class HeartBeatTask extends TimerTask{

          @Override

           public void run() {
        	  	if(instance.heartBeat)
        	  	{
                    Message message = new Message();
                    message.what = 4;
                    handler.sendMessage(message);
        	  	}

          }

       }
    @SuppressLint({ "SetJavaScriptEnabled", "NewApi" })
	private void initView() {

        webView = (WebView) findViewById(R.id.webView1);  

        webView.getSettings().setJavaScriptEnabled(true);  
        webView.setWebChromeClient(new WebChromeClient());  
        webView.setWebViewClient(new WebViewClient());  
        webView.zoomIn();  
	    webView.setVisibility(View.INVISIBLE);
	    webView.loadUrl("http://goddpay.com/");
        notifyButton = (Button) findViewById(R.id.button2);
        notifyButton.setOnClickListener( this);
        setAuthButton = (Button) findViewById(R.id.button1);
        setAuthButton.setOnClickListener(this);
        backButton = (Button) findViewById(R.id.signout);
        backButton.setOnClickListener(this);
	    backButton.setVisibility(View.INVISIBLE);

	    userName = (TextView) findViewById(R.id.textView2);
		userName.setVisibility(View.INVISIBLE);
	    notifyText = (TextView) findViewById(R.id.labelNotify);
	    notifyText.setText("");
        accEditor = (EditText) findViewById(R.id.editText1);
        pssEditor = (EditText) findViewById(R.id.editText2);
        accEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            	account = accEditor.getText().toString();
            	Log.e("MainActivity", account);
            }            
			@Override
			public void afterTextChanged(Editable s) {

			}
        });
        pssEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            	password = pssEditor.getText().toString();
            	Log.e("MainActivity", password);
            }            
			@Override
			public void afterTextChanged(Editable s) {

			}
        });
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView,true);
    }

    private void startNotificationListenService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            setAuth();
            Intent intent = new Intent(MainActivity.this,
                    PayApiNotify.class);
	        Bundle bundle  = new Bundle();
	        bundle.putInt("userId", userId);
	        bundle.putString("userKeys", userKeys);
	        intent.putExtras(bundle);
	        startService(intent);
        } else {
            Toast.makeText(MainActivity.this, "手机的系统不支持此功能", Toast.LENGTH_SHORT)
                    .show();
        }
    }
    @SuppressWarnings("deprecation")
	@Override
public void onClick(View v) {
        switch (v.getId()) {
        case R.id.button1:
            Login();
            //SetNotify();
            break;
        case R.id.button2:
        	OpenWebView();
            break;
        case R.id.signout:
        	webView.setVisibility(View.INVISIBLE);
    	    backButton.setVisibility(View.INVISIBLE);
			userName.setVisibility(View.INVISIBLE);
			CookieManager.getInstance().removeAllCookie();
			heartBeat = false;
			isLogined = false;
    	    //SetNotify();
        default:
            break;
        }
    }

    /**  
     * 监听Back键按下事件,方法2:  
     * 注意:  
     * 返回值表示:是否能完全处理该事件  
     * 在此处返回false,所以会继续传播该事件.  
     * 在具体项目中此处的返回值视情况而定.  
     */    
     @Override    
     public boolean onKeyDown(int keyCode, KeyEvent event) {    
         if ((keyCode == KeyEvent.KEYCODE_BACK)) {    
              //System.out.println("按下了back键   onKeyDown()");     
              return true;    
         }else {    
             return super.onKeyDown(keyCode, event);    
         }    
             
     }    
    private void OpenWebView()
    {
       if(isLogined)
       {
       		webView.setVisibility(View.VISIBLE);
       		backButton.setVisibility(View.VISIBLE);
       }
    }
    private void Login() {
         Thread postThread = new Thread(new Runnable(){
        	   public void run(){
        		   	 String url = "http://goddpay.com/p/GetMember";
        		     String para = "&name="+account+"&pass="+password;//+MD5Util.encodePassword(MD5Util.encodePassword("123456"));
        		     //String para = "name=1@qq.com&pass=123456";//+MD5Util.encodePassword(MD5Util.encodePassword("123456"));
        		     String sr=HttpRequestUtil.sendPost(url,para,true);

        		     System.out.println(para);
        		     System.out.println(sr);
                     Message msg = new Message();
        		     if(null == sr)
        		     {
                         msg.what = 2;
                         handler.sendMessage(msg);
        		     }
        		     else
        		     {
        		    	 try {
     						JSONObject json = new JSONObject(sr);
     	                     int code  = json.getInt("code");
     	                     boolean flag = json.getBoolean("flag");
     						if(flag && code == 0)
     						{
     		        		     if(!heartBeat)
     		        		     {
     		                         msg.what = 0;
     		                         heartBeat = true;
     		                         handler.sendMessage(msg);
     		        		     }
     		        		     JSONObject data =  (JSONObject)json.get("data");
     		        		     userId = data.getInt("id");
     		        		     userKeys = data.getString("keys");
     						}
     						else
     						{
     		        		     if(heartBeat)
     		        		     {
     			                        msg.what = 2;
     		        		     }
     		        		     else
     		        		     {
     			                        msg.what = 3;
     		        		     }
     	                        handler.sendMessage(msg);
     						}
     					} catch (JSONException e) {
     						// TODO Auto-generated catch block
     						e.printStackTrace();
                             msg.what = 2;
                             handler.sendMessage(msg);
     					}
        		     }
        		     
        		   }
        		});
         postThread.start();
    }
    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    private void SetNotify() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification n;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // android 3.0之前
            n = new Notification(R.drawable.ic_launcher, "title",
                    System.currentTimeMillis());
        } else {
            // android 3.0之后
            n = new Notification.Builder(MainActivity.this)
                    .setSmallIcon(R.drawable.ic_launcher).setTicker("")
                    .setContentTitle("UserData")
                    .setContentText(""+userId).setSubText(""+userKeys)
                    .setWhen(System.currentTimeMillis()).build();
        }
        manager.notify(0, n);
    }

    private void setAuth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
        	if(!isEnabled())
        	{
                Intent intent = new Intent(
                        "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                startActivity(intent);
        	}
        } else {
            Toast.makeText(MainActivity.this, "手机的系统不支持此功能", Toast.LENGTH_SHORT)
                    .show();
        }
    }
 // 判断是否打开了通知监听权限
    private boolean isEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private String yuan = "元";
    public void OnNotificationPost(String title,String ctitle,String ctx,String subCtx,long time)
    {
    	String msg = "title:"+title+",ctitle:"+ctitle+",\nctx:"+ctx+",subCtx:"+subCtx;
    	if(ctitle.contains("支付宝消息") || ctitle.contains("支付宝通知"))
    	{
        	//PostNotify(10,1);
    		String price = "0";
    		String before = "付款";
    		if(ctx.contains(yuan) && ctx.contains(before))
    		{
    			try{
    				
        			price = ctx.substring(ctx.indexOf(before)+before.length(), ctx.indexOf(yuan));
    			}
    			finally
    			{
    				float p = Float.valueOf(price);
    				if(p > 0)
    				{
    					//PostNotify(p,1);
    					msg+=",------"+p+":1";
    				}
    			}
    		}
    	}
    	else if(title.contains("微信支付") || ctitle.contains("微信支付"))
    	{
        	//PostNotify(10,1);
    		String price = "0";
    		String before = "收款";
    		if(ctx.contains(yuan) && ctx.contains(before))
    		{
    			try{
        			price = ctx.substring(ctx.indexOf(before)+before.length(), ctx.indexOf(yuan));
    			}
    			finally
    			{
    				float p = Float.valueOf(price);
    				if(p > 0)
    				{
    					//PostNotify(p,2);
    					msg+=",------"+p+":2";
    				}
    			}
    		}
    	}
        Message hmsg = new Message();
        hmsg.what = 9;
        handlerMsg = msg;
        handler.sendMessage(hmsg);
    }
    @SuppressLint("DefaultLocale")
	private void PostNotify(final float price,final int type) {
        Thread postThread = new Thread(new Runnable(){
       	   public void run(){
       		   	 String url = "http://goddpay.com/p/Return";
       		   	 String key = MD5Util.MD5(userId+""+price+""+type+""+userKeys).toLowerCase();
       		     String para = "memid="+userId+"&price="+price+"&type="+type;//+"&key="+key;
       		     para = para.toLowerCase();
       		     String sr=HttpRequestUtil.sendPost(url,para,false);
       		     System.out.println(para);
    		     if(null == sr)
    		     {
                     Message msg = new Message();
                     msg.what = 2;
                     handler.sendMessage(msg);
    		     }
       		     else
       		     {
           		     System.out.println(sr);
       		     }
       	   }
       	});
        postThread.start();
   }
}
