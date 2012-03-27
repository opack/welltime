package fr.redmoon.tictac;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import fr.redmoon.tictac.bus.PreferencesUtils;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.gui.sweep.Direction;

public class MainActivity extends TabActivity {
	private TabHost tabHost;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.tictac);
	    createTabs();
	    initPrefs();
	    int n = Color.argb(255, 245, 245, 245);
	    Log.d("TicTac", "Col = " + n);
	}
	
	private void initPrefs() {
	    // Initialisation des pr�f�rences
	    if (PreferencesUtils.isFirstLaunch(this)) {
	    	// Premier lancement : on initialise les pr�f�rences avec les valeurs par d�faut
	    	// Note : les 3 lignes qui suivent devraient pouvoir �tre remplac�es par l'appel
	    	// � PreferenceManager.setDefaultValues(this, R.xml.preferences, true), mais �a
	    	// ne fonctionne pas donc on doit �crire les pr�f�rences � la main :'(
	    	PreferencesUtils.updatePreferencesBean(this);
	    	PreferencesBean.instance.isFirstLaunch = false;
	    	PreferencesUtils.savePreferencesBean(this);
	    	
	    	// Affichage de l'assistant de premier lancement
	    	final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.dlg_title_first_launch);
			builder.setMessage(R.string.dlg_msg_first_launch);
			builder.setPositiveButton(R.string.btn_close, new DialogInterface.OnClickListener() {
	        	@Override
	    		public void onClick(DialogInterface dialog, int id) {
	        		dialog.dismiss();
	    	    	
	    	    	// On affiche l'�cran des pr�f�rences pour que l'utilisateur les valide
	    	    	PreferencesUtils.showPreferences(MainActivity.this);
	        	}
	        });
			
			// Affiche la jolie bo�-bo�te
			builder.show();
	    }
	    PreferencesUtils.updatePreferencesBean(this);
	}
	
	private void createTabs() {
		tabHost = getTabHost();
		
		// Ajout des onglets
		addTab(R.string.tab_day, R.drawable.tab_days, DayActivity.class);
		addTab(R.string.tab_week, R.drawable.tab_weeks, WeekActivity.class);
		addTab(R.string.tab_manage, R.drawable.tab_manage, ManageActivity.class);
		
		// On supprime la barre sous les onglets
		// TODO
		
	    // On d�marre sur l'onglet "Jour"
	    tabHost.setCurrentTab(0);
	}
	
	private void addTab(int labelId, int drawableId, Class<? extends Activity> activityClass) {
		Intent intent = new Intent(this, activityClass);
		TabHost.TabSpec spec = tabHost.newTabSpec("tab" + labelId);		
		
		View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, getTabWidget(), false);
		
		TextView title = (TextView) tabIndicator.findViewById(R.id.title);
		title.setText(labelId);
		ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
		icon.setImageResource(drawableId);
		
		spec.setIndicator(tabIndicator);
		spec.setContent(intent);
		tabHost.addTab(spec);
	}
	
	public void switchTab(final int tabIndexToSwitchTo, final long date){
		switchTab(tabIndexToSwitchTo, date, -1);
	}
	
	/**
	 * Modifie l'onglet affich�
	 * @param tabIndexToSwitchTo
	 * @param date
	 * @param viewId Si sp�cifi� (sup�rieur � 0), le viewFlipper flip vers la vue indiqu�e
	 */
	public void switchTab(final int tabIndexToSwitchTo, final long date, final int viewId){
		tabHost.setCurrentTab(tabIndexToSwitchTo);
		final TicTacActivity a = (TicTacActivity)getCurrentActivity();
		if (viewId > 0) {
			a.switchDetailsView(Direction.left, viewId);
		}
		a.populateView(date);		
	}
}