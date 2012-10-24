package fr.redmoon.tictac.bus;

import fr.redmoon.tictac.gui.activities.PreferencesActivity;


public enum PreferenceKeys {
// Nombre de lancements de l'application
	isFirstLaunch("isFirstLaunch"),
	
// Paramétrage du widget
	// Le widget doit-il afficher un timepicker ou pointer à l'heure courante ?
	widgetDisplayTimePicker("widget_displaytimepicker"),
	
// Décalage entre l'horloge de la pointeuse et celle du téléphone
	clockShift("clock_shift"),
	
// Activer la synchronisation avec le calendrier
	syncCalendar("sync_calendar"),
	
// Limites
	// Temps de travail quotidien en minutes
	dayMin("limits_day_min"),
	dayMax("limits_day_max"),
	
	// Pause repas
	lunchMin("limits_lunch_min"),		// en minutes
	lunchStart("limits_lunch_start"),	// au format hhmm
	lunchEnd("limits_lunch_end"),		// au format hhmm
	
	// Temps de travail hebdomadaire en minutes
	weekMin("limits_week_min"),
	weekMax("limits_week_max"),
	
	// Nombre de jours dans la semaine
	nbDaysInWeek("limits_nb_days_in_week"),
	
	// Jours travaillés
	workedDays("limits_worked_days"),
	
	// Horaire variable en minutes
	flexMin("limits_flex_min"),
	flexMax("limits_flex_max"),
	
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
	dayTypeLabel(PreferencesActivity.PREF_DAYTYPE_LABEL + "_"),
	dayTypeTime(PreferencesActivity.PREF_DAYTYPE_TIME + "_"), // en minutes
	dayTypeColor(PreferencesActivity.PREF_DAYTYPE_COLOR + "_"); // en entier
	
	private final String key;
	
	private PreferenceKeys(final String key) {
		this.key = key;
	}
	
	public String getKey() {
		return key;
	}
}
