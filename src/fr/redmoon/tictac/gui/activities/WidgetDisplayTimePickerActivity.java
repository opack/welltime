package fr.redmoon.tictac.gui.activities;

import android.os.Bundle;
import android.text.format.Time;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.TimeUtils;

public class WidgetDisplayTimePickerActivity extends TicTacActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Time now = TimeUtils.getNowTime();
    	final Integer checking = now.hour * 100 + now.minute;
    	promptAddChecking(DateUtils.getCurrentDayId(), checking, true);
    }

	@Override
	public void populateView(long day) {
		// Rien à faire : on n'affiche rien
	}
}