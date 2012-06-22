package fr.redmoon.tictac.gui.dialogs.listeners;

import fr.redmoon.tictac.gui.ViewSynchronizer;
import fr.redmoon.tictac.gui.activities.TicTacActivity;

public class ShowDayListener extends DateSetListener {
    	public ShowDayListener(final TicTacActivity activity) {
    		super(activity);
		}
    	
		@Override
    	public void onDateSet(final long newDate) {
			mActivity.populateView(newDate);
			
			// Sauvegarde du jour courant dans le synchroniseur de vues pour accorder
	    	// toutes les vues sur le même jour
	    	ViewSynchronizer.getInstance().setCurrentDay(newDate);
		}
	}