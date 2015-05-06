package com.encinitaslabs.wirfid.hpreader;

public class CmdReaderModuleGpioPinAccess {

	public enum Mask {
		GPIO0_Select((byte)0x01),
		GPIO1_Select((byte)0x02),
		GPIO2_Select((byte)0x04),
		GPIO3_Select((byte)0x08);
		
		private byte bMask;
		
		Mask(byte bMask) {
			this.bMask = bMask;
		}
	}

	public enum Configuration {
		GPIO0_AsOutput((byte)0x01),
		GPIO1_AsOutput((byte)0x02),
		GPIO2_AsOutput((byte)0x04),
		GPIO3_AsOutput((byte)0x08);
		
		private byte bConfiguration;
		
		Configuration(byte bConfiguration) {
			this.bConfiguration = bConfiguration;
		}
	}

	public enum Value {
		GPIO0_HighState((byte)0x01),
		GPIO1_HighState((byte)0x02),
		GPIO2_HighState((byte)0x04),
		GPIO3_HighState((byte)0x08);
		
		private byte bValue;
		
		Value(byte bValue) {
			this.bValue = bValue;
		}
	}

	
	/************************************************************
	 **			RFID_RadioSetGpioPinsConfiguration				*
	 ************************************************************/
	static final class RFID_RadioSetGpioPinsConfiguration extends MtiCmd {
		public RFID_RadioSetGpioPinsConfiguration(){
			mCmdHead = CmdHead.RFID_RadioSetGpioPinsConfiguration;
		};

		public int setCmd(Mask mask, Configuration configuration) {
			mParam.clear();
			mParam.add(mask.bMask);
			mParam.add(configuration.bConfiguration);
			composeCmd();

			return checkResponse(120);
		}

		public int setCmd(byte mask, byte configuration) {
			mParam.clear();
			mParam.add(mask);
			mParam.add(configuration);
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **			RFID_RadioGetGpioPinsConfiguration				*
	 ************************************************************/
	static final class RFID_RadioGetGpioPinsConfiguration extends MtiCmd {
		public RFID_RadioGetGpioPinsConfiguration(){
			mCmdHead = CmdHead.RFID_RadioGetGpioPinsConfiguration;
		};

		public int setCmd() {
			mParam.clear();
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **					RFID_RadioWriteGpioPins					*
	 ************************************************************/
	static final class RFID_RadioWriteGpioPins extends MtiCmd {
		public RFID_RadioWriteGpioPins(){
			mCmdHead = CmdHead.RFID_RadioWriteGpioPins;
		};

		public int setCmd(Mask mask, Value value) {
			mParam.clear();
			mParam.add(mask.bMask);
			mParam.add(value.bValue);
			composeCmd();

			return checkResponse(120);
		}

		public int setCmd(byte mask, byte value) {
			mParam.clear();
			mParam.add(mask);
			mParam.add(value);
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **					RFID_RadioReadGpioPins					*
	 ************************************************************/
	static final class RFID_RadioReadGpioPins extends MtiCmd {
		public RFID_RadioReadGpioPins(){
			mCmdHead = CmdHead.RFID_RadioReadGpioPins;
		};

		public int setCmd(Mask mask) {
			mParam.clear();
			mParam.add(mask.bMask);
			composeCmd();

			return checkResponse(120);
		}

		public int setCmd(byte mask) {
			mParam.clear();
			mParam.add(mask);
			composeCmd();

			return checkResponse(120);
		}
	}

}
