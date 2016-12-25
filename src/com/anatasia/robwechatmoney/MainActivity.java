package com.anatasia.robwechatmoney;

import com.anatasia.service.AsService;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	private final String TAG = "Money";
	private AsService serviceInstance = null;
	private Button startButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		startButton = (Button) findViewById(R.id.start);
		startButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//if(getServiceInstance()!=null){
					getServiceInstance();
//				}else{
//					Log.i(TAG, "service为空");
//				}
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	 private AsService getServiceInstance(){
		 	Log.i(TAG,"应用页面:"+getApplicationContext().toString());
	        if (! isAccessibilitySettingsOn(getApplicationContext())) {
	            Toast.makeText(MainActivity.this, "尚未开启辅助功能，请在新弹出的对话框中开启ASDemo辅助功能！", Toast.LENGTH_LONG).show();
	            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
	            return null;
	        }else{
	            serviceInstance=AsService.getInstance();
	        }
	        return serviceInstance;
	    }
	    
	    private boolean isAccessibilitySettingsOn(Context mContext) {

	        int accessibilityEnabled = 0;
	        final String service = "com.anatasia.robwechatmoney/com.anatasia.service.AsService";
	        boolean accessibilityFound = false;
	        try {
	            accessibilityEnabled = Settings.Secure.getInt(
	                    mContext.getApplicationContext().getContentResolver(),
	                    Settings.Secure.ACCESSIBILITY_ENABLED);
	            Log.i(TAG, "accessibilityEnabled = " + accessibilityEnabled);
	        } catch (Settings.SettingNotFoundException e) {
	            Log.e(TAG, "Error finding setting, default accessibility to not found: "
	                    + e.getMessage());
	        }
	        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

	        if (accessibilityEnabled == 1) {
	            Log.i(TAG, "***ACCESSIBILIY IS ENABLED*** -----------------");
	            String settingValue = Settings.Secure.getString(
	                    mContext.getApplicationContext().getContentResolver(),
	                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
	            if (settingValue != null) {
	                TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
	                splitter.setString(settingValue);
	                while (splitter.hasNext()) {
	                    String accessabilityService = splitter.next();

	                    Log.i(TAG, "-------------- > accessabilityService :: " + accessabilityService);
	                    if (accessabilityService.equalsIgnoreCase(service)) {
	                        Log.i(TAG, "We've found the correct setting - accessibility is switched on!");
	                        accessibilityFound=true;
	                        return true;
	                    }
	                }
	            }
	        } else {
	            Log.i(TAG, "***ACCESSIBILIY IS DISABLED***");
	        }

	        return accessibilityFound;
	    }

}
