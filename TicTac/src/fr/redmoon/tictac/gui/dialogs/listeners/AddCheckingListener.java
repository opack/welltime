package fr.redmoon.tictac.gui.dialogs.listeners;

import android.widget.Toast;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.TicTacActivity;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.FlexUtils;
import fr.redmoon.tictac.gui.widgets.WidgetProvider;

public class AddCheckingListener extends TimeSetListener {
		
    	public AddCheckingListener(final TicTacActivity activity) {
    		super(activity);
		}
    	
    	public void prepare(final long date, final int checkingToEdit) {
    		super.prepare(date);
    	}

		@Override
    	public void onTimeSet(final int selectedTime) {
			if (selectedTime == 0) {
				return;
			}
			
			boolean dbUpdated = false;
			
			// Mise � jour de la base de donn�es
			if (mDb.isCheckingExisting(mDate, selectedTime)) {
				// Le pointage existe d�j� : affichage d'un message
				Toast.makeText(
					mActivity,
					mActivity.getString(R.string.error_checking_already_exists),
					Toast.LENGTH_LONG)
				.show();
			} else {
				// Cr�ation du nouveau pointage
				dbUpdated = mDb.createChecking(mDate, selectedTime);
				
				// Si le pointage n'est pas au cours de la semaine courante,
    			// alors on met � jour l'HV des semaines qui suivent ce jour
    			if (!DateUtils.isInTodaysWeek(mDate)) {
    				final FlexUtils flexUtils = new FlexUtils(mDb);
    				flexUtils.updateFlex(mDate);
    			}
			}
			
			// Mise � jour de l'affichage
			if (dbUpdated) {
				mActivity.populateView(mDate);
				
				// Mise � jour des widgets
				if (mDate == DateUtils.getCurrentDayId()) {
					WidgetProvider.updateClockinImage(mActivity);
				}
			}
		}
	}