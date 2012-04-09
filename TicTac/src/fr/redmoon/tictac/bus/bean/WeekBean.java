package fr.redmoon.tictac.bus.bean;

public class WeekBean {
	/**
	 * Jour dont la semaine est récupérée en base.
	 * En général c'est un lundi.
	 */
	public long date;
	
	/**
	 * Horaire variable à ce jour/semaine
	 */
	public int flexTime;
	
	/**
	 * Indique si les données sont issues de la base
	 */
	public boolean isValid;
}
