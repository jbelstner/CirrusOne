package com.encinitaslabs.wirfid.hpreader;

public class CmdAntennaPortConf {
	
	public enum State {
		Disabled((byte)0x00),
		Enabled((byte)0x01);
		
		private byte bState;
		
		State(byte bState) {
			this.bState = bState;
		}
	}
	
	
	/************************************************************
	 **					RFID_AntennaPortSetState				*
	 ************************************************************/
	static final class RFID_AntennaPortSetState extends MtiCmd {
		public RFID_AntennaPortSetState(){
			mCmdHead = CmdHead.RFID_AntennaPortSetState;
		};

		public int setCmd(byte antennaPort, State state) {
			mParam.clear();
			mParam.add(antennaPort);
			mParam.add(state.bState);
			composeCmd();

			return checkResponse(120);
		}
		
		public int setCmd(byte antennaPort, byte state) {
			mParam.clear();
			mParam.add(antennaPort);
			mParam.add(state);
			composeCmd();

			return checkResponse(120);
		}
	}
	
	
	/************************************************************
	 **					RFID_AntennaPortGetState				*
	 ************************************************************/
	static final class RFID_AntennaPortGetState extends MtiCmd {
		public RFID_AntennaPortGetState() {
			mCmdHead = CmdHead.RFID_AntennaPortGetState;
		}

		public int setCmd(byte antennaPort) {
			mParam.clear();
			composeCmd();
			
			return checkResponse(120);
		}
		
		public byte getAntennaState() {
			return mResponse[STATUS_POS+1];
		}
	}
	

	/************************************************************
	 **				RFID_AntennaPortSetConfiguration			*
	 ************************************************************/
	static final class RFID_AntennaPortSetConfiguration extends MtiCmd {
		public RFID_AntennaPortSetConfiguration(){
			mCmdHead = CmdHead.RFID_AntennaPortSetConfiguration;
		};

		public int setCmd(byte antennaPort, short powerLevel, short dwellTime, short numberInventoryCycles) {
			mParam.clear();
			mParam.add(antennaPort);
			addParam(powerLevel);
			addParam(dwellTime);
			addParam(numberInventoryCycles);
			composeCmd();

			return checkResponse(120);
		}
	}
	
	
	/************************************************************
	 **				RFID_AntennaPortGetConfiguration			*
	 ************************************************************/
	static final class RFID_AntennaPortGetConfiguration extends MtiCmd {
		public RFID_AntennaPortGetConfiguration() {
			mCmdHead = CmdHead.RFID_AntennaPortGetConfiguration;
		}

		public int setCmd(byte antennaPort) {
			mParam.clear();
			composeCmd();
			
			return checkResponse(120);
		}
		
		public short getPowerLevel() {
			return getShort(1);
		}
		
		public short getDwellTime() {
			return getShort(3);
		}
		
		public short getNumberInventoryCycles() {
			return getShort(5);
		}
	}
	

	/************************************************************
	 **				RFID_AntennaPortSetSenseThreshold			*
	 ************************************************************/
	static final class RFID_AntennaPortSetSenseThreshold extends MtiCmd {
		public RFID_AntennaPortSetSenseThreshold(){
			mCmdHead = CmdHead.RFID_AntennaPortSetSenseThreshold;
		};

		public int setCmd(int antennaSenseThreshold) {
			mParam.clear();
			addParam(antennaSenseThreshold);
			composeCmd();

			return checkResponse(120);
		}
	}
	
	
	/************************************************************
	 **				RFID_AntennaPortGetSenseThreshold			*
	 ************************************************************/
	static final class RFID_AntennaPortGetSenseThreshold extends MtiCmd {
		public RFID_AntennaPortGetSenseThreshold(){
			mCmdHead = CmdHead.RFID_AntennaPortGetSenseThreshold;
		};

		public int setCmd() {
			mParam.clear();
			composeCmd();

			return checkResponse(120);
		}
		
		public int getSenseThreshold() {
			return getInt(1);
		}
	}
	
}
