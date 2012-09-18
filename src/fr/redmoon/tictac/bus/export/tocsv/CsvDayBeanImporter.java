package fr.redmoon.tictac.bus.export.tocsv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.export.FileImporter;

public class CsvDayBeanImporter extends FileImporter<List<DayBean>> {
	public final static String MIME_TYPE = "text/csv";	
	private final static String CSV_SEPARATOR = ",";
	
	public final static int POS_DATA = 0;
	public final static int POS_EXTRA = 1;
	// Cette colonne est ignorée lors de l'import, car on ne stocke pas le total en base
	//public final static int POS_TOTAL = 2;
	public final static int POS_TYPE_MORNING = 3;
	public final static int POS_TYPE_AFTERNOON = 4;
	public final static int POS_NOTE = 5;
	public final static int POS_FIRST_CHECKINGS = 6;
	
	private final Map<String, Integer> mDayTypes;
	
	public CsvDayBeanImporter(final Activity activity){
		super(activity);
		
		final String[] dayTypes = mResources.getStringArray(R.array.dayTypesEntries);
		mDayTypes = new HashMap<String, Integer>();
		for (int curType = 0; curType < dayTypes.length; curType++) {
			mDayTypes.put(dayTypes[curType], curType);
		}
	}
	
	protected boolean performImport(final File file) throws IOException{
		if (mData == null) {
			return false;
		}
		
		final BufferedReader reader = new BufferedReader(new FileReader(file));
		
		DayBean day;
		String line = reader.readLine(); // Lecture de la première ligne (entête), qui sera ignorée
		// Lecture d'une ligne
		while ((line = reader.readLine()) != null) {
			// Conversion des données
			day = convertFromCsv(line);
			
			// Ajout à la liste des jours
			mData.add(day);
		}
		
		// Fermeture du fichier
		reader.close();
		return true;
	}
	
	private DayBean convertFromCsv(final String line) {
		final String[]data = line.split(CSV_SEPARATOR);
		
		final DayBean day = new DayBean();
		day.date = DateUtils.parseDateDDMMYYYY(data[POS_DATA]);
		day.extra = TimeUtils.parseTime(data[POS_EXTRA]);
		day.typeMorning = mDayTypes.get(data[POS_TYPE_MORNING]);
		day.typeAfternoon = mDayTypes.get(data[POS_TYPE_AFTERNOON]);
		day.note = data[POS_NOTE].trim();
		
		// Ajout des pointages
		for (int curCheckingPos = POS_FIRST_CHECKINGS; curCheckingPos < data.length; curCheckingPos++) {
			day.checkings.add(TimeUtils.parseTime(data[curCheckingPos]));
		}
		
		return day;
	}
}
