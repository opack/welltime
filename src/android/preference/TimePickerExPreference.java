package android.preference;

import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.gui.numberpicker.NumberPicker;

public class TimePickerExPreference extends DialogPreference {
	protected static final Pattern REGEXP_HHMM_TIME = Pattern.compile("([0-2]*[0-9]):([0-5]*[0-9])");
	
	public final static int DEFAULT_HOUR_MIN = 0;
	public final static int DEFAULT_HOUR_MAX = 24;
	public final static int DEFAULT_MINUTE_MIN = 0;
	public final static int DEFAULT_MINUTE_MAX = 60;
	
    private NumberPicker mHourPicker;
    private int mHourMin;
    private int mHourMax;
    
    private NumberPicker mMinutePicker;
    private int mMinuteMin;
    private int mMinuteMax;
    
    private String mDefaultValue;
    
    public TimePickerExPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        if (attrs == null) {
            return;
        }
        
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.timepickerex);
        
        mHourMin = arr.getInteger(R.styleable.timepickerex_hourMin, DEFAULT_HOUR_MIN);
        mHourMax = arr.getInteger(R.styleable.timepickerex_hourMax, DEFAULT_HOUR_MAX);
        
        mMinuteMin = arr.getInteger(R.styleable.timepickerex_minuteMin, DEFAULT_MINUTE_MIN);
        mMinuteMax = arr.getInteger(R.styleable.timepickerex_minuteMax, DEFAULT_MINUTE_MAX);
        
        arr.recycle();
                        
        setDialogLayoutResource(R.layout.time_picker_ex_preference);                
    }
    
    public TimePickerExPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.dialogPreferenceStyle);
    }
    
    public TimePickerExPreference(Context context) {
        this(context, null);
    }
    
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        
        final String[] time = getValue().split(":");
        final int hour = Integer.parseInt(time[0]);
        final int minute = Integer.parseInt(time[1]);
        
        mHourPicker = (NumberPicker) view.findViewById(R.id.hour);
        mHourPicker.setRange(mHourMin, mHourMax);
        mHourPicker.setCurrent(hour);
        
        mMinutePicker = (NumberPicker) view.findViewById(R.id.minute);
        mMinutePicker.setRange(mMinuteMin, mMinuteMax);
        mMinutePicker.setCurrent(minute);
    }
    
    public void setRange(int start, int end) {
        mHourPicker.setRange(start, end);
    }
    
    private String getValue() {
        return getSharedPreferences().getString(getKey(), mDefaultValue);
    }
    
    @Override
	protected void onDialogClosed(boolean positiveResult) {
    	super.onDialogClosed(positiveResult);
    	
		if (positiveResult) {
			mHourPicker.clearFocus();
			mMinutePicker.clearFocus();
			
			// Récupère la valeur saisie
			final String time = mHourPicker.getCurrent() + ":" + mMinutePicker.getCurrent();
			
			// Sauvegarde de la valeur saisie
			persistString(time);
			callChangeListener(time);
		}
	}
    
    @Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		final String value = a.getString(index);
 
		if (value == null || !REGEXP_HHMM_TIME.matcher(value).matches()) {
			return null;
		}
 
		mDefaultValue = value;
		return value;
	}
    
    public String getDefaultValue() {
		return mDefaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		mDefaultValue = defaultValue;
	}
}

