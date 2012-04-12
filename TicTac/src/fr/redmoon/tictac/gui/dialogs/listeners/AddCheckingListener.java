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
			
			// Mise à jour de la base de données
			if (mDb.isCheckingExisting(mDate, selectedTime)) {
				// Le pointage existe déjà : affichage d'un message
				Toast.makeText(
					mActivity,
					mActivity.getString(R.string.error_checking_already_exists),
					Toast.LENGTH_LONG)
				.show();
			} else {
				// Création du nouveau pointage
				dbUpdated = mDb.createChecking(mDate, selectedTime);
				
				// Si le pointage n'est pas au cours de la semaine courante,
    			// alors on met à jour l'HV des semaines qui suivent ce jour
    			if (!DateUtils.isInTodaysWeek(mDate)) {
    				final FlexUtils flexUtils = new FlexUtils(mDb);
    				flexUtils.updateFlex(mDate);
    			}
			}
			
			// Mise à jour de l'affichage
			if (dbUpdated) {
				mActivity.populateView(mDate);
				
				// Mise à jour des widgets
				if (mDate == DateUtils.getCurrentDayId()) {
					WidgetProvider.updateClockinImage(mActivity);
				}
			}
		}
	}