package fr.redmoon.tictac.gui.dialogs.listeners;

import fr.redmoon.tictac.gui.activities.TicTacActivity;

public class EditExtraListener extends TimeSetListener {
    	public EditExtraListener(final TicTacActivity activity) {
    		super(activity);
		}
    	
    	public void prepare(final long date) {
    		super.prepare(date);
    	}

		@Override
    	public void onTimeSet(final int newTime) {
			// Mise � jour de la base de donn�es
			if (mDb.updateDayExtra(mDate, newTime)) {
				// Mise � jour de l'affichage
				mActivity.populateView(mDate);
			}
		}
	}