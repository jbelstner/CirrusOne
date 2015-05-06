package com.encinitaslabs.wirfid.hpreader;

public class CmdReaderModuleControl {

	public enum PowerState {
		Full((byte)0x00),
		Standby((byte)0x02);
		
		private byte bPowerState;
		
		PowerState(byte bPowerState) {
			this.bPowerState = bPowerState;
		}
	}

	
	/************************************************************
	 **						RFID_ControlCancel					*
	 ************************************************************/
	static final class RFID_ControlCancel extends MtiCmd {
		public RFID_ControlCancel(){
			mCmdHead = CmdHead.RFID_ControlCancel;
		};

		public int setCmd() {
			mParam.clear();
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **						RFID_ControlPause					*
	 ************************************************************/
	static final class RFID_ControlPause extends MtiCmd {
		public RFID_ControlPause(){
			mCmdHead = CmdHead.RFID_ControlPause;
		};

		public int setCmd() {
			mParam.clear();
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **						RFID_ControlResume					*
	 ************************************************************/
	static final class RFID_ControlResume extends MtiCmd {
		public RFID_ControlResume(){
			mCmdHead = CmdHead.RFID_ControlResume;
		};

		public int setCmd() {
			mParam.clear();
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **					RFID_ControlSoftReset					*
	 ************************************************************/
	static final class RFID_ControlSoftReset extends MtiCmd {
		public RFID_ControlSoftReset(){
			mCmdHead = CmdHead.RFID_ControlSoftReset;
		};

		public int setCmd() {
			mParam.clear();
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **				RFID_ControlResetToBootloader				*
	 ************************************************************/
	static final class RFID_ControlResetToBootloader extends MtiCmd {
		public RFID_ControlResetToBootloader(){
			mCmdHead = CmdHead.RFID_ControlResetToBootloader;
		};

		public int setCmd() {
			mParam.clear();
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **					RFID_ControlSetPowerState				*
	 ************************************************************/
	static final class RFID_ControlSetPowerState extends MtiCmd {
		public RFID_ControlSetPowerState(){
			mCmdHead = CmdHead.RFID_ControlSetPowerState;
		};

		public int setCmd(PowerState powerState) {
			mParam.clear();
			mParam.add(powerState.bPowerState);
			composeCmd();

			return checkResponse(120);
		}

		public int setCmd(byte powerState) {
			mParam.clear();
			mParam.add(powerState);
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **				RFID_ControlGetPowerState					*
	 ************************************************************/
	static final class RFID_ControlGetPowerState extends MtiCmd {
		public RFID_ControlGetPowerState(){
			mCmdHead = CmdHead.RFID_ControlGetPowerState;
		};

		public int setCmd() {
			mParam.clear();
			composeCmd();

			return checkResponse(120);
		}
	}

}
