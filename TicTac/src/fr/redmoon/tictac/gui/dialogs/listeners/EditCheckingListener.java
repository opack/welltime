package fr.redmoon.tictac.gui.dialogs.listeners;

import java.util.ArrayList;
import java.util.List;

import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.FlexUtils;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.bus.export.tocalendar.CalendarAccess;
import fr.redmoon.tictac.gui.activities.TicTacActivity;
import fr.redmoon.tictac.gui.widgets.WidgetProvider;

public class EditCheckingListener extends TimeSetListener {
		private int mOldCheckingValue;
		
    	public EditCheckingListener(final TicTacActivity activity) {
    		super(activity);
		}
    	
    	public void prepare(final long date, final int checkingToEdit) {
    		super.prepare(date);
    		mOldCheckingValue = checkingToEdit;
    	}

		@Override
    	public void onTimeSet(final int newTime) {
			boolean dbUpdated = false;

			// Si on a choisit un pointage valide et qu'il n'existe
			// pas encore, on le cr�e et on supprime l'ancien.
			if (newTime != 0
			&& !mDb.isCheckingExisting(mDate, newTime)) {
				dbUpdated = mDb.createChecking(mDate, newTime);
			}
			
			// On souhaite supprimer l'ancien pointage s'il est diff�rent du nouveau
			if (mOldCheckingValue != TimeUtils.UNKNOWN_TIME && mOldCheckingValue != newTime) {
				dbUpdated = mDb.deleteChecking(mDate, mOldCheckingValue);
			}
			
			// Mise � jour de l'HV.
			final FlexUtils flexUtils = new FlexUtils(mDb);
			flexUtils.updateFlex(mDate);
			
			// Mise � jour de l'affichage
			if (dbUpdated) {
				mActivity.populateView(mDate);
				
				// Ajout du pointage dans le calendrier
				final List<Integer> checkings = new ArrayList<Integer>();
				mDb.fetchCheckings(mDate, checkings);
				CalendarAccess.getInstance().createWorkEvents(mDate, checkings);
				
				// Mise � jour des widgets
				if (mDate == DateUtils.getCurrentDayId()) {
					WidgetProvider.updateClockinImage(mActivity);
				}
			}
		}
	}