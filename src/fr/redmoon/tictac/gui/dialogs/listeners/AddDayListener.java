package fr.redmoon.tictac.gui.dialogs.listeners;

import fr.redmoon.tictac.bus.DayTypes;
import fr.redmoon.tictac.bus.FlexUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.bus.export.calendar.CalendarAccess;
import fr.redmoon.tictac.gui.activities.TicTacActivity;

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
				day.typeMorning = DayTypes.normal.ordinal();
				day.typeAfternoon = DayTypes.normal.ordinal();
				mDb.createDay(day);
				dbUpdated = day.isValid;
				
				// Mise � jour de l'HV.
		    	final FlexUtils flexUtils = new FlexUtils(mDb);
		    	flexUtils.updateFlex(day.date);
		    	
		    	// Ajout des �v�nements dans le calendrier
				if (dbUpdated && PreferencesBean.instance.syncCalendar) {
					CalendarAccess.getInstance().createDayTypeEvent(day.date, day.typeMorning, day.typeAfternoon);
				}
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