package com.encinitaslabs.wirfid.hpreader;

public enum CmdHead {
	// RFID Reader/Module Configuration
	RFID_RadioSetDeviceID((byte)0x00),
	RFID_RadioGetDeviceID((byte)0x01),
	RFID_RadioSetOperationMode((byte)0x02),
	RFID_RadioGetOperationMode((byte)0x03),
	RFID_RadioSetCurrentLinkProfile((byte)0x04),
	RFID_RadioGetCurrentLinkProfile((byte)0x05),
	RFID_RadioWriteRegister((byte)0x06),
	RFID_RadioReadRegister((byte)0x07),
	RFID_RadioWriteBankedRegister((byte)0x08),
	RFID_RadioReadBankedRegister((byte)0x09),
	RFID_RadioReadRegisterInfo((byte)0x0A),
	// Antenna Port Configuration
	RFID_AntennaPortSetState((byte)0x10),
	RFID_AntennaPortGetState((byte)0x11),
	RFID_AntennaPortSetConfiguration((byte)0x12),
	RFID_AntennaPortGetConfiguration((byte)0x13),
	RFID_AntennaPortSetSenseThreshold((byte)0x14),
	RFID_AntennaPortGetSenseThreshold((byte)0x15),
	// ISO 18000-6C Tag Select Operation
	RFID_18K6CSetActiveSelectCriteria((byte)0x20),
	RFID_18K6CGetActiveSelectCriteria((byte)0x21),
	RFID_18K6CSetSelectCriteria((byte)0x22),
	RFID_18K6CGetSelectCriteria((byte)0x23),
	RFID_18K6CSetSelectMaskData((byte)0x24),
	RFID_18K6CGetSelectMaskData((byte)0x25),
	RFID_18K6CSetPostMatchCriteria((byte)0x26),
	RFID_18K6CGetPostMatchCriteria((byte)0x27),
	RFID_18K6CSetPostMatchMaskData((byte)0x28),
	RFID_18K6CGetPostMatchMaskData((byte)0x29),
	// ISO 18000-6C Tag Access Parameters
	RFID_18K6CSetQueryTagGroup((byte)0x30),
	RFID_18K6CGetQueryTagGroup((byte)0x31),
	RFID_18K6CSetCurrentSingulationAlgorithm((byte)0x32),
	RFID_18K6CGetCurrentSingulationAlgorithm((byte)0x33),
	RFID_18K6CSetCurrentSingulationAlgorithmParameters((byte)0x34),
	RFID_18K6CGetCurrentSingulationAlgorithmParameters((byte)0x35),
	RFID_18K6CSetTagAccessPassword((byte)0x36),
	RFID_18K6CGetTagAccessPassword((byte)0x37),
	RFID_18K6CSetTagWriteDataBuffer((byte)0x38),
	RFID_18K6CGetTagWriteDataBuffer((byte)0x39),
	RFID_18K6CGetGuardBufferTagNum((byte)0x3A),
	RFID_18K6CGetGuardBufferTagInfo((byte)0x3B),
	// ISO 18000-6C Tag Protocol Operation
	RFID_18K6CTagInventory((byte)0x40),
	RFID_18K6CTagRead((byte)0x41),
	RFID_18K6CTagWrite((byte)0x42),
	RFID_18K6CTagKill((byte)0x43),
	RFID_18K6CTagLock((byte)0x44),
	RFID_18K6CTagMultipleWrite((byte)0x45),
	RFID_18K6CTagBlockWrite((byte)0x46),
	RFID_18K6CTagBlockErase((byte)0x47),
	// RFID Reader/Module Control Operation
	RFID_ControlCancel((byte)0x50),
	RFID_ControlPause((byte)0x52),
	RFID_ControlResume((byte)0x53),
	RFID_ControlSoftReset((byte)0x54),
	RFID_ControlResetToBootloader((byte)0x55),
	RFID_ControlSetPowerState((byte)0x56),
	RFID_ControlGetPowerState((byte)0x57),
	// RFID Reader/Module Firmware Access
	RFID_MacGetFirmwareVersion((byte)0x60),
	RFID_MacGetDebug((byte)0x61),
	RFID_MacClearError((byte)0x62),
	RFID_MacGetError((byte)0x63),
	RFID_MacGetBootloaderVersion((byte)0x64),
	reserved((byte)0x65),
	RFID_MacWriteOemData((byte)0x66),
	RFID_MacReadOemData((byte)0x67),
	RFID_MacBypassWriteRegister((byte)0x68),
	RFID_MacBypassReadRegister((byte)0x69),
	RFID_MacSetRegion((byte)0x6A),
	RFID_MacGetRegion((byte)0x6B),
	RFID_MacGetOEMCfgVersion((byte)0x6C),
	RFID_MacGetOEMCfgUpdateNumber((byte)0x6D),
	// RFID Reader/Module GPIO Pin Access (Encinitas Labs)
	RFID_RadioSetGpioPinsConfiguration((byte)0x70),
	RFID_RadioGetGpioPinsConfiguration((byte)0x71),
	RFID_RadioWriteGpioPins((byte)0x72),
	RFID_RadioReadGpioPins((byte)0x73),
	// RFID Reader/Module Region Test Support
	RFID_TestSetAntennaPortConfiguration((byte)0x80),
	RFID_TestGetAntennaPortConfiguration((byte)0x81),
	RFID_TestSetFrequencyConfiguration((byte)0x82),
	RFID_TestGetFrequencyConfiguration((byte)0x83),
	RFID_TestSetRandomDataPulseTime((byte)0x84),
	RFID_TestGetRandomDataPulseTime((byte)0x85),
	RFID_TestSetInventoryConfiguration((byte)0x86),
	RFID_TestGetInventoryConfiguration((byte)0x87),
	RFID_TestTurnOnCarrierWave((byte)0x88),
	RFID_TestTurnOffCarrierWave((byte)0x89),
	RFID_TestInjectRandomData((byte)0x8A),
	RFID_TestTransmitRandomData((byte)0x8B);
	
	private byte byte1st;
	
	CmdHead(byte byte1st) {
		this.byte1st = byte1st;
	}
	
	public byte get1stCmd() {
		return this.byte1st;
	}
}
