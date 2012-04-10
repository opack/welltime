package fr.redmoon.tictac.bus.export;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.bus.bean.WeekBean;

public class CsvWeekBeanImporter extends FileImporter<List<WeekBean>> {
	public final static String MIME_TYPE = "text/csv";	
	private final static String CSV_SEPARATOR = ",";
	
	public CsvWeekBeanImporter(final Activity activity){
		super(activity);
	}
	
	protected boolean performImport(final File file) throws IOException{
		if (mData == null) {
			return false;
		}
		
		final BufferedReader reader = new BufferedReader(new FileReader(file));
		
		WeekBean week;
		String line = reader.readLine(); // Lecture de la premi�re ligne (ent�te), qui sera ignor�e
		// Lecture d'une ligne
		while ((line = reader.readLine()) != null) {
			// Conversion des donn�es
			week = convertFromCsv(line);
			
			// Ajout � la liste des semaines
			mData.add(week);
		}
		
		// Fermeture du fichier
		reader.close();
		return true;
	}
	
	private static WeekBean convertFromCsv(final String line) {
		final String[]data = line.split(CSV_SEPARATOR);
		
		final WeekBean day = new WeekBean();
		day.date = DateUtils.parseDateDDMMYYYY(data[0]);
		day.flexTime = TimeUtils.parseTime(data[1]);
		
		return day;
	}
}