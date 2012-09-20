package fr.redmoon.tictac.bus.bean;

import java.util.ArrayList;
import java.util.List;

public class DayBean {
	public long date;
	public String typeMorning;
	public String typeAfternoon;
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
		typeMorning = null;
		typeAfternoon = null;
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
