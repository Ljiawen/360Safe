package com.ithiema.utils;

import com.ithiema.mobilesafe.SplashActivity;

import android.test.AndroidTestCase;

public class Test extends AndroidTestCase {
public void test(){
	new SplashActivity().checkVersion();
	System.out.println("运行成功");
}
}
