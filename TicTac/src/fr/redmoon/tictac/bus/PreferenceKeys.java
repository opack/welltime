package fr.redmoon.tictac.bus;

import fr.redmoon.tictac.gui.activities.PreferencesActivity;


public enum PreferenceKeys {
// Nombre de lancements de l'application
	isFirstLaunch("isFirstLaunch"),
	
// Le widget doit-il afficher un timepicker ou pointer à l'heure courante ?
	widgetDisplayTimePicker("widget_display_timepicker"),
	
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
	dayTypeLabel(PreferencesActivity.PREF_DAYTYPE_TITLE + "_"),
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
