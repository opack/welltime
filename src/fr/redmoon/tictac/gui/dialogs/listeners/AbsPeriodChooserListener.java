package fr.redmoon.tictac.gui.dialogs.listeners;

import android.app.Activity;
import android.content.DialogInterface;
import android.widget.DatePicker;
import fr.redmoon.tictac.db.DbAdapter;

public abstract class AbsPeriodChooserListener implements DialogInterface.OnClickListener {
	protected final Activity mActivity;
	protected final DbAdapter mDb;
	protected final DatePicker mDate1;
	protected final DatePicker mDate2;
	
	public AbsPeriodChooserListener(final Activity _activity, final DbAdapter _db, final DatePicker _date1, final DatePicker _date2) {
		mActivity = _activity;
		mDb = _db;
		mDate1 = _date1;
		mDate2 = _date2;
	}
}
