package fr.redmoon.tictac.gui.listeners;

import fr.redmoon.tictac.TicTacActivity;

public class UpdateExtraListener extends TimeSetListener {
    	public UpdateExtraListener(final TicTacActivity activity) {
    		super(activity);
		}
    	
    	public void prepare(final long date) {
    		super.prepare(date);
    	}

		@Override
    	public void onTimeSet(final int newTime) {
			// Mise à jour de la base de données
			if (mDb.updateDayExtra(mDate, newTime)) {
				// Mise à jour de l'affichage
				mActivity.populateView(mDate);
			}
		}
	}