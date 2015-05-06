package com.encinitaslabs.wirfid.hpreader;

import java.nio.ByteBuffer;
import java.util.LinkedList;

import android.app.Application;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.os.SystemClock;
import android.util.Log;

public class UsbCommunication extends Application{
	public static final String TAG = "UsbControl";
	private static final boolean DEBUG = false;

	private static UsbDevice mDevice;
	private static UsbInterface mInterface;
	private static UsbDeviceConnection mDeviceConnection;
	private static UsbEndpoint mEndpointOut;
	private static UsbEndpoint mEndpointIn;
	private static UsbRequest txRequest = null;
//	private static UsbRequest rxRequest = null;
	private static boolean messageWaiting = false;
	
	public static boolean mStop;
	private static Thread mThread;
	private static final int DATA_LENGTH = 64;
	private static ByteBuffer mDataBufferIn = ByteBuffer.allocate(DATA_LENGTH);
	private static ByteBuffer mDataBufferUp = ByteBuffer.allocate(DATA_LENGTH);
	private static ByteBuffer mEmptyBuffer  = ByteBuffer.allocate(DATA_LENGTH);
//	private static final LinkedList<UsbRequest> mOutRequestPool = new LinkedList<UsbRequest>();
//	private static final LinkedList<UsbRequest> mInRequestPool = new LinkedList<UsbRequest>();

	public static boolean setUsbInterface(UsbManager manager, UsbDevice device) {
		if(device != null) {
			if(mDeviceConnection != null) {
				if(mInterface != null) {
					mDeviceConnection.releaseInterface(mInterface);
					mInterface = null;
				}
				mDeviceConnection.close();
				mDeviceConnection = null;
				mDevice = null;
			}
			
			UsbInterface intf = device.getInterface(0);
			UsbDeviceConnection connection = manager.openDevice(device);
			if(connection != null) {
				if(DEBUG) Log.d(TAG, "open succeeded");
				if(connection.claimInterface(intf, true)) {
					if(DEBUG) Log.d(TAG, "claim interface succeeded");
					mDevice = device;
					mInterface = intf;
					mDeviceConnection = connection;

					UsbEndpoint epOut = null;
					UsbEndpoint epIn = null;

					for(int i = 0; i < intf.getEndpointCount(); i++) {
						UsbEndpoint ep = intf.getEndpoint(i);
						if(ep.getDirection() == UsbConstants.USB_DIR_OUT)
							epOut = ep;
						else if (ep.getDirection() == UsbConstants.USB_DIR_IN)
							epIn = ep;
					}
					
					if(epOut == null || epIn == null) {
						Log.e(TAG, "not all endpoints found");
						throw new IllegalArgumentException("not all endpoints found.");
					}
					mEndpointOut = epOut;
					mEndpointIn = epIn;

//					txRequest = new UsbRequest();
//		   			rxRequest = new UsbRequest();
//		   			txRequest.initialize(mDeviceConnection, mEndpointOut);
//	    			rxRequest.initialize(mDeviceConnection, mEndpointIn);
					
					startThread();
					clearBuffer(1000);

					return true;
				} else {
					Log.e(TAG, "claim interface failed");
					connection.close();
				}
			} else {
				Log.e(TAG, "open failed");
			}
		} else {
			clearBuffer(1000);
			stopThread();
			mDeviceConnection = null;
			mInterface = null;
			mDevice = null;
		}
		return false;
	}
	
	public static void clearBuffer(int ms) {
		SystemClock.sleep(ms);
		mDataBufferIn.clear();
		mDataBufferUp.clear();
		mEmptyBuffer.clear();
	}

	public static UsbDevice getUsbDevice() {
		return mDevice;
	}

	public synchronized static boolean sendCmd(byte[] message, int length) {
		boolean bStatus = false;
		if (mDevice != null) {
			synchronized(mDevice) {
				txRequest = new UsbRequest();
	   			txRequest.initialize(mDeviceConnection, mEndpointOut);
	   			bStatus = txRequest.queue(ByteBuffer.wrap(message), length);
	   			if(DEBUG) Log.d(TAG, "sendCmd");
			}
		}
		return bStatus;
	}
	

	public static byte[] getResponse() {
   		if (messageWaiting == true) {
   			mDataBufferUp = mDataBufferIn;
   			messageWaiting = false;
   			if(DEBUG) Log.d(TAG, "getResponse(true)");
   			return mDataBufferUp.array();
   		} else {
   			if(DEBUG) Log.d(TAG, "getResponse(null)");
   			return null;
   		}
	}
	
	
	public static Thread startThread() {
		if(DEBUG) Log.d(TAG, "startThread");
		mStop = false;

		mThread = new Thread(new NewRunnable());
		mThread.start();

		return mThread;
	}

	public static void stopThread() {
		if(DEBUG) Log.d(TAG, "stopThread");
		mStop = true;
		/*
		try {
			mThread.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
	}
	
	private static class NewRunnable implements Runnable {
		@Override
		public void run() {
			while(true) {
		        synchronized(this) {
					if (mStop) {
						if(DEBUG) Log.d(TAG, "Stop Thread");
						return;
					}
		        }
				// mDeviceConnection could become null if the USB is going up and down
		        if ((mDeviceConnection != null) && (messageWaiting == false)) {
		        	if(DEBUG) Log.d(TAG, "queuing another receive buffer");
	                // URB for the incoming data
	                UsbRequest inRequest = new UsbRequest(); 
	                inRequest.initialize(mDeviceConnection, mEndpointIn);
	                // the direction is dictated by this initialization to the incoming End-point.
	                if (inRequest.queue(mDataBufferIn, DATA_LENGTH) == true) {
	    		        // Wait for 10 ms
//	    				SystemClock.sleep(10);
	    				try {
		                	if (mDeviceConnection.requestWait() != null) {
		                		if(DEBUG) Log.d(TAG, "something received");
			                    // wait for this request to be completed
			                    // at this point buffer contains the data received
			                	messageWaiting = true;	                		
		                	}
	    				} catch (NullPointerException e) {
	    					// if we are here, its likely the USB connection went down
	    				}
	                }
		        }
			}
		}
	}
}