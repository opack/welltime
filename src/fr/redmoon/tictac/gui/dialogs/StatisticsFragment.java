package fr.redmoon.tictac.gui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.gui.activities.StatisticsResultsActivity;

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
		final long firstDay = DateUtils.getDayId(mDate1.getYear(), mDate1.getMonth(), mDate1.getDayOfMonth());
		final long lastDay = DateUtils.getDayId(mDate2.getYear(), mDate2.getMonth(), mDate2.getDayOfMonth());

		final Intent resultsIntent = new Intent(getActivity(), StatisticsResultsActivity.class);
		resultsIntent.putExtra(StatisticsResultsActivity.EXTRA_FIRST_DAY, firstDay);
		resultsIntent.putExtra(StatisticsResultsActivity.EXTRA_LAST_DAY, lastDay);
		getActivity().startActivity(resultsIntent);
	}
}