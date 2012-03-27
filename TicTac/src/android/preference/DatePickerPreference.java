package android.preference;
 
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;
 
/**
 * A preference type that allows a user to choose a date
 */
public class DatePickerPreference extends DialogPreference {
 
	private DatePicker picker; // to get this dialog from other functions
	
	/**
	 * The validation expression for this preference
	 */
	private static final String VALIDATION_EXPRESSION = "[0-9]{8}";
 
	/**
	 * The default value for this preference
	 */
	private String defaultValue;
 
	/**
	 * @param context
	 * @param attrs
	 */
	public DatePickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}
 
	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public DatePickerPreference(Context context, AttributeSet attrs,
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
 
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.preference.DialogPreference#onCreateDialogView()
	 */
	@Override
	protected View onCreateDialogView() {
 
		final Context context = getContext();
		picker = new DatePicker(context);
 
		// Initialisation du composant en fonction de la valeur sauvegardée
		final String date = getPersistedString(this.defaultValue);
		if (date != null && date.matches(VALIDATION_EXPRESSION)) {
			final int year = Integer.valueOf(date.substring(0, 4));
			final int monthOfYear = Integer.valueOf(date.substring(4, 6)) - 1; // Pour Android, Janvier = 0. Nous on a stocké 1.
			final int dayOfMonth = Integer.valueOf(date.substring(6));
			
			if (year > 0 && monthOfYear >= 0 && dayOfMonth > 0) {
				picker.init(year, monthOfYear, dayOfMonth, null);
			}
		}
 
		return picker;
	}
 
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			picker.clearFocus();
			
			// Récupération des valeurs saisies
			final int year = picker.getYear();
			final int monthOfYear = picker.getMonth() + 1; // Pour Android, Janvier = 0. Nous on veut 1.
			final int dayOfMonth = picker.getDayOfMonth();
			
			// Mise de la date au format yyyymmdd
			final StringBuilder sb = new StringBuilder(8);
			sb.append(year);
			if (monthOfYear < 10) {
				sb.append("0");
			}
			sb.append(monthOfYear);
			if (dayOfMonth < 10) {
				sb.append("0");
			}
			sb.append(dayOfMonth);
			
			// Sauvegarde de la valeur saisie
			final String result = sb.toString();
			persistString(result);
			callChangeListener(result);
		}
	}
 
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.preference.Preference#setDefaultValue(java.lang.Object)
	 */
	@Override
	public void setDefaultValue(Object defaultValue) {
		// BUG this method is never called if you use the 'android:defaultValue' attribute in your XML preference file, not sure why it isn't		
 
		super.setDefaultValue(defaultValue);
 
		if (!(defaultValue instanceof String)) {
			return;
		}
 
		if (!((String) defaultValue).matches(VALIDATION_EXPRESSION)) {
			return;
		}
 
		this.defaultValue = (String) defaultValue;
	}
}