package fr.redmoon.tictac.bus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.text.format.Time;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.bus.bean.WeekBean;
import fr.redmoon.tictac.db.DbAdapter;

public class FlexUtils {
	public static final int NB_DAYS_IN_WEEK = 5;
	
	private final Time mWorkTime;
	private final DbAdapter mDb;
	
	public FlexUtils(final DbAdapter db) {
		mWorkTime = new Time();
		mDb = db;
	}
	
	/**
	 * Met � jour l'HV de toutes les semaines depuis la date indiqu�e jusqu'au
	 * lundi de la semaine du dernier jour pr�sent en base.
	 * @param initialDay
	 * @return
	 */
	public void updateFlex(final long initialDay) {
		final List<DayBean> days = new ArrayList<DayBean>();
		final Calendar calendar = new GregorianCalendar(DateUtils.extractYear(initialDay), DateUtils.extractMonth(initialDay), DateUtils.extractDayOfMonth(initialDay));
		long firstDay = initialDay;
		final WeekBean weekData = new WeekBean();
		
		// R�cup�ration de l'HV pour la date indiqu�e
		mDb.fetchLastFlexTime(initialDay, weekData);
		int flex = weekData.flexTime;

		// D�termination du dimanche de la semaine
		TimeUtils.parseDate(weekData.date, mWorkTime);
		DateUtils.getDateOfDayOfWeek(mWorkTime, Time.SUNDAY, mWorkTime);
		calendar.set(mWorkTime.year, mWorkTime.month, mWorkTime.monthDay);
		long lastDay = DateUtils.getDayId(calendar);

		// R�cup�ration des jours entre cette date et le prochain dimanche
		mDb.fetchDays(firstDay, lastDay, days);
		while (!days.isEmpty()){
			// Calcul de l'HV accumul� au cours de ces jours
			flex += computeWeekFlex(days);
			
			// Si la semaine contient moins de 5 jours, alors les autres jours
			// sont consid�r�s comme des jours de type "non travaill�"
			flex -= computeWeekNotWorkedTime(days.size());			
			
			// Calcul du prochain lundi et dimanche qui serviront de bornes
			calendar.add(Calendar.DAY_OF_YEAR, 1);
			firstDay = DateUtils.getDayId(calendar);
			calendar.add(Calendar.DAY_OF_YEAR, 6);
			lastDay = DateUtils.getDayId(calendar);
			
			// Mise � jour du nouvel HV au lundi
			mDb.updateFlexTime(firstDay, flex);
			
			// R�cup�ration des jours entre cette date et le prochain dimanche
			mDb.fetchDays(firstDay, lastDay, days);
		}
	}

	/**
	 * Retourne le temps "non travaill�" cette semaine. Ce temps est d�duit du nombre
	 * de jours travaill�s (nbWorkedDays) et du temps de travail th�orique d'un jour
	 * (PreferencesBean.instance.dayMin)
	 * @param nbWorkedDays
	 * @return
	 */
	public int computeWeekNotWorkedTime(final int nbWorkedDays) {
		return (NB_DAYS_IN_WEEK - nbWorkedDays) * PreferencesBean.instance.dayMin;
	}

	/**
	 * Si aucun enregistrement pour cette semaine existe, on
	 * en cr�e un et on met � jour le temps HV depuis le
	 * dernier enregistrement avant cette date jusqu'au dernier
	 * jour en base.
	 * @param date
	 */
	public void updateFlexIfNeeded(final long date) {
		// R�cup�ration du lundi de la semaine
		TimeUtils.parseDate(date, mWorkTime);
		DateUtils.getDateOfDayOfWeek(mWorkTime, Time.MONDAY, mWorkTime);
		
		// V�rification de l'existence de donn�es pour cette semaine
		if (mDb.isWeekExisting(date)) {
			// Un temps HV existe pour cette date : on ne fait rien de plus.
			return;
		}
		
		// Si rien n'existe, on met � jour l'HV
		updateFlex(date);
	}
	
	/**
	 * Retourne le temps total effectu� au cours de la semaine contenant le jour indiqu�.
	 * @param aDay
	 * @return
	 */
	public int computeWeekFlex(final long aDay) {
		// On r�cup�re le lundi correspondant au jour indiqu�
		TimeUtils.parseDate(aDay, mWorkTime);
		DateUtils.getDateOfDayOfWeek(mWorkTime, Time.MONDAY, mWorkTime);
		final long firstDay = DateUtils.getDayId(mWorkTime);
		
		// On prend le dernier jour de la semaine, donc 6 jours plus tard.
		// On fait un min pour dire : si le dimanche trouv� est apr�s ajourd'hui, alors on prend plut�t la date d'aujourd'hui.
		// On peut penser que �a ne sert � rien car aujourd'hui sera toujours le dernier jour. En r�alit� c'est faux car on
		// pourra pointer en avance des jours de vacances par exemple.
		DateUtils.getDateOfDayOfWeek(mWorkTime, Time.SUNDAY, mWorkTime);
		final long lastDay = DateUtils.getDayId(mWorkTime);
		
		// On r�cup�re les jours de la semaine
		final List<DayBean> days = new ArrayList<DayBean>();
		mDb.fetchDays(firstDay, lastDay, days);
		
		return computeWeekFlex(days);
	}

	/**
	 * Retourne le temps total effectu� au cours de la semaine dont les jours sont
	 * pass�s en param�tre
	 * @param aDay
	 * @return
	 */
	public int computeWeekFlex(final List<DayBean> days) {
		int flex = 0;
		int dayTotal = 0;
		for (final DayBean day : days) {
			dayTotal = TimeUtils.computeTotal(day);
			if (dayTotal > PreferencesBean.instance.dayMax) {
				dayTotal = PreferencesBean.instance.dayMax;
			}
			flex += dayTotal - PreferencesBean.instance.dayMin;
		}
		
		return flex;
	}
}