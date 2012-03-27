package fr.redmoon.tictac.bus;

import fr.redmoon.tictac.R;
import android.content.Context;

public enum DayTypes {
	
	normal (R.string.pref_daytype_normal),
	RTT  (R.string.pref_daytype_rtt),
	vacancy (R.string.pref_daytype_vacancy),
	publicHoliday (R.string.pref_daytype_publicholiday),
	illness (R.string.pref_daytype_illness);
	
	private final int labelResId;
	
	private DayTypes(final int labelResId) {
		this.labelResId = labelResId;
	}
	
	/**
	 * Retourne une chaine correspondant au libellé à afficher pour ce type de jour.
	 * @param context
	 * @return
	 */
	public String getLabel(final Context context) {
		return context.getResources().getString(labelResId);
	}
}
