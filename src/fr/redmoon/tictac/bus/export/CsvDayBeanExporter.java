package fr.redmoon.tictac.bus.export;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.bus.bean.DayBean;

public class CsvDayBeanExporter extends FileExporter<List<DayBean>> {
	
	private final static String CSV_SEPARATOR = ",";
	
	private final static String HEADER_DATE = "DATE";
	private final static String HEADER_EXTRA = "EXTRA";
	private final static String HEADER_NOTE = "NOTE";
	private final static String HEADER_TOTAL = "TOTAL";
	private final static String HEADER_TYPE = "TYPE";
	private final static String HEADER_CHECKING = "POINTAGE";
	
	public CsvDayBeanExporter(final Activity activity) {
		super(activity);
	}
	
	@Override
	public boolean performExport(final File file) throws IOException {
		if (mData.isEmpty()) {
			return false;
		}
		
		final FileWriter writer = new FileWriter(file);
		
		// Ecriture de la ligne d'entête
		int maxNbCheckings = 0;
		for (DayBean day : mData) {
			// Mise à jour du nombre
			maxNbCheckings = Math.max(maxNbCheckings, day.checkings.size());
		}
		final String csvHeader = createCsvHeader(maxNbCheckings);
		writer.append(csvHeader);

		// Ecriture des jours
		final String[] dayTypes = mResources.getStringArray(R.array.dayTypesEntries);
		String converted;
		for (DayBean day : mData) {
			// Conversion des données
			converted = convertToCsv(day, dayTypes);

			// Ecriture des données
			writer.append(converted);
		}
		
		writer.flush();
		writer.close();
		return true;
	}

	private static String createCsvHeader(final int nbCheckingsMax) {
		final StringBuilder csv = new StringBuilder();
		csv.append(HEADER_DATE).append(CSV_SEPARATOR);
		csv.append(HEADER_EXTRA).append(CSV_SEPARATOR);
		csv.append(HEADER_TOTAL).append(CSV_SEPARATOR);
		csv.append(HEADER_TYPE).append(CSV_SEPARATOR);
		csv.append(HEADER_NOTE).append(CSV_SEPARATOR);
		
		// Ajout des pointages
		for (int curChecking = 1; curChecking <= nbCheckingsMax; curChecking++) {
			csv.append(HEADER_CHECKING).append(curChecking).append(CSV_SEPARATOR);
		}
		
		// Ajout d'un retour à la ligne
		csv.append(System.getProperty("line.separator"));

		return csv.toString();
	}

	private static String convertToCsv(final DayBean day, final String[] dayTypes) {
		// Calcule le temps total
		final int total = TimeUtils.computeTotal(day);

		// Ajout des propriétés du jour
		final StringBuilder csv = new StringBuilder();
		csv.append(DateUtils.formatDateDDMMYYYY(day.date)).append(CSV_SEPARATOR);
		csv.append(TimeUtils.formatTime(day.extra)).append(CSV_SEPARATOR);
		csv.append(TimeUtils.formatMinutes(total)).append(CSV_SEPARATOR);
		csv.append(dayTypes[day.type]).append(CSV_SEPARATOR);
		
		// S'il n'y a pas de note, on met un espace pour que l'import fonctionne.
		String note = day.note;
		if (note == null || note.isEmpty()) {
			note = " ";
		}
		csv.append(note).append(CSV_SEPARATOR);
		
		// Ajout des pointages
		for (Integer checking : day.checkings) {
			csv.append(TimeUtils.formatTime(checking)).append(CSV_SEPARATOR);
		}
		
		// Ajout d'un retour à la ligne
		csv.append(System.getProperty("line.separator"));
		
		return csv.toString();
	}

	@Override
	protected String getFilename() {
		final long startDate = mData.get(0).date;
		final long endDate = mData.get(mData.size() - 1).date;
		return mResources.getString(R.string.export_days_filename_pattern, startDate, endDate);
	}
}
