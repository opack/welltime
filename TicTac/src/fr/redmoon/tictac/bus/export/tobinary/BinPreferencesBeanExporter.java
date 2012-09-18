package fr.redmoon.tictac.bus.export.tobinary;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import android.app.Activity;
import android.text.format.Time;
import android.util.Log;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.bus.export.FileExporter;

public class BinPreferencesBeanExporter extends FileExporter<PreferencesBean> {
	public final static String EXTENSION = "prefs";
	
	private final String timestamp;
	
	public BinPreferencesBeanExporter(final Activity activity) {
		super(activity);
		final Time now = TimeUtils.getNowTime();
		timestamp = "" + DateUtils.getCurrentDayId() + " " + now.hour + now.minute + now.second;
	}
	
	@Override
	public boolean performExport(final File file) throws IOException {
		if (mData == null) {
			return false;
		}
		
		// Ecriture des données : on sérialise tout simplement les préférences
		final FileOutputStream fos = new FileOutputStream(file);
		try {
			// Sérialisation des préférences dans un tableau de bytes
			final ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
			ObjectOutput out = new ObjectOutputStream(bos);
			out.writeObject(mData);
			out.close();
			byte[] buf = bos.toByteArray();

			// Ecriture dans le fichier de sortie
			fos.write(buf);
		} catch (IOException ioe) {
			Log.e("Welltime", "Une erreur s'est produite durant la sérialisation des préférences.", ioe);
			throw ioe;
		} finally {
			// Fermeture du fichier
			fos.flush();
			fos.close();
		}
		
		return true;
	}

	@Override
	public String getFilename() {
		return mResources.getString(R.string.export_prefs_filename, timestamp);
	}
}
