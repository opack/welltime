package fr.redmoon.tictac.gui.listeners;

import android.widget.Toast;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.TicTacActivity;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.gui.datepicker.DateSetListener;

public class ShowDayListener extends DateSetListener {
    	public ShowDayListener(final TicTacActivity activity) {
    		super(activity);
		}
    	
		@Override
    	public void onDateSet(final long newDate) {
			// On vérifie que le jour existe bien
			if (mDb.isDayExisting(newDate)) {
				mActivity.populateView(newDate);
			} else {
				// Le jour n'existe pas
				Toast.makeText(
					mActivity,
					mActivity.getString(R.string.error_day_does_not_exist, DateUtils.formatDateDDMMYYYY(newDate)),
					Toast.LENGTH_LONG)
				.show();
			}
		}
	}