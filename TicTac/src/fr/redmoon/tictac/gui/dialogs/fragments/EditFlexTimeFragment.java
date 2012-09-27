package fr.redmoon.tictac.gui.dialogs.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.FlexUtils;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.db.DbAdapter;
import fr.redmoon.tictac.gui.activities.TicTacActivity;
import fr.redmoon.tictac.gui.dialogs.DialogArgs;

public class EditFlexTimeFragment extends DialogFragment implements DialogInterface.OnClickListener {
	public final static String TAG = EditFlexTimeFragment.class.getName();
	
	private final CharSequence NEGATIVE_SIGN = "-";
	private final CharSequence POSITIVE_SIGN = "+";
	
	private long mDate;
	private int mOldTime;
	
	private DatePicker mDatePicker;
	private TimePicker mTimePicker;
	private Button mBtnSign;
	
	@Override
	public void setArguments(Bundle args) {
		mDate = args.getLong(DialogArgs.DATE.name());
		mOldTime = args.getInt(DialogArgs.TIME.name());
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		//On instancie notre layout en tant que View
		LayoutInflater factory = LayoutInflater.from(getActivity());
        final View dialogView = factory.inflate(R.layout.dlg_date_time, null);
        
        //Cr�ation de l'AlertDialog
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
 
        //On affecte la vue personnalis� que l'on a cr�e � notre AlertDialog
        adb.setView(dialogView);
        
        //On donne un titre � l'AlertDialog
		adb.setTitle(R.string.dlg_title_edit_flex_time);
		
		//On modifie l'ic�ne de l'AlertDialog pour le fun ;)
        //adb.setIcon(android.R.drawable.ic_dialog_alert);
		
		mBtnSign = (Button)dialogView.findViewById(R.id.btn_sign);
		mBtnSign.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if ((Integer)mBtnSign.getTag() == -1) {
					mBtnSign.setText(POSITIVE_SIGN);
					mBtnSign.setTag(1);
				} else {
					mBtnSign.setText(NEGATIVE_SIGN);
					mBtnSign.setTag(-1);
				}
			}
		});
		
		// Initialisation du DatePicker, du TimePicker et du bouton de signe
		mDatePicker = (DatePicker)dialogView.findViewById(R.id.date);
		mDatePicker.updateDate(DateUtils.extractYear(mDate), DateUtils.extractMonth(mDate), DateUtils.extractDayOfMonth(mDate));
		mTimePicker = (TimePicker)dialogView.findViewById(R.id.time);
		mTimePicker.setIs24HourView(DateFormat.is24HourFormat(getActivity()));
		// Si le temps est inconnu, alors on initialise la bo�te de dialogue � 0
		int time = mOldTime;
		if (time == TimeUtils.UNKNOWN_TIME) {
			time = 0;
		}
		if (time < 0) {
			mBtnSign.setText(NEGATIVE_SIGN);
			mBtnSign.setTag(-1);
			
			// Pour ne pas perturber l'initialisation du TimePicker, on fait comme si time
			// �tait positif
			time = -time;
		} else {
			mBtnSign.setText(POSITIVE_SIGN);
			mBtnSign.setTag(1);
		}
		mTimePicker.setCurrentHour(time / 60);
		mTimePicker.setCurrentMinute(time % 60);
		
		// Initialisation des listeners
		adb.setPositiveButton(R.string.btn_save, this);
		
		adb.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
			@Override
    		public void onClick(DialogInterface dialog, int id) {
            	dialog.dismiss();
        	}
		});
		
		return adb.create();
	}
	
	@Override
	public void onClick(DialogInterface dialog, int id) {
		// Suppression du focus pour conserver la valeur �ventuellement saisie
		// par l'utilisateur � la main dans les champs
		mDatePicker.clearFocus();
		mTimePicker.clearFocus();
		
		// DBG Bizarre qu'on ne mette pas � jour l'HV du jour s�lectionn�... A creuser.
		//final long date = DateUtils.getDayId(mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth());
		final int sign = (Integer)mBtnSign.getTag();
		final int time = sign * (mTimePicker.getCurrentHour() * 60 + mTimePicker.getCurrentMinute());
		
		final TicTacActivity activity = (TicTacActivity)getActivity();
		final DbAdapter db = activity.getDbAdapter();
		if (db == null || time == mOldTime) {
			return;
		}
		
		
		// Mise � jour de la base de donn�es
		if (db.updateFlexTime(mDate, time)) {
			// Mise � jour de tous les HV qui suivent cette date
			final FlexUtils flexUtils = new FlexUtils(db);
			flexUtils.updateFlex(mDate);
			
			// Mise � jour de l'affichage
			activity.populateView(mDate);
		}
	}
}