package fr.redmoon.tictac.gui.dialogs.listeners;

import fr.redmoon.tictac.TicTacActivity;
import fr.redmoon.tictac.bus.FlexUtils;

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
			// Rien � faire
			return;
		}
		
		// Mise � jour de la base de donn�es
		if (mDb.updateFlexTime(mDate, time)) {
			// Mise � jour de tous les HV qui suivent cette date
			final FlexUtils flexUtils = new FlexUtils(mDb);
			flexUtils.updateFlex(mDate);
			
			// Mise � jour de l'affichage
			mActivity.populateView(mDate);
		}
	}
}