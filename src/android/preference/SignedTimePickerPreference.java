// Please note this must be the package if you want to use XML-based preferences
package android.preference;
 
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TimePicker;
import fr.redmoon.tictac.R;
 
/**
 * A preference type that allows a user to choose a time
 */
public class SignedTimePickerPreference extends TimePickerPreference implements OnClickListener {
	protected static final Pattern REGEXP_SIGNED_TIME = Pattern.compile("([+-]?)([0-2]*[0-9]):([0-5]*[0-9])");
	private static final CharSequence NEGATIVE_SIGN = "-";
	private static final CharSequence POSITIVE_SIGN = "+";
	
	private Button sign;
	private boolean isNegativeTime;
	private TimePicker picker;
	
	/**
	 * @param context
	 * @param attrs
	 */
	public SignedTimePickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
 
	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public SignedTimePickerPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}
 
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.preference.DialogPreference#onCreateDialogView()
	 */
	@Override
	protected View onCreateDialogView() {
 
		final Context context = getContext();
		
		LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = vi.inflate(R.layout.signed_timepicker_preference, null);
		
		// Initialisation du bouton
		sign = (Button)v.findViewById(R.id.btn_sign);
		sign.setOnClickListener(this);
		
		// Initialisation du picker
		picker = (TimePicker)v.findViewById(R.id.pck_time);
		picker.setIs24HourView(DateFormat.is24HourFormat(context));
		setPicker(picker);
 
		// Initialisation du contenu des composants en fonction de la valeur de préférence actuelle.
		final String time = getPersistedString(getDefaultValue());
		initComponents(time);
		
		return v;
	}
	
	@Override
	protected void initComponents(final String time) {
		if (time != null) {
			final Matcher matcher = REGEXP_SIGNED_TIME.matcher(time);
			if (matcher.matches()) {
				// Initialisation du bouton
				final String sign = matcher.group(1);
				if (sign == null) {
					// Pas de signe : on considère qu'on a un nombre positif
					isNegativeTime = false;
				} else {
					// Un signe est spécifié : on regarde si c'est un -.
					// Si non, on considère qu'on a un nombre positif.
					isNegativeTime = NEGATIVE_SIGN.equals(sign);
				}
				updateSignButtonText();
				
				// Initialisation du picker
				final int hour = Integer.valueOf(matcher.group(2));
				final int minute = Integer.valueOf(matcher.group(3));
				if (hour >= 0 && minute >= 0) {
					picker.setCurrentHour(hour);
					picker.setCurrentMinute(minute);
				}
			}
		}
	}
 
	@Override
	protected String formatTimePicked() {
		// Récupération des valeurs saisies
		final int hour = picker.getCurrentHour();
		final int minute = picker.getCurrentMinute();
		
		// Mise du temps au format (+-)hh:mm
		final StringBuilder sb = new StringBuilder(6);
		if (isNegativeTime){
			sb.append(NEGATIVE_SIGN);
		}
		if (hour < 10) {
			sb.append("0");
		}
		sb.append(hour).append(":");
		if (minute < 10) {
			sb.append("0");
		}
		sb.append(minute);
		return sb.toString();
	}
 
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		final String value = a.getString(index);
 
		if (value == null || !REGEXP_SIGNED_TIME.matcher(value).matches()) {
			return null;
		}
 
		setDefaultValue(value);
		return value;
	}
	
	@Override
	public void onClick(View v) {
		isNegativeTime = !isNegativeTime;
		updateSignButtonText();
	}

	/**
	 * Met à jour le texte du bouton de changement de signe
	 * en fonction de la valeur de la variable isNegativeTime.
	 */
	private void updateSignButtonText() {
		if (isNegativeTime) {
			sign.setText(NEGATIVE_SIGN);
		} else {
			sign.setText(POSITIVE_SIGN);
		}
	}
}