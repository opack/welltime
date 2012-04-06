package fr.redmoon.tictac.gui.dialogs.listeners;

import fr.redmoon.tictac.TicTacActivity;
import fr.redmoon.tictac.bus.DayTypes;
import fr.redmoon.tictac.bus.bean.DayBean;

public class AddDayListener extends DateSetListener {
    	public AddDayListener(final TicTacActivity activity) {
    		super(activity);
		}
    	
		@Override
    	public void onDateSet(final long newDate) {
			boolean dbUpdated = false;
			
			// Mise � jour de la base de donn�es
			if (!mDb.isDayExisting(newDate)) {
				final DayBean day = new DayBean();
				day.date = newDate;
				day.type = DayTypes.normal.ordinal();
				mDb.createDay(day);
				dbUpdated = day.isValid;
			} else {
				// Si le jour existe d�j�, tant pis : on l'affiche.
				dbUpdated = true;
			}
			
			// Mise � jour de l'affichage
			if (dbUpdated) {
				mActivity.populateView(newDate);
			}
		}
	}