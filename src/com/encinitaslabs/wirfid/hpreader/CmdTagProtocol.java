package com.encinitaslabs.wirfid.hpreader;


public class CmdTagProtocol {

	public enum PerformSelect {
		NoSelectCmd((byte)0x00),
		PerformSelectCmd((byte)0x01);
		
		private byte bPerformSelect;
		
		PerformSelect(byte bPerformSelect) {
			this.bPerformSelect = bPerformSelect;
		}
	}

	public enum PerformPostMatch {
		NoPostSigulationMatchCmd((byte)0x00),
		PerformPostSigulationMatchCmd((byte)0x01);
		
		private byte bPerformPostMatch;
		
		PerformPostMatch(byte bPerformPostMatch) {
			this.bPerformPostMatch = bPerformPostMatch;
		}
	}

	public enum PerformGuardMode {
		RealtimeMode((byte)0x00),
		ScreeningMode((byte)0x01),
		NoScreeningDisCmdWorkMode((byte)0x02),
		ScreeningDisCmdWorkMode((byte)0x03),
		NoScreeningEnCmdWorkMode((byte)0x04),
		ScreeningEnCmdWorkMode((byte)0x05);
		
		private byte bPerformGuardMode;
		
		PerformGuardMode(byte bPerformGuardMode) {
			this.bPerformGuardMode = bPerformGuardMode;
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

	public enum PwdPermissions {
		Accessible((byte)0x00),
		AlwaysAccessible((byte)0x01),
		PasswordAccessible((byte)0x02),
		AlwaysNotAccessible((byte)0x03),
		NoChange((byte)0x04);
		
		private byte bPwdPermissions;
		
		PwdPermissions(byte bPwdPermissions) {
			this.bPwdPermissions = bPwdPermissions;
		}
	}

	public enum MemBankPermissions {
		Writeable((byte)0x00),
		AlwaysWriteable((byte)0x01),
		PasswordWriteable((byte)0x02),
		AlwaysNotWriteable((byte)0x03),
		NoChange((byte)0x04);
		
		private byte bMemBankPermissions;
		
		MemBankPermissions(byte bMemBankPermissions) {
			this.bMemBankPermissions = bMemBankPermissions;
		}
	}

	
	/************************************************************
	 **					RFID_18K6CTagInventory					*
	 ************************************************************/
	static final class RFID_18K6CTagInventory extends MtiCmd {
		int iStatus = 0x00;
		
		public RFID_18K6CTagInventory(){
			mCmdHead = CmdHead.RFID_18K6CTagInventory;
		};

		public int setCmd(PerformSelect select, PerformPostMatch PostMatch, PerformGuardMode guardMode) {
			mParam.clear();
			mParam.add(select.bPerformSelect);
			mParam.add(PostMatch.bPerformPostMatch);
			mParam.add(guardMode.bPerformGuardMode);
			if (composeCmd() == true) {
				iStatus = checkResponse(50);
//				if ((iStatus = checkResponse(50)) == 0x00)
//					iStatus = checkBegin(10);
//				else
//					checkBegin(10);				
			} else {
				iStatus = 0xff12; // USB send error
			}
			return iStatus;
		}
		
		public int setCmd(byte select, byte postMatch, byte guardMode) {
			mParam.clear();
			mParam.add(select);
			mParam.add(postMatch);
			mParam.add(guardMode);
			if (composeCmd() == true) {
				iStatus = checkResponse(50);
//				if ((iStatus = checkResponse(50)) == 0x00)
//					iStatus = checkBegin(10);
//				else
//					checkBegin(10);				
			} else {
				iStatus = 0xff12; // USB send error
			}
			return iStatus;
		}
	}

	
	/************************************************************
	 **					RFID_18K6CTagRead						*
	 ************************************************************/
	static final class RFID_18K6CTagRead extends MtiCmd {
		int iStatus = 0x00;
		
		public RFID_18K6CTagRead(){
			mCmdHead = CmdHead.RFID_18K6CTagRead;
		};

		public int setCmd(Bank bank, short offset, byte count, byte retryCount, PerformSelect select, PerformPostMatch postMatch) {
			mParam.clear();
			mParam.add(bank.bBank);
			addParam(offset);
			mParam.add(count);
			mParam.add(retryCount);
			mParam.add(select.bPerformSelect);
			mParam.add(postMatch.bPerformPostMatch);
			composeCmd();

			if((iStatus =checkResponse(50)) == 0x00)
				iStatus = checkBegin(10);
			return iStatus;
		}
		
		public int setCmd(byte bank, short offset, byte count, byte retryCount, byte select, byte postMatch) {
			mParam.clear();
			mParam.add(bank);
			addParam(offset);
			mParam.add(count);
			mParam.add(retryCount);
			mParam.add(select);
			mParam.add(postMatch);
			composeCmd();

			if((iStatus =checkResponse(50)) == 0x00)
				iStatus = checkBegin(10);
			return iStatus;
		}
	}
	
	
	/************************************************************
	 **					RFID_18K6CTagWrite						*
	 ************************************************************/
	static final class RFID_18K6CTagWrite extends MtiCmd {
		public RFID_18K6CTagWrite(){
			mCmdHead = CmdHead.RFID_18K6CTagWrite;
		};

		public int setCmd(Bank bank, short offset, short data, byte retryCount, PerformSelect select, PerformPostMatch postMatch) {
			mParam.clear();
			mParam.add(bank.bBank);
			addParam(offset);
			addParam(data);
			mParam.add(retryCount);
			mParam.add(select.bPerformSelect);
			mParam.add(postMatch.bPerformPostMatch);
			composeCmd();

			return checkResponse(120);
		}
		
		public int setCmd(byte bank, short offset, short data, byte retryCount, byte select, byte postMatch) {
			mParam.clear();
			mParam.add(bank);
			addParam(offset);
			addParam(data);
			mParam.add(retryCount);
			mParam.add(select);
			mParam.add(postMatch);
			composeCmd();

			return checkResponse(120);
		}
	}
	
	
	/************************************************************
	 **					RFID_18K6CTagKill						*
	 ************************************************************/
	static final class RFID_18K6CTagKill extends MtiCmd {
		public RFID_18K6CTagKill(){
			mCmdHead = CmdHead.RFID_18K6CTagKill;
		};

		public int setCmd(int killPwd, byte retryCount, PerformSelect select, PerformPostMatch postMatch) {
			mParam.clear();
			addParam(killPwd);
			mParam.add(retryCount);
			mParam.add(select.bPerformSelect);
			mParam.add(postMatch.bPerformPostMatch);
			composeCmd();

			return checkResponse(120);
		}
		
		public int setCmd(int killPwd, byte retryCount, byte select, byte postMatch) {
			mParam.clear();
			addParam(killPwd);
			mParam.add(retryCount);
			mParam.add(select);
			mParam.add(postMatch);
			composeCmd();

			return checkResponse(120);
		}
	}
	
	
	/************************************************************
	 **					RFID_18K6CTagLock						*
	 ************************************************************/
	static final class RFID_18K6CTagLock extends MtiCmd {
		public RFID_18K6CTagLock(){
			mCmdHead = CmdHead.RFID_18K6CTagLock;
		};

		public int setCmd(PwdPermissions killPwd, PwdPermissions accessPwd,
				MemBankPermissions epcMemBank, MemBankPermissions tidMemBank, MemBankPermissions userMemBank,
				byte retryCount, PerformSelect select, PerformPostMatch postMatch) {
			mParam.clear();
			mParam.add(killPwd.bPwdPermissions);
			mParam.add(accessPwd.bPwdPermissions);
			mParam.add(epcMemBank.bMemBankPermissions);
			mParam.add(tidMemBank.bMemBankPermissions);
			mParam.add(userMemBank.bMemBankPermissions);
			mParam.add(retryCount);
			mParam.add(select.bPerformSelect);
			mParam.add(postMatch.bPerformPostMatch);
			composeCmd();

			return checkResponse(120);
		}
		
		public int setCmd(byte killPwd, byte accessPwd, byte epcMemBank, byte tidMemBank, byte userMemBank,
				byte retryCount, byte select, byte postMatch) {
			mParam.clear();
			mParam.add(killPwd);
			mParam.add(accessPwd);
			mParam.add(epcMemBank);
			mParam.add(tidMemBank);
			mParam.add(userMemBank);
			mParam.add(retryCount);
			mParam.add(select);
			mParam.add(postMatch);
			composeCmd();

			return checkResponse(120);
		}
	}
	
	
	/************************************************************
	 **					RFID_18K6CTagWrite						*
	 ************************************************************/
	static final class RFID_18K6CTagMultipleWrite extends MtiCmd {
		public RFID_18K6CTagMultipleWrite(){
			mCmdHead = CmdHead.RFID_18K6CTagMultipleWrite;
		};

		public int setCmd(Bank bank, short offset, byte dataLength, byte retryCount,
				PerformSelect select, PerformPostMatch postMatch) {
			mParam.clear();
			mParam.add(bank.bBank);
			addParam(offset);
			mParam.add(dataLength);
			addParam((byte)0x0);
			mParam.add(retryCount);
			mParam.add(select.bPerformSelect);
			mParam.add(postMatch.bPerformPostMatch);
			composeCmd();

			return checkResponse(120);
		}
		
		public int setCmd(byte bank, short offset, byte dataLength, byte retryCount, byte select, byte postMatch) {
			mParam.clear();
			mParam.add(bank);
			addParam(offset);
			mParam.add(dataLength);
			mParam.add((byte)0x0);
			mParam.add(retryCount);
			mParam.add(select);
			mParam.add(postMatch);
			composeCmd();

			return checkResponse(120);
		}
	}
	
	
	/************************************************************
	 **					RFID_18K6CTagBlockWrite					*
	 ************************************************************/
	static final class RFID_18K6CTagBlockWrite extends MtiCmd {
		public RFID_18K6CTagBlockWrite(){
			mCmdHead = CmdHead.RFID_18K6CTagBlockWrite;
		};

		public int setCmd(Bank bank, short offset, byte dataLength, byte retryCount,
				PerformSelect select, PerformPostMatch postMatch) {
			mParam.clear();
			mParam.add(bank.bBank);
			addParam(offset);
			mParam.add(dataLength);
			addParam((byte)0x0);
			mParam.add(retryCount);
			mParam.add(select.bPerformSelect);
			mParam.add(postMatch.bPerformPostMatch);
			composeCmd();

			return checkResponse(120);
		}
		
		public int setCmd(byte bank, short offset, byte dataLength, byte retryCount, byte select, byte postMatch) {
			mParam.clear();
			mParam.add(bank);
			addParam(offset);
			mParam.add(dataLength);
			mParam.add((byte)0x0);
			mParam.add(retryCount);
			mParam.add(select);
			mParam.add(postMatch);
			composeCmd();

			return checkResponse(120);
		}
	}
	
	
	/************************************************************
	 **					RFID_18K6CTagBlockErase					*
	 ************************************************************/
	static final class RFID_18K6CTagBlockErase extends MtiCmd {
		public RFID_18K6CTagBlockErase(){
			mCmdHead = CmdHead.RFID_18K6CTagBlockErase;
		};

		public int setCmd(Bank bank, short offset, byte dataLength, byte retryCount,
				PerformSelect select, PerformPostMatch postMatch) {
			mParam.clear();
			mParam.add(bank.bBank);
			addParam(offset);
			mParam.add(dataLength);
			addParam((byte)0x0);
			mParam.add(retryCount);
			mParam.add(select.bPerformSelect);
			mParam.add(postMatch.bPerformPostMatch);
			composeCmd();

			return checkResponse(120);
		}
		
		public int setCmd(byte bank, short offset, byte dataLength, byte retryCount, byte select, byte postMatch) {
			mParam.clear();
			mParam.add(bank);
			addParam(offset);
			mParam.add(dataLength);
			mParam.add((byte)0x0);
			mParam.add(retryCount);
			mParam.add(select);
			mParam.add(postMatch);
			composeCmd();

			return checkResponse(120);
		}
	}
}
