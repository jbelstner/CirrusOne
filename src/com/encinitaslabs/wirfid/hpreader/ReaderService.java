package com.encinitaslabs.wirfid.hpreader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class ReaderService extends Service {
    private static final String TAG = "ReaderService";
	private static final String ACTION_USB_PERMISSION = "com.encinitaslabs.wirfid.hpreader.USB_PERMISSION";
    private static final boolean D = true;
	private static final int[] PID = {0x824, 0x861};
	private static final int VID = 0x24e9;
	private static final int CAMERA_PID = 0x6860;
	private static final int CAMERA_VID = 0x4e8;
    
    public static final String USB_CONNECTED = "USB_CONNECTED";
	public static final String USB_DISCONNECTED = "USB_DISCONNECTED";
	public static final String TIMER_TIC = "Periodic Timer Expired";
	public static final String WEBSOCKET_NOT_CONNECTED = "WebSocket not connected";
	public static final String WEBSOCKET_CLOSED = "WebSocket closed";
	public static final String WEBSOCKET_OPENED = "WebSocket opened";
	public static final int ticTimerInMilliseconds = 250;
    // Message types sent to and from a Local Socket Controller
	public static final String READER_TO_CONTROLLER_SOCKET_ADDRESS = "reader.to.controller.address";
	public static final String CONTROLLER_TO_READER_SOCKET_ADDRESS = "controller.to.reader.address";
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
	// Default values
	public static final int defaultGpioUpdateInterval_sec = 1;
	public static final int defaultPowerLevel_dBm = 30; // RU861
	public static final int defaultQValue = 7;
	public static final int defaultWebsocketReconnectDelay_sec = 10;
	public static final int defaultPowerOnDelay_sec = 5;
	public static final int defaultBluetoothNotConnectedTimeout_sec = 180;
	public static final String defaultAntennaPort = "0";
	public static final String defaultScanTime_ms = "1000";
	public static final String defaultNumberInventoryCycles = "0";
	public static final String defaultSession = "S2";
	public static final String defaultTarget = "A";
	public static final String defaultRetryCount = "0";
	public static final String defaultIntervalDelay_ms = "1000";
	public static final String defaultPingDelay_sec = "30";
	public static final String defaultPrefixUrl = "";
//	public static final String defaultPrefixUrl = "http://www.rfidentertainment.com/samplegame/?location=test&answer=1&tag=";
//	public static final String defaultPrefixUrl = "http://hhn23experience.com/rfid/test?location=walkingdead&tag=";
	public static final String defaultWebSocketUrl = "";
//	public static final String defaultWebSocketUrl = "ws://192.168.1.72:8887";
	public static final boolean defaultRepeatUntilNoTags = false;
	public static final boolean defaultBluetoothControl = false;
	public static final boolean defaultLocalSocketControl = true;
	public static final boolean defaultScanAutostart = true;
	public static enum ReaderState { IDLE, UPDATING, READING, FINISHED, WAITING };  
	public static enum WebSocketState { CLOSED, OPENING, OPEN };  
	public static enum BluetoothState { CONNECTED, CONNECTING, LISTEN, NONE };  

	private WebSocketClient cc = null;
	private SharedPreferences mSharedpref = null;

	private int antennaPort = Integer.parseInt(defaultAntennaPort);
	private int powerLevel_dBm = defaultPowerLevel_dBm;
	private int qValue = defaultQValue;
	private String scanTime_ms = defaultScanTime_ms;
	private String numberInventoryCycles = defaultNumberInventoryCycles;
	private String session = defaultSession;
	private String target = defaultTarget;
	private String retryCount = defaultRetryCount;
	private String prefixUrl = defaultPrefixUrl;
	private String webSocketUrl = defaultWebSocketUrl;
	private boolean repeatUntilNoTags = defaultRepeatUntilNoTags;
	private boolean scanAutostart = defaultScanAutostart;
    // Local Socket and Bluetooth variables
    private String mConnectedDeviceName = null;
    private String mCirrusOneDeviceName = "CirrusOne";
    private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothState bluetoothState = BluetoothState.NONE;
    private BluetoothChatService mChatService = null;
	private boolean bluetoothControl = defaultBluetoothControl;
	private boolean localSocketControl = defaultLocalSocketControl;
	private boolean localSocketListenerAttached = false;
	private int bluetoothNotConnectedTimeout = defaultBluetoothNotConnectedTimeout_sec * (1000 / ticTimerInMilliseconds);

	private int gpioUpdateDelay = defaultGpioUpdateInterval_sec * (1000 / ticTimerInMilliseconds);
	private int pingDelay = Integer.parseInt(defaultPingDelay_sec) * (1000 / ticTimerInMilliseconds);
	private int intervalDelay = Integer.parseInt(defaultIntervalDelay_ms) / ticTimerInMilliseconds;
	private int websocketReconnectDelay = defaultWebsocketReconnectDelay_sec * (1000 / ticTimerInMilliseconds);
	private int powerOnDelay = defaultPowerOnDelay_sec * (1000 / ticTimerInMilliseconds);
	private ReaderState readerState = ReaderState.IDLE;
	private WebSocketState webSocketState = WebSocketState.CLOSED;
	
	private int gpioUpdateCounter = 0;
	private int ticTimerCounter = 0;
	private int scanCounter = 0;
	private int pingCounter = 0;
	private int scanIntervalCounter = 0;
	private int websocketReconnectCounter = 0;
	private int bluetoothNotConnectedCounter = 0;
	private int consecutiveReadErrorCounter = 0;
	private int consecutiveReadErrorCounterThreshold = 3;
	private int powerOnCounter = powerOnDelay;
	private int maximumMissedTags = 1048575;
	private int missedLocalSocketKeepAliveMessages = 0;
	private int maxMissedLocalSocketKeepAliveMessages = 3;
	private boolean localSocketKeepAliveMessageReceived = false;
	private boolean readerParameterChanged = false;
	private boolean debugMessages = false;
	private boolean readerBusy = false;
	private boolean gpioConfigured = false;
	private byte lastGpioState = 0x00;
	private boolean gpio3State = false;
	private boolean fileInterfaceActive = false;
	private boolean restartBluetoothServices = false;
	// USB local handling
	private UsbManager mManager;
	private PendingIntent mPermissionIntent;
	public static boolean usbState = false;
	public static int devicePid = 0;
	// USB camera connection
	private UsbDevice mCameraDevice;
	private UsbInterface mCameraInterface;
	private UsbDeviceConnection mCameraDeviceConnection;
	private UsbEndpoint mCameraEndpointOut;
	private UsbEndpoint mCameraEndpointIn;
	public static boolean cameraConnected = false;
	// Status icon stuff
	private NotificationManager mNotificationManager = null;
	private static int mID = 6969;


	public static ArrayList<String> tagList = new ArrayList<String>();
	public static ArrayList<String> missedTagList = new ArrayList<String>();

    // The Handler that gets information back from the BluetoothChatService
    private final Handler btHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothChatService.STATE_CONNECTED:
                	bluetoothState = BluetoothState.CONNECTED;
                    break;
                case BluetoothChatService.STATE_CONNECTING:
                	bluetoothState = BluetoothState.CONNECTING;
                    break;
                case BluetoothChatService.STATE_LISTEN:
                	bluetoothState = BluetoothState.LISTEN;
                	break;
                case BluetoothChatService.STATE_NONE:
                	bluetoothState = BluetoothState.NONE;
                    break;
                }
                break;
            case MESSAGE_WRITE:
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                processReaderCommand(readMessage);
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                sendToWebSocket("Bluetooth: Connected to " + mConnectedDeviceName);
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                sendToWebSocket("Bluetooth: " + msg.getData().getString(TOAST));
                break;
            }
        }
    };

    private Handler timerHandler = new Handler();    
    private Runnable timerRunnable = new Runnable() {
    	@Override
    	public void run() {
    		/* do what you need to do */
    		notificationRunnable.setMessage(TIMER_TIC);
    		handler.post(notificationRunnable);
    		/* and here comes the "trick" */
    		timerHandler.postDelayed(this, ticTimerInMilliseconds);
    	}
    };
    
	// background threads use this Handler to post messages to
	// the main application thread
	private final Handler handler = new Handler();

	public class NotificationRunnable implements Runnable {
		private String message = null;
		
		public void run() {
			if (message != null && message.length() > 0) {
				showNotification(message);
			}
		}
		/**
		 * @param message the message to set
		 */
		public void setMessage(String message) {
			this.message = message;
		}
	}
	// post this to the Handler when the background thread notifies
	private final NotificationRunnable notificationRunnable = new NotificationRunnable();

	public void showNotification(String message) {
		
		if (message.startsWith("command=")) {
			String st[] = message.split("command=");
			if (st.length == 2) {
				processReaderCommand(st[1]);
			}
		}
		else if (message.startsWith("EPC=")) {
			String st[] = message.split("EPC=");
			if (st.length == 2) {
				tagList.add(st[1]);
				sendToRemote("EPC " + st[1], false);
			}
		}
		else if (message.startsWith("TAGS=")) {
			String st[] = message.split("TAGS=");
			if (st.length == 2) {
				tagList.add(st[1]);
				sendToRemote("Tags Read = " + st[1], false);
			}
		}
		else if (message.startsWith("missed=")) {
			String st[] = message.split("missed=");
			if (st.length == 2) {
				storeDataLocally(st[1]);
			}
		}
		else if (message.contains("scanError=")) {
			String st[] = message.split("scanError=");
			if (st.length == 2) {
//				sendToRemote("Scan ERROR " + st[1], false);
				sendToWebSocket("USB ERROR " + st[1]);
			} else {
//				sendToRemote("Scan ERROR", false);
				sendToWebSocket("USB ERROR unknown");
			}
			if (++consecutiveReadErrorCounter > consecutiveReadErrorCounterThreshold) {
				scanCounter = 0;
				consecutiveReadErrorCounter = 0;
				readerReset(true);
				powerOnCounter = powerOnDelay;
			}
		}
		else if (message.contains(TIMER_TIC)) {
			processTicTimer();
		}
		else if (message.contains(USB_DISCONNECTED)) {
			sendToRemote("USB Reader Disconnected", true);
		}
		else if (message.contains(WEBSOCKET_NOT_CONNECTED)) {
			if (webSocketState == WebSocketState.OPEN) {
				webSocketState = WebSocketState.CLOSED;
				websocketReconnectCounter = websocketReconnectDelay;
				Toast.makeText(this, WEBSOCKET_NOT_CONNECTED, Toast.LENGTH_SHORT).show();					
			}
		}
		else if (message.contains(WEBSOCKET_CLOSED)) {
			if (webSocketState == WebSocketState.OPEN) {
				webSocketState = WebSocketState.CLOSED;
				websocketReconnectCounter = websocketReconnectDelay;
				Toast.makeText(this, WEBSOCKET_CLOSED, Toast.LENGTH_SHORT).show();
			}
		}
		else if (message.contains(WEBSOCKET_OPENED)) {
			webSocketState = WebSocketState.OPENING;
			websocketReconnectCounter = websocketReconnectDelay;
			Toast.makeText(this, WEBSOCKET_OPENED, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * A simple LocalSocket implementation.
	 */
	class SocketListener extends Thread {
		private Handler handler = null;
		private NotificationRunnable runnable = null;

		public SocketListener(Handler handler, NotificationRunnable runnable) {
			this.handler = handler;
			this.runnable = runnable;
			this.handler.post(this.runnable);
		}

		/**
		 * Show UI notification.
		 * @param message
		 */
		private void showMessage(String message) {
			this.runnable.setMessage(message);
			this.handler.post(this.runnable);
		}

		@Override
		public void run() {
			// showMessage("DEMO: SocketListener started!");
			try {
				LocalServerSocket server = new LocalServerSocket(CONTROLLER_TO_READER_SOCKET_ADDRESS);
				while (true) {
					LocalSocket receiver = server.accept();
					if (receiver != null) {
						InputStream input = receiver.getInputStream();
						// simply for java.util.ArrayList
						int readed = input.read();
						int size = 0;
						int capacity = 0;
						byte[] bytes = new byte[capacity];

						// reading
						while (readed != -1) {
							// java.util.ArrayList.Add(E e);
							capacity = (capacity * 3)/2 + 1;
							//bytes = Arrays.copyOf(bytes, capacity);
							byte[] copy = new byte[capacity];
							System.arraycopy(bytes, 0, copy, 0, bytes.length);
							bytes = copy;
							bytes[size++] = (byte)readed;

							// read next byte
							readed = input.read();
						}

						showMessage("command=" + new String(bytes, 0, size));
					}
				}
			} catch (IOException e) {
				Log.e(getClass().getName(), e.getMessage());
			}
		}
	}
	    
	/**
	 * A simple WebSocketClient implementation.
	 */
	public class MyClient extends WebSocketClient {

		private Handler handler = null;
		private NotificationRunnable runnable = null;
		
		public MyClient( URI serverUri , Draft draft ) {
			super( serverUri, draft );
		}

		public MyClient( URI serverURI, Handler handler, NotificationRunnable runnable ) {
			super( serverURI );
			this.handler = handler;
			this.runnable = runnable;
			this.handler.post(this.runnable);
		}

		@Override
		public void onOpen( ServerHandshake handshakedata ) {
			showMessage(WEBSOCKET_OPENED);
		}

		@Override
		public void onMessage( String message ) {
			showMessage("command=" + message);
		}

		@Override
		public void onClose( int code, String reason, boolean remote ) {
			showMessage(WEBSOCKET_CLOSED);
		}

		@Override
		public void send(String message) {
			try {
				super.send(message);
			} catch (WebsocketNotConnectedException e) {
				showMessage(WEBSOCKET_NOT_CONNECTED);
//				Toast.makeText(this, "WebsocketNotConnectedException", Toast.LENGTH_SHORT).show();	
			} catch (IllegalArgumentException e) {
//				Toast.makeText(this, "IllegalArgumentException", Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onError( Exception ex ) {
			ex.printStackTrace();
			// if the error is fatal then onClose will be called additionally
		}
		
		private void showMessage(String message) {
			this.runnable.setMessage(message);
			this.handler.post(this.runnable);
		}	    
	}

	public void scanForTag() {
		if (usbState) {
			// Send a command to scan for a tag
			new Thread() {
				public void run() {
					ArrayList<String> dupList = new ArrayList<String>();
					dupList.clear();
					String tagId;
					int i = 0; 
					int tagCount = 0; 
					readerBusy = true;
					int status = MtiCmd.RFID_ERROR_NO_RESPONSE;
					CmdTagProtocol.RFID_18K6CTagInventory mMtiCmd = new CmdTagProtocol.RFID_18K6CTagInventory();
					try {
						status = mMtiCmd.setCmd(CmdTagProtocol.PerformSelect.NoSelectCmd,
												CmdTagProtocol.PerformPostMatch.NoPostSigulationMatchCmd,
												CmdTagProtocol.PerformGuardMode.RealtimeMode);
					} catch (NullPointerException e) {
						// Something bad happened here, likely USB failure
					}
					if (status == MtiCmd.RFID_STATUS_OK) {
						while ((mMtiCmd.checkEnd(50) != 0x00) && (i < 500)) {
							if (((tagId = mMtiCmd.getTagIds()) != null) && !dupList.contains(tagId)) {
//							if ((tagId = mMtiCmd.getTagIds()) != null) {
					            notificationRunnable.setMessage("EPC=" + tagId);
								handler.post(notificationRunnable);
								dupList.add(tagId);
//								tagCount++;
							}
							i++;
						}
						consecutiveReadErrorCounter = 0;
//			            notificationRunnable.setMessage("TAGS=" + Integer.toString(tagCount));
//						handler.post(notificationRunnable);
					} else {
			            notificationRunnable.setMessage("scanError=" + Integer.toString(status));
						handler.post(notificationRunnable);
					}
					readerBusy = false;
				}
	 		}.start();
		} else {
            notificationRunnable.setMessage(USB_DISCONNECTED);
			handler.post(notificationRunnable);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

        WebSocketImpl.DEBUG = false;
        // Allow posting to Web URL from inside Service
        if (Build.VERSION.SDK_INT >= 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy); 
        }

        // Initialize USB services
        mManager = (UsbManager)getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        // Register for USB actions
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(ACTION_USB_PERMISSION);
        registerReceiver(usbReceiver, filter);

        pingCounter = pingDelay;
		tagList.clear();
		missedTagList.clear();

		loadSharedPreferences();
		startBluetoothServices();
		initFileInterface();
		
		// Delay connection to the WebSocket
		websocketReconnectCounter = websocketReconnectDelay;

        // Start the tic timer
	    timerHandler.postDelayed(timerRunnable, ticTimerInMilliseconds);

        // Check if LocalSocket control is enabled
        if (localSocketControl == true) {
    		new SocketListener(this.handler, this.notificationRunnable).start();
			Toast.makeText(this, "LocalSocket Control Enabled", Toast.LENGTH_SHORT).show();
			// assume true for now
			localSocketListenerAttached = true;
        }

        // Check for USB permissions on all connected devices
		int vid = 0;
		int pid = 0;
		HashMap<String, UsbDevice> deviceList = mManager.getDeviceList();
		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		while (deviceIterator.hasNext()) {
			UsbDevice device = deviceIterator.next();
			vid = device.getVendorId();
			pid = device.getProductId();

			// Only allow RFID USB Device
//			if (mManager.hasPermission(device) == false) {
//				mManager.requestPermission(device, mPermissionIntent);
//				Toast.makeText(this, "Request permission USB device VID/PID = " + Integer.toString(vid) + "/" + Integer.toString(pid), Toast.LENGTH_SHORT).show();
//			} else {
				// Check if this device was RFID, Galaxy Camera or something else
				if (vid == VID) {
					Toast.makeText(this, "RFID USB device connected", Toast.LENGTH_SHORT).show();
					UsbCommunication.setUsbInterface(mManager, device);
					devicePid = pid;
					usbState = true;
				} else if ((vid == CAMERA_VID) && (pid == CAMERA_PID)) {
					Toast.makeText(this, "Galaxy Camera USB device connected", Toast.LENGTH_SHORT).show();
					setCameraUsbInterface(mManager, device);
					cameraConnected = true;
				} else {
					Toast.makeText(this, "USB device connected VID/PID = " + Integer.toString(vid) + "/" + Integer.toString(pid), Toast.LENGTH_SHORT).show();
				}
//			}
		}
		
//		updateReaderParameters();
//		configureGPIO();
		putIconInStatusBar();

		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
        removeIconFromStatusBar();
		writeToLogFile("COA ReaderService exiting");
        super.onDestroy();
        // Close the WebSocket
		if ((cc != null) && (cc.getConnection() != null)) {
	        cc.close();
		}
        // Stop the Bluetooth chat services
        if (mChatService != null) {
        	mChatService.stop();
			Toast.makeText(this, "Stopping Bluetooth Service", Toast.LENGTH_SHORT).show();
        }
        // Close the USB connections
		UsbCommunication.setUsbInterface(null, null);
		// Unregister the event listeners
		unregisterReceiver(usbReceiver);
	}
	
	private void processReaderCommand(String message) {
		int parsedValue = 0;

		if (message.contains("scanCount")) {
			String st[] = message.split(" ");
			if (st.length == 2) {
				scanCounter = Short.parseShort(st[1]);
				if (scanCounter > 999) {
					st[1] = "continuous";
				}
				else if (scanCounter == 0) {
					st[1] = "stop scanning";
				}
				sendToRemote("ACK scanCounter " + st[1], false);
			}
		}
		else if (message.contains("antennaPort")) {
			String st[] = message.split(" ");
			if (st.length == 2) {
				parsedValue = Integer.parseInt(st[1]);
				if ((antennaPort >= 0) && (antennaPort <= 1)) {
					Editor editor = mSharedpref.edit();
				    editor.putString("cfg_antenna_port", st[1]);
				    editor.commit();
					antennaPort = parsedValue;
					readerParameterChanged = true;
					sendToRemote("ACK antennaPort " + st[1], false);
				}
				else {
					sendToRemote("NACK antennaPort", false);
				}
			}
		}
		else if (message.contains("powerLevel")) {
			String st[] = message.split(" ");
			if (st.length == 2) {
				parsedValue = Integer.parseInt(st[1]);
				if ((parsedValue >= 0) && (parsedValue <= 30)) {
					Editor editor = mSharedpref.edit();
					editor.putInt("cfg_pwr_level", parsedValue);
				    editor.commit();
					powerLevel_dBm = parsedValue;
					readerParameterChanged = true;
					sendToRemote("ACK powerLevel " + st[1] + " dBm", false);
				}
				else {
					sendToRemote("NACK powerLevel", false);
				}
			}
		}
		else if (message.contains("qValue")) {
			String st[] = message.split(" ");
			if (st.length == 2) {
				parsedValue = Integer.parseInt(st[1]);
				if ((parsedValue >= 0) && (parsedValue <= 15)) {
					Editor editor = mSharedpref.edit();
					editor.putInt("cfg_q_value", parsedValue);
				    editor.commit();
				    qValue = parsedValue;
					readerParameterChanged = true;
					sendToRemote("ACK qValue " + st[1], false);
				}
				else {
					sendToRemote("NACK qValue", false);
				}
			}
		}
		else if (message.contains("intervalDelay")) {
			String st[] = message.split(" ");
			if (st.length == 2) {
				parsedValue = Integer.parseInt(st[1]);
				if ((parsedValue >= 0) && (parsedValue <= 10000)) {
					intervalDelay = parsedValue / ticTimerInMilliseconds;
					readerParameterChanged = true;
					// write the new value to Shared Preferences
					Editor editor = mSharedpref.edit();
				    editor.putString("cfg_interval_delay", st[1]);
				    editor.commit();
					sendToRemote("ACK intervalDelay " + st[1] + " milliseconds", false);
				} else {
					sendToRemote("NACK intervalDelay " + st[1] + " milliseconds out of range", false);
				}
			}
		}
		else if (message.contains("scanTime")) {
			String st[] = message.split(" ");
			if (st.length == 2) {
				parsedValue = Integer.parseInt(st[1]);
				if ((parsedValue >= 0) && (parsedValue <= 10000)) {
					scanTime_ms = st[1];
					readerParameterChanged = true;
					// write the new value to Shared Preferences
					Editor editor = mSharedpref.edit();
				    editor.putString("cfg_dwell_time", st[1]);
				    editor.commit();
					sendToRemote("ACK scanTime " + st[1] + " milliseconds", false);
				} else {
					sendToRemote("NACK scanTime " + st[1] + " milliseconds out of range", false);
				}
			}
		}
		else if (message.contains("invCycles")) {
			String st[] = message.split(" ");
			if (st.length == 2) {
				numberInventoryCycles = st[1];
				readerParameterChanged = true;
				// write the new value to Shared Preferences
				Editor editor = mSharedpref.edit();
			    editor.putString("cfg_number_inventory_cycles", st[1]);
			    editor.commit();
				sendToRemote("ACK numberInventoryCycles " + st[1], false);
			}
		}
		else if (message.contains("session")) {
			String st[] = message.split(" S");
			if (st.length == 2) {
				parsedValue = Integer.parseInt(st[1]);
				if ((parsedValue >= 0) && (parsedValue <= 4)) {
					session = "S" + Integer.toString(parsedValue);
					readerParameterChanged = true;
					// write the new value to Shared Preferences
					Editor editor = mSharedpref.edit();
				    editor.putString("cfg_session", session);
				    editor.commit();
					sendToRemote("ACK session S" + st[1], false);
				}
				else {
					sendToRemote("NACK session", false);					
				}
			}
		}
		else if (message.contains("target")) {
			String st[] = message.split(" ");
			if (st.length == 2) {
				if ((st[1].contains("A")) || (st[1].contains("B"))) {
					target = st[1];
					readerParameterChanged = true;
					// write the new value to Shared Preferences
					Editor editor = mSharedpref.edit();
				    editor.putString("cfg_target", target);
				    editor.commit();
					sendToRemote("ACK target " + st[1], false);
				}
				else {
					sendToRemote("NACK target", false);					
				}
			}
		}
		else if (message.contains("retryCount")) {
			String st[] = message.split(" ");
			if (st.length == 2) {
				parsedValue = Integer.parseInt(st[1]);
				if ((parsedValue >= 0) && (parsedValue <= 4)) {
					retryCount = Integer.toString(parsedValue);
					readerParameterChanged = true;
					// write the new value to Shared Preferences
					Editor editor = mSharedpref.edit();
				    editor.putString("cfg_fixed_retry_count", retryCount);
				    editor.commit();
					sendToRemote("ACK retryCount " + st[1], false);
				}
				else {
					sendToRemote("NACK retryCount", false);					
				}
			}
		}
		else if (message.contains("repeatUntilNoTags")) {
			String st[] = message.split(" ");
			if (st.length == 2) {
				if (st[1].contains("true")) {
					repeatUntilNoTags = true;
					readerParameterChanged = true;
					// write the new value to Shared Preferences
					Editor editor = mSharedpref.edit();
				    editor.putBoolean("cfg_repeat_until_no_tages", repeatUntilNoTags);
				    editor.commit();
					sendToRemote("ACK repeatUntilNoTags " + st[1], false);
				}
				else if (st[1].contains("false")) {
					repeatUntilNoTags = false;
					readerParameterChanged = true;
					// write the new value to Shared Preferences
					Editor editor = mSharedpref.edit();
				    editor.putBoolean("cfg_repeat_until_no_tages", repeatUntilNoTags);
				    editor.commit();
					sendToRemote("ACK repeatUntilNoTags " + st[1], false);
				}
				else {
					sendToRemote("NACK repeatUntilNoTags", false);					
				}
			}
		}
		else if (message.contains("pingDelay")) {
			String st[] = message.split(" ");
			if (st.length == 2) {
				parsedValue = Integer.parseInt(st[1]);
				if (parsedValue > 1) {
					Editor editor = mSharedpref.edit();
				    editor.putString("cfg_ping_delay", st[1]);
				    editor.commit();
					pingDelay = parsedValue * (1000 / ticTimerInMilliseconds);
					sendToRemote("ACK pingDelay " + st[1] + " seconds", false);
				}
				else {
					sendToRemote("NACK pingDelay", false);
				}
			}
		}
		else if (message.contains("prefixUrl")) {
			String st[] = message.split(" ");
			if (st.length == 2) {
				prefixUrl = st[1];
				// write the new value to Shared Preferences
				Editor editor = mSharedpref.edit();
			    editor.putString("cfg_web_url", st[1]);
			    editor.commit();
				sendToRemote("ACK prefixUrl " + st[1], false);
			}
		}
		else if (message.contains("webSocketUrl")) {
			String st[] = message.split(" ");
			if (st.length == 2) {
				webSocketUrl = st[1];
				// write the new value to Shared Preferences
				Editor editor = mSharedpref.edit();
			    editor.putString("cfg_wss_url", st[1]);
			    editor.commit();
				sendToRemote("ACK webSocketUrl " + st[1], false);
			}
		}
		else if (message.contains("bluetoothControl")) {
			String st[] = message.split(" ");
			if (st.length == 2) {
				if (st[1].contains("true")) {
					bluetoothControl = true;
					// write the new value to Shared Preferences
					Editor editor = mSharedpref.edit();
				    editor.putBoolean("cfg_bluetooth_control", bluetoothControl);
				    editor.commit();
					sendToRemote("ACK bluetoothControl " + st[1], true);
				}
				else if (st[1].contains("false")) {
					bluetoothControl = false;
					// write the new value to Shared Preferences
					Editor editor = mSharedpref.edit();
				    editor.putBoolean("cfg_bluetooth_control", bluetoothControl);
				    editor.commit();
					sendToRemote("ACK bluetoothControl " + st[1], false);
				}
				else {
					sendToRemote("NACK bluetoothControl", false);					
				}
			}
		}
		else if (message.contains("localSocketControl")) {
			String st[] = message.split(" ");
			if (st.length == 2) {
				if (st[1].contains("true")) {
					localSocketControl = true;
					// write the new value to Shared Preferences
					Editor editor = mSharedpref.edit();
				    editor.putBoolean("cfg_localsocket_control", localSocketControl);
				    editor.commit();
					sendToRemote("ACK localSocketControl " + st[1], true);
				}
				else if (st[1].contains("false")) {
					localSocketControl = false;
					// write the new value to Shared Preferences
					Editor editor = mSharedpref.edit();
				    editor.putBoolean("cfg_localsocket_control", localSocketControl);
				    editor.commit();
					sendToRemote("ACK localSocketControl " + st[1], false);
				}
				else {
					sendToRemote("NACK localSocketControl", false);					
				}
			}
		}
		else if (message.contains("scanAutostart")) {
			String st[] = message.split(" ");
			if (st.length == 2) {
				if (st[1].contains("true")) {
					scanAutostart = true;
					// write the new value to Shared Preferences
					Editor editor = mSharedpref.edit();
				    editor.putBoolean("cfg_scan_autostart", scanAutostart);
				    editor.commit();
					sendToRemote("ACK scanAutostart " + st[1], false);
					scanCounter = 1000; //continuous scan				
				}
				else if (st[1].contains("false")) {
					scanAutostart = false;
					// write the new value to Shared Preferences
					Editor editor = mSharedpref.edit();
				    editor.putBoolean("cfg_scan_autostart", scanAutostart);
				    editor.commit();
					sendToRemote("ACK scanAutostart " + st[1], false);
					scanCounter = 0; //stop scan				
				}
				else {
					sendToRemote("NACK scanAutostart", false);					
				}
			}
		}
		else if (message.contains("useDefaults")) {
			sendToRemote("ACK useDefaults", false);
			restoreDefaults();
		}
		else if (message.contains("showSettings")) {
			sendSettings();
		}
		else if (message.contains("showSwVersion")) {
			sendToRemote("SW Version " + getString(R.string.about_sw_ver_sum), false);
		}
		else if (message.contains("showState")) {
			sendState();
		}
		else if (message.contains("resetReader")) {
			String st[] = message.split(" ");
			if (st.length == 2) {
				if (st[1].contains("true")) {
					scanCounter = 0;
					readerReset(true);
					sendToRemote("ACK resetReader " + st[1], false);
				}
				else if (st[1].contains("false")) {
					scanCounter = 0;
					consecutiveReadErrorCounter = 0;
					readerReset(false);
					powerOnCounter = powerOnDelay;
					sendToRemote("ACK resetReader " + st[1], false);
				}
				else {
					sendToRemote("NACK resetReader", false);					
				}
			}
		}
		else if (message.contains("readLogFile")) {
			sendToRemote("ACK readLogFile", false);
			readFromLogFile();
		}
		else if (message.contains("testTag")) {
			sendToRemote("EPC 30 34 2C 03 60 4A AB 80 00 01 24 95", false);
			sendToWebServer("30 34 2C 03 60 4A AB 80 00 01 24 95");
		}
		else if (message.contains("scanDebug")) {
			String st[] = message.split(" ");
			if ((st.length == 2) && (st[1].contains("true"))) {
				debugMessages = true;
				sendToRemote("ACK scanDebug true", false);
			}
			else {
				debugMessages = false;
				sendToRemote("ACK scanDebug false", false);
			}
		}
		else if (message.contains("recoverMissed")) {
			recoverMissed();
		}
		else if (message.contains("FAA") || message.contains("FFA")) {
			localSocketKeepAliveMessageReceived = true;
			missedLocalSocketKeepAliveMessages = 0;
		}
		else {
			sendToRemote("Unable to process command", false);
		}
	}

	public String getIpAddr() {
		String ipString = "";
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		
		int ip = wifiInfo.getIpAddress();
		if (ip > 0) {
			ipString = String.format("%d %d %d %d",
					(ip >> 0 & 0xff),
					(ip >> 8 & 0xff),
					(ip >> 16 & 0xff),
					(ip >> 24 & 0xff));			
		}
		return ipString;
	}

	private void attemptWebSocketConnect(String address) {
		if (address == "") {
			Toast.makeText(this, "Websocket Url not configured!", Toast.LENGTH_SHORT).show();
		}
//		else if (getIpAddr() == "") {
//			Toast.makeText(this, "Device has no IP address!", Toast.LENGTH_SHORT).show();
//		}
		else {
			try {
				cc = new MyClient( new URI( address ), this.handler, this.notificationRunnable );
				cc.connect();
				Toast.makeText(this, "Opening " + address, Toast.LENGTH_SHORT).show();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	}

	private void processTicTimer() {

		ticTimerCounter++;
		// Decrement the Counters
		if (pingCounter > 0) { pingCounter--; }
		if (scanIntervalCounter > 0) { scanIntervalCounter--; }
		if (websocketReconnectCounter > 0) { websocketReconnectCounter--; }
		if (bluetoothNotConnectedCounter > 0) { bluetoothNotConnectedCounter--; }
		if (powerOnCounter > 0) { powerOnCounter--; }
		if (gpioUpdateCounter > 0) {gpioUpdateCounter--; }

		// Power-On Processing			
		if (powerOnCounter == 0)  {
			powerOnCounter = -1;
			updateReaderParameters();
//			configureGPIO();
			if (scanAutostart == true) {
				scanCounter = 1000; //continuous scan auto-initiate				
				sendToRemote("starting continuous scan", false);
				Toast.makeText(this, "starting continuous scan", Toast.LENGTH_SHORT).show();
			}
		}
		// WebSocket Reconnect Processing
		if ((webSocketUrl != "") && (websocketReconnectCounter == 0))  {
			if (webSocketState == WebSocketState.CLOSED) {
				websocketReconnectCounter = websocketReconnectDelay;
			    attemptWebSocketConnect(webSocketUrl);
			}
			else if (webSocketState == WebSocketState.OPENING) {
				webSocketState = WebSocketState.OPEN;
				sendToRemote(mCirrusOneDeviceName, false);
			}
		}
		// Ping Processing			
		if (pingCounter == 0) {
			pingCounter = pingDelay;
			sendPing();
		}
		// Toggle GPIO3 every 30 seconds
		if (ticTimerCounter % (30 * (1000 / ticTimerInMilliseconds)) == 0) {
			if (gpio3State == true) {
				gpio3State = false;
			} else {
				gpio3State = true;
			}
		}
		// GPIO Update Processing
		if (gpioUpdateCounter == 0) {
			gpioUpdateCounter = gpioUpdateDelay;
//			updateGPIO();
		}
		// Bluetooth Not Connected Timeout Processing			
		if ((bluetoothControl == true) && (bluetoothNotConnectedCounter == 0)) {
			if (bluetoothState != BluetoothState.CONNECTED) {
				// Reset the BLuetooth Adapter and Chat Service
				resetBluetoothAdapter();
				Toast.makeText(this, "Reset Bluetooth Adapter", Toast.LENGTH_SHORT).show();
			}
			bluetoothNotConnectedCounter = bluetoothNotConnectedTimeout;
		}
		if (restartBluetoothServices == true) {
			restartBluetoothServices = false;
			startBluetoothServices();
			bluetoothNotConnectedCounter = bluetoothNotConnectedTimeout;
		}
		// Reader State Processing			
		if (scanIntervalCounter == 0)  {
			if (readerState == ReaderState.IDLE) {
				// IDLE State Processing
				if (scanCounter > 0) {
					readerState = ReaderState.UPDATING;
					sendToRemote("Reader is UPDATING", true);
					if (readerParameterChanged == true) {
						updateReaderParameters();
					}
				}
			} else if (readerState == ReaderState.UPDATING) {
				// UPDATING State Processing
				readerState = ReaderState.READING;
				sendToRemote("Reader is READING", true);
				scanForTag();
			} else if (readerState == ReaderState.READING) {
				// READING State Processing
				if (readerBusy == false) {
					readerState = ReaderState.FINISHED;
					sendToRemote("Reader is FINISHED", true);
				}
			} else if (readerState == ReaderState.FINISHED) {
				// FINISHED State Processing
				uploadTagList();
				if ((scanCounter > 0) && (scanCounter < 999)) {
					scanCounter--;
				}
				if (scanCounter == 0) {
					readerState = ReaderState.IDLE;
					sendToRemote("Reader is IDLE", true);
				} else {
					readerState = ReaderState.WAITING;
					sendToRemote("Reader is WAITING", true);
					scanIntervalCounter = intervalDelay;
				}
			} else if (readerState == ReaderState.WAITING) {
				// WAITING State Processing
				readerState = ReaderState.READING;
				sendToRemote("Reader is READING", true);
				scanForTag();
			}
		}
	}
	
	private void restoreDefaults() {
		antennaPort = Integer.parseInt(defaultAntennaPort);
		powerLevel_dBm = defaultPowerLevel_dBm;
		qValue = defaultQValue;
		scanTime_ms = defaultScanTime_ms;
		numberInventoryCycles = defaultNumberInventoryCycles;
		session = defaultSession;
		target = defaultTarget;
		retryCount = defaultRetryCount;
		repeatUntilNoTags = defaultRepeatUntilNoTags;
		prefixUrl = defaultPrefixUrl;
//		webSocketUrl = defaultWebSocketUrl;
		bluetoothControl = defaultBluetoothControl;
		intervalDelay = Integer.parseInt(defaultIntervalDelay_ms) / ticTimerInMilliseconds;
		pingDelay = Integer.parseInt(defaultPingDelay_sec) * (1000 / ticTimerInMilliseconds);
		readerParameterChanged = true;
		// write the default values to Shared Preferences
		if (mSharedpref == null) {
	        Context ctx = getApplicationContext();
	        mSharedpref = PreferenceManager.getDefaultSharedPreferences(ctx);
		}
		Editor editor = mSharedpref.edit();
	    editor.putString("cfg_antenna_port", defaultAntennaPort);
		editor.putInt("cfg_pwr_level", powerLevel_dBm);
		editor.putInt("cfg_q_value", qValue);
	    editor.putString("cfg_dwell_time", scanTime_ms);
	    editor.putString("cfg_number_inventory_cycles", numberInventoryCycles);
	    editor.putString("cfg_session", session);
	    editor.putString("cfg_target", target);
	    editor.putString("cfg_fixed_retry_count", retryCount);
	    editor.putString("cfg_interval_delay", defaultIntervalDelay_ms);
	    editor.putString("cfg_ping_delay", defaultPingDelay_sec);
	    editor.putString("cfg_web_url", prefixUrl);
//	    editor.putString("cfg_wss_url", webSocketUrl);
	    editor.putBoolean("cfg_repeat_until_no_tages", repeatUntilNoTags);
	    editor.putBoolean("cfg_bluetooth_control", bluetoothControl);
	    editor.putBoolean("cfg_localsocket_control", localSocketControl);
	    editor.commit();
	}

	private void sendSettings() {
		sendToRemote("antennaPort " + Integer.toString(antennaPort), false);
		sendToRemote("powerLevel " + Integer.toString(powerLevel_dBm) + " dBm", false);
		sendToRemote("qValue " + Integer.toString(qValue), false);
		sendToRemote("scanTime " + scanTime_ms + " ms", false);
		sendToRemote("numberInventoryCycles " + numberInventoryCycles, false);
		sendToRemote("session " + session, false);
		sendToRemote("target " + target, false);
		sendToRemote("intervalDelay " + Integer.toString(intervalDelay * ticTimerInMilliseconds) + " ms", false);
		sendToRemote("pingDelay " + Integer.toString(pingDelay / (1000 / ticTimerInMilliseconds)) + " seconds", false);
		sendToRemote("prefixUrl " + prefixUrl, false);
		sendToRemote("webSocketUrl " + webSocketUrl, false);
		if (bluetoothControl == true) {
			sendToRemote("bluetoothControl true", false);			
		} else {
			sendToRemote("bluetoothControl false", false);			
		}
		if (scanAutostart == true) {
			sendToRemote("scanAutostart true", false);			
		} else {
			sendToRemote("scanAutostart false", false);			
		}
		sendToRemote("SW Version " + getString(R.string.about_sw_ver_sum), false);
	}

	private void sendState() {
		sendToRemote("scanCount " + Integer.toString(scanCounter), false);
		sendToRemote("pingCounter " + Integer.toString(pingCounter), false);
		sendToRemote("scanIntervalCounter " + Integer.toString(scanIntervalCounter), false);
		sendToRemote("websocketReconnectCounter " + Integer.toString(websocketReconnectCounter), false);
		if (readerBusy == true) {
			sendToRemote("readerBusy true", false);			
		} else {
			sendToRemote("readerBusy false", false);			
		}
		if (readerState == ReaderState.IDLE) {
			sendToRemote("readerState IDLE", false);			
		}
		else if (readerState == ReaderState.READING) {
			sendToRemote("readerState READING", false);
		}
		else if (readerState == ReaderState.FINISHED) {
			sendToRemote("readerState FINISHED", false);
		}
		else if (readerState == ReaderState.WAITING) {
			sendToRemote("readerState WAITING", false);
		}
		sendToRemote("missedTagCount " + Integer.toString(missedTagList.size()), false);
	}

	private void recoverMissed() {
		int numberOfMissedTags = missedTagList.size();
		if (numberOfMissedTags == 0) {
			sendToRemote("No missed tags", false);
		}
		else {
			for (int i = 0; i < numberOfMissedTags; i++) {
				String tagWithTimeStamp = missedTagList.get(i);
				String st[] = tagWithTimeStamp.split(" at ");
				sendToRemote(tagWithTimeStamp, false);
				// Send only the tagId to the web server
				sendToWebServerASYNC(st[0]);
			}
			missedTagList.clear();
		}
	}

	private void uploadTagList() {
		for (int i = 0; i < tagList.size(); i++) {
//			sendToRemote("EPC " + tagList.get(i), false);
			sendToWebServerASYNC(tagList.get(i));
		}
		tagList.clear();
	}

	private void storeDataLocally(String tagId) {
		if (missedTagList.size() < maximumMissedTags) {
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
			Date date = new Date();
			missedTagList.add(tagId + " at " + dateFormat.format(date));			
		}
	}

	private void sendToRemote(String message, boolean forDebug) {

		if ((forDebug == true) && (debugMessages == false)) { return; }
		
		if (webSocketUrl != "") {
			sendToWebSocket(message);			
		}
		if (mChatService != null) {
			sendToBluetooth(message);
		}
		if (localSocketControl == true) {
			try {
				sendToLocalSocket(message);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (cameraConnected == true) {
			if (message.startsWith("EPC")) {
				sendToCameraDevice(message);
			}
		}
		// Pause briefly
		SystemClock.sleep(50);
	}

	public void sendToWebSocket(String message) {
        // Check that we're actually connected before trying anything
		if (webSocketState == WebSocketState.OPEN) {
	        cc.send(message);			
		}
	}

    private class sendToWebSocketASYNC extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
        	String msg = urls[0];
        	
    		if (webSocketState == WebSocketState.OPEN) {
    	        cc.send(msg);			
    		}
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
 
        }
    }

    private void sendToBluetooth(String message) {
        // Check that we're actually connected before trying anything
    	if (mChatService != null) {
            if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
                // Check that there's actually something to send
                if (message.length() > 0) {
                    // Get the message bytes and tell the BluetoothChatService to write
                    byte[] send = message.getBytes();
                    mChatService.write(send);
                }
            }
    	} else {
            Toast.makeText(this, "Bluetooth not connected!", Toast.LENGTH_SHORT).show();    		
    	}
    }

	public void sendToLocalSocket(String message) throws IOException {
		LocalSocket sender = new LocalSocket();
		try {
			sender.connect(new LocalSocketAddress(READER_TO_CONTROLLER_SOCKET_ADDRESS));
			sender.getOutputStream().write(message.getBytes());
			sender.getOutputStream().close();
			sender.close();
			if (localSocketListenerAttached == false) {
				localSocketListenerAttached = true;
	            Toast.makeText(this, "LocalSocket listener attached", Toast.LENGTH_SHORT).show();    		
				writeToLogFile("sendToLocalSocket: LocalSocket listener attached");
			}
		} catch (IOException e) {
			if (localSocketListenerAttached == true) {
				localSocketListenerAttached = false;
	            Toast.makeText(this, "No LocalSocket listener!", Toast.LENGTH_SHORT).show();    		
				writeToLogFile("sendToLocalSocket: No LocalSocket listener!");
			}
		}
	}
	    
	private void loadSharedPreferences() {
		// Load local variables from shared preferences
        Context ctx = getApplicationContext();
        mSharedpref = PreferenceManager.getDefaultSharedPreferences(ctx);
	    powerLevel_dBm = mSharedpref.getInt("cfg_pwr_level", defaultPowerLevel_dBm);
		qValue = mSharedpref.getInt("cfg_q_value", defaultQValue);
		scanTime_ms = mSharedpref.getString("cfg_dwell_time", defaultScanTime_ms);
		numberInventoryCycles = mSharedpref.getString("cfg_number_inventory_cycles", defaultNumberInventoryCycles);
		antennaPort = Integer.parseInt(mSharedpref.getString("cfg_antenna_port", defaultAntennaPort));
		session = mSharedpref.getString("cfg_session", defaultSession);
		target = mSharedpref.getString("cfg_target", defaultTarget);
	    retryCount = mSharedpref.getString("cfg_fixed_retry_count", defaultRetryCount);
	    repeatUntilNoTags = mSharedpref.getBoolean("cfg_repeat_until_no_tages", defaultRepeatUntilNoTags);
	    scanAutostart = mSharedpref.getBoolean("cfg_scan_autostart", defaultScanAutostart);
		intervalDelay = Integer.parseInt(mSharedpref.getString("cfg_interval_delay", defaultIntervalDelay_ms)) / ticTimerInMilliseconds;
		pingDelay = Integer.parseInt(mSharedpref.getString("cfg_ping_delay", defaultPingDelay_sec)) * (1000 / ticTimerInMilliseconds);
        prefixUrl = mSharedpref.getString("cfg_web_url", defaultPrefixUrl);
        webSocketUrl = mSharedpref.getString("cfg_wss_url", defaultWebSocketUrl);
	    bluetoothControl = mSharedpref.getBoolean("cfg_bluetooth_control", defaultBluetoothControl);
	    localSocketControl = mSharedpref.getBoolean("cfg_localsocket_control", defaultLocalSocketControl);
		readerParameterChanged = true;
	}

	private void updateReaderParameters() {
		if (usbState) {
			int status = MtiCmd.RFID_ERROR_NO_RESPONSE;
			CmdTagAccess.RFID_18K6CSetCurrentSingulationAlgorithm mMtiCmd0x32 = new CmdTagAccess.RFID_18K6CSetCurrentSingulationAlgorithm();
			status = mMtiCmd0x32.setCmd(CmdTagAccess.Algorithm.FixedQ);

			qValue = mSharedpref.getInt("cfg_q_value", defaultQValue);
		    retryCount = mSharedpref.getString("cfg_fixed_retry_count", defaultRetryCount);
		    repeatUntilNoTags = mSharedpref.getBoolean("cfg_repeat_until_no_tages", defaultRepeatUntilNoTags);
			CmdTagAccess.RFID_18K6CSetCurrentSingulationAlgorithmParameters mMtiCmd0x34 = new CmdTagAccess.RFID_18K6CSetCurrentSingulationAlgorithmParameters();
			try {
				status = mMtiCmd0x34.setCmd((byte)0x0,
											(byte)qValue,
											(byte)(Integer.valueOf(retryCount) & 0xff),
											(byte)0x1,
											(byte)(repeatUntilNoTags ? 1 : 0));
			} catch (NullPointerException e) {
				// Something bad happened here, likely USB failure
			}
		    powerLevel_dBm = mSharedpref.getInt("cfg_pwr_level", defaultPowerLevel_dBm);
			scanTime_ms = mSharedpref.getString("cfg_dwell_time", defaultScanTime_ms);
			numberInventoryCycles = mSharedpref.getString("cfg_number_inventory_cycles", defaultNumberInventoryCycles);
			CmdAntennaPortConf.RFID_AntennaPortSetConfiguration mMtiCmd0x12 = new CmdAntennaPortConf.RFID_AntennaPortSetConfiguration();
			try {
				status = mMtiCmd0x12.setCmd((byte)antennaPort,
											(short)(powerLevel_dBm * 10),
											(short)(Integer.valueOf(scanTime_ms) & 0xffff),
											(short)(Integer.valueOf(numberInventoryCycles) & 0xffff));
			} catch (NullPointerException e) {
				// Something bad happened here, likely USB failure
			}
			// Reader Parameters now updated
			if (status == MtiCmd.RFID_STATUS_OK) {
				readerParameterChanged = false;
			} else {
				sendToWebSocket("USB ERROR " + Integer.toString(status));
			}
		} else {
			sendToRemote("USB Reader Disconnected", false);
			writeToLogFile("USB Reader Disconnected");
		}
	}

	private void configureGPIO() {
		if (usbState) {
			// GPIO0: WebSocket Status LED
			// GPIO1: Bluetooth Status LED
			// GPIO2: Successful Tag Scan LED
			// GPIO3: Reserved for future use
			byte mask = 0x0F; // Select GPIO 0-3
			byte configuration = 0x0F; // GPIO 0-3 as Outputs

			int status = MtiCmd.RFID_ERROR_NO_RESPONSE;
			CmdReaderModuleGpioPinAccess.RFID_RadioSetGpioPinsConfiguration mMtiCmd = new CmdReaderModuleGpioPinAccess.RFID_RadioSetGpioPinsConfiguration();
			try {
				status = mMtiCmd.setCmd( mask, configuration );
			} catch (NullPointerException e) {
				// Something bad happened here, likely USB failure
			}
			if (status == MtiCmd.RFID_STATUS_OK) {
				gpioConfigured = true;
			} else {
				sendToWebSocket("USB ERROR " + Integer.toString(status));
			}
		} else {
			sendToRemote("USB Reader Disconnected", true);
		}
		gpioUpdateCounter = gpioUpdateDelay;
	}
		
	private void updateGPIO() {
		// GPIO0: WebSocket Status LED
		// GPIO1: Bluetooth Status LED
		// GPIO2: Successful Tag Scan LED
		// GPIO3: Reserved for future use
		byte mask = 0x0F; // Select GPIO 0-3
		byte value = 0x00;
		if (webSocketState == WebSocketState.OPEN) {
			value += 0x01;			
		}
        if ((mChatService != null) && (mChatService.getState() == BluetoothChatService.STATE_CONNECTED)) {
			value += 0x02;			
        }
        if (tagList.isEmpty() == false) {
			value += 0x04;			        	
        }
        if (gpio3State == true) {
			value += 0x08;			        	
        }
        if ((value != lastGpioState) && gpioConfigured) {
    		if (usbState) {
	    		int status = MtiCmd.RFID_ERROR_NO_RESPONSE;
	    		CmdReaderModuleGpioPinAccess.RFID_RadioWriteGpioPins mMtiCmd = new CmdReaderModuleGpioPinAccess.RFID_RadioWriteGpioPins();
	    		try {
	    			status = mMtiCmd.setCmd( mask, value );
	    		} catch (NullPointerException e) {
	    			// Something bad happened here, likely USB failure
	    		}
	    		if (status == MtiCmd.RFID_STATUS_OK) {
	        		lastGpioState = value;
	    		} else {
	    			sendToWebSocket("USB ERROR " + Integer.toString(status));
	    		}
    		} else {
    			sendToRemote("USB Reader Disconnected", true);
    		}
        }
	}
		
	public void startBluetoothServices() {
        // Get local BluetoothAdapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
        } else {
            // Initialize the BluetoothChatService to perform Bluetooth connections
            if ((mChatService == null) && (bluetoothControl == true)) {
                if (mBluetoothAdapter.isEnabled()) {
                    mChatService = new BluetoothChatService(this, btHandler);        	
        			Toast.makeText(this, "Starting Bluetooth Service", Toast.LENGTH_SHORT).show();
                    // Start the BluetoothChat services
                    mChatService.start();
                    mCirrusOneDeviceName = mBluetoothAdapter.getName();
    				bluetoothNotConnectedCounter = bluetoothNotConnectedTimeout;
                }
                else {
        			Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
        			bluetoothControl = false;
                }
            }            
        }
	}
	
	public void readerReset(boolean withRestart) {
		if (usbState) {
			// Send the reset command down to the reader
			int status = MtiCmd.RFID_ERROR_NO_RESPONSE;
			CmdReaderModuleControl.RFID_ControlSoftReset mMtiCmd = new CmdReaderModuleControl.RFID_ControlSoftReset();
			try {
				status = mMtiCmd.setCmd();
			} catch (NullPointerException e) {
				// Something bad happened here, likely USB failure
			}
		} else {
			sendToRemote("USB Reader Disconnected!", false);
			writeToLogFile("USB Reader Disconnected!");
		}
		if (withRestart == true) {
			// Force a close of the application
	        onDestroy();
			SystemClock.sleep(500);
			android.os.Process.killProcess(android.os.Process.myPid());			
		}
	}
	
	public void resetBluetoothAdapter() {
    	// Stop the Chat service
        if (mChatService != null) {
        	mChatService.stop();
        	mChatService = null;
        }
		// Do this part in the background
		Thread btReset = new Thread() {
            public void run () {
        		// Reset the Adapter
                if (mBluetoothAdapter != null) {
                	mBluetoothAdapter.disable();
                	SystemClock.sleep(3000);
                	mBluetoothAdapter.enable();
                	SystemClock.sleep(3000);
                	restartBluetoothServices = true;
                }   	
            }
        };
        btReset.start();
	}
		
	private boolean sendToWebServer(String tagId) {
		String uriApi;
		boolean success = false;
		
		if (prefixUrl != "") {
			uriApi = prefixUrl + tagId.replace(" ", "");
			
		    HttpClient httpclient = new DefaultHttpClient();
		    // Prepare a request object
		    HttpGet httpget = new HttpGet(uriApi); 

		    // Execute the request
		    HttpResponse response;
		    try {
		        response = httpclient.execute(httpget);
		        if (response.getStatusLine().getStatusCode() != 200) {
		            notificationRunnable.setMessage("missed=" + tagId);
					handler.post(notificationRunnable);
		        }
		        // Examine the response status
//		        sendToRemote("HTTP Response = " + response.getStatusLine().toString(), true);
		    } catch (Exception e) {
//				sendToRemote("HTTP Exception = " + e.toString(), true);
	            notificationRunnable.setMessage("missed=" + tagId);
				handler.post(notificationRunnable);
		    }			
 
	    }
		else {
//			sendToRemote("No WebUrl Configured", true);
		}
	    
	    return success;
	}

	private boolean sendToWebServerASYNC(String tagId) {
		boolean success = false;
		
		if (prefixUrl != "") {
			
			TxWebServerASYNC task = new TxWebServerASYNC();
	        task.execute(new String[] { tagId });
	    }
		else {
//			sendToRemote("No WebUrl Configured", true);
		}
	    
	    return success;
	}

    private class TxWebServerASYNC extends AsyncTask<String, Void, String> {
    	
        @Override
        protected String doInBackground(String... urls) {

        	String tagId = urls[0];
			String uriApi = prefixUrl + tagId.replace(" ", "");
      	
		    HttpClient httpclient = new DefaultHttpClient();
		    // Prepare a request object
		    HttpGet httpget = new HttpGet(uriApi); 

		    // Execute the request
		    HttpResponse response;
		    try {
		        response = httpclient.execute(httpget);
		        if (response.getStatusLine().getStatusCode() != 200) {
		            notificationRunnable.setMessage("missed=" + tagId);
					handler.post(notificationRunnable);
		        }
		        // Examine the response status
//		        sendToRemote("HTTP Response = " + response.getStatusLine().toString(), true);
		    } catch (Exception e) {
//				sendToRemote("HTTP Exception = " + e.toString(), true);
	            notificationRunnable.setMessage("missed=" + tagId);
				handler.post(notificationRunnable);
		    }			
 
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
 
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
	private boolean initFileInterface() {
		boolean success = false;
		// Check status of External Storage
        if (isExternalStorageReadable() && isExternalStorageWritable()) {
        	fileInterfaceActive = true;
        	String directory = Environment.getExternalStorageDirectory().toString();
			Toast.makeText(this, "Log FIle: " + directory + "/" + RfidContainer.logFilename, Toast.LENGTH_SHORT).show();        	
        } else {
			Toast.makeText(this, "External Storage is NOT Read/Write", Toast.LENGTH_SHORT).show();        	
        }
		return success;
	}

	private void readFromLogFile() {
		// Check status of External Storage
        if (isExternalStorageReadable() && isExternalStorageWritable()) {
			File logFile = new File(Environment.getExternalStorageDirectory() + "/" + RfidContainer.logFilename);
			if (logFile.exists()) {
				try {
	    			BufferedReader reader = new BufferedReader(new FileReader(logFile));
	    			String line = null;
	    		    try {
						while ((line = reader.readLine()) != null) {
							sendToRemote(line, false);
						}
						reader.close();
	            		if (!logFile.delete()) {
	                        notificationRunnable.setMessage("Toast=Cannot delete log file");
	    					handler.post(notificationRunnable);
	            		}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				sendToRemote("Log File is empty", false);
			}
        }
	}

	private boolean writeToLogFile(String message) {
		boolean success = false;
		if (fileInterfaceActive == true) {
			File logFile = new File(Environment.getExternalStorageDirectory() + "/" + RfidContainer.logFilename);
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
	
	// #### broadcast receiver ####
	BroadcastReceiver usbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
			int vid = device.getVendorId();
			int pid = device.getProductId();
			
			if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
				if (mManager.hasPermission(device)) {
					mManager.requestPermission(device, mPermissionIntent);
					Toast.makeText(context, "Request Permission USB device VID/PID = " + Integer.toString(vid) + "/" + Integer.toString(pid), Toast.LENGTH_SHORT).show();
				} else {
					// Check if this device was RFID, Galaxy Camera or something else
					if (vid == VID) {
						Toast.makeText(context, "RFID USB device connected", Toast.LENGTH_SHORT).show();
						UsbCommunication.setUsbInterface(mManager, device);
						devicePid = pid;
						usbState = true;
					} else if ((vid == CAMERA_VID) && (pid == CAMERA_PID)) {
						Toast.makeText(context, "Galaxy Camera USB device connected", Toast.LENGTH_SHORT).show();
						setCameraUsbInterface(mManager, device);
						cameraConnected = true;
					} else {
						Toast.makeText(context, "USB device connected VID/PID = " + Integer.toString(vid) + "/" + Integer.toString(pid), Toast.LENGTH_SHORT).show();
					}
				}
			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				// Check if this device was RFID, Galaxy Camera or something else
				if (vid == VID) {
					Toast.makeText(context, "RFID USB device disconnected!", Toast.LENGTH_SHORT).show();
					UsbCommunication.setUsbInterface(null, null);
					usbState = false;
					// Wait 1 second and exit
					new Thread() {
						public void run() {
							readerReset(true);
						}
					}.start();
					
				} else if ((vid == CAMERA_VID) && (pid == CAMERA_PID)) {
					Toast.makeText(context, "Galaxy Camera USB device disconnected", Toast.LENGTH_SHORT).show();
					setCameraUsbInterface(null, null);
					cameraConnected = false;
				} else {
					Toast.makeText(context, "USB device disconnected VID/PID = " + Integer.toString(vid) + "/" + Integer.toString(pid), Toast.LENGTH_SHORT).show();
				}
			} else if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized(this) {
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						// Check if this device was RFID, Galaxy Camera or something else
						if (vid == VID) {
							Toast.makeText(context, "RFID USB device connected", Toast.LENGTH_SHORT).show();
							UsbCommunication.setUsbInterface(mManager, device);
							devicePid = pid;
							usbState = true;
						} else if ((vid == CAMERA_VID) && (pid == CAMERA_PID)) {
							Toast.makeText(context, "Galaxy Camera USB device connected", Toast.LENGTH_SHORT).show();
							setCameraUsbInterface(mManager, device);
							cameraConnected = true;
						} else {
							Toast.makeText(context, "USB device connected VID/PID = " + Integer.toString(vid) + "/" + Integer.toString(pid), Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(context, "USB device permission denied VID/PID = " + Integer.toString(vid) + "/" + Integer.toString(pid), Toast.LENGTH_SHORT).show();
						if (vid == VID) {
							usbState = false;
						}
					}
				}
			}
		}
	};
	
	// Camera USB Interface
	private boolean setCameraUsbInterface(UsbManager manager, UsbDevice device) {
		// Close any existing connection
		if (mCameraDeviceConnection != null) {
			if (mCameraInterface != null) {
				mCameraDeviceConnection.releaseInterface(mCameraInterface);
				mCameraInterface = null;
			}
			mCameraDeviceConnection.close();
			mCameraDeviceConnection = null;
			mCameraDevice = null;
		}
		if (device != null) {
			UsbInterface intf = device.getInterface(0);
			UsbDeviceConnection connection = manager.openDevice(device);
			if (connection != null) {
				if (connection.claimInterface(intf, true)) {
					mCameraDevice = device;
					mCameraInterface = intf;
					mCameraDeviceConnection = connection;

					UsbEndpoint epOut = null;
					UsbEndpoint epIn = null;

					for (int i = 0; i < intf.getEndpointCount(); i++) {
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
					mCameraEndpointOut = epOut;
					mCameraEndpointIn = epIn;

					return true;
				} else {
					Toast.makeText(this, "claimInterface failed", Toast.LENGTH_SHORT).show();
					connection.close();
				}
			} else {
				Toast.makeText(this, "openDevice failed", Toast.LENGTH_SHORT).show();
			}
		} else {
			mCameraDeviceConnection = null;
			mCameraInterface = null;
			mCameraDevice = null;
		}
		return false;
	}
	
	private void sendPing() {
		// Send normal PING only if USB Device is connected
		if (usbState) {
			sendToRemote(mCirrusOneDeviceName, false);
		} else {
			sendToRemote("USB Reader Disconnected", false);
			writeToLogFile("USB Reader Disconnected");
		}
		// The things we do for the local socket controller
		if (localSocketControl == true) {
			if (localSocketListenerAttached == false) {
				sendToRemote("No LocalSocket listener!", false);
				writeToLogFile("sendToLocalSocket: No LocalSocket listener!");
			}
			if (localSocketKeepAliveMessageReceived == true) {
				localSocketKeepAliveMessageReceived = false;
			} else {
				sendToRemote("No KAM from FFA since last ping", false);
				writeToLogFile("processTicTimer: No KAM from FFA since last ping");
				missedLocalSocketKeepAliveMessages++;
				if (missedLocalSocketKeepAliveMessages >= maxMissedLocalSocketKeepAliveMessages) {
					// reset the camera
					try {
						sendToRemote("Rebooting camera", false);
						writeToLogFile("processTicTimer: Rebooting camera");
						onDestroy();
						SystemClock.sleep(500);
						Runtime.getRuntime().exec(new String[]{"/system/xbin/su","-c","reboot now"});
					} catch (IOException e) {
						sendToRemote("Unable to reboot camera", false);
						writeToLogFile("processTicTimer: Unable to reboot camera");
						Toast.makeText(this, "Unable to reboot camera", Toast.LENGTH_SHORT).show();
					}
				}
			}
		}
	}
	
	private boolean sendToCameraDevice(String message) {
		boolean status = false;
		
		byte[] byteMessage = message.getBytes();
		int length = byteMessage.length;
		
		if (mCameraDevice != null) {
			synchronized(mCameraDevice) {
				UsbRequest txRequest = new UsbRequest();
	   			txRequest.initialize(mCameraDeviceConnection, mCameraEndpointOut);
	   			status = txRequest.queue(ByteBuffer.wrap(byteMessage), length);
			}
		}
		return status;
	}
	
	public void putIconInStatusBar() {
		Notification.Builder builder = new Notification.Builder(this).setSmallIcon(R.drawable.cirrus_one);      
		Intent intent = new Intent( this, ReaderService.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, mID , intent, 0);
		builder.setContentIntent(pIntent);
		
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (mNotificationManager != null) {
			Notification notif = builder.build();
			mNotificationManager.notify(mID, notif);
		}
	}
	
	public void removeIconFromStatusBar() {
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (mNotificationManager != null) {
			mNotificationManager.cancelAll();			
		}
	}
}