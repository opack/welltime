package fr.redmoon.tictac.bus.export;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.bus.bean.DayBean;

public class CsvDayBeanImporter extends FileImporter<List<DayBean>> {
	public final static String MIME_TYPE = "text/csv";	
	private final static String CSV_SEPARATOR = ",";
	
	public CsvDayBeanImporter(final Activity activity){
		super(activity);
	}
	
	protected boolean performImport(final File file) throws IOException{
		if (mData == null) {
			return false;
		}
		
		final BufferedReader reader = new BufferedReader(new FileReader(file));
		
		final String[] dayTypes = mResources.getStringArray(R.array.dayTypesEntries);
		DayBean day;
		String line = reader.readLine(); // Lecture de la première ligne (entête), qui sera ignorée
		// Lecture d'une ligne
		while ((line = reader.readLine()) != null) {
			// Conversion des données
			day = convertFromCsv(line, dayTypes);
			
			// Ajout à la liste des jours
			mData.add(day);
		}
		
		// Fermeture du fichier
		reader.close();
		return true;
	}
	
	private static DayBean convertFromCsv(final String line, final String[] dayTypes) {
		final String[]data = line.split(CSV_SEPARATOR);
		
		final DayBean day = new DayBean();
		day.date = DateUtils.parseDateDDMMYYYY(data[0]);
		day.extra = TimeUtils.parseTime(data[1]);
		for (int curType = 0; curType < dayTypes.length; curType++) {
			if (dayTypes[curType].equals(data[3])) {// En position 4 (donc index 3) car on ignore la valeur "total" en position 3 (donc index 2)
				day.type = curType;
				break;
			}
		}
		day.note = data[4].trim();
		
		// Ajout des pointages
		for (int curCheckingPos = 5; curCheckingPos < data.length; curCheckingPos++) {
			day.checkings.add(TimeUtils.parseTime(data[curCheckingPos]));
		}
		
		return day;
	}
}
