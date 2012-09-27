package fr.redmoon.tictac.bus.export.tobinary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import android.app.Activity;
import android.util.Log;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.bus.export.FileImporter;

public class BinPreferencesBeanImporter extends FileImporter<PreferencesBean> {
	
	public BinPreferencesBeanImporter(final Activity activity){
		super(activity);
	}
	
	protected boolean performImport(final File file) throws IOException{
		if (file == null) {
			return false;
		}

		final ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		try {
			final PreferencesBean readPrefs = (PreferencesBean)in.readObject();
			mData.clone(readPrefs);
		} catch(ClassNotFoundException cnfe) { 
		      Log.e("Welltime", "Une erreur s'est produite durant la désérialisation des préférences.", cnfe); 
		      return false; 
	    } finally {
	    	// Fermeture du fichier
			in.close();
	    }
		return true;
	}
}
