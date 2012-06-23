package fr.redmoon.tictac.gui.dialogs.listeners;

import java.util.ArrayList;
import java.util.List;

import android.widget.Toast;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.DayTypes;
import fr.redmoon.tictac.bus.FlexUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.bus.export.CalendarAccess;
import fr.redmoon.tictac.gui.activities.TicTacActivity;
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
				if (!mDb.isDayExisting(mDate)) {
					// Le jour n'existe pas : on le crée.
					final DayBean day = new DayBean();
					day.date = mDate;
					day.typeMorning = DayTypes.normal.ordinal();
					day.typeAfternoon = DayTypes.normal.ordinal();
					day.checkings.add(selectedTime);
					mDb.createDay(day);
					dbUpdated = day.isValid;
					
					// Ajout des évènements dans le calendrier
					if (dbUpdated && PreferencesBean.instance.syncCalendar) {
						CalendarAccess.getInstance().createWorkEvents(day.date, day.checkings);
						CalendarAccess.getInstance().createDayTypeEvent(day.date, day.typeMorning, day.typeAfternoon);
					}
				} else {
					// Le jour existe : on ajoute simplement le pointage
					dbUpdated = mDb.createChecking(mDate, selectedTime);
					
					if (dbUpdated && PreferencesBean.instance.syncCalendar) {
						// Ajout du pointage dans le calendrier
						final List<Integer> checkings = new ArrayList<Integer>();
						mDb.fetchCheckings(mDate, checkings);
						CalendarAccess.getInstance().createWorkEvents(mDate, checkings);
					}
				}
				
				// Mise à jour de l'HV.
				final FlexUtils flexUtils = new FlexUtils(mDb);
				flexUtils.updateFlex(mDate);
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