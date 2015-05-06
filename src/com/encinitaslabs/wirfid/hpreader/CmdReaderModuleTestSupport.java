package com.encinitaslabs.wirfid.hpreader;

public class CmdReaderModuleTestSupport {

	public enum PhysicalPort {
		Transmit((byte)0x00),
		Receive((byte)0x02);
		
		private byte bPhysicalPort;
		
		PhysicalPort(byte bPhysicalPort) {
			this.bPhysicalPort = bPhysicalPort;
		}
	}

	public enum ChannelFlag {
		RegionOperation((byte)0x00),
		SingleChannel((byte)0x02);
		
		private byte bChannelFlag;
		
		ChannelFlag(byte bChannelFlag) {
			this.bChannelFlag = bChannelFlag;
		}
	}

	public enum ContinuousOperation {
		Disabled((byte)0x00),
		Enabled((byte)0x02);
		
		private byte bContinuousOperation;
		
		ContinuousOperation(byte bContinuousOperation) {
			this.bContinuousOperation = bContinuousOperation;
		}
	}

	public enum Control {
		Continuous((byte)0x00),
		Pulsing((byte)0x02);
		
		private byte bControl;
		
		Control(byte bControl) {
			this.bControl = bControl;
		}
	}

	
	/************************************************************
	 **			RFID_TestSetAntennaPortConfiguration			*
	 ************************************************************/
	static final class RFID_TestSetAntennaPortConfiguration extends MtiCmd {
		public RFID_TestSetAntennaPortConfiguration(){
			mCmdHead = CmdHead.RFID_TestSetAntennaPortConfiguration;
		};

		public int setCmd(PhysicalPort physicalPort, short powerLevel) {
			mParam.clear();
			mParam.add(physicalPort.bPhysicalPort);
			addParam(powerLevel);
			composeCmd();

			return checkResponse(120);
		}

		public int setCmd(byte physicalPort, short powerLevel) {
			mParam.clear();
			mParam.add(physicalPort);
			addParam(powerLevel);
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **			RFID_TestGetAntennaPortConfiguration			*
	 ************************************************************/
	static final class RFID_TestGetAntennaPortConfiguration extends MtiCmd {
		public RFID_TestGetAntennaPortConfiguration(){
			mCmdHead = CmdHead.RFID_TestGetAntennaPortConfiguration;
		};

		public int setCmd() {
			mParam.clear();
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **				RFID_TestSetFrequencyConfiguration			*
	 ************************************************************/
	static final class RFID_TestSetFrequencyConfiguration extends MtiCmd {
		public RFID_TestSetFrequencyConfiguration(){
			mCmdHead = CmdHead.RFID_TestSetFrequencyConfiguration;
		};

		public int setCmd(ChannelFlag channelFlag, int exactFrequency) {
			mParam.clear();
			mParam.add(channelFlag.bChannelFlag);
			addParam(exactFrequency);
			composeCmd();

			return checkResponse(120);
		}

		public int setCmd(byte channelFlag, int exactFrequency) {
			mParam.clear();
			mParam.add(channelFlag);
			addParam(exactFrequency);
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **			RFID_TestGetFrequencyConfiguration				*
	 ************************************************************/
	static final class RFID_TestGetFrequencyConfiguration extends MtiCmd {
		public RFID_TestGetFrequencyConfiguration(){
			mCmdHead = CmdHead.RFID_TestGetFrequencyConfiguration;
		};

		public int setCmd() {
			mParam.clear();
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **				RFID_TestSetRandomDataPulseTime				*
	 ************************************************************/
	static final class RFID_TestSetRandomDataPulseTime extends MtiCmd {
		public RFID_TestSetRandomDataPulseTime(){
			mCmdHead = CmdHead.RFID_TestSetRandomDataPulseTime;
		};

		public int setCmd(short onTime, short offTime) {
			mParam.clear();
			addParam(onTime);
			addParam(offTime);
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **				RFID_TestGetRandomDataPulseTime				*
	 ************************************************************/
	static final class RFID_TestGetRandomDataPulseTime extends MtiCmd {
		public RFID_TestGetRandomDataPulseTime(){
			mCmdHead = CmdHead.RFID_TestGetRandomDataPulseTime;
		};

		public int setCmd() {
			mParam.clear();
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **			RFID_TestSetInventoryConfiguration				*
	 ************************************************************/
	static final class RFID_TestSetInventoryConfiguration extends MtiCmd {
		public RFID_TestSetInventoryConfiguration(){
			mCmdHead = CmdHead.RFID_TestSetInventoryConfiguration;
		};

		public int setCmd(ContinuousOperation continuousOperation) {
			mParam.clear();
			mParam.add(continuousOperation.bContinuousOperation);
			composeCmd();

			return checkResponse(120);
		}

		public int setCmd(byte continuousOperation) {
			mParam.clear();
			mParam.add(continuousOperation);
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **			RFID_TestGetInventoryConfiguration				*
	 ************************************************************/
	static final class RFID_TestGetInventoryConfiguration extends MtiCmd {
		public RFID_TestGetInventoryConfiguration(){
			mCmdHead = CmdHead.RFID_TestGetInventoryConfiguration;
		};

		public int setCmd() {
			mParam.clear();
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **				RFID_TestTurnOnCarrierWave					*
	 ************************************************************/
	static final class RFID_TestTurnOnCarrierWave extends MtiCmd {
		public RFID_TestTurnOnCarrierWave(){
			mCmdHead = CmdHead.RFID_TestTurnOnCarrierWave;
		};

		public int setCmd() {
			mParam.clear();
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **				RFID_TestTurnOffCarrierWave					*
	 ************************************************************/
	static final class RFID_TestTurnOffCarrierWave extends MtiCmd {
		public RFID_TestTurnOffCarrierWave(){
			mCmdHead = CmdHead.RFID_TestTurnOffCarrierWave;
		};

		public int setCmd() {
			mParam.clear();
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **				RFID_TestInjectRandomData					*
	 ************************************************************/
	static final class RFID_TestInjectRandomData extends MtiCmd {
		public RFID_TestInjectRandomData(){
			mCmdHead = CmdHead.RFID_TestInjectRandomData;
		};

		public int setCmd(int count) {
			mParam.clear();
			addParam(count);
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **				RFID_TestTransmitRandomData					*
	 ************************************************************/
	static final class RFID_TestTransmitRandomData extends MtiCmd {
		public RFID_TestTransmitRandomData(){
			mCmdHead = CmdHead.RFID_TestTransmitRandomData;
		};

		public int setCmd(Control control, int duration) {
			mParam.clear();
			mParam.add(control.bControl);
			addParam(duration);
			composeCmd();

			return checkResponse(120);
		}

		public int setCmd(byte control, int duration) {
			mParam.clear();
			mParam.add(control);
			addParam(duration);
			composeCmd();

			return checkResponse(120);
		}
	}

}
