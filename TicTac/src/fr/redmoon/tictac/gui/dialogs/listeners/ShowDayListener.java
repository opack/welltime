package fr.redmoon.tictac.gui.dialogs.listeners;

import fr.redmoon.tictac.TicTacActivity;
import fr.redmoon.tictac.gui.ViewSynchronizer;

public class ShowDayListener extends DateSetListener {
    	public ShowDayListener(final TicTacActivity activity) {
    		super(activity);
		}
    	
		@Override
    	public void onDateSet(final long newDate) {
			mActivity.populateView(newDate);
			
			// Sauvegarde du jour courant dans le synchroniseur de vues pour accorder
	    	// toutes les vues sur le m�me jour
	    	ViewSynchronizer.getInstance().setCurrentDay(newDate);
		}
	}