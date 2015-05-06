package com.encinitaslabs.wirfid.hpreader;

import com.encinitaslabs.wirfid.hpreader.R;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SeekBarPreference extends DialogPreference {
	private static final String PREFERENCE_NS = "http://schemas.android.com/apk/res/com.encinitaslabs.wirfid.hpreader";
	private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
	private Context context;
	private TextView mTextView;
	private SeekBar mSeekBar;
	private int maxValue = 0;
	private int minValue = 0;
	private int defaultValue = 0;
	private int mValue;

	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogLayoutResource(R.layout.seekbar_pref);
		if(maxValue == 0)
			maxValue = attrs.getAttributeIntValue(PREFERENCE_NS, "maxValue", 30);
		minValue = attrs.getAttributeIntValue(PREFERENCE_NS, "minValue", 10);
		defaultValue = attrs.getAttributeIntValue(ANDROID_NS, "defaultValue", 30); 
		mValue = defaultValue;
		this.context = context;
	}

	
	@Override
	protected View onCreateDialogView() {
		View view = super.onCreateDialogView();
		mSeekBar = (SeekBar)view.findViewById(R.id.sbValue);
		mTextView = (TextView)view.findViewById(R.id.tvValue);
		
		mSeekBar.setOnSeekBarChangeListener(listener);
		mSeekBar.setMax(maxValue - minValue);
		mSeekBar.setProgress(getPersistedInt(mValue));

		return view;
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if(positiveResult) {
			persistInt(mValue);
		}
		callChangeListener(mValue);
	}
	
	
	OnSeekBarChangeListener listener = new OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//			if(progress < minValue)
//				mSeekBar.setProgress(minValue);
			
			mValue = progress;
			mTextView.setText(String.valueOf(progress));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			
		}
	};
	
	public void setMaxValue(int max) {
		maxValue = max;
	}

	public String getValue() {
		return 	String.valueOf(getPersistedInt(mValue));
	}
	
	public void setValue(String value) {
		mValue = Integer.parseInt(value);
		persistInt(mValue);
	}

}
