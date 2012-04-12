package fr.redmoon.tictac.gui.dialogs.listeners;

import fr.redmoon.tictac.TicTacActivity;

public class EditExtraListener extends TimeSetListener {
    	public EditExtraListener(final TicTacActivity activity) {
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