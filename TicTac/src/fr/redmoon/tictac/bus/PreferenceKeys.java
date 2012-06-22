package fr.redmoon.tictac.bus;


public enum PreferenceKeys {
// Nombre de lancements de l'application
	isFirstLaunch("isFirstLaunch"),
	
// Décalage entre l'horloge de la pointeuse et celle du téléphone
	clockShift("clock_shift"),
	
// Activer la synchronisation avec le calendrier
	syncCalendar("sync_calendar"),
	
// Limites
	// Temps de travail quotidien en minutes
	dayMin("day_min"),
	dayMax("day_max"),
	
	// Pause repas
	lunchMin("lunch_min"),		// en minutes
	lunchStart("lunch_start"),	// au format hhmm
	lunchEnd("lunch_end"),		// au format hhmm
	
	// Temps de travail hebdomadaire en minutes
	weekMin("week_min"),
	weekMax("week_max"),
	
	// Horaire variable en minutes
	flexMin("flex_min"),
	flexMax("flex_max"),
	
// Initialisation de l'horaire variable
	// Date initiale au format yyyymmdd
	flexInitDate("flex_init_date"),
	
	// Horaire en minutes
	flexInitTime("flex_init_time"),
	
	// Date jusqu'à laquelle l'HV a été calculé au format yyyymmdd
	flexCurDate("flex_cur_date"),
	
	// HV calculé en minutes
	flexCurTime("flex_cur_time"),
	
// Types de jour
	// Normal
	dayTypeNormalTime("daytype_normal_time"),		// en minutes
	dayTypeNormalColor("daytype_normal_color"),		// en entier
	
	// Congé payé
	dayTypeVacancyTime("daytype_vacancy_time"),		// en minutes
	dayTypeVacancyColor("daytype_vacancy_color"),	// en entier
	
	// RTT
	dayTypeRttTime("daytype_rtt_time"),				// en minutes
	dayTypeRttColor("daytype_rtt_color"),			// en entier
	
	// Maladie
	dayTypeIllnessTime("daytype_illness_time"),		// en minutes
	dayTypeIllnessColor("daytype_illness_color"),	// en entier
	
	// Férié
	dayTypePublicHolidayTime("daytype_publicholiday_time"),		// en minutes
	dayTypePublicHolidayColor("daytype_publicholiday_color");	// en entier
	
	private final String key;
	
	private PreferenceKeys(final String key) {
		this.key = key;
	}
	
	public String getKey() {
		return key;
	}
}
