package fr.redmoon.tictac.bus.export.tocsv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.bus.bean.WeekBean;
import fr.redmoon.tictac.bus.export.FileExporter;

public class CsvWeekBeanExporter extends FileExporter<List<WeekBean>> {
	
	private final static String CSV_SEPARATOR = ",";
	
	private final static String HEADER_DATE = "DATE";
	private final static String HEADER_FLEX = "FLEX";
	
	private final long mStartDate;
	private final long mEndDate;
	
	public CsvWeekBeanExporter(final Activity activity, final long exportStartDate, final long exportEndDate) {
		super(activity);
		mStartDate = exportStartDate;
		mEndDate = exportEndDate;
	}
	
	@Override
	public boolean performExport(final File file) throws IOException {
		if (mData == null) {
			return false;
		}
		
		final FileWriter writer = new FileWriter(file);
		
		final String csvHeader = createCsvHeader();
		writer.append(csvHeader);

		// Ecriture des jours
		final String[] dayTypes = mResources.getStringArray(R.array.dayTypesEntries);
		String converted;
		for (WeekBean day : mData) {
			// Conversion des données
			converted = convertToCsv(day, dayTypes);

			// Ecriture des données
			writer.append(converted);
		}
		
		writer.flush();
		writer.close();
		return true;
	}

	private static String createCsvHeader() {
		final StringBuilder csv = new StringBuilder();
		csv.append(HEADER_DATE).append(CSV_SEPARATOR);
		csv.append(HEADER_FLEX).append(CSV_SEPARATOR);
		
		// Ajout d'un retour à la ligne
		csv.append(System.getProperty("line.separator"));

		return csv.toString();
	}

	private static String convertToCsv(final WeekBean week, final String[] dayTypes) {
		// Ajout des propriétés de la semaine
		final StringBuilder csv = new StringBuilder();
		csv.append(DateUtils.formatDateDDMMYYYY(week.date)).append(CSV_SEPARATOR);
		csv.append(TimeUtils.formatTime(week.flexTime)).append(CSV_SEPARATOR);
		
		// Ajout d'un retour à la ligne
		csv.append(System.getProperty("line.separator"));
		
		return csv.toString();
	}

	@Override
	public String getFilename() {
		return mResources.getString(R.string.export_weeks_filename_pattern, mStartDate, mEndDate);
	}
}
