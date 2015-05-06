package com.encinitaslabs.wirfid.hpreader;

public class CmdTagSelect {
	
	public enum ActiveState {
		Disable((byte)0x00),
		Enable((byte)0x01);
		
		private byte bActiveState;
		
		ActiveState(byte bActiveState) {
			this.bActiveState = bActiveState;
		}
	}

	public enum Bank {
		Reserved((byte)0x00),
		EPC((byte)0x01),
		TID((byte)0x02),
		User((byte)0x03);
		
		private byte bBank;
		
		Bank(byte bBank) {
			this.bBank = bBank;
		}
	}

	public enum Target {
		S0((byte)0x00),
		S1((byte)0x01),
		S2((byte)0x02),
		S4((byte)0x03),
		Selected((byte)0x05);
		
		private byte bTarget;
		
		Target(byte bTarget) {
			this.bTarget = bTarget;
		}
	}
	
	public enum Truncation {
		Disable((byte)0x00),
		Enable((byte)0x01);
		
		private byte bTruncation;
		
		Truncation(byte bTruncation) {
			this.bTruncation = bTruncation;
		}
	}
	
	public enum Match {
		NotMatch((byte)0x00),
		Match((byte)0x01);
		
		private byte bMatch;
		
		Match(byte bMatch) {
			this.bMatch = bMatch;
		}
	}

	
	/************************************************************
	 **				RFID_18K6CSetActiveSelectCriteria			*
	 ************************************************************/
	static final class RFID_18K6CSetActiveSelectCriteria extends MtiCmd {
		public RFID_18K6CSetActiveSelectCriteria(){
			mCmdHead = CmdHead.RFID_18K6CSetActiveSelectCriteria;
		};

		public int setCmd(byte criteriaIndex, ActiveState activeState) {
			mParam.clear();
			mParam.add(criteriaIndex);
			mParam.add(activeState.bActiveState);
			composeCmd();

			return checkResponse(120);
		}

		public int setCmd(byte criteriaIndex, byte activeState) {
			mParam.clear();
			mParam.add(criteriaIndex);
			mParam.add(activeState);
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **				RFID_18K6CGetActiveSelectCriteria			*
	 ************************************************************/
	static final class RFID_18K6CGetActiveSelectCriteria extends MtiCmd {
		public RFID_18K6CGetActiveSelectCriteria(){
			mCmdHead = CmdHead.RFID_18K6CGetActiveSelectCriteria;
		};

		public int setCmd(byte criteriaIndex) {
			mParam.clear();
			mParam.add(criteriaIndex);
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **					RFID_18K6CSetSelectCriteria				*
	 ************************************************************/
	static final class RFID_18K6CSetSelectCriteria extends MtiCmd {
		public RFID_18K6CSetSelectCriteria(){
			mCmdHead = CmdHead.RFID_18K6CSetSelectCriteria;
		};

		public int setCmd(byte criteriaIndex, Bank bank, short offset, byte count,
				Target target, byte action, Truncation truncation) {
			mParam.clear();
			mParam.add(criteriaIndex);
			mParam.add(bank.bBank);
			addParam(offset);
			mParam.add(count);
			mParam.add(target.bTarget);
			mParam.add(action);
			mParam.add(truncation.bTruncation);
			composeCmd();

			return checkResponse(120);
		}

		public int setCmd(byte criteriaIndex, byte bank, short offset, byte count,
				byte target, byte action, byte truncation) {
			mParam.clear();
			mParam.add(criteriaIndex);
			mParam.add(bank);
			addParam(offset);
			mParam.add(count);
			mParam.add(target);
			mParam.add(action);
			mParam.add(truncation);
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **					RFID_18K6CGetSelectCriteria				*
	 ************************************************************/
	static final class RFID_18K6CGetSelectCriteria extends MtiCmd {
		public RFID_18K6CGetSelectCriteria(){
			mCmdHead = CmdHead.RFID_18K6CGetSelectCriteria;
		};

		public int setCmd(byte criteriaIndex) {
			mParam.clear();
			mParam.add(criteriaIndex);
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **					RFID_18K6CSetSelectMaskData				*
	 ************************************************************/
	static final class RFID_18K6CSetSelectMaskData extends MtiCmd {
		public RFID_18K6CSetSelectMaskData(){
			mCmdHead = CmdHead.RFID_18K6CSetSelectMaskData;
		};

		public int setCmd(byte criteriaIndex, byte maskIndex, byte maskData0,
				byte maskData1, byte maskData2, byte maskData3) {
			mParam.clear();
			mParam.add(criteriaIndex);
			mParam.add(maskIndex);
			mParam.add(maskData0);
			mParam.add(maskData1);
			mParam.add(maskData2);
			mParam.add(maskData3);
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **					RFID_18K6CGetSelectMaskData				*
	 ************************************************************/
	static final class RFID_18K6CGetSelectMaskData extends MtiCmd {
		public RFID_18K6CGetSelectMaskData(){
			mCmdHead = CmdHead.RFID_18K6CGetSelectMaskData;
		};

		public int setCmd(byte criteriaIndex, byte maskIndex) {
			mParam.clear();
			mParam.add(criteriaIndex);
			mParam.add(maskIndex);
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **				RFID_18K6CSetPostMatchCriteria				*
	 ************************************************************/
	static final class RFID_18K6CSetPostMatchCriteria extends MtiCmd {
		public RFID_18K6CSetPostMatchCriteria(){
			mCmdHead = CmdHead.RFID_18K6CSetPostMatchCriteria;
		};

		public int setCmd(Match match, short offset, short count) {
			mParam.clear();
			mParam.add(match.bMatch);
			addParam(offset);
			addParam(count);
			composeCmd();

			return checkResponse(120);
		}

		public int setCmd(byte match, short offset, short count) {
			mParam.clear();
			mParam.add(match);
			addParam(offset);
			addParam(count);
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **				RFID_18K6CGetPostMatchCriteria				*
	 ************************************************************/
	static final class RFID_18K6CGetPostMatchCriteria extends MtiCmd {
		public RFID_18K6CGetPostMatchCriteria(){
			mCmdHead = CmdHead.RFID_18K6CGetPostMatchCriteria;
		};

		public int setCmd() {
			mParam.clear();
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **				RFID_18K6CSetPostMatchMaskData				*
	 ************************************************************/
	static final class RFID_18K6CSetPostMatchMaskData extends MtiCmd {
		public RFID_18K6CSetPostMatchMaskData(){
			mCmdHead = CmdHead.RFID_18K6CSetPostMatchMaskData;
		};

		public int setCmd(byte maskIndex, byte maskData0, byte maskData1, byte maskData2, byte maskData3) {
			mParam.clear();
			mParam.add(maskIndex);
			mParam.add(maskData0);
			mParam.add(maskData1);
			mParam.add(maskData2);
			mParam.add(maskData3);
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **				RFID_18K6CGetPostMatchMaskData				*
	 ************************************************************/
	static final class RFID_18K6CGetPostMatchMaskData extends MtiCmd {
		public RFID_18K6CGetPostMatchMaskData(){
			mCmdHead = CmdHead.RFID_18K6CGetPostMatchMaskData;
		};

		public int setCmd(byte maskIndex) {
			mParam.clear();
			mParam.add(maskIndex);
			composeCmd();

			return checkResponse(120);
		}
	}
	
}
