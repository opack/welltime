package fr.redmoon.tictac.bus.bean;

import java.util.ArrayList;
import java.util.List;

import fr.redmoon.tictac.bus.DayTypes;

public class DayBean {
	public long date;
	public int typeMorning;
	public int typeAfternoon;
	public int extra;
	public String note;
	public List<Integer> checkings;
	public boolean isValid;
	
	public DayBean() {
		checkings = new ArrayList<Integer>();
		reset();
	}

	public void reset() {
		date = 0;
		typeMorning = DayTypes.not_worked.ordinal();
		typeAfternoon = DayTypes.not_worked.ordinal();
		extra = 0;
		note = null;
		checkings.clear();
		isValid = false;
	}

	/**
	 * Supprime les donn�es associ�es au jour, mais conserve la date
	 */
	public void emptyDayData() {
		final long oldDate = date;
		reset();
		date = oldDate;
	}
}
