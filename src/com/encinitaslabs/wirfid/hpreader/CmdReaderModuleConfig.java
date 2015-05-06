package com.encinitaslabs.wirfid.hpreader;


public class CmdReaderModuleConfig {
	
	public enum OperationMode {
		Continuous((byte)0x00),
		NonContinuous((byte)0x01);
		
		private byte bOperationMode;
		
		OperationMode(byte bOperationMode) {
			this.bOperationMode = bOperationMode;
		}
	}


	/************************************************************
	 **					RFID_RadioSetDeviceID					*
	 ************************************************************/
	static final class RFID_RadioSetDeviceID extends MtiCmd {
		public RFID_RadioSetDeviceID() {
			mCmdHead = CmdHead.RFID_RadioSetDeviceID;
		}

		public int setCmd(byte deviceId) {
			mParam.clear();
			mParam.add(deviceId);
			composeCmd();
			
			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **					RFID_RadioGetDeviceID					*
	 ************************************************************/
	static final class RFID_RadioGetDeviceID extends MtiCmd {
		public RFID_RadioGetDeviceID() {
			mCmdHead = CmdHead.RFID_RadioGetDeviceID;
		}

		public int setCmd() {
			mParam.clear();
			composeCmd();
			
			return checkResponse(120);
		}
		
		public byte getDeviceId() {
			return mResponse[STATUS_POS+1];
		}
	}

	
	/************************************************************
	 **				RFID_RadioSetOperationMode					*
	 ************************************************************/
	static final class RFID_RadioSetOperationMode extends MtiCmd {
		public RFID_RadioSetOperationMode() {

			mCmdHead = CmdHead.RFID_RadioSetOperationMode;
		}

		public int setCmd(OperationMode operationMode) {
			mParam.clear();
			mParam.add(operationMode.bOperationMode);
			composeCmd();
			
			return checkResponse(120);
		}

		public int setCmd(byte operationMode) {
			mParam.clear();
			mParam.add(operationMode);
			composeCmd();
			
			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **				RFID_RadioGetOperationMode					*
	 ************************************************************/
	static final class RFID_RadioGetOperationMode extends MtiCmd {
		public RFID_RadioGetOperationMode() {
			mCmdHead = CmdHead.RFID_RadioGetOperationMode;
		}

		public int setCmd() {
			mParam.clear();
			composeCmd();
			
			return checkResponse(120);
		}
		
		public byte getOperationMode() {
			return mResponse[STATUS_POS+1];
		}
	}

	
	/************************************************************
	 **				RFID_RadioSetCurrentLinkProfile				*
	 ************************************************************/
	static final class RFID_RadioSetCurrentLinkProfile extends MtiCmd {
		public RFID_RadioSetCurrentLinkProfile() {
			mCmdHead = CmdHead.RFID_RadioSetCurrentLinkProfile;
		}

		public int setCmd(byte linkProfile) {
			mParam.clear();
			mParam.add(linkProfile);
			composeCmd();
			
			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **				RFID_RadioGetCurrentLinkProfile				*
	 ************************************************************/
	static final class RFID_RadioGetCurrentLinkProfile extends MtiCmd {
		public RFID_RadioGetCurrentLinkProfile() {
			mCmdHead = CmdHead.RFID_RadioGetCurrentLinkProfile;
		}

		public int setCmd() {
			mParam.clear();
			composeCmd();
			
			return checkResponse(120);
		}
		
		public byte getCurrentLinkProfile() {
			return mResponse[STATUS_POS+1];
		}
	}

	
	/************************************************************
	 **					RFID_RadioWriteRegister					*
	 ************************************************************/
	static final class RFID_RadioWriteRegister extends MtiCmd {
		public RFID_RadioWriteRegister() {
			mCmdHead = CmdHead.RFID_RadioWriteRegister;
		}

		public int setCmd(byte[] address, byte[] value) {
			mParam.clear();
			mParam.add(address[0]);
			mParam.add(address[1]);
			mParam.add(value[0]);
			mParam.add(value[1]);
			mParam.add(value[2]);
			mParam.add(value[3]);
			composeCmd();
			
			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **					 RFID_RadioReadRegister					*
	 ************************************************************/
	static final class RFID_RadioReadRegister extends MtiCmd {
		public RFID_RadioReadRegister() {
			mCmdHead = CmdHead.RFID_RadioReadRegister;
		}

		public int setCmd(byte[] address) {
			mParam.clear();
			mParam.add(address[0]);
			mParam.add(address[1]);
			composeCmd();
			
			return checkResponse(120);
		}
		
		public int getRegisterValue() {
			return getInt(STATUS_POS + 1);
		}
	}

	
	
	/************************************************************
	 **				RFID_RadioWriteBankedRegister				*
	 ************************************************************/
	static final class RFID_RadioWriteBankedRegister extends MtiCmd {
		public RFID_RadioWriteBankedRegister() {
			mCmdHead = CmdHead.RFID_RadioWriteBankedRegister;
		}

		public int setCmd(byte[] address, byte[] bankSelector, byte[] value) {
			mParam.clear();
			mParam.add(address[0]);
			mParam.add(address[1]);
			mParam.add(bankSelector[0]);
			mParam.add(bankSelector[1]);
			mParam.add(value[0]);
			mParam.add(value[1]);
			mParam.add(value[2]);
			mParam.add(value[3]);
			composeCmd();
			
			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **				 RFID_RadioReadBankedRegister				*
	 ************************************************************/
	static final class RFID_RadioReadBankedRegister extends MtiCmd {
		public RFID_RadioReadBankedRegister() {
			mCmdHead = CmdHead.RFID_RadioReadBankedRegister;
		}

		public int setCmd(byte[] address, byte[] bankSelector) {
			mParam.clear();
			mParam.add(address[0]);
			mParam.add(address[1]);
			mParam.add(bankSelector[0]);
			mParam.add(bankSelector[1]);
			composeCmd();
			
			return checkResponse(120);
		}
		
		public int getBankedRegisterValue() {
			return getInt(STATUS_POS + 1);
		}
	}

	
	/************************************************************
	 **				 RFID_RadioReadRegisterInfo					*
	 ************************************************************/
	static final class RFID_RadioReadRegisterInfo extends MtiCmd {
		public RFID_RadioReadRegisterInfo() {
			mCmdHead = CmdHead.RFID_RadioReadRegisterInfo;
		}

		public int setCmd(byte[] address) {
			mParam.clear();
			mParam.add(address[0]);
			mParam.add(address[1]);
			composeCmd();
			
			return checkResponse(120);
		}
		
		public byte getRegisterType() {
			return mResponse[STATUS_POS+1];
		}
		
		public byte getAccessType() {
			return mResponse[STATUS_POS+2];
		}
		
		public byte getBankSize() {
			return mResponse[STATUS_POS+3];
		}
		
		public short getSelectorAddress() {
			return getShort(STATUS_POS + 4);
		}
		
		public short getCurrentSelector() {
			return getShort(STATUS_POS + 6);
		}
	}

}
