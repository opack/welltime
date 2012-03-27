package fr.redmoon.tictac.gui.listeners;

import android.widget.Toast;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.TicTacActivity;
import fr.redmoon.tictac.bus.DateUtils;
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
				// Le jour existe d�j� : affichage d'un message
				Toast.makeText(
					mActivity,
					mActivity.getString(R.string.error_checking_already_exists),
					Toast.LENGTH_LONG)
				.show();
			} else {
				// Cr�ation du nouveau pointage
				dbUpdated = mDb.createChecking(mDate, selectedTime);
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