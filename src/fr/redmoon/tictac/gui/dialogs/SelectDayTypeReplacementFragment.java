package fr.redmoon.tictac.gui.dialogs;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.bean.DayType;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.db.DbAdapter;
import fr.redmoon.tictac.gui.activities.TicTacActivity;
import fr.redmoon.tictac.gui.adapters.DayTypeAdapter;

public class SelectDayTypeReplacementFragment extends DialogFragment implements DialogInterface.OnClickListener {
	public final static String TAG = SelectDayTypeReplacementFragment.class.getName();

	public interface OnDayTypeReplacedListener {
		void onDayTypeReplaced();
	}
	
	private String mUnknownDayTypeLabel;
	private String mTempDayTypeId;
	private Spinner mSpnDayType;
	
	private OnDayTypeReplacedListener mListener;
	
	public void setListener(OnDayTypeReplacedListener listener) {
		mListener = listener;
	}

	@Override
	public void setArguments(Bundle args) {
		mUnknownDayTypeLabel = args.getString(DialogArgs.UNKNOWN_DAYTYPE.name());
		mTempDayTypeId = args.getString(DialogArgs.TEMP_DAYTYPE_ID.name());
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		//On instancie notre layout en tant que View
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View dialogView = factory.inflate(R.layout.dlg_select_day_replacement, null);
 
        // Sauvegarde des contrôles pour lecture lors de la validation
        mSpnDayType = (Spinner)dialogView.findViewById(R.id.day_type);
		
        //Création de l'AlertDialog
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
 
        //On affecte la vue personnalisé que l'on a crée à notre AlertDialog
        adb.setView(dialogView);
        
        //On donne un titre à l'AlertDialog
        adb.setTitle(R.string.dlg_title_edit_day_type);
        
        // On modifie le libellé du message
        final String message = getActivity().getString(R.string.import_days_unknown_daytype, mUnknownDayTypeLabel);
        final TextView unknownDayType = (TextView)dialogView.findViewById(R.id.unknown_daytype);
        unknownDayType.setText(message);
 
        //On modifie l'icône de l'AlertDialog pour le fun ;)
        //adb.setIcon(android.R.drawable.ic_dialog_alert);
        
        // Remplissage de la combobox
        final List<DayType> dayTypes = new ArrayList<DayType>(PreferencesBean.instance.dayTypes.values());
        final ArrayAdapter<DayType> adapter = new DayTypeAdapter(getActivity(), android.R.layout.simple_spinner_item, dayTypes);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpnDayType.setAdapter(adapter);
        
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
		// Récupération de la valeur choisie
		final DayType type = (DayType)mSpnDayType.getSelectedItem();
		
		// Mise à jour de tous les types de jours en base qui correspondent
		final TicTacActivity activity = (TicTacActivity)getActivity();
		final int nbUpdated = DbAdapter.getInstance().updateDayType(mTempDayTypeId, type.id);
		
		// Affichage des résultats
		Toast.makeText(activity, "La mise à jour a été effectuée sur " + nbUpdated + " jours.", Toast.LENGTH_SHORT).show();
		
		// Notification des listeners
		if (mListener != null) {
			mListener.onDayTypeReplaced();
		}
	}
}