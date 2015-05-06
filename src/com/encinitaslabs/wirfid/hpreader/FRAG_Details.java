package com.encinitaslabs.wirfid.hpreader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import com.encinitaslabs.wirfid.hpreader.R;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

public class FRAG_Details extends Fragment implements OnFocusChangeListener {

	private View mView;
	private EditText mEpcLength;
	private EditText mEpcNew;
	private EditText mPwdNew;
	private Spinner mMemoryBank;
	private EditText mReadWriteOffset;
	private EditText mReadWriteLength;
	private EditText mReadWriteData;
	private Spinner mKillPwd;
	private Spinner mAccessPwd;
	private Spinner mEpcMemBank;
	private Spinner mTidMemBank;
	private Spinner mUserMemBank;
	
	private SharedPreferences mSharedpref;
	
	public static FRAG_Details newInstance(int index, String tagId) {
		FRAG_Details f = new FRAG_Details();
		
		Bundle args = new Bundle();
		args.putInt("index", index);
		args.putString("tagid", tagId);
		f.setArguments(args);
		
		return f;
	}
	
	private int getShownIndex() {
		return getArguments().getInt("index", 0);
	}
	
	private String getTagId() {
		return getArguments().getString("tagid");
	}

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		int iEpcLength;
		String tagId;
		TextView tv;
		
		mView = inflater.inflate(R.layout.frag_detail, container, false);
		
        createSpinnerItems(mView, R.id.sp_memorybank, R.array.memory_bank);
        createSpinnerItems(mView, R.id.sp_killpwd, R.array.password_permission);
        createSpinnerItems(mView, R.id.sp_accesspwd, R.array.password_permission);
        createSpinnerItems(mView, R.id.sp_epcmembank, R.array.memory_bank_permission);
        createSpinnerItems(mView, R.id.sp_tidmembank, R.array.memory_bank_permission);
        createSpinnerItems(mView, R.id.sp_usermembank, R.array.memory_bank_permission);
		
		tagId = getTagId();
		tv = (TextView)mView.findViewById(R.id.tv_tagid);
		tv.setText(tagId);
		
		iEpcLength = (tagId.length() + 1) / 6;
		mEpcLength = (EditText)mView.findViewById(R.id.et_epclength);
		mEpcLength.setFilters(new InputFilter[]{
				new InputFilterMinMax("1", "27")
		});
		mEpcLength.setText(String.valueOf(iEpcLength));
		
		mEpcNew = (EditText)mView.findViewById(R.id.et_epcnew);
		mEpcNew.setText(getTagId().replace(" ", ""));
		
		mPwdNew = (EditText)mView.findViewById(R.id.et_pwdnew);
		
		mMemoryBank = (Spinner)mView.findViewById(R.id.sp_memorybank);
		mReadWriteOffset = (EditText)mView.findViewById(R.id.et_readwriteoffset);
		mReadWriteLength = (EditText)mView.findViewById(R.id.et_readwritelength);
		mReadWriteData = (EditText)mView.findViewById(R.id.et_readwritedata);
		
		mKillPwd = (Spinner)mView.findViewById(R.id.sp_killpwd);
		mKillPwd.setSelection(4);
		mAccessPwd = (Spinner)mView.findViewById(R.id.sp_accesspwd);
		mAccessPwd.setSelection(4);
		mEpcMemBank = (Spinner)mView.findViewById(R.id.sp_epcmembank);
		mEpcMemBank.setSelection(4);
		mTidMemBank = (Spinner)mView.findViewById(R.id.sp_tidmembank);
		mTidMemBank.setSelection(4);
		mUserMemBank = (Spinner)mView.findViewById(R.id.sp_usermembank);
		mUserMemBank.setSelection(4);
		
		return mView;
	}

	    
	private void createSpinnerItems(View mView, int viewId, int itemArray) {
	    Spinner sp = (Spinner) mView.findViewById(viewId);
	    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	    		mView.getContext() , itemArray, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);
        sp.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View mView, int position, long id) {
                    }
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
    }


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mEpcLength.setOnFocusChangeListener(this);
		mEpcNew.setOnFocusChangeListener(this);

		mSharedpref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

		final Button btnEpc = (Button) mView.findViewById(R.id.btn_epc);
		btnEpc.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setEpc();
			}
		});

		final Button btnGetPwd = (Button) mView.findViewById(R.id.btn_get_pwd);
		btnGetPwd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		    	getPassword();
			}
		});

		final Button btnSetPwd = (Button) mView.findViewById(R.id.btn_set_pwd);
		btnSetPwd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		    	setPassword();
			}
		});
		
		final Button btnLock = (Button) mView.findViewById(R.id.btn_lock);
		btnLock.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		    	lockTag();
			}
		});
		
		final Button btnRead = (Button) mView.findViewById(R.id.btn_read);
		btnRead.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		    	read();
			}
		});
		
		final Button btnWrite = (Button) mView.findViewById(R.id.btn_write);
		btnWrite.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				write();
			}
		});
		
		final Button btnKill = (Button) mView.findViewById(R.id.btn_kill);
		btnKill.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				killTag();
			}
		});
	}

	
	@Override
	public void onFocusChange(View view, boolean hasFocus) {
		int iEpcLength;
		int resId = view.getId();
		
		if(!hasFocus) {
			switch(view.getId()){
				case R.id.et_epclength:
					if(mEpcLength.getText().length() == 0)
						// #### process error ####
						return;
					else {
						iEpcLength = Integer.valueOf(mEpcLength.getText().toString());
						if(resId == R.id.et_epclength) {
							
					    	if( iEpcLength < 1 || iEpcLength > 27) {
					    		openOptionsDialog(getString(R.string.dlg_epc_length_title),
					    			getString(R.string.dlg_epc_length_message),
					    			getResources().getString(android.R.string.ok));
					    	}
						}
					}
					break;
				
				case R.id.et_epcnew:
					if(mEpcLength.getText().length() == 0)
						// #### process error ####
						return;
					else {
						iEpcLength = Integer.valueOf(mEpcLength.getText().toString());
						if(hasFocus) {
					    	InputFilter[] arrFilter = new InputFilter[1];
					    	arrFilter[0] = new InputFilter.LengthFilter(iEpcLength * 4);
					    	mEpcNew.setFilters(arrFilter);
						} else {
							if(iEpcLength != (mEpcNew.length() / 4)) {
					    		openOptionsDialog(getString(R.string.dlg_epc_new_title),
						    			getString(R.string.dlg_epc_new_message),
						    			getResources().getString(android.R.string.ok));
							}
						}
					}
					break;
			}
		}
	}
    
    
	private void openOptionsDialog(String title, String message, String posBtn) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(mView.getContext());
        dialog.setIconAttribute(android.R.attr.alertDialogIcon)
			.setTitle(title)
			.setMessage(message)
			.setPositiveButton(posBtn,
			new DialogInterface.OnClickListener(){
				public void onClick(
							DialogInterface dialoginterface, int i) {}
			})
			.show();
	}

	
	private int setEpc() {
		int iStatus = 0xffff;
		
		if(RfidContainer.getUsbState()) {
			CmdTagProtocol.RFID_18K6CTagWrite mtiCmd = new CmdTagProtocol.RFID_18K6CTagWrite();
			byte[] wData = mtiCmd.stringToByteArray(mEpcNew.getText().toString().replace(" ", ""));
			byte[] subData = new byte[4];
			short iData = 0;
			for(int i = 0; i < Math.ceil(wData.length / 2); i++) {
				subData = Arrays.copyOfRange(wData, i * 2, i * 2 + 2);
				ByteBuffer byteBuffer = ByteBuffer.wrap(subData);
//				byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
				iData = byteBuffer.getShort();
				if((iStatus = mtiCmd.setCmd(CmdTagProtocol.Bank.EPC,
						(short)(2 + i),
						iData,
						(byte)0x3,
						CmdTagProtocol.PerformSelect.PerformSelectCmd,
						CmdTagProtocol.PerformPostMatch.NoPostSigulationMatchCmd)) == 0x00) {
					while(mtiCmd.checkEnd(20) != 0x00) {
						Log.d(UsbCommunication.TAG, "1st");
					}
				} else
					Toast.makeText(getActivity(), mtiCmd.errMsg(iStatus), Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(getActivity(), "The Reader is not connected", Toast.LENGTH_SHORT).show();
		}
		return iStatus;
	}
	
	private int setPassword() {
		int iStatus = 0xffff;
		
		if(RfidContainer.getUsbState()) {
			CmdTagAccess.RFID_18K6CSetTagAccessPassword mtiCmd = new CmdTagAccess.RFID_18K6CSetTagAccessPassword();
			byte[] wData = mtiCmd.stringToByteArray(mPwdNew.getText().toString());
			int iPassword = 0;
			ByteBuffer byteBuffer = ByteBuffer.wrap(wData);
			byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
			iPassword = byteBuffer.getInt() & 0xffffffff;
			if((iStatus = mtiCmd.setCmd(iPassword)) != 0x00)
				Toast.makeText(getActivity(), mtiCmd.errMsg(iStatus), Toast.LENGTH_SHORT).show();
			getPassword();
		} else {
			Toast.makeText(getActivity(), "The Reader is not connected", Toast.LENGTH_SHORT).show();
		}
		return iStatus;
	}

	private int getPassword() {
		int iStatus = 0xffff;
		
		if(RfidContainer.getUsbState()) {
			CmdTagAccess.RFID_18K6CGetTagAccessPassword mtiCmd = new CmdTagAccess.RFID_18K6CGetTagAccessPassword();
			if((iStatus = mtiCmd.setCmd()) == 0x00)
				mPwdNew.setText(mtiCmd.getPassword());
			else
				Toast.makeText(getActivity(), mtiCmd.errMsg(iStatus), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getActivity(), "The Reader is not connected", Toast.LENGTH_SHORT).show();
		}
		return iStatus;
	}
	
	private int read() {
		int iStatus = 0xffff;
		
		if(RfidContainer.getUsbState()) {
			CmdTagProtocol.RFID_18K6CTagRead mtiCmd = new CmdTagProtocol.RFID_18K6CTagRead();
			if((iStatus = mtiCmd.setCmd(CmdTagProtocol.Bank.valueOf(mMemoryBank.getSelectedItem().toString()),
					Short.parseShort(mReadWriteOffset.getText().toString()),
					(byte)(Short.parseShort(mReadWriteLength.getText().toString()) & 0xff),
					(byte)0x3,
					CmdTagProtocol.PerformSelect.PerformSelectCmd,
					CmdTagProtocol.PerformPostMatch.NoPostSigulationMatchCmd)) == 0x00) {
				while(mtiCmd.checkEnd(20) != 0x00) {
					if(iStatus == 0x00)
						mReadWriteData.setText(mtiCmd.readData());
					else
						Toast.makeText(getActivity(), mtiCmd.errMsg(iStatus), Toast.LENGTH_SHORT).show();
				}
			}
		} else {
			Toast.makeText(getActivity(), "The Reader is not connected", Toast.LENGTH_SHORT).show();
		}
		return iStatus;
	}
	
	private int write() {
		int iStatus = 0xffff;
		
		if(RfidContainer.getUsbState()) {
			CmdTagProtocol.RFID_18K6CTagWrite mtiCmd = new CmdTagProtocol.RFID_18K6CTagWrite();
			byte[] wData = mtiCmd.stringToByteArray(mReadWriteData.getText().toString().replace(" ", ""));
			byte[] subData = new byte[4];
			short iData = 0;
			for(int i = 0; i < Math.ceil(wData.length / 2); i++) {
				subData = Arrays.copyOfRange(wData, i * 2, i * 2 + 2);
				ByteBuffer byteBuffer = ByteBuffer.wrap(subData);
//				byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
				iData = byteBuffer.getShort();
				if((iStatus = mtiCmd.setCmd(CmdTagProtocol.Bank.valueOf(mMemoryBank.getSelectedItem().toString()),
						(short)(Short.parseShort(mReadWriteOffset.getText().toString()) + i),
						iData,
						(byte)0x3,
						CmdTagProtocol.PerformSelect.PerformSelectCmd,
						CmdTagProtocol.PerformPostMatch.NoPostSigulationMatchCmd)) == 0x00) {
					while(mtiCmd.checkEnd(20) != 0x00) {
/*
						if(iStatus == 0x00)
							mReadWriteData.setText(mtiCmd.readData());
						else
							Toast.makeText(getActivity(), mtiCmd.errMsg(iStatus), Toast.LENGTH_SHORT).show();
*/
					}
				}
			}
		} else {
			Toast.makeText(getActivity(), "The Reader is not connected", Toast.LENGTH_SHORT).show();
		}
		return iStatus;
	}

	private int lockTag() {
		int iStatus = 0xffff;
		
		if(RfidContainer.getUsbState()) {
			CmdTagProtocol.RFID_18K6CTagLock mtiCmd = new CmdTagProtocol.RFID_18K6CTagLock();
			if((iStatus = mtiCmd.setCmd(CmdTagProtocol.PwdPermissions.valueOf(mKillPwd.getSelectedItem().toString().replace(" ", "")),
					CmdTagProtocol.PwdPermissions.valueOf(mAccessPwd.getSelectedItem().toString().replace(" ", "")),
					CmdTagProtocol.MemBankPermissions.valueOf(mEpcMemBank.getSelectedItem().toString().replace(" ", "")),
					CmdTagProtocol.MemBankPermissions.valueOf(mTidMemBank.getSelectedItem().toString().replace(" ", "")),
					CmdTagProtocol.MemBankPermissions.valueOf(mUserMemBank.getSelectedItem().toString().replace(" ", "")),
					(byte)0x3,
					CmdTagProtocol.PerformSelect.PerformSelectCmd,
					CmdTagProtocol.PerformPostMatch.NoPostSigulationMatchCmd)) != 0x00)
				Toast.makeText(getActivity(), mtiCmd.errMsg(iStatus), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getActivity(), "The Reader is not connected", Toast.LENGTH_SHORT).show();
		}
		return iStatus;
	}
	
	private int killTag() {
		int iStatus = 0xffff;
		
		if(RfidContainer.getUsbState()) {
			CmdTagProtocol.RFID_18K6CTagKill mtiCmd = new CmdTagProtocol.RFID_18K6CTagKill();
			byte[] wData = mtiCmd.stringToByteArray(mPwdNew.getText().toString());
			int iPassword = 0;
			ByteBuffer byteBuffer = ByteBuffer.wrap(wData);
			byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
			iPassword = byteBuffer.getInt();
	
			if((iStatus = mtiCmd.setCmd(iPassword, (byte)0x03,
					CmdTagProtocol.PerformSelect.PerformSelectCmd,
					CmdTagProtocol.PerformPostMatch.NoPostSigulationMatchCmd)) == 0x00)
				Toast.makeText(getActivity(), mtiCmd.errMsg(iStatus), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getActivity(), "The Reader is not connected", Toast.LENGTH_SHORT).show();
		}
		return iStatus;
	}
}
