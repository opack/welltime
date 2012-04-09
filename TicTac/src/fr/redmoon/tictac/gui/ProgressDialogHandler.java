package fr.redmoon.tictac.gui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import fr.redmoon.tictac.R;

/**
 * Définit le Handler qui reçoit les messages du thread et met à jour la barre de progression
 */
public class ProgressDialogHandler extends Handler {
	private final int mDialogId;
	private final Activity mActivity;
	private final ProgressDialog mProgressDialog;
	
	public ProgressDialogHandler(final Activity activity, final ProgressDialog progressDialog, final int dialogId) {
		mActivity = activity;
		mProgressDialog = progressDialog;
		mDialogId = dialogId;
	}
	
    public void handleMessage(Message msg) {
    	if (msg.what == DbInserterThread.STATE_IMPORTING_DAYS
    	|| msg.what == DbInserterThread.STATE_IMPORTING_WEEKS) {
    		mProgressDialog.setProgress(msg.arg1);
            mProgressDialog.setSecondaryProgress(msg.arg2);
    	} else {
    		// Fermeture de la boîte de dialogue
    		mActivity.dismissDialog(mDialogId);
    		
    		// Affichage du nombre de jours créés/màj
    		final int nbDaysCreated = msg.arg1;
        	final int nbDaysUpdated = msg.arg2;
        	
        	if (msg.what == DbInserterThread.STATE_DONE) {
        		// Fin normale de l'import
	            Toast.makeText(
	            		mActivity,
	            		mActivity.getString(
	    					R.string.import_days_results,
	    					nbDaysCreated, 
	    					nbDaysUpdated),
	    				Toast.LENGTH_LONG)
	    			.show();
        	} else {
        		// Annulation de l'utilisateur
        		Toast.makeText(
	            		mActivity,
	            		mActivity.getString(
	    					R.string.import_days_canceled,
	    					nbDaysCreated, 
	    					nbDaysUpdated),
	    				Toast.LENGTH_LONG)
	    			.show();
        	}
        }
    }
};