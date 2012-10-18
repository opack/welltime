package fr.redmoon.tictac.gui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.FlexUtils;
import fr.redmoon.tictac.db.DbAdapter;
import fr.redmoon.tictac.gui.activities.TicTacActivity;

public class CleanDaysFragment extends DialogFragment implements OnClickListener {
	public final static String TAG = CleanDaysFragment.class.getName();
	
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
 
        //Création de l'AlertDialog
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
 
        //On affecte la vue personnalisé que l'on a crée à notre AlertDialog
        adb.setView(dialogView);
        
        //On donne un titre à l'AlertDialog
        adb.setTitle(R.string.period_cleaner_title);
 
        //On modifie l'icône de l'AlertDialog pour le fun ;)
        //adb.setIcon(android.R.drawable.ic_dialog_alert);
        
        //On affecte un bouton "OK" à notre AlertDialog et on lui affecte un évènement
        adb.setPositiveButton(R.string.btn_ok, this);
 
        //On crée un bouton "Annuler" à notre AlertDialog et on lui affecte un évènement
        adb.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });
        
        return adb.create();
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		final TicTacActivity activity = (TicTacActivity)getActivity();
		
		// Identifiants des jours saisis
		final long firstDay = DateUtils.getDayId(mDate1.getYear(), mDate1.getMonth(), mDate1.getDayOfMonth());
		final long lastDay = DateUtils.getDayId(mDate2.getYear(), mDate2.getMonth(), mDate2.getDayOfMonth());
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(R.string.app_name);
		builder.setMessage(R.string.period_cleaner_confirm);
		builder.setCancelable(false);
		builder.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// Suppression des jours
				final DbAdapter db = DbAdapter.getInstance(activity);
				db.openDatabase();
				if (db.deleteDays(firstDay, lastDay)) {
					// Mise à jour de l'HV.
			    	final FlexUtils flexUtils = new FlexUtils(db);
			    	flexUtils.updateFlex(firstDay);
			    	
					final String message = activity.getResources().getString(
						R.string.period_cleaner_success,
						DateUtils.formatDateDDMMYYYY(firstDay),
						DateUtils.formatDateDDMMYYYY(lastDay));
					Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(activity, R.string.period_cleaner_failed, Toast.LENGTH_SHORT).show();
				}
				db.closeDatabase();
			}
		});
		builder.setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
			}
		});
		builder.show();
	}
}