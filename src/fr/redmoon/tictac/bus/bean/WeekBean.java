package fr.redmoon.tictac.bus.bean;

public class WeekBean {
	/**
	 * Jour dont la semaine est r�cup�r�e en base.
	 * En g�n�ral c'est un lundi.
	 */
	public long date;
	
	/**
	 * Horaire variable � ce jour/semaine
	 */
	public int flexTime;
	
	/**
	 * Indique si les donn�es sont issues de la base
	 */
	public boolean isValid;
}
