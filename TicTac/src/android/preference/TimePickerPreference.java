// Please note this must be the package if you want to use XML-based preferences
package android.preference;
 
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;
 
/**
 * A preference type that allows a user to choose a time
 */
public class TimePickerPreference extends DialogPreference {
	protected static final Pattern REGEXP_HHMM_TIME = Pattern.compile("([0-2]*[0-9]):([0-5]*[0-9])");
	
	private TimePicker picker; // to get this dialog from other functions
	
	/**
	 * The default value for this preference
	 */
	private String defaultValue;
 
	/**
	 * @param context
	 * @param attrs
	 */
	public TimePickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}
 
	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public TimePickerPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}
	
	/**
	 * Initialize this preference
	 */
	private void initialize() {
		setPersistent(true);
	}
 
	public TimePicker getPicker() {
		return picker;
	}

	public void setPicker(TimePicker picker) {
		this.picker = picker;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.preference.DialogPreference#onCreateDialogView()
	 */
	@Override
	protected View onCreateDialogView() {
		// Initialisation du picker
		final Context context = getContext();
		
		picker = new TimePicker(context);
		picker.setIs24HourView(DateFormat.is24HourFormat(context));
 
		// Initialisation du contenu des composants en fonction de la valeur de préférence actuelle.
		final String time = getPersistedString(this.defaultValue);
		initComponents(time);
		
		return picker;
	}
 
	/**
	 * Initialise les composants graphiques à partir de la valeur spécifiée
	 * @param matcher
	 */
	protected void initComponents(final String time) {
		if (time != null) {
			final Matcher matcher = REGEXP_HHMM_TIME.matcher(time);
			if (matcher.matches()) {
				// Initialisation du picker
				final int hour = Integer.valueOf(matcher.group(1));
				final int minute = Integer.valueOf(matcher.group(2));
				if (hour >= 0 && minute >= 0) {
					picker.setCurrentHour(hour);
					picker.setCurrentMinute(minute);
				}
			}
		}
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			picker.clearFocus();
			
			// Récupère la valeur saisie
			final String result = formatTimePicked();
			
			// Sauvegarde de la valeur saisie
			persistString(result);
			callChangeListener(result);
		}
	}
 
	/**
	 * Retourne une chaine au format adéquat à partir des valeurs sélectionnées.
	 * @return
	 */
	protected String formatTimePicked() {
		// Récupération des valeurs saisies
		final int hour = picker.getCurrentHour();
		final int minute = picker.getCurrentMinute();
		
		// Mise du temps au format (+-)hh:mm
		final StringBuilder sb = new StringBuilder(6);
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
 
		if (value == null || !REGEXP_HHMM_TIME.matcher(value).matches()) {
			return null;
		}
 
		this.defaultValue = value;
		return value;
	}
}