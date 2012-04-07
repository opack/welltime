package fr.redmoon.tictac.gui.dialogs.listeners;

import fr.redmoon.tictac.TicTacActivity;

public class ShowDayListener extends DateSetListener {
    	public ShowDayListener(final TicTacActivity activity) {
    		super(activity);
		}
    	
		@Override
    	public void onDateSet(final long newDate) {
			mActivity.populateView(newDate);
		}
	}