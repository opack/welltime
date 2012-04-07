package fr.redmoon.tictac;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import fr.redmoon.tictac.bus.PreferencesUtils;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.gui.sweep.Direction;

public class MainActivity extends TabActivity {
	public static final int TAB_DAY_POS = 0;
	public static final int TAB_WEEK_POS = 1;
	public static final int TAB_MONTH_POS = 2;
	public static final int TAB_MANAGE_POS = 3;
	
	private TabHost tabHost;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.tictac);
	    createTabs();
	    initPrefs();
	}
	
	private void initPrefs() {
	    // Initialisation des préférences
	    if (PreferencesUtils.isFirstLaunch(this)) {
	    	// Premier lancement : on initialise les préférences avec les valeurs par défaut
	    	// Note : les 3 lignes qui suivent devraient pouvoir être remplacées par l'appel
	    	// à PreferenceManager.setDefaultValues(this, R.xml.preferences, true), mais ça
	    	// ne fonctionne pas donc on doit écrire les préférences à la main :'(
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
	    	    	
	    	    	// On affiche l'écran des préférences pour que l'utilisateur les valide
	    	    	PreferencesUtils.showPreferences(MainActivity.this);
	        	}
	        });
			
			// Affiche la jolie boî-boîte
			builder.show();
	    }
	    PreferencesUtils.updatePreferencesBean(this);
	}
	
	private void createTabs() {
		tabHost = getTabHost();
		
		// Ajout des onglets
		addTab(R.string.tab_day, R.drawable.tab_days, DayActivity.class);
		addTab(R.string.tab_week, R.drawable.tab_weeks, WeekActivity.class);
		addTab(R.string.tab_month, R.drawable.tab_months, MonthActivity.class);
		addTab(R.string.tab_manage, R.drawable.tab_manage, ManageActivity.class);
		
		// On supprime la barre sous les onglets
		// TODO
		
	    // On démarre sur l'onglet "Jour"
	    tabHost.setCurrentTab(TAB_DAY_POS);
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
	 * Modifie l'onglet affiché
	 * @param tabIndexToSwitchTo
	 * @param date
	 * @param viewId Si spécifié (supérieur à 0), le viewFlipper flip vers la vue indiquée
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