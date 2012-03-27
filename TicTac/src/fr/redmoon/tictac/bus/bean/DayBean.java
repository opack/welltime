package fr.redmoon.tictac.bus.bean;

import java.util.ArrayList;
import java.util.List;

public class DayBean {
	public long date;
	public int type;
	public int extra;
	public String note;
	public List<Integer> checkings;
	public boolean isValid;
	
	public DayBean() {
		checkings = new ArrayList<Integer>();
	}

	public void reset() {
		date = 0;
		type = 0;
		extra = 0;
		note = null;
		checkings.clear();
		isValid = false;
	}

	/**
	 * Supprime les données associées au jour, mais conserve la date
	 */
	public void emptyDayData() {
		final long oldDate = date;
		reset();
		date = oldDate;
	}
}
