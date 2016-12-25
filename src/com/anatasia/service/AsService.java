package com.anatasia.service;
/*
 * author:Anatasia
 * function:rob red bags on wechat
 * 开发过程中遇到的问题：
 * 1.问题：领取红包，使用Accessibilitynodeinfo.ACTION_CLICK无法实现点击，accessibility报错，Disconnected node
 *   原因：找到的控件是textview类型的，不是layout型的，没有ACTION_CLICK这个选项
 *   解决办法：打印控件详细信息，判断指定的控件可以进行的操作，找到view的父节点，看是否有ACTION_CLICK这个选项
 *   
 * 
 * 
 */
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


import android.R.integer;
import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

@SuppressLint("NewApi")
public class AsService extends AccessibilityService{

	private final String TAG = "Money";
	private static AsService serviceInstance=null;
	private final String WECHAT_PACKAGENAME="com.tencent.mm";
	Handler handler = new Handler();
	
	public void onServiceConnected() {
		Log.i(TAG, "service connected");
		serviceInstance = this;
	}

	public boolean onUnbind(Intent intent) {
		serviceInstance = null;
		return true;
	}
	
	public void testRob(){
		int totalCount = 25;
		if(openApp("com.tencent.mm")){
			Log.i(TAG,"打开应用成功");
			try {
				Thread.sleep(10*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(int count =0;count<totalCount;count++){
				Log.i(TAG,"第"+(count+1)+"/"+totalCount+"次页面获取");
				AccessibilityNodeInfo rootNode = getRootInActiveWindow();
				List<AccessibilityNodeInfo> nodeList = rootNode.findAccessibilityNodeInfosByText("领取红包");
				for(AccessibilityNodeInfo node:nodeList){
					node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
					Log.i(TAG,"红包已点开");
				}
			}
		}
			
		
	}
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		// TODO Auto-generated method stub
		int eventType =event.getEventType();
		//Log.d(TAG,"事件------>"+event);
		switch (eventType) {
		case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED://通知栏事件
			List<CharSequence> texts=event.getText();
			if(!texts.isEmpty()){
				for(CharSequence text:texts){
					String content = text.toString();
					Log.i(TAG,"通知栏:"+content);
					if(content.contains("[微信红包]")){
						//有微信红包信息，打开通知
						if(event.getParcelableData()!=null&&event.getParcelableData() instanceof Notification){
							Notification notification = (Notification)event.getParcelableData();
							PendingIntent pendingIntent = notification.contentIntent;
							try {
								pendingIntent.send();
							} catch (CanceledException e) {
								// TODO Auto-generated catch block
								Log.i(TAG, e.toString());
							}
						}
					}
				}
			}
			break;
		//case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED://窗口内容发生变化
		case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
			//ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
			//String runningActivity = activityManager.getRunningTasks(1).get(0).topActivity
					//.getClassName();
			String className = event.getClassName().toString();
			Log.i(TAG, "窗口："+className);
			if(className.equals("com.tencent.mm.ui.LauncherUI")){//聊天页面
				getRedbag();//领取红包
			}else if(className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI")){//打开红包
				//点中红包，下一步是拆开红包
				openRedbag();
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub
		
	}
	
	//第一步，在聊天窗口点开红包
	private void getRedbag(){
		 AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
	        if (nodeInfo == null) {
	            Log.w(TAG, "rootWindow为空");
	            return;
	        }
	        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
	        if (list.isEmpty()) {
	            list = nodeInfo.findAccessibilityNodeInfosByText("查看红包");
	            for (int i=list.size()-1;i>=0;i--) {
	            	AccessibilityNodeInfo parent = list.get(i).getParent();
	                Log.i(TAG, "-->查看红包:" + parent);
	                if(parent!=null){
	                	parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
	 	                break;
	                }
	               
	            }
	        } else {
	            //最新的红包领起
	            for (int i = list.size() - 1; i >= 0; i--) {
	                AccessibilityNodeInfo parent = list.get(i).getParent();
	                Log.i(TAG, "-->领取红包:" + parent);
	                if (parent != null) {
	                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
	                    break;
	                }
	            }
	        }
	}
	//第二步，点击拆开红包
	private void openRedbag(){
		AccessibilityNodeInfo rootNode = getRootInActiveWindow();
		if(rootNode!=null){
			List<AccessibilityNodeInfo> nodeInfos = rootNode.findAccessibilityNodeInfosByText("发了一个红包，金额随机");
			Log.i(TAG, "发现待拆红包个数"+nodeInfos.size());
			if(!nodeInfos.isEmpty()){
				List<AccessibilityNodeInfo> list = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bdh");
				for(AccessibilityNodeInfo node:list){
					Log.i(TAG,"待拆红包:"+node);
					if(node!=null){
						node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
						return;
					}
				}
			}
//			for(AccessibilityNodeInfo node:nodeInfos){
//				if(node.getParent()!=null&&node.getParent().getChildCount()==3){
//					Log.i(TAG,"待拆红包:"+node.getParent().getChild(2));
//					//Log.i(TAG, "点击拆开红包");
//					node.getParent().getChild(2).performAction(AccessibilityNodeInfo.ACTION_CLICK);
//				}
//			}
		}
	}
	
	public static AsService getInstance() {
		return serviceInstance;
	}
	
	// 打开应用
		private boolean openApp(String packageString) {

			HashMap<Integer, String> appHashMap = new HashMap<Integer, String>();
			HashMap<Integer, String> packageHashMap = new HashMap<Integer, String>();
			HashMap<Integer, String> activityHashMap = new HashMap<Integer, String>();

			int size;
			int j = 0;
			List<PackageInfo> packages = getPackageManager()
					.getInstalledPackages(0);
			for (int i = 0; i < packages.size(); i++) {
				PackageInfo packageInfo = packages.get(i);
				String appname = packageInfo.applicationInfo.loadLabel(
						getPackageManager()).toString();
				String packagename = packageInfo.packageName;
				appHashMap.put(j, appname);
				packageHashMap.put(j, packagename);
				j++;
			}
			size = appHashMap.size();
			PackageManager pm = this.getPackageManager(); // 获得PackageManager对象
			Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			// 通过查询，获得所有ResolveInfo对象.
			List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent,
					PackageManager.GET_ACTIVITIES);
			// 调用系统排序 ， 根据name排序
			// 该排序很重要，否则只能显示系统应用，而不能列出第三方应用程序
			Collections.sort(resolveInfos,
					new ResolveInfo.DisplayNameComparator(pm));

			for (ResolveInfo reInfo : resolveInfos) {
				String pkgName = reInfo.activityInfo.packageName; // 获得应用程序的包名
				String activityName = reInfo.activityInfo.name; // 获得该应用程序的启动Activity的name
				for (int i = 0; i < size; i++) {
					if (pkgName.equals(packageHashMap.get(i))) {
						activityHashMap.put(i, activityName);
						break;
					}
				}
			}

			String activityString1 = "";
			for (int i = 0; i < appHashMap.size(); i++) {
				if (packageString.equals(packageHashMap.get(i))) {
					activityString1 = activityHashMap.get(i);
					break;
				}
			}

			try {
				Intent intent = new Intent();
				intent.setClassName(packageString, activityString1);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);

				return true;
			} catch (Exception e) {
				Log.i(TAG, "应用程序启动失败：" + e.getMessage());
				Toast.makeText(this, "APP启动失败", Toast.LENGTH_SHORT).show();
				return false;
			}
		}

}
