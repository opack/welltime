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
	private final Time mWorkTime;
	private final DbAdapter mDb;
	
	public FlexUtils(final DbAdapter db) {
		mWorkTime = new Time();
		mDb = db;
	}
	
	/**
	 * Appelle updateFlex(long) en lui passant le premier
	 * jour poss�dant un HV en base
	 */
	public void updateFlex() {
		final WeekBean weekData = new WeekBean();
		mDb.fetchLastFlexTime(weekData);
		updateFlex(weekData.date);
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
		final WeekBean weekData = new WeekBean();
		
		// On r�cup�re le lundi correspondant au jour indiqu�
		TimeUtils.parseDate(initialDay, mWorkTime);
		DateUtils.getDateOfDayOfWeek(mWorkTime, Time.MONDAY, mWorkTime);
		long firstDay = DateUtils.getDayId(mWorkTime);
		
		// R�cup�ration de l'HV pour la date indiqu�e
		mDb.fetchLastFlexTime(initialDay, weekData);
		int flex = weekData.flexTime;

		// D�termination du dimanche de la semaine
		TimeUtils.parseDate(weekData.date, mWorkTime);
		DateUtils.getDateOfDayOfWeek(mWorkTime, Time.SUNDAY, mWorkTime);
		calendar.set(mWorkTime.year, mWorkTime.month, mWorkTime.monthDay);
		long lastDay = DateUtils.getDayId(calendar);
		
		// R�cup�ration du dernier jour en base
		long lastExistingDay = mDb.fetchPreviousDay(99999999);

		// R�cup�ration des jours entre cette date et le prochain dimanche
		mDb.fetchDays(firstDay, lastDay, days);
		int curWeekTotal = 0;
		while (lastDay <= lastExistingDay){
			// Calcul de l'HV accumul� au cours de ces jours
			curWeekTotal = computeWeekTotal(days);
			
			// Si la semaine contient moins de 5 jours, alors les autres jours
			// sont consid�r�s comme des jours de type "non travaill�"
			curWeekTotal -= computeWeekNotWorkedTime(days.size());		
			
			// On calcule l'HV
			flex = computeFlexTime(curWeekTotal, flex);
			
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
	 * Retourne le nouvel HV en ajoutant � l'HV initial le temps effectu�
	 * au cours de la semaine.
	 * Le temps de travail de la semaine et l'HV sont born�s avec les
	 * valeurs des pr�f�rences.
	 * @param weekWorked
	 * @param initialFlex
	 * @return
	 */
	public int computeFlexTime(final int weekWorked, final int initialFlex) {
		// Borne � 41h le temps travaill�
		int weekCounted = Math.min(weekWorked, PreferencesBean.instance.weekMax);
		
		// Calcul du temps suppl�mentaire pour la semaine
		int curWeekFlex = weekCounted - PreferencesBean.instance.weekMin;
		
		// Ajout du temps suppl�mentaire au temps initial pour avoir l'HV en fin de semaine
		int newFlex = initialFlex + curWeekFlex;
		
		// Borne l'HV
		newFlex = Math.min(newFlex, PreferencesBean.instance.flexMax);
		newFlex = Math.max(newFlex, PreferencesBean.instance.flexMin);
		
		return newFlex;
	}

	/**
	 * Retourne le temps "non travaill�" cette semaine. Ce temps est d�duit du nombre
	 * de jours travaill�s (nbWorkedDays), du nombre de jours qui devraient �tre
	 * travaill�s (PreferencesBean.instance.nbDaysInWeek) et du temps de travail
	 * th�orique d'un jour (PreferencesBean.instance.dayMin)
	 * @param nbWorkedDays
	 * @return
	 */
	public int computeWeekNotWorkedTime(final int nbWorkedDays) {
		if (nbWorkedDays >= PreferencesBean.instance.nbDaysInWeek){
			return 0;
		}
		return (PreferencesBean.instance.nbDaysInWeek - nbWorkedDays) * PreferencesBean.instance.dayMin;
	}

	/**
	 * Retourne le temps total effectu� en plus du temps minimum 
	 * au cours de la semaine contenant le jour indiqu�.
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
		
		return computeWeekTotal(days) - PreferencesBean.instance.weekMin;
	}

	/**
	 * Retourne le temps total effectu� au cours de la semaine dont les jours 
	 * sont pass�s en param�tre.
	 * @param aDay
	 * @return
	 */
	public int computeWeekTotal(final List<DayBean> days) {
		int weekTotal = 0;
		for (final DayBean day : days) {
			weekTotal += TimeUtils.computeTotal(day);
		}
		
		// Calcul le temps suppl�mentaire effectu�
		return weekTotal;
	}
}
