package com.encinitaslabs.wirfid.hpreader;

import java.util.Collections;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;

public class InventoryTask extends AsyncTask<Void, CharSequence, Void> {
	private Activity activity;
	private ProgressDialog dialog;
	
	public InventoryTask(Context cxt) {
		activity = (Activity)cxt;
		dialog = new ProgressDialog(cxt);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
	    dialog.setTitle("Inventory");
		dialog.setMessage("Searching...");
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
		setOrientationSensor(false);
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		
		Collections.sort(FRAG_Tag.tagList);
		FRAG_Tag.tagAdapter.notifyDataSetChanged();
		dialog.dismiss();
		setOrientationSensor(true);
	}

	@Override
	protected void onProgressUpdate(CharSequence... values) {
		super.onProgressUpdate(values);
		FRAG_Tag.tagAdapter.notifyDataSetChanged();
	}

	@Override
	protected Void doInBackground(Void... param) {
		String tagId;
		int i = 0; 
		
    	FRAG_Tag.tagList.clear();
//		ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
		CmdTagProtocol.RFID_18K6CTagInventory mMtiCmd = new CmdTagProtocol.RFID_18K6CTagInventory();
		
		if((mMtiCmd.setCmd(CmdTagProtocol.PerformSelect.NoSelectCmd,
				CmdTagProtocol.PerformPostMatch.NoPostSigulationMatchCmd,
				CmdTagProtocol.PerformGuardMode.RealtimeMode)) == 0x00) {
			while((mMtiCmd.checkEnd(50) != 0x00) && (i < 100)) {
				if((tagId = mMtiCmd.getTagIds()) != null) {
					if(!FRAG_Tag.tagList.contains(tagId) && !tagId.equals("")) {
						FRAG_Tag.tagList.add(tagId);
//						tg.startTone(ToneGenerator.TONE_PROP_BEEP);
						publishProgress(tagId);
					}
				}
				i++;
			}
		} else {
			// process error
		}
		return null;
	}

	private void setOrientationSensor(boolean status) {
		if(status)
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
		else {
			switch(activity.getWindowManager().getDefaultDisplay().getRotation()) {
				case 0:
					activity.setRequestedOrientation(RfidContainer.isPhone() ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					break;
				case 1:
					activity.setRequestedOrientation(RfidContainer.isPhone() ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
					break;
				case 2:
					activity.setRequestedOrientation(RfidContainer.isPhone() ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
					break;
				case 3:
					activity.setRequestedOrientation(RfidContainer.isPhone() ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					break;
			}
		}
	}
}
