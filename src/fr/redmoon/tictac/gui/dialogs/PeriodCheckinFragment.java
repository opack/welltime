package fr.redmoon.tictac.gui.dialogs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.FlexUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.bean.DayType;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.bus.export.tocalendar.CalendarAccess;
import fr.redmoon.tictac.db.DbAdapter;
import fr.redmoon.tictac.gui.activities.TicTacActivity;
import fr.redmoon.tictac.gui.adapters.DayTypeAdapter;

public class PeriodCheckinFragment extends DialogFragment implements OnClickListener {
	public final static String TAG = PeriodCheckinFragment.class.getName();
	
	private DatePicker mDate1; 
	private DatePicker mDate2;
	private Spinner mDayType;
	private EditText mNote;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		//On instancie notre layout en tant que View
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View dialogView = factory.inflate(R.layout.dlg_period_checkin, null);
 
        // Sauvegarde des contrôles pour lecture lors de la validation
    	mDate1 = (DatePicker)dialogView.findViewById(R.id.date1);
		mDate2 = (DatePicker)dialogView.findViewById(R.id.date2);
		mDayType = (Spinner)dialogView.findViewById(R.id.day_type);
		mNote = (EditText)dialogView.findViewById(R.id.note);
		
        //Création de l'AlertDialog
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
 
        //On affecte la vue personnalisé que l'on a crée à notre AlertDialog
        adb.setView(dialogView);
        
        //On donne un titre à l'AlertDialog
        adb.setTitle(R.string.period_checkin_title);
 
        //On modifie l'icône de l'AlertDialog pour le fun ;)
        //adb.setIcon(android.R.drawable.ic_dialog_alert);
        
        final List<DayType> dayTypes = new ArrayList<DayType>(PreferencesBean.instance.dayTypes.values());
		ArrayAdapter<DayType> adapter = new DayTypeAdapter(getActivity(), android.R.layout.simple_spinner_item, dayTypes);
        final Spinner spinner = (Spinner)dialogView.findViewById(R.id.day_type);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner.setAdapter(adapter);
	    spinner.setPromptId(R.string.dlg_title_edit_day_type);
 
        //On affecte un bouton "OK" à notre AlertDialog et on lui affecte un évènement
        adb.setPositiveButton(R.string.btn_checkin, this);
 
        //On crée un bouton "Annuler" à notre AlertDialog et on lui affecte un évènement
        adb.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
          } });
		return adb.create();
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		int nbDaysCreated = 0;
		int nbDaysUpdated = 0;
		
		// Récupération des dates et du type de jour.
		final Calendar calendar = new GregorianCalendar(mDate1.getYear(), mDate1.getMonth(), mDate1.getDayOfMonth());
		final long lastDay = DateUtils.getDayId(mDate2.getYear(), mDate2.getMonth(), mDate2.getDayOfMonth());
		final String note = mNote.getText().toString();
		final DayType dayType = (DayType)mDayType.getSelectedItem();
		
		// Parcours des jours
		final TicTacActivity activity = (TicTacActivity)getActivity();
		final long firstDay = DateUtils.getDayId(calendar);
		long curDate = firstDay;
		final DayBean dayData = new DayBean();
		while (curDate <= lastDay) {
			// On vérifie s'il s'agit d'un jour travaillé dans la semaine
			if (DateUtils.isWorkingWeekDay(calendar)) {
				// On prépare le jour à créer ou mettre à jour
				DbAdapter.getInstance().fetchDay(curDate, dayData);
				dayData.typeMorning = dayType.id;
				dayData.typeAfternoon = dayType.id;
				if (note != null) {
					dayData.note = note;
				}
				
				// On crée ce jour en base s'il n'existe pas, sinon on le
				// met à jour
				if (dayData.isValid) {
					DbAdapter.getInstance().updateDay(dayData);
					if (dayData.isValid) {
						nbDaysUpdated++;
					}
				} else {
					DbAdapter.getInstance().createDay(dayData);
					if (dayData.isValid) {
						nbDaysCreated++;
					}
				}
				if (dayData.isValid && PreferencesBean.instance.syncCalendar) {
					// Créé ou mis à jour, le jour existe désormais et son type
					// a été renseigné. Il faut à présent ajoutr l'évènement
					// corresondant dans le calendrier
					CalendarAccess.getInstance().createDayTypeEvent(dayData.date, dayData.typeMorning, dayData.typeAfternoon);
				}
			}
			
			// On passe au lendemain
			calendar.add(Calendar.DAY_OF_YEAR, 1);
			curDate = DateUtils.getDayId(calendar);
		}
		
		// Mise à jour de l'HV.
    	final FlexUtils flexUtils = new FlexUtils();
    	flexUtils.updateFlex(firstDay);
		
		// Affichage des résultats
		Toast.makeText(
				activity,
				activity.getString(
					R.string.period_checkin_results,
					nbDaysCreated, 
					nbDaysUpdated),
				Toast.LENGTH_LONG)
			.show();
	}
}