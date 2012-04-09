package fr.redmoon.tictac.bus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import fr.redmoon.tictac.PreferencesActivity;
import fr.redmoon.tictac.bus.bean.PreferencesBean;

public class PreferencesUtils {
	public static void updatePreferencesBean(final Context context) {
		// Met � jour le bean PreferencesBean avec les valeurs sauvegard�es
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	// Flag de premier lancement
		PreferencesBean.instance.isFirstLaunch = prefs.getBoolean(PreferenceKeys.isFirstLaunch.getKey(), true);
		
	// D�calage pointeuse/t�l�phone
		PreferencesBean.instance.clockShift = TimeUtils.parseMinutes(prefs.getString(PreferenceKeys.clockShift.getKey(), "00:00"));
		
	// Limites
		// Temps de travail quotidien
		PreferencesBean.instance.dayMin = TimeUtils.parseMinutes(prefs.getString(PreferenceKeys.dayMin.getKey(), "07:00"));
		PreferencesBean.instance.dayMax = TimeUtils.parseMinutes(prefs.getString(PreferenceKeys.dayMax.getKey(), "09:00"));
		
		// Pause repas
		PreferencesBean.instance.lunchMin = TimeUtils.parseMinutes(prefs.getString(PreferenceKeys.lunchMin.getKey(), "00:45"));
		PreferencesBean.instance.lunchStart = TimeUtils.parseTime(prefs.getString(PreferenceKeys.lunchStart.getKey(), "11:45"));
		PreferencesBean.instance.lunchEnd = TimeUtils.parseTime(prefs.getString(PreferenceKeys.lunchEnd.getKey(), "14:00"));
		
		// Temps de travail hebdomadaire
		PreferencesBean.instance.weekMin = TimeUtils.parseMinutes(prefs.getString(PreferenceKeys.weekMin.getKey(), "35:00"));
		PreferencesBean.instance.weekMax = TimeUtils.parseMinutes(prefs.getString(PreferenceKeys.weekMax.getKey(), "39:00"));
		
		// Horaire variable
		PreferencesBean.instance.flexMin = TimeUtils.parseMinutes(prefs.getString(PreferenceKeys.flexMin.getKey(), "00:00"));
		PreferencesBean.instance.flexMax = TimeUtils.parseMinutes(prefs.getString(PreferenceKeys.flexMax.getKey(), "07:00"));
		
	// Types de jour
		// Normal
		PreferencesBean.instance.dayTypeNormalTime = TimeUtils.parseMinutes(prefs.getString(PreferenceKeys.dayTypeNormalTime.getKey(), "00:00"));
		PreferencesBean.instance.dayTypeNormalColor = prefs.getInt(PreferenceKeys.dayTypeNormalColor.getKey(), -657931);
		
		// Cong� pay�
		PreferencesBean.instance.dayTypeVacancyTime = TimeUtils.parseMinutes(prefs.getString(PreferenceKeys.dayTypeVacancyTime.getKey(), "07:00"));
		PreferencesBean.instance.dayTypeVacancyColor = prefs.getInt(PreferenceKeys.dayTypeVacancyColor.getKey(), -5381890);
		
		// RTT
		PreferencesBean.instance.dayTypeRttTime = TimeUtils.parseMinutes(prefs.getString(PreferenceKeys.dayTypeRttTime.getKey(), "07:00"));
		PreferencesBean.instance.dayTypeRttColor = prefs.getInt(PreferenceKeys.dayTypeRttColor.getKey(), -5381890);
		
		// Maladie
		PreferencesBean.instance.dayTypeIllnessTime = TimeUtils.parseMinutes(prefs.getString(PreferenceKeys.dayTypeIllnessTime.getKey(), "07:00"));
		PreferencesBean.instance.dayTypeIllnessColor = prefs.getInt(PreferenceKeys.dayTypeIllnessColor.getKey(), -12619008);
		
		// F�ri�
		PreferencesBean.instance.dayTypePublicHolidayTime = TimeUtils.parseMinutes(prefs.getString(PreferenceKeys.dayTypePublicHolidayTime.getKey(), "07:00"));
		PreferencesBean.instance.dayTypePublicHolidayColor = prefs.getInt(PreferenceKeys.dayTypePublicHolidayColor.getKey(), -3312092);
	}
	
	public static void savePreference(final Context context, final String key, final boolean value) {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		final SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(key, value);
      	editor.commit();
	}
	
	public static void savePreference(final Context context, final String key, final int value) {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		final SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(key, value);
      	editor.commit();
	}
	
	public static void savePreference(final Context context, final String key, final long value) {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		final SharedPreferences.Editor editor = preferences.edit();
		editor.putLong(key, value);
      	editor.commit();
	}
	
	public static boolean isFirstLaunch(final Context context) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(PreferenceKeys.isFirstLaunch.getKey(), true);
	}
	
	public static int getPreference(final Context context, final String key, final int defaultValue) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getInt(key, defaultValue);
	}

	/**
	 * Met � jour les pr�f�rences avec les valeurs du bean
	 * @param context
	 */
	public static void savePreferencesBean(final Context context) {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		final SharedPreferences.Editor editor = preferences.edit();
		
	// Flag de premier lancement
		editor.putBoolean(PreferenceKeys.isFirstLaunch.getKey(), PreferencesBean.instance.isFirstLaunch);
		
	// D�calage pointeuse/t�l�phone
		editor.putString(PreferenceKeys.clockShift.getKey(), TimeUtils.formatMinutes(PreferencesBean.instance.clockShift));
		
	// Limites
		// Temps de travail quotidien
		editor.putString(PreferenceKeys.dayMin.getKey(), TimeUtils.formatMinutes(PreferencesBean.instance.dayMin));
		editor.putString(PreferenceKeys.dayMax.getKey(), TimeUtils.formatMinutes(PreferencesBean.instance.dayMax));
		
		// Pause repas
		editor.putString(PreferenceKeys.lunchMin.getKey(), TimeUtils.formatMinutes(PreferencesBean.instance.lunchMin));
		editor.putString(PreferenceKeys.lunchStart.getKey(), TimeUtils.formatTime(PreferencesBean.instance.lunchStart));
		editor.putString(PreferenceKeys.lunchEnd.getKey(), TimeUtils.formatTime(PreferencesBean.instance.lunchEnd));
		
		// Temps de travail hebdomadaire
		editor.putString(PreferenceKeys.weekMin.getKey(), TimeUtils.formatMinutes(PreferencesBean.instance.weekMin));
		editor.putString(PreferenceKeys.weekMax.getKey(), TimeUtils.formatMinutes(PreferencesBean.instance.weekMax));
		
		// Horaire variable
		editor.putString(PreferenceKeys.flexMin.getKey(), TimeUtils.formatMinutes(PreferencesBean.instance.flexMin));
		editor.putString(PreferenceKeys.flexMax.getKey(), TimeUtils.formatMinutes(PreferencesBean.instance.flexMax));
		
	// Types de jour
		// Normal
		editor.putString(PreferenceKeys.dayTypeNormalTime.getKey(), TimeUtils.formatMinutes(PreferencesBean.instance.dayTypeNormalTime));
		editor.putInt(PreferenceKeys.dayTypeNormalColor.getKey(), PreferencesBean.instance.dayTypeNormalColor);
		
		// Cong� pay�
		editor.putString(PreferenceKeys.dayTypeVacancyTime.getKey(), TimeUtils.formatMinutes(PreferencesBean.instance.dayTypeVacancyTime));
		editor.putInt(PreferenceKeys.dayTypeVacancyColor.getKey(), PreferencesBean.instance.dayTypeVacancyColor);
		
		// RTT
		editor.putString(PreferenceKeys.dayTypeRttTime.getKey(), TimeUtils.formatMinutes(PreferencesBean.instance.dayTypeRttTime));
		editor.putInt(PreferenceKeys.dayTypeRttColor.getKey(), PreferencesBean.instance.dayTypeRttColor);
		
		// Maladie
		editor.putString(PreferenceKeys.dayTypeIllnessTime.getKey(), TimeUtils.formatMinutes(PreferencesBean.instance.dayTypeIllnessTime));
		editor.putInt(PreferenceKeys.dayTypeIllnessColor.getKey(), PreferencesBean.instance.dayTypeIllnessColor);
		
		// F�ri�
		editor.putString(PreferenceKeys.dayTypePublicHolidayTime.getKey(), TimeUtils.formatMinutes(PreferencesBean.instance.dayTypePublicHolidayTime));
		editor.putInt(PreferenceKeys.dayTypePublicHolidayColor.getKey(), PreferencesBean.instance.dayTypePublicHolidayColor);
		
      	editor.commit();
	}

	/**
	 * Affiche les pr�f�rences
	 * @param activity
	 */
	public static void showPreferences(final Activity activity) {
		final Intent prefsActivity = new Intent(activity, PreferencesActivity.class);
		activity.startActivity(prefsActivity);
	}
}
