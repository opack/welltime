package fr.redmoon.tictac.bus.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

/**
 * Classe qui conserve la derni�re version connue des pr�f�rences
 */
public class PreferencesBean implements Serializable{
	private static final long serialVersionUID = 2782406634197394191L;
	
	public static PreferencesBean instance = new PreferencesBean();

// Flag indiquant s'il s'agit du premier lancement
	public boolean isFirstLaunch;
	
// Le widget doit-il afficher un timepicker ou pointer � l'heure courante ?
	public boolean widgetDisplayTimePicker;
	
// Flag indiquant s'il faut synchroniser les donn�es avec le calendrier
	public boolean syncCalendar;
	
// Limites
	// Temps de travail quotidien
	public int dayMin;	// minutes
	public int dayMax;	// minutes
	
	// Pause repas
	public int lunchMin;	// minutes
	public int lunchStart;
	public int lunchEnd;
	
	// Temps de travail hebdomadaire
	public int weekMin;	// minutes
	public int weekMax;	// minutes
	
	// Nombre de jours travaill�s dans la semaine
	// Pas directement �ditable depuis les pr�f�rence, cette
	// valeur est d�duite du contenu de workedDays.
	public int nbDaysInWeek;
	
	// Jours travaill�s
	public String workedDays;
	
	// Horaire variable
	public int flexMin;	// minutes
	public int flexMax;	// minutes
	
// Types de jour
	public Map<String, DayType> dayTypes = new HashMap<String, DayType>();
	
	// Normal
	public int dayTypeNormalTime;	// minutes
	public int dayTypeNormalColor;
	
	// Cong� pay�
	public int dayTypeVacancyTime;	// minutes
	public int dayTypeVacancyColor;
	
	// RTT
	public int dayTypeRttTime;	// minutes
	public int dayTypeRttColor;
	
	// Maladie
	public int dayTypeIllnessTime;	// minutes
	public int dayTypeIllnessColor;
	
	// F�ri�
	public int dayTypePublicHolidayTime;	// minutes
	public int dayTypePublicHolidayColor;

// D�calage avec la pointeuse
	public int clockShift;	// minutes
	
	/**
	 * Retourne le temps (en minutes) de ce type de jour
	 * @param type
	 * @return
	 */
	public static int getTimeByDayType(final String type) {
		int time = 0;
		final DayType dayType = instance.dayTypes.get(type);
		if (dayType != null) {
			time = dayType.time;
		}
		return time;
	}
	
	/**
	 * Retourne la couleur de ce type de jour
	 * @param type
	 * @return
	 */
	public static int getColorByDayType(final String type) {
		int color = 0;
		final DayType dayType = instance.dayTypes.get(type);
		if (dayType != null) {
			color = dayType.color;
		}
		return color;
	}
	
	/**
	 * Retourne le libell� de ce type de jour
	 * @param type
	 * @return
	 */
	public static String getLabelByDayType(final String type) {
		String label = "";
		final DayType dayType = instance.dayTypes.get(type);
		if (dayType != null) {
			label = dayType.label;
		} else {
			Log.w(PreferencesBean.class.getName(), "getLabelByDayType : type inconnu : " + type);
		}
		return label;
	}

	public void clone(final PreferencesBean otherPrefs) {
		// G�n�ral
		clockShift = otherPrefs.clockShift;
		syncCalendar = otherPrefs.syncCalendar;

		// Limites
		dayMin = otherPrefs.dayMin;
		dayMax = otherPrefs.dayMax;
		lunchMin = otherPrefs.lunchMin;
		lunchStart = otherPrefs.lunchStart;
		lunchEnd = otherPrefs.lunchEnd;
		weekMin = otherPrefs.weekMin;
		weekMax = otherPrefs.weekMax;
		flexMin = otherPrefs.flexMin;
		flexMax = otherPrefs.flexMax;

		// Types de jour
		dayTypes.clear();
		dayTypes.putAll(otherPrefs.dayTypes);
		dayTypeNormalTime = otherPrefs.dayTypeNormalTime;
		dayTypeNormalColor = otherPrefs.dayTypeNormalColor;
		dayTypeVacancyTime = otherPrefs.dayTypeVacancyTime;
		dayTypeVacancyColor = otherPrefs.dayTypeVacancyColor;
		dayTypeRttTime = otherPrefs.dayTypeRttTime;
		dayTypeRttColor = otherPrefs.dayTypeRttColor;
		dayTypeIllnessTime = otherPrefs.dayTypeIllnessTime;
		dayTypeIllnessColor = otherPrefs.dayTypeIllnessColor;
		dayTypePublicHolidayTime = otherPrefs.dayTypePublicHolidayTime;
		dayTypePublicHolidayColor = otherPrefs.dayTypePublicHolidayColor;
	}
}
