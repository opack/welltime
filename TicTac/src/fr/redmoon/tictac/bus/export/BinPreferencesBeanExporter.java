package fr.redmoon.tictac.bus.export;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import android.app.Activity;
import android.util.Log;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.bean.PreferencesBean;

public class BinPreferencesBeanExporter extends FileExporter<PreferencesBean> {
	
	public BinPreferencesBeanExporter(final Activity activity) {
		super(activity);
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
		return mResources.getString(R.string.export_prefs_filename);
	}
}
