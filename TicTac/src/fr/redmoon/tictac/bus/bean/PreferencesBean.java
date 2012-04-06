package fr.redmoon.tictac.bus.bean;

import java.io.Serializable;


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
	
// Initialisation de l'horaire variable
	// Date initiale
	public long flexInitDate;
	
	// Horaire
	public int flexInitTime;	// minutes
	
	// Date jusqu'� laquelle l'HV a �t� calcul�
	public long flexCurDate;
	
	// HV calcul�
	public int flexCurTime;	// minutes
	
	
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
		switch (type) {
			// Normal
			case 0:
				return instance.dayTypeNormalTime;
			//RTT
			case 1:
				return instance.dayTypeRttTime;
			// CP
			case 2:
				return instance.dayTypeVacancyTime;
			// F�ri�
			case 3:
				return instance.dayTypePublicHolidayTime;
			// Maladie
			case 4:
				return instance.dayTypeIllnessTime;
		}
		// Type de jour inconnu
		return 0;
	}
	
	/**
	 * Retourne la couleur de ce type de jour
	 * @param type
	 * @return
	 */
	public static int getColorByDayType(final int type) {
		switch (type) {
			// Normal
			case 0:
				return instance.dayTypeNormalColor;
			//RTT
			case 1:
				return instance.dayTypeRttColor;
			// CP
			case 2:
				return instance.dayTypeVacancyColor;
			// F�ri�
			case 3:
				return instance.dayTypePublicHolidayColor;
			// Maladie
			case 4:
				return instance.dayTypeIllnessColor;
		}
		// Type de jour inconnu
		return 0;
	}

	public void clone(final PreferencesBean otherPrefs) {
		// G�n�ral
		clockShift = otherPrefs.clockShift;

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

		// Initialisation de l'horaire variable
		flexInitDate = otherPrefs.flexInitDate;
		flexInitTime = otherPrefs.flexInitTime;
		flexCurDate = otherPrefs.flexCurDate;
		flexCurTime = otherPrefs.flexCurTime;

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