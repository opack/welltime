package fr.redmoon.tictac.gui.dialogs.listeners;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.DialogInterface;
import android.widget.DatePicker;
import android.widget.Toast;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.export.CsvDayBeanExporter;
import fr.redmoon.tictac.bus.export.FileExporter;
import fr.redmoon.tictac.db.DbAdapter;

public class PeriodExporterListener extends AbsPeriodChooserListener {
	public PeriodExporterListener(final Activity _activity, final DbAdapter _db, final DatePicker _date1, final DatePicker _date2) {
		super(_activity, _db, _date1, _date2);
	}
	
	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		// Identifiants des jours saisis
		final long firstDay = DateUtils.getDayId(mDate1.getYear(), mDate1.getMonth(), mDate1.getDayOfMonth());
		final long lastDay = DateUtils.getDayId(mDate2.getYear(), mDate2.getMonth(), mDate2.getDayOfMonth());
		
		// Récupération des jours à extraire
		final List<DayBean> days = new ArrayList<DayBean>();
		mDb.fetchDays(firstDay, lastDay, days);
		
		// Export des données vers le fichier texte
		String message = "";
		final FileExporter<List<DayBean>> exporter = new CsvDayBeanExporter(mActivity);
		if (days.isEmpty()) {
			message = mActivity.getString(R.string.export_days_no_data);
		} else if (exporter.exportData(days)) {
			message = mActivity.getString(
				R.string.export_days_success,
				DateUtils.formatDateDDMMYYYY(firstDay),
				DateUtils.formatDateDDMMYYYY(lastDay)
			);
		} else {
			message = mActivity.getString(R.string.export_days_fail);
		}
		Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
	}
}
