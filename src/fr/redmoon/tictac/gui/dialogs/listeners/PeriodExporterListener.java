package fr.redmoon.tictac.gui.dialogs.listeners;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import fr.redmoon.tictac.bus.export.ZipCompress;
import fr.redmoon.tictac.db.DbAdapter;

public class PeriodExporterListener extends AbsPeriodChooserListener {
	public static final String FILE_DAYS_CSV = "days.csv";
	public static final String FILE_WEEKS_CSV = "weeks.csv";
	public static final String MIME_TYPE = "application/zip";
	
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
		
		// Export des jours et des semaines
		final boolean resExportDays = exportDays(firstDay, lastDay);
		final boolean resExportSemaines = exportWeeks(firstDay, lastDay);
		
		// Compression des fichiers dans un zip
		final Map<String, String> files = new HashMap<String, String>();
		files.put(mRootDir + "/" + mDaysFilename, FILE_DAYS_CSV);
		files.put(mRootDir + "/" + mWeeksFilename, FILE_WEEKS_CSV);
		final String zipFilename = mActivity.getString(R.string.export_zip_filename_pattern, firstDay, lastDay);
		final ZipCompress zip = new ZipCompress(files, mRootDir + "/" + zipFilename);
		zip.zip();
		
		// Suppression des fichiers source
		final File exportedDaysCsvFile = new File(mRootDir, mDaysFilename);
		exportedDaysCsvFile.delete();
		final File exportedWeeksCsvFile = new File(mRootDir, mWeeksFilename);
		exportedWeeksCsvFile.delete();
		
		// Affichage du résultat
		String message = "";
		if (resExportDays && resExportSemaines) {
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
