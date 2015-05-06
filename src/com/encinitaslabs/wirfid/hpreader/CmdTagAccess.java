package com.encinitaslabs.wirfid.hpreader;

import java.util.Arrays;

public class CmdTagAccess {

	public enum Selected {
		All((byte)0x00),
		Deasserted((byte)0x02),
		Asserted((byte)0x03);
		
		private byte bSelected;
		
		Selected(byte bSelected) {
			this.bSelected = bSelected;
		}
	}

	public enum Session {
		S0((byte)0x00),
		S1((byte)0x01),
		S2((byte)0x02),
		S3((byte)0x03);
		
		private byte bSession;
		
		Session(byte bSession) {
			this.bSession = bSession;
		}
	}

	public enum Target {
		A((byte)0x00),
		B((byte)0x01);
		
		private byte bTarget;
		
		Target(byte bTarget) {
			this.bTarget = bTarget;
		}
	}

	public enum Algorithm {
		FixedQ((byte)0x00),
		DynamicQ((byte)0x01);
		
		private byte bAlgorithm;
		
		Algorithm(byte bAlgorithm) {
			this.bAlgorithm = bAlgorithm;
		}
	}

	public enum ToggleTarget {
		NotToggled((byte)0x00),
		Toggled((byte)0x01);
		
		private byte bToggleTarget;
		
		ToggleTarget(byte bToggleTarget) {
			this.bToggleTarget = bToggleTarget;
		}
	}

	public enum RepeatUntilNoTags {
		No((byte)0x00),
		Yes((byte)0x01);
		
		private byte bRepeatUntilNoTags;
		
		RepeatUntilNoTags(byte bRepeatUntilNoTags) {
			this.bRepeatUntilNoTags = bRepeatUntilNoTags;
		}
	}

	
	/************************************************************
	 **					RFID_18K6CSetQueryTagGroup				*
	 ************************************************************/
	static final class RFID_18K6CSetQueryTagGroup extends MtiCmd {
		public RFID_18K6CSetQueryTagGroup(){
			mCmdHead = CmdHead.RFID_18K6CSetQueryTagGroup;
		};

		public int setCmd(Selected selected, Session session, Target target) {
			mParam.clear();
			mParam.add(selected.bSelected);
			mParam.add(session.bSession);
			mParam.add(target.bTarget);
			composeCmd();

			return checkResponse(120);
		}

		public int setCmd(byte selected, byte session, byte target) {
			mParam.clear();
			mParam.add(selected);
			mParam.add(session);
			mParam.add(target);
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **					RFID_18K6CGetQueryTagGroup				*
	 ************************************************************/
	static final class RFID_18K6CGetQueryTagGroup extends MtiCmd {
		public RFID_18K6CGetQueryTagGroup(){
			mCmdHead = CmdHead.RFID_18K6CGetQueryTagGroup;
		};

		public int setCmd() {
			mParam.clear();
			composeCmd();

			return checkResponse(120);
		}
		
		public byte getSelectedFlag() {
			return mResponse[STATUS_POS+1];
		}
		
		public short getSession() {
			return mResponse[STATUS_POS+2];
		}
		
		public short getTarget() {
			return mResponse[STATUS_POS+3];
		}
	}

	
	/************************************************************
	 **			RFID_18K6CSetCurrentSingulationAlgorithm		*
	 ************************************************************/
	static final class RFID_18K6CSetCurrentSingulationAlgorithm extends MtiCmd {
		public RFID_18K6CSetCurrentSingulationAlgorithm(){
			mCmdHead = CmdHead.RFID_18K6CSetCurrentSingulationAlgorithm;
		};

		public int setCmd(Algorithm algorithm) {
			mParam.clear();
			mParam.add(algorithm.bAlgorithm);
			composeCmd();

			return checkResponse(120);
		}
		
		public int setCmd(byte algorithm) {
			mParam.clear();
			mParam.add(algorithm);
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **			RFID_18K6CGetCurrentSingulationAlgorithm		*
	 ************************************************************/
	static final class RFID_18K6CGetCurrentSingulationAlgorithm extends MtiCmd {
		public RFID_18K6CGetCurrentSingulationAlgorithm(){
			mCmdHead = CmdHead.RFID_18K6CGetCurrentSingulationAlgorithm;
		};

		public int setCmd() {
			mParam.clear();
			composeCmd();

			return checkResponse(120);
		}
		
		public byte getSingulationAlgorithm() {
			return mResponse[STATUS_POS+1];
		}
	}

	
	/************************************************************
	 **		RFID_18K6CSetCurrentSingulationAlgorithmParameters	*
	 ************************************************************/
	static final class RFID_18K6CSetCurrentSingulationAlgorithmParameters extends MtiCmd {
		public RFID_18K6CSetCurrentSingulationAlgorithmParameters(){
			mCmdHead = CmdHead.RFID_18K6CSetCurrentSingulationAlgorithmParameters;
		};

		public int setCmd(Algorithm algorithm, byte qValue, byte retryCount, ToggleTarget toggleTarget, RepeatUntilNoTags repeatUntilNoTags) {
			mParam.clear();
			mParam.add(algorithm.bAlgorithm);
			mParam.add(qValue);
			mParam.add(retryCount);
			mParam.add(toggleTarget.bToggleTarget);
			mParam.add(repeatUntilNoTags.bRepeatUntilNoTags);
			composeCmd();

			return checkResponse(120);
		}

		public int setCmd(byte algorithm, byte qValue, byte retryCount, byte toggleTarget, byte repeatUntilNoTags) {
			mParam.clear();
			mParam.add(algorithm);
			mParam.add(qValue);
			mParam.add(retryCount);
			mParam.add(toggleTarget);
			mParam.add(repeatUntilNoTags);
			composeCmd();

			return checkResponse(120);
		}

		public int setCmd(byte algorithm, byte startQvalue, byte minQvalue, byte maxQvaule, byte retryCount, ToggleTarget toggleTarget, byte thresholdMultiplier) {
			mParam.clear();
			mParam.add(algorithm);
			mParam.add(startQvalue);
			mParam.add(minQvalue);
			mParam.add(maxQvaule);
			mParam.add(retryCount);
			mParam.add(toggleTarget.bToggleTarget);
			mParam.add(thresholdMultiplier);
			composeCmd();

			return checkResponse(120);
		}

		public int setCmd(byte algorithm, byte startQvalue, byte minQvalue, byte maxQvaule, byte retryCount, byte toggleTarget, byte thresholdMultiplier) {
			mParam.clear();
			mParam.add(algorithm);
			mParam.add(startQvalue);
			mParam.add(minQvalue);
			mParam.add(maxQvaule);
			mParam.add(retryCount);
			mParam.add(toggleTarget);
			mParam.add(thresholdMultiplier);
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **		RFID_18K6CGetCurrentSingulationAlgorithmParameters	*
	 ************************************************************/
	static final class RFID_18K6CGetCurrentSingulationAlgorithmParameters extends MtiCmd {
		public RFID_18K6CGetCurrentSingulationAlgorithmParameters(){
			mCmdHead = CmdHead.RFID_18K6CGetCurrentSingulationAlgorithmParameters;
		};

		public int setCmd(Algorithm algorithm) {
			mParam.clear();
			mParam.add(algorithm.bAlgorithm);
			composeCmd();

			return checkResponse(120);
		}

		public int setCmd(byte algorithm) {
			mParam.clear();
			mParam.add(algorithm);
			composeCmd();

			return checkResponse(120);
		}
		
		public byte getQValue() {
			return mResponse[STATUS_POS+1];
		}
		
		public byte getFixedQRetryCount() {
			return mResponse[STATUS_POS+2];
		}
		
		public byte getFixedQToggleTarget() {
			return mResponse[STATUS_POS+3];
		}
		
		public byte getRepeatUntilNoTags() {
			return mResponse[STATUS_POS+4];
		}
		
		public byte getStartQValue() {
			return mResponse[STATUS_POS+1];
		}
		
		public byte getMinQValue() {
			return mResponse[STATUS_POS+2];
		}
		
		public byte getMaxQValue() {
			return mResponse[STATUS_POS+3];
		}
		
		public byte getDynamicQRetryCount() {
			return mResponse[STATUS_POS+4];
		}
		
		public byte getDynamicQToggleTarget() {
			return mResponse[STATUS_POS+5];
		}
		
		public byte getThresholdMultiPlier() {
			return mResponse[STATUS_POS+6];
		}
	}

	
	/************************************************************
	 **				RFID_18K6CSetTagAccessPassword				*
	 ************************************************************/
	static final class RFID_18K6CSetTagAccessPassword extends MtiCmd {
		public RFID_18K6CSetTagAccessPassword(){
			mCmdHead = CmdHead.RFID_18K6CSetTagAccessPassword;
		};

		public int setCmd(int password) {
			mParam.clear();
			addParam(password);
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **				RFID_18K6CGetTagAccessPassword				*
	 ************************************************************/
	static final class RFID_18K6CGetTagAccessPassword extends MtiCmd {
		public RFID_18K6CGetTagAccessPassword(){
			mCmdHead = CmdHead.RFID_18K6CGetTagAccessPassword;
		};

		public int setCmd() {
			mParam.clear();
			composeCmd();

			return checkResponse(120);
		}
		
		public String getPassword() {
			byte[] password = Arrays.copyOfRange(mResponse, STATUS_POS + 1, STATUS_POS + 5);

			return byteArrayToString(password, false);
		}
	}

	
	/************************************************************
	 **				RFID_18K6CSetTagWriteDataBuffer				*
	 ************************************************************/
	static final class RFID_18K6CSetTagWriteDataBuffer extends MtiCmd {
		public RFID_18K6CSetTagWriteDataBuffer(){
			mCmdHead = CmdHead.RFID_18K6CSetTagWriteDataBuffer;
		};

		public int setCmd(byte bufferIndex,short bufferData) {
			mParam.clear();
			mParam.add(bufferIndex);
			addParam(bufferData);
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **				RFID_18K6CGetTagWriteDataBuffer				*
	 ************************************************************/
	static final class RFID_18K6CGetTagWriteDataBuffer extends MtiCmd {
		public RFID_18K6CGetTagWriteDataBuffer(){
			mCmdHead = CmdHead.RFID_18K6CGetTagWriteDataBuffer;
		};

		public int setCmd(byte bufferIndex) {
			mParam.clear();
			mParam.add(bufferIndex);
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **				RFID_18K6CGetGuardBufferTagNum				*
	 ************************************************************/
	static final class RFID_18K6CGetGuardBufferTagNum extends MtiCmd {
		public RFID_18K6CGetGuardBufferTagNum(){
			mCmdHead = CmdHead.RFID_18K6CGetGuardBufferTagNum;
		};

		public int setCmd() {
			mParam.clear();
			composeCmd();

			return checkResponse(120);
		}
	}

	
	/************************************************************
	 **				RFID_18K6CGetGuardBufferTagInfo				*
	 ************************************************************/
	static final class RFID_18K6CGetGuardBufferTagInfo extends MtiCmd {
		public RFID_18K6CGetGuardBufferTagInfo(){
			mCmdHead = CmdHead.RFID_18K6CGetGuardBufferTagInfo;
		};

		public int setCmd(byte bufferIndex) {
			mParam.clear();
			mParam.add((byte)0);
			mParam.add(bufferIndex);
			composeCmd();

			return checkResponse(120);
		}
	}

}
