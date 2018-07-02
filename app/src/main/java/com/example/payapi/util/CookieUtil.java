package com.example.payapi.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import android.util.Log;


public class CookieUtil {
	static String cookieVal = null;
	static String sessionId = null;
	static String date = null;
	static String path = null;
	static String domain = null;
	public static boolean GetCookie(HttpURLConnection huc)
	{
		// 取得sessionID.
		cookieVal = huc.getHeaderField("Set-Cookie");
		sessionId = null;
		if(cookieVal != null)
		{
		    sessionId = cookieVal.substring(0, cookieVal.indexOf(";"));
		}
		date = huc.getHeaderField("Date");
		Log.d("Cookie", cookieVal+","+sessionId+",d "+date);
		return true;
		
	}
	public static boolean SetCookie(HttpURLConnection huc)
	{
		// 发送设置cookie：
		if(sessionId != null)
		{
			huc.setRequestProperty("Cookie", sessionId);
			return true;
		}
		return false;
	}
	public static String GetCookie()
	{
		return sessionId;
	}
}
