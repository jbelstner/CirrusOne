package com.encinitaslabs.wirfid.hpreader;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.InputFilter;
import android.widget.Toast;

public class FRAG_Config extends PreferenceFragment{
	public PreferenceScreen prefScr;

	public static FRAG_Config newInstance() {
		FRAG_Config f = new FRAG_Config();
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.frag_config);
        
        prefScr = getPreferenceScreen();

		initListPreference("cfg_region");
		initSeekBarPreference("cfg_pwr_level");
		initEditTextPreference("cfg_dwell_time");
		initEditTextPreference("cfg_number_inventory_cycles");
		initEditTextPreference("cfg_antenna_port");
		
		initListPreference("cfg_link_profile");
		
		initListPreference("cfg_selected_flag");
		initListPreference("cfg_session");
		initListPreference("cfg_target");
		
//		initListPreference("cfg_q_algorithm");
		
		initSeekBarPreference("cfg_q_value");
		initEditTextPreference("cfg_fixed_retry_count");
		initCheckBoxPreference("cfg_fixed_toggle_target");
		initCheckBoxPreference("cfg_repeat_until_no_tages");
/*
		initSeekBarPreference("cfg_start_qvalue");
		initSeekBarPreference("cfg_min_qvalue");
		initSeekBarPreference("cfg_max_qvalue");
		initEditTextPreference("cfg_dynamic_retry_count");
		initCheckBoxPreference("cfg_dynamic_toggle_target");
		initSeekBarPreference("cfg_threshold_multiplier");
*/
		initEditTextPreference("cfg_interval_delay");
		initEditTextPreference("cfg_ping_delay");
		initEditTextPreference("cfg_web_url");
		initEditTextPreference("cfg_wss_url");
		initCheckBoxPreference("cfg_bluetooth_control");
		initCheckBoxPreference("cfg_localsocket_control");
		initCheckBoxPreference("cfg_scan_autostart");

		if(savedInstanceState == null) {
        	if(RfidContainer.getUsbState()) {
				setReaderRegion();
				setRfConfig();
				setQueryTagGroup();
				setLinkProfile();
//				setSingulationAlgorithm();
				setFixedQAlgorithm();
//				setDynamicQAlgorithm();
			} else {
				Toast.makeText(getActivity(), "The reader is not connected", Toast.LENGTH_SHORT).show();
			}
		}
		
	}

	private void initSeekBarPreference(final String strPrefName) {
		SeekBarPreference skPref = (SeekBarPreference) prefScr.findPreference(strPrefName);
		skPref.setSummary(skPref.getValue());

		if(strPrefName.equals("cfg_pwr_level")) {
			if(RfidContainer.getPid() == 0x824)
				skPref.setMaxValue(24 + 10);
			else
				skPref.setMaxValue(30 + 10);
		}

		skPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				SeekBarPreference skPref = (SeekBarPreference)preference;
				String strPref = newValue.toString();

				skPref.setSummary(strPref);
				
				if(strPrefName.equals("cfg_pwr_level")) {
					setRfConfig();
				} else if(strPrefName.equals("cfg_q_value"))
					setFixedQAlgorithm();
/*
				else if(strPrefName.equals("cfg_start_qvalue") || strPrefName.equals("cfg_min_qvalue") || 
						strPrefName.equals("cfg_max_qvalue") || strPrefName.equals("cfg_dynamic_retry_count") || 
						strPrefName.equals("cfg_dynamic_toggle_target") || strPrefName.equals("cfg_threshold_multiplier"))
					setDynamicQAlgorithm();
*/					
				return false;
			}
		});
	}
	
	private void initListPreference(final String strPrefName) {
        ListPreference lPref = (ListPreference) prefScr.findPreference(strPrefName);
        lPref.setSummary(lPref.getValue());

        lPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				ListPreference lPref = (ListPreference) preference;
				String strPref = newValue.toString();
					
				lPref.setValue(strPref);
				lPref.setSummary(strPref);
				
				if(strPrefName.equals("cfg_region")) {
					setReaderRegion();
				} else if(strPrefName.equals("cfg_selected_flag") || strPrefName.equals("cfg_session") || strPrefName.equals("cfg_target")) {
					setQueryTagGroup();
				} else if(strPrefName.equals("cfg_link_profile")) {
					setLinkProfile();
/*
				} else if(strPrefName.equals("cfg_q_algorithm")) {
					setSingulationAlgorithm();
*/
				}
				return false;
			}
		});
	}
	
	
	private void initEditTextPreference(final String strPrefName){
		EditTextPreference etPref = (EditTextPreference) prefScr.findPreference(strPrefName);
        etPref.setSummary(etPref.getText());
		etPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				EditTextPreference etPref = (EditTextPreference) preference;
				String strPref = newValue.toString();
				
				etPref.setText(strPref);
				etPref.setSummary(strPref);

				if(strPrefName.equals("cfg_dwell_time") || strPrefName.equals("cfg_number_inventory_cycles")) {
					etPref.getEditText().setFilters(new InputFilter[]{
							new InputFilterMinMax("0", "10000")
					});
					setRfConfig();
				}
				else if(strPrefName.equals("cfg_interval_delay")) {
					etPref.getEditText().setFilters(new InputFilter[]{
							new InputFilterMinMax("0", "10000")
					});
				}
				else if(strPrefName.equals("cfg_fixed_retry_count")) {
					etPref.getEditText().setFilters(new InputFilter[]{
							new InputFilterMinMax("0", "255")
					});
					setFixedQAlgorithm();
				}
/*
				else if(strPrefName.equals("cfg_dynamic_retry_count")) {
					etPref.getEditText().setFilters(new InputFilter[]{
							new InputFilterMinMax("0", "255")
					});
					setDynamicQAlgorithm();
				}
*/
				return false;
			}
		});
	}


	private void initCheckBoxPreference(final String strPrefName) {
        CheckBoxPreference cbPref = (CheckBoxPreference) prefScr.findPreference(strPrefName);
        
		cbPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if(strPrefName.equals("cfg_fixed_toggle_target") || strPrefName.equals("cfg_repeat_until_no_tages")) {
					setFixedQAlgorithm();
				}
				return true;
			}
		});
	}


	private int setReaderRegion() {
		int iStatus = 0xffff;
		
       	if(RfidContainer.getUsbState()) {
			ListPreference lPref = (ListPreference) prefScr.findPreference("cfg_region");
	
			CmdReaderModuleFirmwareAccess.RFID_MacSetRegion mtiCmd = new CmdReaderModuleFirmwareAccess.RFID_MacSetRegion();
			iStatus = mtiCmd.setCmd((byte)lPref.findIndexOfValue(lPref.getValue()));
			getReaderRegion();
       	} else {
			Toast.makeText(getActivity(), "The Reader is not connected", Toast.LENGTH_SHORT).show();
       	}
		return iStatus;
	}
	
	private int getReaderRegion() {
		int iStatus = 0xffff;

       	if(RfidContainer.getUsbState()) {
			CmdReaderModuleFirmwareAccess.RFID_MacGetRegion mtiCmd = new CmdReaderModuleFirmwareAccess.RFID_MacGetRegion();
			if((iStatus = mtiCmd.setCmd()) == 0x00) {
				ListPreference lPref = (ListPreference) prefScr.findPreference("cfg_region");
	
				lPref.setValueIndex(mtiCmd.getRegion());
				lPref.setSummary(lPref.getValue());
			}
       	} else {
			Toast.makeText(getActivity(), "The Reader is not connected", Toast.LENGTH_SHORT).show();
       	}
		return iStatus;
	}

	private int setRfConfig() {
		int iStatus = 0xffff;

       	if(RfidContainer.getUsbState()) {
			SeekBarPreference skPowerLevel = (SeekBarPreference) prefScr.findPreference("cfg_pwr_level");
			EditTextPreference etDwellTime = (EditTextPreference) prefScr.findPreference("cfg_dwell_time");
			EditTextPreference etNumInventory = (EditTextPreference) prefScr.findPreference("cfg_number_inventory_cycles");

			CmdAntennaPortConf.RFID_AntennaPortSetConfiguration mMtiCmd = new CmdAntennaPortConf.RFID_AntennaPortSetConfiguration();
			iStatus = mMtiCmd.setCmd((byte)0,
					(short)(Short.parseShort(skPowerLevel.getSummary().toString()) * 10),
					(short)(Integer.parseInt(etDwellTime.getSummary().toString()) & 0xffff),
					(short)(Integer.parseInt(etNumInventory.getSummary().toString()) & 0xffff));
			getRfConfig();
       	} else {
			Toast.makeText(getActivity(), "The Reader is not connected", Toast.LENGTH_SHORT).show();
       	}
		return iStatus;
	}

	private int getRfConfig() {
		int iStatus = 0xffff;

       	if(RfidContainer.getUsbState()) {
			CmdAntennaPortConf.RFID_AntennaPortGetConfiguration mMtiCmd = new CmdAntennaPortConf.RFID_AntennaPortGetConfiguration();
			if((iStatus = mMtiCmd.setCmd((byte)0)) == 0x00) {
//				String scrSummary = "";
				
				SeekBarPreference sbPref = (SeekBarPreference) prefScr.findPreference("cfg_pwr_level");
				sbPref.setValue(String.valueOf(mMtiCmd.getPowerLevel() / 10));
				sbPref.setSummary(sbPref.getValue());
//				scrSummary += etPref.getText() + "dBm / ";
				
				EditTextPreference etPref = (EditTextPreference) prefScr.findPreference("cfg_dwell_time");
				etPref.setText(String.valueOf(mMtiCmd.getDwellTime() & 0xffff));
				etPref.setSummary(etPref.getText());
//				scrSummary += etPref.getText() + "ms / ";
				
				etPref = (EditTextPreference) prefScr.findPreference("cfg_number_inventory_cycles");
				etPref.setText(String.valueOf(mMtiCmd.getNumberInventoryCycles() & 0xffff));
				etPref.setSummary(etPref.getText()); 
//				scrSummary += etPref.getText();
				
//				PreferenceScreen subScr = (PreferenceScreen) prefScr.findPreference("cfg_rf_conf");
//				subScr.setSummary(scrSummary);
			}
       	} else {
			Toast.makeText(getActivity(), "The Reader is not connected", Toast.LENGTH_SHORT).show();
       	}
		return iStatus;
	}
	
	private int setQueryTagGroup() {
		int iStatus = 0xffff;

       	if(RfidContainer.getUsbState()) {
			ListPreference lSelectedFlag = (ListPreference) prefScr.findPreference("cfg_selected_flag");
			ListPreference lSession = (ListPreference) prefScr.findPreference("cfg_session");
			ListPreference lTarget = (ListPreference) prefScr.findPreference("cfg_target");
	
			CmdTagAccess.RFID_18K6CSetQueryTagGroup mMtiCmd = new CmdTagAccess.RFID_18K6CSetQueryTagGroup();
			iStatus = mMtiCmd.setCmd((byte)(lSelectedFlag.findIndexOfValue(lSelectedFlag.getValue()) == 0 ?
					0 : lSelectedFlag.findIndexOfValue(lSelectedFlag.getValue()) + 1),
					(byte)lSession.findIndexOfValue(lSession.getValue()),
					(byte)lTarget.findIndexOfValue(lTarget.getValue()));
			getQueryTagGroup();
       	} else {
			Toast.makeText(getActivity(), "The Reader is not connected", Toast.LENGTH_SHORT).show();
       	}
		return iStatus;
	}
	
	private int getQueryTagGroup() {
		int iStatus = 0xffff;

       	if(RfidContainer.getUsbState()) {
			CmdTagAccess.RFID_18K6CGetQueryTagGroup mMtiCmd = new CmdTagAccess.RFID_18K6CGetQueryTagGroup();
			if((iStatus = mMtiCmd.setCmd()) == 0x00) {
				ListPreference lPref = (ListPreference) prefScr.findPreference("cfg_selected_flag");
				lPref.setValueIndex(mMtiCmd.getSelectedFlag() == 0 ? 0 : (mMtiCmd.getSelectedFlag() - 1));
				lPref.setSummary(lPref.getValue());
				
				lPref = (ListPreference) prefScr.findPreference("cfg_session");
				lPref.setValueIndex(mMtiCmd.getSession());
				lPref.setSummary(lPref.getValue());
				
				lPref = (ListPreference) prefScr.findPreference("cfg_target");
				lPref.setValueIndex(mMtiCmd.getTarget());
				lPref.setSummary(lPref.getValue());
			}		
       	} else {
			Toast.makeText(getActivity(), "The Reader is not connected", Toast.LENGTH_SHORT).show();
       	}
		return iStatus;
	}
	
	private int setLinkProfile() {
		int iStatus = 0xffff;
		
       	if(RfidContainer.getUsbState()) {
			ListPreference lPref = (ListPreference) prefScr.findPreference("cfg_link_profile");
	
			CmdReaderModuleConfig.RFID_RadioSetCurrentLinkProfile mtiCmd = new CmdReaderModuleConfig.RFID_RadioSetCurrentLinkProfile();
			iStatus = mtiCmd.setCmd((byte)lPref.findIndexOfValue(lPref.getValue()));
			getLinkProfile();
       	} else {
			Toast.makeText(getActivity(), "The Reader is not connected", Toast.LENGTH_SHORT).show();
       	}
		return iStatus;
	}
	
	private int getLinkProfile() {
		int iStatus = 0xffff;
		
       	if(RfidContainer.getUsbState()) {
			CmdReaderModuleConfig.RFID_RadioGetCurrentLinkProfile mtiCmd = new CmdReaderModuleConfig.RFID_RadioGetCurrentLinkProfile();
			if((iStatus = mtiCmd.setCmd()) == 0x00) {
				ListPreference lPref = (ListPreference) prefScr.findPreference("cfg_link_profile");
	
				lPref.setValueIndex(mtiCmd.getCurrentLinkProfile());
				lPref.setSummary(lPref.getValue());
			}
       	} else {
			Toast.makeText(getActivity(), "The Reader is not connected", Toast.LENGTH_SHORT).show();
       	}
		return iStatus;
	}
	
	private int setSingulationAlgorithm() {
		int iStatus = 0xffff;
		
       	if(RfidContainer.getUsbState()) {
			ListPreference lPref = (ListPreference) prefScr.findPreference("cfg_q_algorithm");
	
			CmdTagAccess.RFID_18K6CSetCurrentSingulationAlgorithm mtiCmd = new CmdTagAccess.RFID_18K6CSetCurrentSingulationAlgorithm();
			iStatus = mtiCmd.setCmd((byte)lPref.findIndexOfValue(lPref.getValue()));
			getSingulationAlgorithm();
       	} else {
			Toast.makeText(getActivity(), "The Reader is not connected", Toast.LENGTH_SHORT).show();
       	}
		return iStatus;
	}
	
	private int getSingulationAlgorithm() {
		int iStatus = 0xffff;
		
       	if(RfidContainer.getUsbState()) {
			CmdTagAccess.RFID_18K6CGetCurrentSingulationAlgorithm mtiCmd = new CmdTagAccess.RFID_18K6CGetCurrentSingulationAlgorithm();
			if ((iStatus = mtiCmd.setCmd()) == 0x00) {
				ListPreference lPref = (ListPreference) prefScr.findPreference("cfg_q_algorithm");
				
				lPref.setValueIndex(mtiCmd.getSingulationAlgorithm());
				lPref.setSummary(lPref.getValue());
			}
       	} else {
			Toast.makeText(getActivity(), "The Reader is not connected", Toast.LENGTH_SHORT).show();
       	}
		return iStatus;
	}

	private int setFixedQAlgorithm() {
		int iStatus = 0xffff;
		
       	if(RfidContainer.getUsbState()) {
			SeekBarPreference skQValue = (SeekBarPreference) prefScr.findPreference("cfg_q_value");
			EditTextPreference etRetryCount = (EditTextPreference) prefScr.findPreference("cfg_fixed_retry_count");
			CheckBoxPreference cbToggleTarget = (CheckBoxPreference) prefScr.findPreference("cfg_fixed_toggle_target");
			CheckBoxPreference cbRepeat = (CheckBoxPreference) prefScr.findPreference("cfg_repeat_until_no_tages");
			
			CmdTagAccess.RFID_18K6CSetCurrentSingulationAlgorithmParameters mMtiCmd = new CmdTagAccess.RFID_18K6CSetCurrentSingulationAlgorithmParameters();
			iStatus = mMtiCmd.setCmd((byte)0x0,
					Byte.parseByte(skQValue.getSummary().toString()),
					(byte)(Integer.parseInt(etRetryCount.getSummary().toString()) & 0xff),
					Byte.parseByte(cbToggleTarget.isChecked() ? "1" : "0"),
					Byte.parseByte(cbRepeat.isChecked() ? "1" : "0"));
			getFixedQAlgorithm();
       	} else {
			Toast.makeText(getActivity(), "The Reader is not connected", Toast.LENGTH_SHORT).show();
       	}
		return iStatus;
	}

	private int getFixedQAlgorithm() {
		int iStatus = 0xffff;
		
       	if(RfidContainer.getUsbState()) {
			CmdTagAccess.RFID_18K6CGetCurrentSingulationAlgorithmParameters mMtiCmd = new CmdTagAccess.RFID_18K6CGetCurrentSingulationAlgorithmParameters();
			if((iStatus = mMtiCmd.setCmd((byte)0)) == 0x00) {
				
				SeekBarPreference sbPref = (SeekBarPreference) prefScr.findPreference("cfg_q_value");
				sbPref.setValue(String.valueOf(mMtiCmd.getQValue()));
				sbPref.setSummary(sbPref.getValue());
				
				EditTextPreference etPref = (EditTextPreference) prefScr.findPreference("cfg_fixed_retry_count");
				etPref = (EditTextPreference) prefScr.findPreference("cfg_fixed_retry_count");
				etPref.setText(String.valueOf(mMtiCmd.getFixedQRetryCount() & 0xff));
				etPref.setSummary(etPref.getText());
				
				CheckBoxPreference cbPref = (CheckBoxPreference) prefScr.findPreference("cfg_fixed_toggle_target");
				cbPref.setChecked((mMtiCmd.getFixedQToggleTarget() == 0) ? false : true);
				
				cbPref = (CheckBoxPreference) prefScr.findPreference("cfg_repeat_until_no_tages");
				cbPref.setChecked((mMtiCmd.getRepeatUntilNoTags() == 0) ? false : true);
			}
       	} else {
			Toast.makeText(getActivity(), "The Reader is not connected", Toast.LENGTH_SHORT).show();
       	}
		return iStatus;
	}

	private int setDynamicQAlgorithm() {
		int iStatus = 0xffff;
		
       	if(RfidContainer.getUsbState()) {
			SeekBarPreference skStartQValue = (SeekBarPreference) prefScr.findPreference("cfg_start_qvalue");
			SeekBarPreference skMinQValue = (SeekBarPreference) prefScr.findPreference("cfg_min_qvalue");
			SeekBarPreference skMaxQValue = (SeekBarPreference) prefScr.findPreference("cfg_max_qvalue");
			EditTextPreference etRetryCount = (EditTextPreference) prefScr.findPreference("cfg_dynamic_retry_count");
			CheckBoxPreference cbToggleTarget = (CheckBoxPreference) prefScr.findPreference("cfg_dynamic_toggle_target");
			SeekBarPreference skThresholdMultiplier = (SeekBarPreference) prefScr.findPreference("cfg_threshold_multiplier");
			
			CmdTagAccess.RFID_18K6CSetCurrentSingulationAlgorithmParameters mMtiCmd = new CmdTagAccess.RFID_18K6CSetCurrentSingulationAlgorithmParameters();
			iStatus = mMtiCmd.setCmd((byte)0x1,
					Byte.parseByte(skStartQValue.getSummary().toString()),
					Byte.parseByte(skMinQValue.getSummary().toString()),
					Byte.parseByte(skMaxQValue.getSummary().toString()),
					(byte)(Integer.parseInt(etRetryCount.getSummary().toString()) & 0xff),
					Byte.parseByte(cbToggleTarget.isChecked() ? "1" : "0"),
					(byte)Integer.parseInt(skThresholdMultiplier.getSummary().toString()));
			getDynamicQAlgorithm();
       	} else {
			Toast.makeText(getActivity(), "The Reader is not connected", Toast.LENGTH_SHORT).show();
       	}
		return iStatus;
	}

	private int getDynamicQAlgorithm() {
		int iStatus = 0xffff;
		
       	if(RfidContainer.getUsbState()) {
			CmdTagAccess.RFID_18K6CGetCurrentSingulationAlgorithmParameters mMtiCmd = new CmdTagAccess.RFID_18K6CGetCurrentSingulationAlgorithmParameters();
			if((iStatus = mMtiCmd.setCmd((byte)1)) == 0x00) {
				
				SeekBarPreference sbPref = (SeekBarPreference) prefScr.findPreference("cfg_start_qvalue");
				sbPref.setValue(String.valueOf(mMtiCmd.getStartQValue()));
				sbPref.setSummary(sbPref.getValue());
				
				sbPref = (SeekBarPreference) prefScr.findPreference("cfg_min_qvalue");
				sbPref.setValue(String.valueOf(mMtiCmd.getMinQValue()));
				sbPref.setSummary(sbPref.getValue());
				
				sbPref = (SeekBarPreference) prefScr.findPreference("cfg_max_qvalue");
				sbPref.setValue(String.valueOf(mMtiCmd.getMaxQValue()));
				sbPref.setSummary(sbPref.getValue());
				
				EditTextPreference etPref = (EditTextPreference) prefScr.findPreference("cfg_dynamic_retry_count");
				etPref = (EditTextPreference) prefScr.findPreference("cfg_dynamic_retry_count");
				etPref.setText(String.valueOf(mMtiCmd.getDynamicQRetryCount() & 0xff));
				etPref.setSummary(etPref.getText());
				
				CheckBoxPreference cbPref = (CheckBoxPreference) prefScr.findPreference("cfg_dynamic_toggle_target");
				cbPref.setChecked((mMtiCmd.getDynamicQToggleTarget() == 0) ? false : true);
				
				sbPref = (SeekBarPreference) prefScr.findPreference("cfg_threshold_multiplier");
				sbPref.setValue(String.valueOf(mMtiCmd.getThresholdMultiPlier() & 0xff));
				sbPref.setSummary(sbPref.getValue());
			}
       	} else {
			Toast.makeText(getActivity(), "The Reader is not connected", Toast.LENGTH_SHORT).show();
       	}
		return iStatus;
	}
}
