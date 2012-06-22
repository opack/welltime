package fr.redmoon.tictac.bus;

import android.content.Context;
import fr.redmoon.tictac.R;

public enum DayTypes {
	
	normal (R.string.pref_daytype_normal),
	RTT  (R.string.pref_daytype_rtt),
	vacancy (R.string.pref_daytype_vacancy),
	publicHoliday (R.string.pref_daytype_publicholiday),
	illness (R.string.pref_daytype_illness),
	not_worked (R.string.pref_daytype_notworked);
	
	private final int labelResId;
	
	private DayTypes(final int labelResId) {
		this.labelResId = labelResId;
	}
	
	/**
	 * Retourne une chaine correspondant au libell� � afficher pour ce type de jour.
	 * @param context
	 * @return
	 */
	public String getLabel(final Context context) {
		return context.getResources().getString(labelResId);
	}
}
