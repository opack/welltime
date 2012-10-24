package fr.redmoon.tictac.bus;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.db.DbAdapter;

public class UpdateFlexTask extends AsyncTask<Long, Void, Void> {
	public interface OnTaskCompleteListener {
		void onTaskComplete();
	}

	private final Context mContext;
	private final OnTaskCompleteListener mListener;
	private ProgressDialog mProgressDialog;

	public UpdateFlexTask(final Context context, final OnTaskCompleteListener listener) {
		mContext = context;
		mListener = listener;
	}
	
	public UpdateFlexTask(final Context context) {
		this(context, null);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		// Préparation de la boîte de dialogue
		mProgressDialog = ProgressDialog.show(
			mContext,
			mContext.getResources().getString(R.string.app_name),
			"Mise à jour de l'HV, veuillez patienter...",
			false,
			false);
	}

	@Override
	protected Void doInBackground(Long... initialDay) {
		final DbAdapter db = DbAdapter.getInstance(mContext);
		db.openDatabase();

		final FlexUtils flexUtils = new FlexUtils(db);
		if (initialDay.length == 0) {
			// Aucun jour n'a été transmis : on calcule l'HV
			// pour toute la base
			flexUtils.updateFlex();
		} else {
			// On a reçu un jour initial donc on calcule l'HV
			// à partir de ce jour
			flexUtils.updateFlex(initialDay[0]);
		}

		db.closeDatabase();
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		mProgressDialog.dismiss();
		if (mListener != null) {
			mListener.onTaskComplete();
		}
	}
}