package fr.redmoon.tictac.gui.dialogs.listeners;

import fr.redmoon.tictac.bus.FlexUtils;
import fr.redmoon.tictac.gui.activities.TicTacActivity;

public class EditFlexTimeListener extends TicTacListener {
	private int mOldTime;
	
	public EditFlexTimeListener(final TicTacActivity activity) {
		super(activity);
	}
	
	public void prepare(long date, int time) {
		super.prepare(date);
		mOldTime = time;
	}

	public void onDateAndTimeSet(long date, int time) {
		if (time == mOldTime) {
			// Rien à faire
			return;
		}
		
		// Mise à jour de la base de données
		if (mDb.updateFlexTime(mDate, time)) {
			// Mise à jour de tous les HV qui suivent cette date
			final FlexUtils flexUtils = new FlexUtils(mDb);
			flexUtils.updateFlex(mDate);
			
			// Mise à jour de l'affichage
			mActivity.populateView(mDate);
		}
	}
}