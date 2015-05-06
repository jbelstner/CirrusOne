package com.encinitaslabs.wirfid.hpreader;

import java.util.ArrayList;

import com.encinitaslabs.wirfid.hpreader.R;

import android.app.Activity;
import android.app.ListFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class FRAG_Tag extends ListFragment implements OnItemLongClickListener {
	private SharedPreferences mSharedpref;

	protected static ArrayList<String> tagList = new ArrayList<String>();
	protected static ArrayAdapter<String> tagAdapter;

	private View mView;
	OnTagSelectedListener mListener;
	
	public static FRAG_Tag newInstance() {
		FRAG_Tag f = new FRAG_Tag();
		return f;
	}
	
	public interface OnTagSelectedListener {
		public void onTagSelected(int tagPosition, String strTag);
		public void onTagLongPress(int tagPosition, String strTag);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mListener = (OnTagSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnTagSelectedListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        tagAdapter = new ArrayAdapter<String> (getActivity(), android.R.layout.simple_list_item_1, tagList);
        setListAdapter(tagAdapter);

 		mView = inflater.inflate(R.layout.frag_tag, container, false);

		return mView;
	}


    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Button btnInventory = (Button) mView.findViewById(R.id.btn_inventory);

		mSharedpref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		
		btnInventory.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Encinitas Labs
	        	if (RfidContainer.getUsbState()) {
	        		new InventoryTask(getActivity()).execute();
	        	}
			}
		});
		
        getListView().setOnItemLongClickListener(this);

        if(savedInstanceState == null) {
        	if(RfidContainer.getUsbState()) {
//				final String algorithm = mSharedpref.getString("cfg_q_algorithm", "Fixed Q");
        		
				CmdTagAccess.RFID_18K6CSetCurrentSingulationAlgorithm mMtiCmd0x32 = new CmdTagAccess.RFID_18K6CSetCurrentSingulationAlgorithm();
				mMtiCmd0x32.setCmd(CmdTagAccess.Algorithm.FixedQ);
	
				final int qValue = mSharedpref.getInt("cfg_q_value", 2);
			    final String retryCount = mSharedpref.getString("cfg_fixed_retry_count", "0");
			    final boolean repeatUntilNoTags = mSharedpref.getBoolean("cfg_repeat_until_no_tages", true);
				CmdTagAccess.RFID_18K6CSetCurrentSingulationAlgorithmParameters mMtiCmd0x34 = new CmdTagAccess.RFID_18K6CSetCurrentSingulationAlgorithmParameters();
				mMtiCmd0x34.setCmd((byte)0x0, (byte)qValue, (byte)(Integer.valueOf(retryCount) & 0xff), (byte)0x1, (byte)(repeatUntilNoTags ? 1 : 0));
		
			    final int powerLevel = mSharedpref.getInt("cfg_pwr_level", (RfidContainer.getPid() == 0x824 ? 24 : 30));
				final String scanTime = mSharedpref.getString("cfg_dwell_time", "2000");
			    final String inventoryCycles = mSharedpref.getString("cfg_number_inventory_cycles", "0");
				CmdAntennaPortConf.RFID_AntennaPortSetConfiguration mMtiCmd0x12 = new CmdAntennaPortConf.RFID_AntennaPortSetConfiguration();
				mMtiCmd0x12.setCmd((byte)0, (short)(powerLevel * 10), (short)(Integer.valueOf(scanTime) & 0xffff), (short)(Integer.valueOf(inventoryCycles) & 0xffff));
			} else {
				Toast.makeText(getActivity(), "The reader is not connected", Toast.LENGTH_SHORT).show();
			}
        }
    }

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		String strTag = getListTagId(position);
		
		if(onItemSelect(strTag) == 0x00) {
			mListener.onTagSelected(position, strTag);
		} else {
			// #### process error ####
		}

	}

	@Override
	public boolean onItemLongClick(AdapterView<?> av, View v, int position, long id) {
	    String strTag = getListTagId(position);
	    mListener.onTagLongPress(position, strTag.replace(" ", ""));

		return true;
	}


	// #### select a tag ####
	private int onItemSelect(String tagId) {
		int cmdCount = 0;
		int iStatus = 0x00;
		byte[] bEpc = null;
		
		if(RfidContainer.getUsbState()) {
		    final int qValue = mSharedpref.getInt("cfg_q_value", 2);
		    final String retryCount = mSharedpref.getString("cfg_fixed_retry_count", "0");
		    final boolean repeatUntilNoTags = mSharedpref.getBoolean("cfg_repeat_until_no_tages", false);
			CmdTagAccess.RFID_18K6CSetCurrentSingulationAlgorithmParameters mMtiCmd0x34 = new CmdTagAccess.RFID_18K6CSetCurrentSingulationAlgorithmParameters();
			iStatus &= mMtiCmd0x34.setCmd((byte)0x0, (byte)qValue, (byte)(Integer.valueOf(retryCount) & 0xff), (byte)0x0, (byte)(repeatUntilNoTags ? 1 : 0));
			if(iStatus != 0x00) {
				Toast.makeText(getActivity(), "[ERROR] RFID_18K6CSetCurrentSingulationAlgorithmParameters !!", Toast.LENGTH_SHORT).show();
				return iStatus;
			}
			
			
			final String spSelectedFlag = mSharedpref.getString("cfg_selected_flag", "All");
			final String spSession = mSharedpref.getString("cfg_session", "S2");
			final String spTarget = mSharedpref.getString("cfg_target", "B");
			CmdTagAccess.RFID_18K6CSetQueryTagGroup mMtiCmd0x30 = new CmdTagAccess.RFID_18K6CSetQueryTagGroup();
			iStatus &= mMtiCmd0x30.setCmd(CmdTagAccess.Selected.valueOf(spSelectedFlag),
					CmdTagAccess.Session.valueOf(spSession), 
					CmdTagAccess.Target.valueOf(spTarget));
			if(iStatus != 0x00) {
				Toast.makeText(getActivity(), "[ERROR] RFID_18K6CSetQueryTagGroup !!", Toast.LENGTH_SHORT).show();
				return iStatus;
			}

			CmdTagSelect.RFID_18K6CSetSelectMaskData mtiCmd0x24 = new CmdTagSelect.RFID_18K6CSetSelectMaskData();
			bEpc = mtiCmd0x24.stringToByteArray(tagId.replace(" ", ""));
			for(cmdCount = 0; cmdCount < bEpc.length / 4; cmdCount++) {
				iStatus &= mtiCmd0x24.setCmd((byte)0x0, (byte)cmdCount,
						(byte)bEpc[cmdCount*4], (byte)bEpc[cmdCount*4+1], (byte)bEpc[cmdCount*4+2], (byte)bEpc[cmdCount*4+3]);
			}
			if(iStatus != 0x00) {
				Toast.makeText(getActivity(), "[ERROR] RFID_18K6CSetSelectMaskData !!", Toast.LENGTH_SHORT).show();
				return iStatus;
			}
			
			CmdTagSelect.RFID_18K6CSetSelectCriteria mtiCmd0x22 = new CmdTagSelect.RFID_18K6CSetSelectCriteria();
			iStatus &= mtiCmd0x22.setCmd((byte)0x0, CmdTagSelect.Bank.EPC, (short)32, (byte)(bEpc.length * 8),
					CmdTagSelect.Target.S2, (byte)(spTarget.equals("A") ? 0 : 4), CmdTagSelect.Truncation.Disable);
			if(iStatus != 0x00) {
				Toast.makeText(getActivity(), "[ERROR] RFID_18K6CSetSelectCriteria !!", Toast.LENGTH_SHORT).show();
				return iStatus;
			}
			
			CmdTagSelect.RFID_18K6CSetActiveSelectCriteria mtiCmd0x20 = new CmdTagSelect.RFID_18K6CSetActiveSelectCriteria();
//			for(int i = 1; i < 8; i++) {
//				iStatus &= mtiCmd0x20.setCmd((byte)i, CmdTagSelect.ActiveState.Disable);
//			}
			iStatus &= mtiCmd0x20.setCmd((byte)0x0, CmdTagSelect.ActiveState.Enable);
			if(iStatus != 0x00) {
				Toast.makeText(getActivity(), "[ERROR] RFID_18K6CSetActiveSelectCriteria !!", Toast.LENGTH_SHORT).show();
				return iStatus;
			}
			
			CmdReaderModuleConfig.RFID_RadioSetOperationMode mtiCmd0x03 = new CmdReaderModuleConfig.RFID_RadioSetOperationMode();
			iStatus &= mtiCmd0x03.setCmd(CmdReaderModuleConfig.OperationMode.NonContinuous);
			if(iStatus != 0x00) {
				Toast.makeText(getActivity(), "[ERROR] RFID_RadioSetOperationMode !!", Toast.LENGTH_SHORT).show();
				return iStatus;
			}
			
		} else {
			iStatus = 0xffff;
			Toast.makeText(getActivity(), "The Reader is not connected", Toast.LENGTH_SHORT).show();
		}
		return iStatus;
	}
	
	// #### get tag id ####
	private String getListTagId(int position) {
		return tagList.get(position).toString();
	}
}
