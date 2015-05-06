package com.encinitaslabs.wirfid.hpreader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.widget.Toast;

public class RfidContainer extends Activity implements FRAG_Tag.OnTagSelectedListener{
	private static final boolean DEBUG = false;
	private static final String ACTION_USB_PERMISSION = "com.encinitaslabs.wirfid.hpreader.USB_PERMISSION";
	private static final int[] PID = {0x824, 0x861};
	private static final int VID = 0x24e9;
	private static final int minUptimeMillis = 30000;
	private static final int powerOnDelay_ms = 30000;
	private static final String TAG = "RfidContainer";

	// Encinitas Labs
	private static boolean serviceStarted = false;
	private PendingIntent mRestartIntent;
	private boolean fileInterfaceActive = false;
	public static String logFilename = "cirrusOne.txt";
	// Encinitas Labs

	private SharedPreferences mSharedpref;
	
	private enum Fragments {About, Config, Detail, Tag, Web};
	private static boolean isPhone;
	private FragmentTransaction ft;
	private Fragment objFragment;
	private boolean bSavedInst = false;

//    protected static UsbCommunication mUsbComm = new UsbCommunication();
	private UsbManager mManager;
	private PendingIntent mPermissionIntent;
    
	private int iLayout;
	private int iMenu; 
    	
	// #### activity ####
	@Override
    public void onCreate(Bundle savedInstanceState) {        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler(this));
        mRestartIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, new Intent(getIntent()), getIntent().getFlags());

        // This code is here because Android immediately attempts to run the application
        // immediately based on the presence of the USB device.  The system is not yet
        // stable so bad things happen. Wait for some minimum up time before proceeding.
        if (SystemClock.uptimeMillis() < minUptimeMillis) {
//    	    AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//    	    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + powerOnDelay_ms, mRestartIntent);
    	    System.exit(2);
        }

        mSharedpref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Point point = new Point();
		getWindowManager().getDefaultDisplay().getSize(point);
		switch (getWindowManager().getDefaultDisplay().getRotation()) {
			case Surface.ROTATION_0:
			case Surface.ROTATION_180:
				isPhone = (point.x - point.y) < 0 ? true : false;
				break;
			case Surface.ROTATION_90:
			case Surface.ROTATION_270:
				isPhone = (point.x - point.y) > 0 ? true : false;
				break;
		}
		
		if (isPhone) {
			iLayout = R.id.TagLayout;
			iMenu = R.menu.menu_option;
		} else {
   			iLayout = R.id.DetailLayout;
   			iMenu = R.menu.menu_option_xlarge;
		}

    	if (savedInstanceState == null) {
    		bSavedInst = true;
			showFragment(Fragments.About, 0, null);
    	}
	
        Timer timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                startReaderService();
            	pushActivityToBackground();
            }
        }, 2000);

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (serviceStarted == true) {
			stopService(new Intent(this, ReaderService.class));			
		}
	}

	@Override
	public void onTagSelected(int tagPosition, String strTag) {
		showFragment(Fragments.Detail, tagPosition, strTag);		
	}
	
	@Override
	public void onTagLongPress(int tagPosition, String strTag) {
		showFragment(Fragments.Web, tagPosition, strTag);
	}
	
	// #### menu ####
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(iMenu, menu);
    	
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()) {
			case R.id.item_tag:
				showFragment(Fragments.Tag, 0, null);
				break;
			case R.id.item_config:
				showFragment(Fragments.Config, 0, null);
				break;
			case R.id.item_about:
				showFragment(Fragments.About, 0, null);
				break;
			case R.id.item_quit:
				finish();
				break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	// #### broadcast receiver ####
	BroadcastReceiver usbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
	        Toast.makeText(context, action, Toast.LENGTH_LONG).show();
		}
	};

	// #### fragment ####
	private void showFragment(Fragments fragmentType ,int index, String tagid) {
		switch(fragmentType) {
			case Tag:
				objFragment = FRAG_Tag.newInstance();
				break;
			case Config:
				objFragment = FRAG_Config.newInstance();
				break;
			case About:
				objFragment = FRAG_About.newInstance();
				break;
			case Detail:
				objFragment = FRAG_Details.newInstance(index, tagid);
				break;
			case Web:
			    final String prefixUrl = mSharedpref.getString("cfg_web_url", "");

			    if(prefixUrl.isEmpty()) {
					Toast.makeText(this, "Please fill the web url in the configuration page.", Toast.LENGTH_SHORT).show();
			    	return;
			    } else
			    	objFragment = FRAG_Web.newInstance(index, tagid);
				break;
		}
		    ft = getFragmentManager().beginTransaction();
		    ft.replace(iLayout, objFragment);
		    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		    if(iLayout == R.id.TagLayout && fragmentType.equals(Fragments.Detail))
	    	ft.addToBackStack(null);

		    ft.commit();
	}
    
	private void insertFragTag() {
		FragmentTransaction ftTag = getFragmentManager().beginTransaction();
	    ftTag.replace(R.id.TagLayout, FRAG_Tag.newInstance());
	    ftTag.commit();
	}
	
	public static boolean getUsbState() {
		return ReaderService.usbState;
	}
	
	public static boolean isPhone() {
		return isPhone;
	}
	
	public static int getPid() {
		return ReaderService.devicePid;
	}
	
	public void startReaderService() {
		if (serviceStarted == false) {
            initFileInterface();
	        Intent startMyService = new Intent(this, ReaderService.class);
	        startService(startMyService);
	        serviceStarted = true;
		}
	}
	
	private void pushActivityToBackground() {
		// Push this activity to the background
		Intent i = new Intent();
	    i.setAction(Intent.ACTION_MAIN);
	    i.addCategory(Intent.CATEGORY_HOME);
	    this.startActivity(i);
	}
	
	public class MyUncaughtExceptionHandler implements UncaughtExceptionHandler {

		Activity activity;

		/**
		 * Used if not activity.
		 */
		public MyUncaughtExceptionHandler() {

		}

		/**
		 * Used if sat in activity.
		 * @param activity
		 */
		public MyUncaughtExceptionHandler(Activity activity) {
		    this.activity= activity;
		}

		@Override
		public void uncaughtException(Thread t, Throwable e) {
			String stackTrace = getStackTraceAsString(e);
			writeToLogFile(stackTrace);
		    Log.wtf(RfidContainer.TAG, "\nUnhandled exception caught" + "\n" + getStackTraceAsString(e) + "\nfrom thread (id): " + t.getId());
//		    AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//		    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 7000, mRestartIntent);
		    System.exit(2);
		}

		private String getStackTraceAsString(Throwable throwable)
		{
		    final Writer result = new StringWriter();
		    final PrintWriter printWriter = new PrintWriter(result);
		    throwable.printStackTrace(printWriter);
		    return result.toString();
		}
	}

	/* Checks if external storage is available for read and write */
	private boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}

	/* Checks if external storage is available to at least read */
	private boolean isExternalStorageReadable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    return false;
	}
	
	/* Initializes the Text File Command Interface */
	private boolean initFileInterface () {
		boolean success = false;
		// Check status of External Storage
        if (isExternalStorageReadable() && isExternalStorageWritable()) {
        	fileInterfaceActive = true;
        	String directory = Environment.getExternalStorageDirectory().toString();
//			Toast.makeText(this, "Creating Log File: " + directory + "/" + logFilename, Toast.LENGTH_SHORT).show();        	
			writeToLogFile("COA version " + getString(R.string.about_sw_ver_sum) + " started");
        } else {
			Toast.makeText(this, "External Storage is NOT Read/Write", Toast.LENGTH_SHORT).show();        	
        }
		return success;
	}

	private boolean writeToLogFile(String message) {
		boolean success = false;
		if (fileInterfaceActive == true) {
			File logFile = new File(Environment.getExternalStorageDirectory() + "/" + logFilename);
			// Create a time stamp		
			DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HHmm");
			Date date = new Date();
			message = dateFormat.format(date) + " " + message;
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
				writer.write(message + "\n");
				writer.close();
				success = true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		        Toast.makeText(this, "File write error", Toast.LENGTH_LONG).show();
			}		
		}
	    return success;
	}
}