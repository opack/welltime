package fr.redmoon.tictac.bus;

import java.util.List;

import android.text.format.Time;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.bean.PreferencesBean;

public class TimeUtils {
	
	/**
	 * Chaine affich�e lorsque le temps est ind�fini
	 */
	public final static String UNKNOWN_TIME_STRING = "--:--";
	
	/**
	 * Valeur indiquant que le temps n'est pas connu
	 */
	public final static int UNKNOWN_TIME = -9999;
	
	/**
	 * String Builder utilis� pour construire des chaines
	 */
	private static final StringBuilder mWorkStringBuilder = new StringBuilder();

	/**
	 * Format un temps au format hhmm en hh:mm, pour l'affichage.
	 * @param time
	 * @param showPlusSign Indique s'il faut, le cas �ch�ant, afficher le signe +
	 * @return
	 */
	public static String formatTime(int time, boolean showPlusSign) {
		mWorkStringBuilder.setLength(0);
		
		if (time < 0) {
			mWorkStringBuilder.append("-");
			time = Math.abs(time);
		} else if (showPlusSign) {
			mWorkStringBuilder.append("+");
		}
		final int hour = time / 100;
		if (hour < 10) {
			mWorkStringBuilder.append("0");
		}
		mWorkStringBuilder.append(hour).append(":");
		
		final int minute = time % 100;
		if (minute < 10) {
			mWorkStringBuilder.append("0");
		}
		mWorkStringBuilder.append(minute);
		
		return mWorkStringBuilder.toString();
	}
	
	public static String formatTime(int time) {
		return formatTime(time, false);
	}
	
	/**
	 * Formatte un nombre de minutes en hh:mm, pour l'affichage.
	 * @param time
	 * @return
	 */
	public static String formatMinutes(final int minutes) {
		return formatTime(convertInTime(minutes));
	}
	
	/**
	 * Formatte un nombre de minutes en hh:mm, pour l'affichage.
	 * @param time
	 * @return
	 */
	public static String formatMinutesWithSign(final int minutes) {
		return formatTime(convertInTime(minutes), true);
	}
	
	/**
	 * Convertit un horaire au format hhmm en un nombre de minutes
	 * @param time
	 * @return
	 */
	public static int convertInMinutes(final int time) {
		return time / 100 * 60 + time % 100;
	}
	
	/**
	 * Convertit un nombre de minutes en un horaire au format hhmm
	 * @param flexCurTime
	 * @return
	 */
	public static int convertInTime(final int minutes) {
		return minutes / 60 * 100 + minutes % 60;
	}

	/**
	 * Retourne le temps (en minutes) effectu� au total de la journ�e en comptant le temps additionnel.
	 * @param day
	 * @return
	 */
	public static int computeTotal(final DayBean day) {
		return computeTotal(day, true);
	}

	/**
	 * Retourne le temps (en minutes) effectu� au total de la journ�e
	 * @param day
	 * @param addExtraAndClip Si true, le total est �cr�t� si n�cessaire et le temps extra est compt�.
	 * @return
	 */
	public static int computeTotal(final DayBean day, final boolean addExtraAndClip) {
		int total = 0;
		
		// Calcul du temps effectu� dans la journ�e
		if (day.checkings != null && !day.checkings.isEmpty()) {
			final int lunchStart = convertInMinutes(PreferencesBean.instance.lunchStart);
			final int lunchMin = convertInMinutes(PreferencesBean.instance.lunchMin);
			final int nbCheckings = day.checkings.size();
			int minLunchEnd = -1;
			int in = 0;
			int out = 0;
			for (int curChecking = 0; curChecking < nbCheckings; curChecking = curChecking + 2) {
				// On prend les 2 prochains pointages.
				in = convertInMinutes(day.checkings.get(curChecking));
				if (curChecking + 1 < nbCheckings) {
					// Si l'intervalle est complet, le pointage de sortie est le prochain
					out = convertInMinutes(day.checkings.get(curChecking + 1));
				} else {
					// Il n'y a pas de pointage de sortie. C'est que la journ�e est en cours.
					// On va donc ajouter un faux pointage pour pouvoir calculer le temps.
					// Ajout d'un faux pointage pour calculer le temps effectuer jusqu'� maintenant.
					// On ne fait �a que si on calcule le temps d'aujourd'hui.
					final Time now = getNowTime();
					if (day.date == DateUtils.getDayId(now)) {
						// Pour avoir le temps effectu� jusqu'� maintenant, on triche et on ajoute
						// un pointage � l'heure actuelle.
						out = convertInMinutes(parseTime(now));
					}
					// Il manque un pointage et on n'est pas en train de calculer le temps d'aujourd'hui.
					// On se contente d'ignorer cet intervalle.
					else {
						continue;
					}
				}
				
				// Si l'entr�e est pendant la pause repas, on d�place ce pointage � l'heure de retour
				// de repas th�orique.
				if (minLunchEnd != -1 && in < minLunchEnd) {
					in = minLunchEnd;
					
					// A pr�sent, on conserve cet intervalle uniquement si le retour est apr�s cette heure.
					// S'il est avant, c'est qu'on est revenu travailler pendant la pause repas, et c'est perdu !
					// Dans ce dernier cas, on passe � l'intervalle suivant.
					if (out < in) {
						continue;
					}
				}
					
				// Si la sortie est apr�s le d�but r�glementaire de la pause repas, alors on calcule l'heure de retour th�orique.
				if (minLunchEnd == -1 && out >= lunchStart) {
					minLunchEnd = out + lunchMin;
				}
				
				// Ajout du temps �coul� au total
				total += out - in;
			}
		}
		
		// On ajoute le temps correspondant au type du jour
		total += PreferencesBean.getTimeByDayType(day.type);
		
		if (addExtraAndClip) {
			// On ajoute ensuite le temps additionnel.
			total += convertInMinutes(day.extra);
			
			// Ecr�tage du temps quotidien
			if (total > PreferencesBean.instance.dayMax) { 
				total = PreferencesBean.instance.dayMax;
			}
		}
	
		return total;
	}
	
	/**
	 * Retourne un temps entier (au format hhmm) � partir d'une chaine au format hh:mm.
	 * @param checking
	 * @return
	 */
	public static int parseTime(final String checking) {
		if (UNKNOWN_TIME_STRING.equals(checking)) {
			return UNKNOWN_TIME;
		}
		final String withoutSemicolon = checking.replace(":", "");
		return Integer.parseInt(withoutSemicolon);
	}
	
	/**
	 * Retourne un temps entier (au format hhmm) � partir d'un objet Time.
	 * @param checking
	 * @return
	 */
	public static int parseTime(final Time checking) {
		return checking.hour * 100 + checking.minute;
	}
	
	/**
	 * Retourne un temps en minutes � partir d'une chaine au format hh:mm.
	 * @param checking
	 * @return
	 */
	public static int parseMinutes(String checking) {
		if (UNKNOWN_TIME_STRING.equals(checking)) {
			return UNKNOWN_TIME;
		}
		int sign = 1;
		// En cas de temps n�gatif, on s'assure qu'on a bien un r�sultat n�gatif.
		if (checking.charAt(0) == '-') {
			// On indique que le signe du r�sultat sera n�gatif
			sign = -1;
			// On fait la conversion sur le reste de la chaine, sans le signe
			checking = checking.substring(1);
		}
		final String[] parts = checking.split(":");
		return sign * (Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]));
	}
	
	public static void parseDate(final long date, final Time timeToUpdate) {
		timeToUpdate.parse(String.valueOf(date));
		timeToUpdate.normalize(true);
	}
	
	public static Time parseDate(final long date) {
		final Time time = new Time();
		parseDate(date, time);
		return time;
	}
	
	/**
	 * Calcule le temps HV effectu� entre les dates indiqu�es.
	 */
	public static int computeFlexTime(final List<DayBean> days) {
		return 0;
	}
	
	public static int computeFlexTime(final DayBean day) {
		return 0;
	}
	
	/**
	 * Retourne l'heure actuelle en prenant en compte le d�calage
	 * de l'horaire entre la pointeuse et le t�l�phone
	 * @param time objet � mettre � jour
	 * @return
	 */
	public static void setToNow(final Time time) {
		time.setToNow();
		time.minute += PreferencesBean.instance.clockShift;
		time.normalize(true);
	}
	
	/**
	 * Retourne l'heure actuelle en prenant en compte le d�calage
	 * de l'horaire entre la pointeuse et le t�l�phone
	 * @return
	 */
	public static Time getNowTime() {
		final Time now = new Time();
		setToNow(now);
		return now;
	}
}
