package fr.redmoon.tictac.gui.dialogs.listeners;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import android.app.Activity;
import android.content.DialogInterface;
import android.widget.DatePicker;
import android.widget.Toast;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.bean.WeekBean;
import fr.redmoon.tictac.bus.export.CsvDayBeanExporter;
import fr.redmoon.tictac.bus.export.CsvWeekBeanExporter;
import fr.redmoon.tictac.bus.export.FileExporter;
import fr.redmoon.tictac.db.DbAdapter;

public class PeriodExporterListener extends AbsPeriodChooserListener {
	public static final String PROPERTY_DAYS_CSV = "days.csv";
	public static final String PROPERTY_WEEKS_CSV = "weeks.csv";
	public static final String MIME_TYPE = "text/plain";
	
	private String mRootDir;
	private String mDaysFilename;
	private String mWeeksFilename;
	
	public PeriodExporterListener(final Activity _activity, final DbAdapter _db, final DatePicker _date1, final DatePicker _date2) {
		super(_activity, _db, _date1, _date2);
	}
	
	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		// Identifiants des jours saisis
		final long firstDay = DateUtils.getDayId(mDate1.getYear(), mDate1.getMonth(), mDate1.getDayOfMonth());
		final long lastDay = DateUtils.getDayId(mDate2.getYear(), mDate2.getMonth(), mDate2.getDayOfMonth());
		
		// Export des jours
		final boolean resExportDays = exportDays(firstDay, lastDay);
		
		// Export des jours
		final boolean resExportSemaines = exportWeeks(firstDay, lastDay);
		
		// Création du fichier properties qui permettra l'import de ces deux fichiers
		final Properties exportInfos = new Properties();
		exportInfos.setProperty(PROPERTY_DAYS_CSV, mDaysFilename);
		exportInfos.setProperty(PROPERTY_WEEKS_CSV, mWeeksFilename);
		// Choix du nom et de l'emplacement (au même endroit que les autres)		
		final String filename = mActivity.getString(R.string.export_infos_filename_pattern, firstDay, lastDay);
		final File infosFile = new File(mRootDir, filename);
		// Ecriture du fichier
		boolean resExportInfos = true;
		try {
			final OutputStream outInfos = new FileOutputStream(infosFile);
			exportInfos.save(outInfos, "");
		} catch (FileNotFoundException e) {
			resExportInfos = false;
		}
		
		// Affichage du résultat
		String message = "";
		if (resExportDays && resExportSemaines && resExportInfos) {
			message = mActivity.getString(
				R.string.export_data_success,
				DateUtils.formatDateDDMMYYYY(firstDay),
				DateUtils.formatDateDDMMYYYY(lastDay));
		} else {
			message = mActivity.getString(R.string.export_data_fail);
		}
		Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
	}

	private boolean exportDays(final long firstDay, final long lastDay) {
		// Récupération des jours à extraire
		final List<DayBean> days = new ArrayList<DayBean>();
		mDb.fetchDays(firstDay, lastDay, days);
		
		// Export des jours vers le fichier texte
		final FileExporter<List<DayBean>> daysExporter = new CsvDayBeanExporter(mActivity, firstDay, lastDay);
		boolean result = daysExporter.exportData(days);
		mRootDir = daysExporter.getRootDir(); // A faire après exportData car la valeur n'est pas renseignée avant !
		mDaysFilename = daysExporter.getFilename();
		return result;
	}
	
	private boolean exportWeeks(final long firstDay, final long lastDay) {
		// Récupération des jours à extraire
		final List<WeekBean> weeks = new ArrayList<WeekBean>();
		mDb.fetchWeeks(firstDay, lastDay, weeks);
		
		// Export des jours vers le fichier texte
		final FileExporter<List<WeekBean>> weeksExporter = new CsvWeekBeanExporter(mActivity, firstDay, lastDay);
		boolean result = weeksExporter.exportData(weeks);
		mWeeksFilename = weeksExporter.getFilename();
		return result;
	}
}
