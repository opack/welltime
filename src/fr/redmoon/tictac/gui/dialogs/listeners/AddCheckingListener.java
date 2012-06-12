package fr.redmoon.tictac.gui.dialogs.listeners;

import android.widget.Toast;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.TicTacActivity;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.DayTypes;
import fr.redmoon.tictac.bus.FlexUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
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
				if (!mDb.isDayExisting(mDate)) {
					// Le jour n'existe pas : on le cr�e.
					final DayBean day = new DayBean();
					day.date = mDate;
					day.typeMorning = DayTypes.normal.ordinal();
					day.typeAfternoon = DayTypes.normal.ordinal();
					day.checkings.add(selectedTime);
					mDb.createDay(day);
					dbUpdated = day.isValid;
				} else {
					// Le jour existe : on ajoute simplement le pointage
					dbUpdated = mDb.createChecking(mDate, selectedTime);
				}
				
				// Mise � jour de l'HV.
				final FlexUtils flexUtils = new FlexUtils(mDb);
				flexUtils.updateFlex(mDate);
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