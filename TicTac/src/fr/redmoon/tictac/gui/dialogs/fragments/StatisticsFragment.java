package fr.redmoon.tictac.gui.dialogs.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.gui.activities.TicTacActivity;

public class StatisticsFragment extends DialogFragment implements OnClickListener {
	public final static String TAG = StatisticsFragment.class.getName();
	
	private DatePicker mDate1; 
	private DatePicker mDate2;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		//On instancie notre layout en tant que View
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View dialogView = factory.inflate(R.layout.dlg_period_chooser, null);
        
        // Sauvegarde des datepickers pour lecture lors de la validation
    	mDate1 = (DatePicker)dialogView.findViewById(R.id.date1);
		mDate2 = (DatePicker)dialogView.findViewById(R.id.date2);
 
        //Cr�ation de l'AlertDialog
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
 
        //On affecte la vue personnalis� que l'on a cr�e � notre AlertDialog
        adb.setView(dialogView);
        
        //On donne un titre � l'AlertDialog
        adb.setTitle(R.string.period_statistics_title);
 
        //On modifie l'ic�ne de l'AlertDialog pour le fun ;)
        //adb.setIcon(android.R.drawable.ic_dialog_alert);
        
        //On affecte un bouton "OK" � notre AlertDialog et on lui affecte un �v�nement
        adb.setPositiveButton(R.string.btn_ok, this);
 
        //On cr�e un bouton "Annuler" � notre AlertDialog et on lui affecte un �v�nement
        adb.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });
        
        return adb.create();
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		// R�cup�ration des jours de la p�riode
		final TicTacActivity activity = (TicTacActivity)getActivity();
		final long firstDay = DateUtils.getDayId(mDate1.getYear(), mDate1.getMonth(), mDate1.getDayOfMonth());
		final long lastDay = DateUtils.getDayId(mDate2.getYear(), mDate2.getMonth(), mDate2.getDayOfMonth());
		final List<DayBean> days = new ArrayList<DayBean>();
		activity.getDbAdapter().fetchDays(firstDay, lastDay, days);
		
		// Calcul des statistiques et affichage d'une bo�te de dialogue avec les r�sultats
		LayoutInflater factory = LayoutInflater.from(activity);
        View dialogView = factory.inflate(R.layout.dlg_period_statistics_period, null);
		computePeriodStatistics(days, firstDay, lastDay, dialogView);
		
		// Affichage des r�sultats
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(dialogView);
		builder.setTitle(R.string.app_name);
		builder.setCancelable(false);
		builder.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.dismiss();
            }
        });
		builder.show();
	}

	/**
	 * Calcule les statistiques de la p�riode et met � jour la bo�te de dialogue
	 * sp�cifi�e.
	 * @param firstDay
	 * @param lastDay
	 * @param dialogView
	 */
	private void computePeriodStatistics(final List<DayBean> days, final long firstDay, final long lastDay, final View dialogView) {
		// Nombre de jours travaill�s de chaque type
		final Map<String, Float> nbWorkedDaysByType = new HashMap<String, Float>();
		for (String dayTypeId : PreferencesBean.instance.dayTypes.keySet()) {
			nbWorkedDaysByType.put(dayTypeId, Float.valueOf(0.0f));
		}
		Float nbWorkedDaysForThisType;
		// Nombre de jours travaill�s (= nombre de jour o� il a fallu aller au travail, donc
		// nombre de jours comptant au moins 1 pointage)
		int nbWorkedDays = 0;
		// Nombre d'heures travaill�es (utile pour les ind�pendants)
		int workTime;
		int totalWorkedTime = 0; // En minutes
		// Nombre d�heures �cr�t�es
		int totalLostTime = 0; // En minutes
		// Temps de travail moyen par jour (en comptant le temps perdu)
		int meanDayWorkTime = 0;
		
		// Parcours des jours et calcul des statistiques
		for (DayBean day : days) {
			// Nombre de jours travaill�s de chaque type
			nbWorkedDaysForThisType = nbWorkedDaysByType.get(day.typeMorning);
			nbWorkedDaysByType.put(day.typeMorning, nbWorkedDaysForThisType.floatValue() + 0.5f);
			nbWorkedDaysForThisType = nbWorkedDaysByType.get(day.typeAfternoon);
			nbWorkedDaysByType.put(day.typeAfternoon, nbWorkedDaysForThisType.floatValue() + 0.5f);
			
			// Nombre de jours travaill�s
			if (!day.checkings.isEmpty()) {
				nbWorkedDays++;
			}
			
			// Nombre d'heures travaill�es
			workTime = TimeUtils.computeTotal(day, false);
			totalWorkedTime += workTime;
			
			// Nombre d�heures �cr�t�es
			totalLostTime += Math.max(0, workTime - PreferencesBean.instance.dayMax);
		}
		// Temps de travail moyen par jour (en comptant le temps perdu)
		if (nbWorkedDays > 0) {
			meanDayWorkTime = totalWorkedTime / nbWorkedDays;
		}
		
		// Mise � jour de la bo�te de dialogue
		final String caption = getResources().getString(
			R.string.period_statistics_caption,
			DateUtils.formatDateDDMMYYYY(firstDay),
			DateUtils.formatDateDDMMYYYY(lastDay));
		final TextView txtCaption = (TextView)dialogView.findViewById(R.id.txt_period_statistics);
		txtCaption.setText(caption);
		final TextView txtTotalWorkedDays = (TextView)dialogView.findViewById(R.id.txt_total_worked_days);
		txtTotalWorkedDays.setText(String.valueOf(nbWorkedDays));
		final TextView txtTotalWorkedTime = (TextView)dialogView.findViewById(R.id.txt_total_worked_time);
		txtTotalWorkedTime.setText(TimeUtils.formatMinutes(totalWorkedTime));
		final TextView txtTotalLostTime = (TextView)dialogView.findViewById(R.id.txt_total_lost_time);
		txtTotalLostTime.setText(TimeUtils.formatMinutes(totalLostTime));
		final TextView txtMeanDayWorkTime = (TextView)dialogView.findViewById(R.id.txt_mean_day_work_time);
		txtMeanDayWorkTime.setText(TimeUtils.formatMinutes(meanDayWorkTime));
	}
}