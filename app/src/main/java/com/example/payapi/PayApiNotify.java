/**
 * 
 */
package com.example.payapi;

import java.lang.reflect.Field;
import java.util.List;

import com.example.payapi.util.MD5Util;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.AppTask;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
 * @author Administrator
 *
 */
	@SuppressLint("NewApi")
	public class PayApiNotify extends NotificationListenerService {
		
		static String TAG = "PayApiNotify";
	    //ActivityManager activityManager = null;
		//private Object activityName;
		//private Object activity_last;
		private int userId;
		private String userKeys;
	    @Override
	    public void onCreate() {
	        super.onCreate();
	        //activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    }
	    @SuppressLint("WrongConstant")
		@Override
	    public int onStartCommand(Intent intent, int flags, int startId) {
	        return super.onStartCommand(intent, START_STICKY, startId);
	    }
	    @Override
	    public void onStart(Intent intent, int startId) {
	        Log.v(TAG, "onStart");
	        if (intent != null) {
	            Bundle bundle = intent.getExtras();
	            if (bundle != null) {
	 
	                userId = bundle.getInt("userId");
	                userKeys = bundle.getString("userKeys");
	                Log.e(TAG, "userId in service "+userId+",key "+userKeys);
	            }
	        }
	 
	    }
	    // 有新的通知
        /*
            com.android.contacts :短信
            com.hpbr.bosszhipin :BOSS直聘
         */
	    @SuppressWarnings("deprecation")
		@Override
	    public void onNotificationPosted(StatusBarNotification sbn) {
            String pacageName=sbn.getPackageName();
	        Log.e("notify", "get notify,"+pacageName);
	        Notification n = sbn.getNotification();
	        if (n == null) {
	            return;
	        }
	        // 标题和时间
	        String title = "";
	        if (n.tickerText != null) {
	            title = n.tickerText.toString();
	        }
	        long when = n.when;
	        // 其它的信息存在一个bundle中，此bundle在android4.3及之前是私有的，需要通过反射来获取；android4.3之后可以直接获取
	        Bundle bundle = null;
	        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR2) {
	            // android 4.3
	            try {
	                Field field = Notification.class.getDeclaredField("extras");
	                bundle = (Bundle) field.get(n);
	            } catch (NoSuchFieldException e) {
	                e.printStackTrace();
	            } catch (IllegalArgumentException e) {
	                e.printStackTrace();
	            } catch (IllegalAccessException e) {
	                e.printStackTrace();
	            }
	        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
	            // android 4.3之后
	            bundle = n.extras;
	        }
	        // 内容标题、内容、副内容
	        String contentTitle = bundle.getString(Notification.EXTRA_TITLE);
	        if (contentTitle == null) {
	            contentTitle = "";
	        }
	        String contentText = bundle.getString(Notification.EXTRA_TEXT);
	        if (contentText == null) {
	            contentText = "";
	        }
	        String contentSubtext = bundle.getString(Notification.EXTRA_SUB_TEXT);
	        if (contentSubtext == null) {
	            contentSubtext = "";
	        }

	        Log.d("notify", "notify msg: title=" + title + " ,when=" + when
	                + " ,contentTitle=" + contentTitle + " ,contentText="
	                + contentText + " ,contentSubtext=" + contentSubtext+",pacagename:"+pacageName);

	        if(contentTitle.equals("UserData") &&  title.equals(""))
	        {
	        	userId = Integer.parseInt(contentText);
	        	userKeys = contentSubtext;

	            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	            manager.cancel(0);
	        }
	        else
	        {
	        	OnNotificationPost(pacageName, title, contentTitle, contentText, contentSubtext, when);
	        	MainActivity ma = MainActivity.instance;
	        	if(null != ma)
	        	{
			        //String shortClassName = info.topActivity.getShortClassName();    //类名
			        //String className = info.topActivity.getClassName();              //完整类名
			        //String packageName = info.topActivity.getPackageName();          //包名
			        //Log.e("Notify", shortClassName + ","+className+","+packageName);
	        		ma.OnNotificationPost(pacageName,title, contentTitle, contentText, contentSubtext, when);
	        	}
	        	else if(!contentTitle.equals("PayApiService") &&  !title.equals("PayApiService"))
	        	{
	        		SetNotify("后台程序以关闭，请重新启动");
	        	}
	        }	       
	    }

	    // 通知被删除了
	    @Override
	    public void onNotificationRemoved(StatusBarNotification sbn) {
	        Log.e("notify", "delete notify");
	    }

	    @Override
	    public void onDestroy() {
	        super.onDestroy();
	    }

	    @SuppressLint("NewApi")
	    @SuppressWarnings("deprecation")
	    private void SetNotify(String context) {
	        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	        Notification n;
	        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
	            // android 3.0之前
	            n = new Notification(R.drawable.ic_launcher, "title",
	                    System.currentTimeMillis());
	        } else {
	            // android 3.0之后
	            n = new Notification.Builder(this)
	                    .setSmallIcon(R.drawable.ic_launcher).setTicker("")
	                    .setContentTitle("PayApiService")
	                    .setContentText(context).setSubText("")
	                    .setWhen(System.currentTimeMillis()).build();
	        }
	        manager.notify(0, n);
	    }
	    private String yuan = "元";
	    public void OnNotificationPost(String pacageName, String title,String ctitle,String ctx,String subCtx,long time)
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
	    					PostNotify(p,1);
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
	    					PostNotify(p,2);
	    					msg+=",------"+p+":2";
	    				}
	    			}
	    		}
	    	}
	    	else {
	    	    if (pacageName.equals("com.shengpay.pos.merchant")){
                    String price = "0";
                    String before = "支付";
                    if(ctx.contains(yuan) )
                    {
                        try{
                            price = ctx.substring(ctx.indexOf(before)+before.length(), ctx.indexOf(yuan));
                        }
                        finally
                        {
                            float p = Float.valueOf(price);
                            if(p > 0)
                            {
                                PostNotify(p,3);
                                msg+=",------"+p+":3";
                            }
                        }
                    }
                }
            }
	    }

	    @SuppressLint("DefaultLocale")
		private void PostNotify(final float price,final int type) {
	        String messge="";
	        if (type==1){
                messge="PayNotifycation from 支付宝,price"+price;
            }
            else if(type==2){
                messge="PayNotifycation from 微信,price"+price;
            }
            else{
                messge="PayNotifycation from 盛付通,price"+price;
            }
	    	SetNotify(messge);
	        Thread postThread = new Thread(new Runnable(){
	       	   public void run(){
	       		   	 String url = "http://goddpay.com/p/Return";
	       		   	 String key = userKeys;//MD5Util.MD5(userId+""+price+""+type+""+userKeys).toLowerCase();
	       		     String para = "memid="+userId+"&price="+price+"&type="+type;//+"&key="+key;
	       		     para = para.toLowerCase();
	       		     String sr=HttpRequestUtil.sendPost(url,para,false);
	       		     System.out.println(para);
	    		     if(null == sr)
	    		     {
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
