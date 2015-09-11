package com.ithiema.mobilesafe;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import org.json.JSONObject;

import com.itheima.mobilesafe.R;
import com.ithiema.mobilesafe.activity.HomeActivity;
import com.ithiema.utils.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class SplashActivity extends Activity {
	private String description;
	protected static final int ENTER_HOME = 1;
	protected static final int ERRO = 2;
	protected static final int ENTER_UPDATE = 3;
	private TextView tv_version;
	private TextView tv_info;
	private Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case ENTER_HOME:
				//进入主界面
				Toast.makeText(SplashActivity.this, "没有新版本",0).show();
				enterHome();
				break;
			case ERRO:
				//错误提示
				Toast.makeText(SplashActivity.this, "出现错误",0).show();
				break;
			case ENTER_UPDATE:
				//弹出升级对话框
				showUpdateDialog();
				break;
			default:
				break;
			}
		}

		private void showUpdateDialog() {
			// TODO Auto-generated method stub
			AlertDialog.Builder builder=new Builder(SplashActivity.this);
			builder.setTitle("提示");
			builder.setMessage(description);
			builder.setNegativeButton("下次再说", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					enterHome();;
					
				}
			});
			builder.setPositiveButton("立刻升级", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//下载apk
					//替换安装
					if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
						FinalHttp http=new FinalHttp();
						http.download("http://188.188.4.111:8080/test/MobileSafe.apk", Environment.getExternalStorageDirectory()+"/mobilesafe2.0.apk", new AjaxCallBack<File>() {

							@Override
							public void onFailure(Throwable t, int errorNo,
									String strMsg) {
								// TODO Auto-generated method stub
								super.onFailure(t, errorNo, strMsg);
								Toast.makeText(getApplicationContext(), "下载失败了",0).show();
							}

							@Override
							public void onLoading(long count, long current) {
								// TODO Auto-generated method stub
								super.onLoading(count, current);
								tv_info.setVisibility(View.VISIBLE);
								int prograss=(int) (current*100/count);
								tv_info.setText("下载进度："+prograss+"%");
								
							}

							@Override
							public void onSuccess(File t) {
								// TODO Auto-generated method stub
								super.onSuccess(t);
								Toast.makeText(getApplicationContext(), "下载成功",0).show();
								installApk(t);
							}
							private void installApk(File t){
								Intent intent=new Intent();
								intent.setAction("android.intent.action.VIEW");
								intent.setDataAndType(Uri.fromFile(t), "application/vnd.android.package-archive");
								startActivity(intent);
							}
						});
					}else{
						Toast.makeText(getApplicationContext(), "sd卡不可用", 0).show();
					}
					
				}
			});
			//这行代码很重要
			builder.show();
		}

		private void enterHome() {
			// TODO Auto-generated method stub
			Intent intent=new Intent(SplashActivity.this,HomeActivity.class);
			startActivity(intent);
			//关闭启动页面
			finish();
			
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_spalsh);
		//设置版本名称
		tv_version=(TextView)findViewById(R.id.tv_version);
		tv_info=(TextView) findViewById(R.id.tv_info);
		tv_version.setText(getVersionName());
		
		//软件的升级
		checkVersion();
	}
	/*
	 * 校验是否有新版本
	 */
	public void checkVersion() {
	//创建子线程
		new Thread(){
			@Override
			public void run() {
				Message msg=Message.obtain();
				//请求网络，得到最新的版本信息
				try {
					URL url=new URL(getString(R.string.updateurl));
					HttpURLConnection conn=(HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");//设置请求方法
					conn.setConnectTimeout(5000);//设置超时的时间
					int code = conn.getResponseCode();
					if(code==200){
						//进来则说明请求成功
						//获得流对象
						InputStream is = conn.getInputStream();
						//把流转换为String字符串类型
						String updateurl = Utils.decodeStream(is);
						
						System.out.println(updateurl);
						//解析json
						String version;
						
							JSONObject obj=new JSONObject(updateurl);
							System.out.println("11");
							version = (String) obj.get("version");
							
							 description=(String) obj.get("description");
							String apkurl=(String) obj.get("apkurl");
						
						//比较本地的版本是否和服务器的是否一致
						if(getVersionName().equals(version)){
							msg.what=ENTER_HOME;
						
						}else{
							msg.what=ENTER_UPDATE;
						}
					}
					
				} catch (Exception e) {
					msg.what=ERRO;
					e.printStackTrace();
				}finally{
					handler.sendMessage(msg);
				}
			}
		}.start();
		
	}
	private String getVersionName(){
		//获得包管理者
		PackageManager pm=getPackageManager();
		//得到包信息
		PackageInfo pckInfo;
		try {
			pckInfo = pm.getPackageInfo(getPackageName(), 0);
			String versionName = pckInfo.versionName;
			return versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return null;
		
	}
}
