package fr.redmoon.tictac.bus.bean;

import java.io.Serializable;

import android.graphics.Color;
import fr.redmoon.tictac.bus.DayTypes;


/**
 * Classe qui conserve la derni�re version connue des pr�f�rences
 */
public class PreferencesBean implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2782406634197394191L;
	
	public static PreferencesBean instance = new PreferencesBean();

// Flag indiquant s'il s'agit du premier lancement
	public boolean isFirstLaunch;
	
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
	
	// Horaire variable
	public int flexMin;	// minutes
	public int flexMax;	// minutes
	
// Types de jour
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
	public static int getTimeByDayType(final int type) {
		if (type == DayTypes.not_worked.ordinal()) {
			return 0;
		} else if (type == DayTypes.normal.ordinal()) {
			return instance.dayTypeNormalTime;
		} else if (type == DayTypes.RTT.ordinal()) {
			return instance.dayTypeRttTime;
		} else if (type == DayTypes.vacancy.ordinal()) {
			return instance.dayTypeVacancyTime;
		} else if (type == DayTypes.publicHoliday.ordinal()) {
			return instance.dayTypePublicHolidayTime;
		} else if (type == DayTypes.illness.ordinal()) {
			return instance.dayTypeIllnessTime;
		} else {
			// Type de jour inconnu
			return 0;
		}
	}
	
	/**
	 * Retourne la couleur de ce type de jour
	 * @param type
	 * @return
	 */
	public static int getColorByDayType(final int type) {
		if (type == DayTypes.not_worked.ordinal()) {
			return Color.rgb(204, 204, 204);
		} else if (type == DayTypes.normal.ordinal()) {
			return instance.dayTypeNormalColor;
		} else if (type == DayTypes.RTT.ordinal()) {
			return instance.dayTypeRttColor;
		} else if (type == DayTypes.vacancy.ordinal()) {
			return instance.dayTypeVacancyColor;
		} else if (type == DayTypes.publicHoliday.ordinal()) {
			return instance.dayTypePublicHolidayColor;
		} else if (type == DayTypes.illness.ordinal()) {
			return instance.dayTypeIllnessColor;
		} else {
			// Type de jour inconnu
			return 0;
		}
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
