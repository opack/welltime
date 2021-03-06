package fr.redmoon.tictac.gui.activities;

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
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.PreferencesUtils;
import fr.redmoon.tictac.bus.export.tocalendar.CalendarAccess;

public class MainActivity extends TabActivity {
	public static final int TAB_DAY_POS = 0;
	public static final int TAB_WEEK_POS = 1;
	public static final int TAB_MONTH_POS = 2;
	public static final int TAB_MANAGE_POS = 3;
	
	private TabHost tabHost;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    initPrefs();
	    setContentView(R.layout.tictac);
	    createTabs();
	    CalendarAccess.getInstance().initAccess(this);
	}
	
	private void initPrefs() {
	    // Initialisation des pr�f�rences
	    if (PreferencesUtils.isFirstLaunch(this)) {
	    	// Premier lancement : on initialise les pr�f�rences avec les valeurs par d�faut
	    	PreferencesUtils.resetPreferences(this);
	    	
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
		addTab(R.string.tab_month, R.drawable.tab_months, MonthActivity.class);
		addTab(R.string.tab_manage, R.drawable.tab_manage, ManageActivity.class);
		
		// On supprime la barre sous les onglets
		// TODO
		
	    // On d�marre sur l'onglet "Jour"
	    tabHost.setCurrentTab(TAB_DAY_POS);
	}
	
	private void addTab(int labelId, int drawableId, Class<? extends Activity> activityClass) {
		Intent intent = new Intent(this, activityClass);
		TabHost.TabSpec spec = tabHost.newTabSpec("tab" + labelId);		
		
		View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, getTabWidget(), false);
		
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
	 * @param pageId Si sp�cifi� (sup�rieur � 0), le viewFlipper flip vers la vue indiqu�e
	 */
	public void switchTab(final int tabIndexToSwitchTo, final long date, final int pageId){
		tabHost.setCurrentTab(tabIndexToSwitchTo);
		final TicTacActivity a = (TicTacActivity)getCurrentActivity();
		if (pageId > -1) {
			a.switchPage(pageId);
		}
		a.populateView(date);		
	}
}