package fr.redmoon.tictac.bus.export.tocsv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.support.v4.app.FragmentActivity;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.bean.DayType;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.bus.export.FileImporter;

public class CsvDayBeanImporter extends FileImporter<List<DayBean>> {
	private final static String CSV_SEPARATOR = ",";
	public static final String UNKNOWN_DAYTYPE = "UNKNOWN_DAYTYPE_";
	
	private final static int POS_DATA = 0;
	private final static int POS_EXTRA = 1;
	// Cette colonne est ignorée lors de l'import, car on ne stocke pas le total en base
	//public final static int POS_TOTAL = 2;
	private final static int POS_TYPE_MORNING = 3;
	private final static int POS_TYPE_AFTERNOON = 4;
	private final static int POS_NOTE = 5;
	private final static int POS_FIRST_CHECKINGS = 6;
	
	private final Map<String, String> mDayTypesIdByLabel;
	private final Map<String, String> mUnknownLabels;
	
	public CsvDayBeanImporter(final FragmentActivity activity){
		super(activity);
		
		mUnknownLabels = new HashMap<String, String>();
		mDayTypesIdByLabel = new HashMap<String, String>();
		DayType curType;
		for (Map.Entry<String, DayType> entry : PreferencesBean.instance.dayTypes.entrySet()) {
			curType = entry.getValue();
			mDayTypesIdByLabel.put(curType.label, entry.getKey());
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
		day.typeMorning = mDayTypesIdByLabel.get(data[POS_TYPE_MORNING]);
		// Si le type n'est pas connu, le marque comme tel et on demandera ensuite à l'utilisateur
		// de choisir un remplacement
		if (day.typeMorning == null) {
			day.typeMorning = getTempId(data[POS_TYPE_MORNING]);
		}
		day.typeAfternoon = mDayTypesIdByLabel.get(data[POS_TYPE_AFTERNOON]);
		if (day.typeAfternoon == null) {
			day.typeAfternoon = getTempId(data[POS_TYPE_MORNING]);
		}
		day.note = data[POS_NOTE].trim();
		
		// Ajout des pointages
		for (int curCheckingPos = POS_FIRST_CHECKINGS; curCheckingPos < data.length; curCheckingPos++) {
			day.checkings.add(TimeUtils.parseTime(data[curCheckingPos]));
		}
		return day;
	}

	/**
	 * Retourne un identifiant de jour temporaire, et stocke cet identifiant
	 * @param unknownDayTypeLabel
	 * @return
	 */
	private String getTempId(final String unknownDayTypeLabel) {
		final String tempId = UNKNOWN_DAYTYPE + unknownDayTypeLabel;
		mDayTypesIdByLabel.put(unknownDayTypeLabel, tempId);
		mUnknownLabels.put(unknownDayTypeLabel, tempId);
		return tempId;
	}

	public Map<String, String> getUnknownDayTypes() {
		return mUnknownLabels;
	}
}
