package com.encinitaslabs.wirfid.hpreader;

public class CmdReaderModuleFirmwareAccess {

	public enum ErrorType {
		CurrentError((byte)0x00),
		LastError((byte)0x02);
		
		private byte bErrorType;
		
		ErrorType(byte bErrorType) {
			this.bErrorType = bErrorType;
		}
	}

	public enum RegionOperation {
		US_CA((byte)0),
		EU((byte)1),
		EU2((byte)2),
		TW((byte)3),
		CN((byte)4),
		KR((byte)5),
		AU_NZ((byte)6),
		BR((byte)7),
		IL((byte)8),
		IN((byte)9),
		Custom((byte)10);
		
		private byte bRegionOperation;
		
		RegionOperation(byte bRegionOperation) {
			this.bRegionOperation = bRegionOperation;
		}
	}


	
	/************************************************************
	 **					RFID_MacGetFirmwareVersion				*
	 ************************************************************/
	static final class RFID_MacGetFirmwareVersion extends MtiCmd {
		public RFID_MacGetFirmwareVersion(){
			mCmdHead = CmdHead.RFID_MacGetFirmwareVersion;
		};

		public int setCmd() {
			mParam.clear();
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **						RFID_MacGetDebug					*
	 ************************************************************/
	static final class RFID_MacGetDebug extends MtiCmd {
		public RFID_MacGetDebug(){
			mCmdHead = CmdHead.RFID_MacGetDebug;
		};

		public int setCmd() {
			mParam.clear();
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **						RFID_MacClearError					*
	 ************************************************************/
	static final class RFID_MacClearError extends MtiCmd {
		public RFID_MacClearError(){
			mCmdHead = CmdHead.RFID_MacClearError;
		};

		public int setCmd() {
			mParam.clear();
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **						RFID_MacGetError					*
	 ************************************************************/
	static final class RFID_MacGetError extends MtiCmd {
		public RFID_MacGetError(){
			mCmdHead = CmdHead.RFID_MacGetError;
		};

		public int setCmd(ErrorType errorType) {
			mParam.clear();
			mParam.add(errorType.bErrorType);
			composeCmd();

			return checkResponse(120);
		}

		public int setCmd(byte errorType) {
			mParam.clear();
			mParam.add(errorType);
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **				RFID_MacGetBootloaderVersion				*
	 ************************************************************/
	static final class RFID_MacGetBootloaderVersion extends MtiCmd {
		public RFID_MacGetBootloaderVersion(){
			mCmdHead = CmdHead.RFID_MacGetBootloaderVersion;
		};

		public int setCmd() {
			mParam.clear();
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **					RFID_MacWriteOemData					*
	 ************************************************************/
	static final class RFID_MacWriteOemData extends MtiCmd {
		public RFID_MacWriteOemData(){
			mCmdHead = CmdHead.RFID_MacWriteOemData;
		};

		public int setCmd(short address, int data) {
			mParam.clear();
			addParam(address);
			addParam(data);
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **					RFID_MacReadOemData						*
	 ************************************************************/
	static final class RFID_MacReadOemData extends MtiCmd {
		public RFID_MacReadOemData(){
			mCmdHead = CmdHead.RFID_MacReadOemData;
		};

		public int setCmd(short address) {
			mParam.clear();
			addParam(address);
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **				RFID_MacBypassWriteRegister					*
	 ************************************************************/
	static final class RFID_MacBypassWriteRegister extends MtiCmd {
		public RFID_MacBypassWriteRegister(){
			mCmdHead = CmdHead.RFID_MacBypassWriteRegister;
		};

		public int setCmd(short address, short data) {
			mParam.clear();
			addParam(address);
			addParam(data);
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **				RFID_MacBypassReadRegister					*
	 ************************************************************/
	static final class RFID_MacBypassReadRegister extends MtiCmd {
		public RFID_MacBypassReadRegister(){
			mCmdHead = CmdHead.RFID_MacBypassReadRegister;
		};

		public int setCmd(short address) {
			mParam.clear();
			addParam(address);
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **					RFID_MacSetRegion						*
	 ************************************************************/
	static final class RFID_MacSetRegion extends MtiCmd {
		public RFID_MacSetRegion(){
			mCmdHead = CmdHead.RFID_MacSetRegion;
		};

		public int setCmd(RegionOperation regionOperation) {
			mParam.clear();
			addParam(regionOperation.bRegionOperation);
			composeCmd();

			return checkResponse(120);
		}

		public int setCmd(byte regionOperation) {
			mParam.clear();
			addParam(regionOperation);
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **					RFID_MacGetRegion						*
	 ************************************************************/
	static final class RFID_MacGetRegion extends MtiCmd {
		public RFID_MacGetRegion(){
			mCmdHead = CmdHead.RFID_MacGetRegion;
		};

		public int setCmd() {
			mParam.clear();
			composeCmd();

			return checkResponse(120);
		}
		
		public byte getRegion() {
			return mResponse[STATUS_POS+1];
		}
	}

	
	/************************************************************
	 **					RFID_MacGetOEMCfgVersion				*
	 ************************************************************/
	static final class RFID_MacGetOEMCfgVersion extends MtiCmd {
		public RFID_MacGetOEMCfgVersion(){
			mCmdHead = CmdHead.RFID_MacGetOEMCfgVersion;
		};

		public int setCmd() {
			mParam.clear();
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **				RFID_MacGetOEMCfgUpdateNumber				*
	 ************************************************************/
	static final class RFID_MacGetOEMCfgUpdateNumber extends MtiCmd {
		public RFID_MacGetOEMCfgUpdateNumber(){
			mCmdHead = CmdHead.RFID_MacGetOEMCfgUpdateNumber;
		};

		public int setCmd() {
			mParam.clear();
			composeCmd();

			return checkResponse(120);
		}
	}

}
