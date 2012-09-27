package fr.redmoon.tictac.bus;

import java.util.Calendar;

import android.text.format.Time;

public class DateUtils {
	
	/**
	 * On définit le dimanche comme étant le dernier jour de la semaine.
	 */
	private static final int SUNDAY_WEEK_DAY_ID = Time.SATURDAY + 1;
	
	/**
	 * Date en format long
	 */
	public static final String FORMAT_DATE_LONG = "%d/%m/%y";
	
	/**
	 * Date en détails
	 */
	public static final String FORMAT_DATE_DETAILS = "%A %d %B %Y";
	
	/**
	 * Mois et année
	 */
	public static final String FORMAT_DATE_MONTH = "%B %Y";
	
	/**
	 * Nombre de millisecondes dans une journée
	 */
	private static final long MS_IN_A_DAY = 86400000;		// 1000 * 3600 * 24
	
	/**
	 * String Builder utilisé pour construire des chaines
	 */
	private static final StringBuilder mWorkStringBuilder = new StringBuilder();

	/**
	 * Retourne un identifiant du jour tel qu'ils sont en base (yyyymmdd) à
	 * partir la date actuelle
	 * @param time
	 * @return
	 */
	public static long getCurrentDayId() {
		return getCurrentDayId(new Time());
	}
	
	/**
	 * Retourne un identifiant du jour tel qu'ils sont en base (yyyymmdd) à
	 * partir la date fournie en paramètre, qui sera modifiée pour refléter
	 * la date du jour
	 * @param time
	 * @return
	 */
	public static long getCurrentDayId(final Time time) {
		time.setToNow();
		return getDayId(time);
	}
	
	/**
	 * Retourne un identifiant de jour tel qu'ils sont en base (yyyymmdd) à
	 * partir des composantes de la date.
	 * @param time
	 * @return
	 */
	public static long getDayId(final int year, final int monthOfYear, final int dayOfMonth) {
		return year * 10000 + (monthOfYear + 1) * 100 + dayOfMonth;
	}
	
	/**
	 * Retourne un identifiant de jour tel qu'ils sont en base (yyyymmdd) à
	 * partir de l'objet Time fournit.
	 * @param time
	 * @return
	 */
	public static long getDayId(final Time time) {
		return time.year * 10000 + (time.month + 1) * 100 + time.monthDay;
	}
	
	/**
	 * Retourne un identifiant de jour tel qu'ils sont en base (yyyymmdd) à
	 * partir de l'objet Calendar fournit.
	 * @param time
	 * @return
	 */
	public static long getDayId(final Calendar calendar) {
		return calendar.get(Calendar.YEAR) * 10000 + (calendar.get(Calendar.MONTH) + 1) * 100 + calendar.get(Calendar.DAY_OF_MONTH);
	}
	
	/**
	 * Formate une date au format dd/mm à partir d'un identifiant de jour au format yyyymmdd
	 * @param date
	 * @return
	 */
	public static String formatDateDDMM(final long date) {
		mWorkStringBuilder.setLength(0);
		
		// Calcul du jour
		final long day = date % 100;
		if (day < 10) {
			mWorkStringBuilder.append("0");
		}
		mWorkStringBuilder.append(day).append("/");
		
		// Calcul du mois
		final long month = date % 10000 / 100;
		if (month < 10) {
			mWorkStringBuilder.append("0");
		}
		mWorkStringBuilder.append(month);
		
		return mWorkStringBuilder.toString();
	}
	
	/**
	 * Formate une date au format dd/mm/yyyy à partir d'un identifiant de jour au format yyyymmdd
	 * @param date
	 * @return
	 */
	public static String formatDateDDMMYYYY(final long date) {
		mWorkStringBuilder.setLength(0);
		
		// Calcul du jour
		final long day = date % 100;
		if (day < 10) {
			mWorkStringBuilder.append("0");
		}
		mWorkStringBuilder.append(day).append("/");
		
		// Calcul du mois
		final long month = date % 10000 / 100;
		if (month < 10) {
			mWorkStringBuilder.append("0");
		}
		mWorkStringBuilder.append(month).append("/");
		
		// Calcul de l'année
		final long year = date / 10000;
		mWorkStringBuilder.append(year);
		
		return mWorkStringBuilder.toString();
	}
	
	public static String formatDateDDMMYYYY(String string) {
		return formatDateDDMMYYYY(Long.parseLong(string));
	}
	
	/**
	 * Remplit un objet Time avec la date spécifiée au format yyyymmdd, tel
	 * qu'elle est notée en base.
	 * @param date
	 * @param time
	 * @return
	 */
	public static void fillTime(final long date, final Time objectToFill) {
		objectToFill.year = extractYear(date);
		objectToFill.month = extractMonth(date);
		objectToFill.monthDay = extractDayOfMonth(date);
	}
	
	/**
	 * Extrait l'année d'une date au format yyyymmdd.
	 * @param date
	 * @return
	 */
	public static int extractYear(final long date) {
		return (int)date / 10000;
	}
	
	/**
	 * Extrait le mois d'une date au format yyyymmdd.
	 * @param date
	 * @return
	 */
	public static int extractMonth(final long date) {
		return (int)date % 10000 / 100 - 1;
	}
	
	/**
	 * Extrait le jour du mois d'une date au format yyyymmdd.
	 * @param date
	 * @return
	 */
	public static int extractDayOfMonth(final long date) {
		return (int)date % 100;
	}
	
	public static Time getDateOfDayOfWeek(final Time aDay, int requestedDay, final Time timeToUpdate) {
		int passedDay = aDay.weekDay;
		if (passedDay == Time.SUNDAY) {
			// Pour Android, le dimanche a le numéro 0. Or en France les semaines
			// commencent un lundi et finissent un dimanche. C'est pourquoi, si le
			// jour passé est un dimanche, on utilise la valeur 7 et non 0 pour les
			// calculs.
			passedDay = SUNDAY_WEEK_DAY_ID;
		}
		if (requestedDay == Time.SUNDAY) {
			// Comme précédemment, on remplace le dimanche par 7.
			requestedDay = SUNDAY_WEEK_DAY_ID;
		}
		final int nbDaysBetween = requestedDay - passedDay;
		
		timeToUpdate.set(aDay.toMillis(true) + nbDaysBetween * MS_IN_A_DAY);
		timeToUpdate.normalize(true);
		return timeToUpdate;
	}

	/**
	 * Retourne une date au format yyyymmdd à partir d'une date au format dd/mm/yyyy
	 * @param date
	 * @return
	 */
	public static long parseDateDDMMYYYY(final String date) {
		final StringBuffer sb = new StringBuffer(8);
		// Ajout de l'année
		sb.append(date.substring(6, 10));
		// Ajout du mois
		sb.append(date.substring(3, 5));
		// Ajout du jour
		sb.append(date.substring(0, 2));
		// Conversion en long
		return Long.parseLong(sb.toString());
	}

	/**
	 * Retourne true si le calendrier spécifié pointe sur un jour qui est considéré
	 * comme travaillé
	 * @param calendar
	 * @return
	 */
	public static boolean isWorkingWeekDay(Calendar calendar) {
		final int dayId = calendar.get(Calendar.DAY_OF_WEEK);
		return Calendar.MONDAY <= dayId && dayId <= Calendar.FRIDAY;
	}

//	/**
//	 * Retourne true si le jour spécifié fait partie de la semaine d'aujourd'hui.
//	 * @param date
//	 * @return
//	 */
//	public static boolean isInTodaysWeek(final long date) {
//		final Time today = new Time();
//		today.setToNow();
//		today.normalize(true);
//		
//		final Time passedDate = new Time();
//		passedDate.set(
//			extractDayOfMonth(date), 
//			extractMonth(date), 
//			extractYear(date));
//		passedDate.normalize(true);
//		
//		// On est dans la même semaine si on est dans la même année
//		// et que le numéro de semaine est identique
//		return
//			today.year == passedDate.year
//			&& today.getWeekNumber() == passedDate.getWeekNumber();
//	}
}
