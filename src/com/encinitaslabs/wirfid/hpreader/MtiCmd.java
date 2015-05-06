package com.encinitaslabs.wirfid.hpreader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import android.os.SystemClock;
import android.util.Log;

public abstract class MtiCmd {
	public static final String TAG = "UsbControl";
	public static final boolean DEBUG = false;

	public static final int CMD_LENGTH = 64;
	public static final int RESPONSE_LENGTH = 64;
	public static final byte[] cmdHeader = {(byte)0x43, (byte)0x49, (byte)0x54, (byte)0x4D};
	public static final int HEADER_SIZE = cmdHeader.length;
	public static final int STATUS_POS = 6;
	
	public static final int RFID_STATUS_OK = 0x0000;
	public static final int RFID_ERROR_INVALID_PARAMETER = 0x00f0;
	public static final int RFID_ERROR_MODULE_FAILURE = 0x00f1;
	public static final int RFID_ERROR_CHECKSUM = 0xff00;
	public static final int RFID_ERROR_COMMAND = 0xff01;
	public static final int RFID_ERROR_RESPONSE = 0xff02;
	public static final int RFID_ERROR_BEGIN = 0xff03;
	public static final int RFID_ERROR_INVENTORY = 0xff04;
	public static final int RFID_ERROR_ACCESS = 0xff05;
	public static final int RFID_ERROR_END = 0xff06;
	public static final int RFID_ERROR_INVENTORY_OR_ACCESS = 0xff10;
	public static final int RFID_ERROR_NO_RESPONSE = 0xff11;
	public static final int RFID_ERROR_USB_SEND = 0xff12;
	public static final int RFID_ERROR_NOT_CONNECTED = 0xffff;

	private static ByteBuffer shortBuffer = ByteBuffer.allocate(2);
	private static ByteBuffer intBuffer = ByteBuffer.allocate(4);
	
//	protected static UsbCommunication mUsbComm = RfidContainer.mUsbComm;
	
	private static byte mStatus;
	protected static CmdHead mCmdHead;
	protected static ArrayList<Byte> mParam = new ArrayList<Byte>();
	private static byte[] mFinalCmd = new byte[CMD_LENGTH];
	protected static byte[] mResponse = new byte[RESPONSE_LENGTH];

	private static ArrayList<String> mTagIds = new ArrayList<String>();
//	private static byte[] tagId = new byte[44];


	protected boolean composeCmd() {
		int cmdLength = 14;
		byte[] command;
		boolean bState;
		
		mFinalCmd = Arrays.copyOfRange(cmdHeader, 0, 16);
		
		mFinalCmd[HEADER_SIZE] = (byte)0xff;
		mFinalCmd[HEADER_SIZE+1] = mCmdHead.get1stCmd();
		
		int arrLength = mParam.size();
		for(int i = 0; i < arrLength; i++) {
			mFinalCmd[HEADER_SIZE+2+i] = mParam.get(i).byteValue();
		}
		
		int Crc = ~Crc16.calculate(mFinalCmd, cmdLength);
		mFinalCmd[cmdLength++] = (byte)((Crc & 0x000000ff));
		mFinalCmd[cmdLength++] = (byte)((Crc & 0x0000ff00) >>> 8);
		
		command = Arrays.copyOfRange(mFinalCmd, 0, cmdLength);
		bState = UsbCommunication.sendCmd(command, cmdLength);
		
		// #### log whole command for debug ####
		if (DEBUG) {
			Log.d(TAG, "TX: " + byteArrayToString(command, true));
		}
		return bState;
	}

	
	public int checkResponse(int ms) {
		int errCode = 0x00;
//		int i = 0;
		
		do {
			SystemClock.sleep(ms);
			byte[] subResponse = UsbCommunication.getResponse();
			if (subResponse == null) {
				errCode = RFID_ERROR_NO_RESPONSE;				        // no response						
			} else {
				mResponse = subResponse;
				if (mResponse[0] == 'R') {
					if (Crc16.check(mResponse, 16)) {
						if (mResponse[5] == mFinalCmd[5]) {
							errCode = mResponse[STATUS_POS];
						} else {
							errCode = 0xff01;			// command  error							
						}
					} else {
						errCode = 0xff00;				// checksum error						
					}
				} else {
					errCode = 0xff02;					// header   error
				}
				// #### log whole command for debug ####
				if (DEBUG) {
					Log.d(UsbCommunication.TAG, "[Response Rx] " + byteArrayToString(mResponse, true));
				}
			}
//			i++;
		} while ((errCode != 0x00) && (errCode != RFID_ERROR_NO_RESPONSE));
//		} while ((errCode != 0x00) && i < 10);
		
		return errCode;
	}

	
	public int checkBegin(int ms) {
		int errCode = 0x00;
//		int i = 0;
		
		do {
			SystemClock.sleep(ms);
			byte[] subResponse = UsbCommunication.getResponse();
			if (subResponse == null) {
				errCode = RFID_ERROR_NO_RESPONSE;				        // no response						
			} else {
				mResponse = subResponse;
				if (mResponse[0] == 'B') {
					if (!Crc16.check(mResponse, 24)) {
						errCode = 0xff00;				// checksum error
					}
				} else {
					errCode = 0xff03;					// header   error
				}
				// #### log whole command for debug ####
				if (DEBUG) {
					Log.d(UsbCommunication.TAG, "[Begin Rx] " + byteArrayToString(mResponse, true));
				}
			}
//			i++;
		} while ((errCode != 0x00) && (errCode != RFID_ERROR_NO_RESPONSE));
//		} while ((errCode != 0x00) && i < 10);
		
		return errCode;
	}

	
	public int checkEnd(int ms) {
		int errCode = 0x00;
//		int i = 0;
		
		do {
			SystemClock.sleep(ms);
			byte[] subResponse = UsbCommunication.getResponse();
			if (subResponse == null) {
				errCode = RFID_ERROR_NO_RESPONSE;				        // no response						
			} else {
				mResponse = subResponse;
				if (mResponse[0] == 'E') {
					if (!Crc16.check(mResponse, 24)) {
						errCode = 0xff00;				// checksum error						
					}
				} else if ((mResponse[0] == 'I') || (mResponse[0] == 'A')) {
					errCode = 0xff10;
				} else {
					errCode = 0xff06;					// header   error
				}
				// #### log whole command for debug ####
				if (DEBUG) {
					Log.d(UsbCommunication.TAG, "[End Rx] " + byteArrayToString(mResponse, true));
				}
			}
//			i++;
		} while ((errCode != 0x00) && (errCode != 0xff10) && (errCode != RFID_ERROR_NO_RESPONSE));
//		} while ((errCode != 0x00) && (errCode != 0xff10) && i < 5);
		
		return errCode;
	}
	
	public int checkInventory() {
		int errCode = 0x00;

		if (mResponse[0] == 'I') {
			if (!Crc16.check(mResponse, 64))
				errCode = 0xff00;				// checksum error
		} else {
			errCode = 0xff04;					// header   error
		}
		
		return errCode;
	}
	
	public int checkAccess() {
		int errCode = 0x00;

		if (mResponse[0] == 'A') {
			if(!Crc16.check(mResponse, 64))
				errCode = 0xff00;				// checksum error
		} else {
			errCode = 0xff03;					// header   error
		}
		
		return errCode;
	}

/*	
	public String responseData(int length) {
		String hexResult = "";

		for (int i = 0; i < length * 2; i++) {
			hexResult += ((mResponse[i + 4] < 0 || mResponse[i + 4] > 15)
						? Integer.toHexString(0xff & (int)mResponse[i + 4])
						: "0" + Integer.toHexString(0xff & (int)mResponse[i + 4]))
						+ (( i % 2 == 1) ? " " : "");
		}
		return hexResult.toUpperCase(Locale.getDefault());
	}
*/
	
	public ArrayList<String> getTagIds(boolean firstTime) {
		if (firstTime) {
			mTagIds.clear();
		}
		if (checkInventory() == 0x00) {
			short iEpcLength = (short) ((getAbsShort(10) - 4) * 4);
			try {
				byte[] subResponse = Arrays.copyOfRange(mResponse, 28, 28 + iEpcLength);
				mTagIds.add(byteArrayToString(subResponse, iEpcLength, true));
			} catch (ArrayIndexOutOfBoundsException e) {
				Log.e(TAG, "ArrayIndexOutOfBoundsException");
			} catch (IllegalArgumentException e) {
				Log.e(TAG, "IllegalArgumentException");				
			} catch (NullPointerException e) {
				Log.e(TAG, "NullPointerException");
			}
		}
		return mTagIds;
	}
	
	public String getTagIds() {
		if (checkInventory() == 0x00) {
			short iEpcLength = (short) ((getAbsShort(10) - 4) * 4);
			try {
				byte[] subResponse = Arrays.copyOfRange(mResponse, 28, 28 + iEpcLength);
				return byteArrayToString(subResponse, iEpcLength, true);
			} catch (ArrayIndexOutOfBoundsException e) {
				Log.e(TAG, "ArrayIndexOutOfBoundsException");
			} catch (IllegalArgumentException e) {
				Log.e(TAG, "IllegalArgumentException");				
			} catch (NullPointerException e) {
				Log.e(TAG, "NullPointerException");
			}
		}
		return null;
	}
	

	public String readData() {
		if (checkAccess() == 0x00) {
			short sDataLength = (short)((getAbsShort(10) - 3) * 4);
			try {
				byte[] subResponse = Arrays.copyOfRange(mResponse, 26, 26 + sDataLength);
				return byteArrayToString(subResponse, sDataLength, true);
			} catch (ArrayIndexOutOfBoundsException e) {
				Log.e(TAG, "ArrayIndexOutOfBoundsException");
			} catch (IllegalArgumentException e) {
				Log.e(TAG, "IllegalArgumentException");				
			} catch (NullPointerException e) {
				Log.e(TAG, "NullPointerException");
			}
		}
		return null;
	}


	public String byteArrayToString(byte[] BtoS, boolean space) {
		String hexResult = "";
		int iLength = BtoS.length;
		
		if(space) {
			for (int i = 0; i < iLength; i++) {
				hexResult += ((BtoS[i] < 0 || BtoS[i] > 15)
							? Integer.toHexString(0xff & (int)BtoS[i])
							: "0" + Integer.toHexString(0xff & (int)BtoS[i]))
							+ ((i == BtoS.length - 1) ? "" : " ");
			}
		} else {
			for (int i = 0; i < iLength; i++) {
				hexResult += ((BtoS[i] < 0 || BtoS[i] > 15)
							? Integer.toHexString(0xff & (int)BtoS[i])
							: "0" + Integer.toHexString(0xff & (int)BtoS[i]));
			}
		}
		return hexResult.toUpperCase(Locale.getDefault());
	}
	

	public String byteArrayToString(byte[] BtoS, int length, boolean space) {
		String hexResult = "";

		if(space) {
			for (int i = 0; i < length; i++) {
				hexResult += ((BtoS[i] < 0 || BtoS[i] > 15)
							? Integer.toHexString(0xff & (int)BtoS[i])
							: "0" + Integer.toHexString(0xff & (int)BtoS[i]))
							+ ((i == length - 1) ? "" : " ");
			}
		} else {
			for (int i = 0; i < length; i++) {
				hexResult += ((BtoS[i] < 0 || BtoS[i] > 15)
							? Integer.toHexString(0xff & (int)BtoS[i])
							: "0" + Integer.toHexString(0xff & (int)BtoS[i]));
			}
		}
		return hexResult.toUpperCase(Locale.getDefault());
	}
	

    public byte[] stringToByteArray(String StoB) {
    	String subStr;
    	int iLength = StoB.length() / 2;
    	byte[] bytes = new byte[iLength];
    	
        for (int i = 0; i < iLength; i++) {
        	subStr = StoB.substring(2 * i, 2 * i + 2);
        	bytes[i] = (byte)Integer.parseInt(subStr, 16);
        }
        return bytes;
    }
    
	
    public void addParam(short param) {
//		ByteBuffer shortBuffer = ByteBuffer.allocate(2);
		shortBuffer.clear();
		shortBuffer.order(ByteOrder.LITTLE_ENDIAN);
		shortBuffer.putShort(param);
		for(int i = 0; i < 2; i++)
			mParam.add(shortBuffer.get(i));
    }
    
    public void addParam(int param) {
//		ByteBuffer intBuffer = ByteBuffer.allocate(4);
    	intBuffer.clear();
		intBuffer.order(ByteOrder.LITTLE_ENDIAN);
		intBuffer.putInt(param);
		for(int i = 0; i < 4; i++)
			mParam.add(intBuffer.get(i));
    }
    
    public int getInt(int startByte) {
    	int absPos = STATUS_POS + startByte;
    	byte[] subResponse = Arrays.copyOfRange(mResponse, absPos, absPos + 4);
		ByteBuffer byteBuffer = ByteBuffer.wrap(subResponse);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		int iResponse = byteBuffer.getInt();
		
		return iResponse;
    }
    
    public short getShort(int startByte) {
    	int absPos = STATUS_POS + startByte;
    	byte[] subResponse = Arrays.copyOfRange(mResponse, absPos, absPos + 2);
		ByteBuffer byteBuffer = ByteBuffer.wrap(subResponse);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		short iResponse = byteBuffer.getShort();

		return iResponse;
    }
    
    public int getAbsInt(int startByte) {
    	byte[] subResponse = Arrays.copyOfRange(mResponse, startByte, startByte + 4);
		ByteBuffer byteBuffer = ByteBuffer.wrap(subResponse);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		int iResponse = byteBuffer.getInt();
		
		return iResponse;
    }
    
    public short getAbsShort(int startByte) {
    	byte[] subResponse = Arrays.copyOfRange(mResponse, startByte, startByte + 2);
		ByteBuffer byteBuffer = ByteBuffer.wrap(subResponse);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		short iResponse = byteBuffer.getShort();

		return iResponse;
    }
    
    public String errMsg(int errCode) {
    	String rtnMsg = null;
    	switch(errCode) {
			case 0x0000:
				rtnMsg = "RFID_STATUS_OK";
				break;
			case 0x00f0:
				rtnMsg = "RFID_ERROR_INVALID_PARAMETER";
				break;
			case 0x00f1:
				rtnMsg = "RFID_ERROR_MODULE_FAILURE";
				break;
	    	case 0xff00:
	    		rtnMsg = "Checksum Error!!";
	    		break;
	    	case 0xff01:
	    		rtnMsg = "Command Error!!";
	    		break;
	    	case 0xff02:
	    		rtnMsg = "Response Error!!";
	    		break;
	    	case 0xff03:
	    		rtnMsg = "Begin Error!!";
	    		break;
	    	case 0xff04:
	    		rtnMsg = "inventory Error!!";
	    		break;
	    	case 0xff05:
	    		rtnMsg = "Access Error!!";
	    		break;
	    	case 0xff06:
	    		rtnMsg = "End Error!!";
	    		break;
	    	case 0xff10:
	    		// inventory or access
	    		break;
	    	case RFID_ERROR_NO_RESPONSE:
	    		rtnMsg = "Pool was Empty!";
	    		break;
	    	case 0xff12:
	    		rtnMsg = "USB Send Error!";
	    		break;
	    	case 0xffff:
	    		rtnMsg = "The reader is not connected!!";
	    		break;
    	}
    	return rtnMsg;
    }
}

