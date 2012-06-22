package fr.redmoon.tictac.gui.activities;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.gui.ViewSynchronizer;
import fr.redmoon.tictac.gui.activities.TicTacActivity.OnDayDeletionListener;
import fr.redmoon.tictac.gui.dialogs.AbsDialogDelegate;
import fr.redmoon.tictac.gui.dialogs.MonthDialogDelegate;
import fr.redmoon.tictac.gui.listadapter.CalendarAdapter;

public class MonthActivity extends TicTacActivity implements OnDayDeletionListener {
	public static final int PAGE_CALENDAR = 0;
	public static final int PAGE_DETAILS = 1;
	
	protected AbsDialogDelegate mDialogDelegate;
	private CalendarAdapter mAdapter;
	private SparseArray<DayBean> items;
	private List<DayBean> mMonthDays;
	
	private long mSelectedDay;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    setDialogDelegate(new MonthDialogDelegate(this));
	    
	    mMonthDays = new ArrayList<DayBean>();
	    items = new SparseArray<DayBean>();
	    mAdapter = new CalendarAdapter(this, DateUtils.extractYear(mToday), DateUtils.extractMonth(mToday));
	    mSelectedDay = -1;
	    
	    // Création de l'interface graphique
        setContentView(R.layout.view_common_frame);
        findViewById(R.id.lyt_clockin).setVisibility(View.INVISIBLE);
	    
        // Initialisation du gestionnaire de pages
        final View pageCalendar = View.inflate(this, R.layout.view_month_calendar, null);
     // DBG Pas encore implémenté final View pageDetails = View.inflate(this, R.layout.view_month_details, null);
        initPages(pageCalendar);// DBG Pas encore implémenté , pageDetails);
        
	    // Initialisation du GridView du calendrier
	    GridView gridview = (GridView)pageCalendar.findViewById(R.id.gridview);
	    gridview.setAdapter(mAdapter);
		gridview.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		    	TextView date = (TextView)v.findViewById(R.id.date);
		        if(date instanceof TextView && !date.getText().equals("")) {
		        	final String day = date.getText().toString();
		        	Toast.makeText(
		        		MonthActivity.this, 
		        		String.valueOf(DateUtils.getDayId(mAdapter.getYear(), mAdapter.getMonth(), Integer.parseInt(day))),
		        		Toast.LENGTH_SHORT)
		        	.show();
		        }
		    }
		});
		
		// On veut être informé si la vue "Semaine" supprime un jour
		TicTacActivity.registerDayDeletionListener(this);
		
		// Affichage du mois courant. Inutile de passer des paramètres car l'adapteur
		// vient juste d'être créé avec les infos (année, mois) du mois courant.
		populateView();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.week_main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_week_add_day:
			promptAddDay();
			return true;
		case R.id.menu_show_day:
			promptShowDay();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.month_contextual, menu);
		
		// Mise à jour du jour sélectionné et du menu (en fonction de l'existence du jour en base)
		mSelectedDay = (Long)v.getTag();
		
		// Si le jour n'existe pas en base, on masque certaines options
		if (!mDb.isDayExisting(mSelectedDay)) {
			menu.findItem(R.id.menu_day_show_checkings).setVisible(false);
			menu.findItem(R.id.menu_day_delete).setVisible(false);
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_day_show_checkings:
			// Sauvegarde du jour affiché pour synchroniser les vues
			ViewSynchronizer.getInstance().setCurrentDay(mSelectedDay);
			
			// Modification de l'onglet courant
			switchTab(MainActivity.TAB_DAY_POS, mSelectedDay, DayActivity.PAGE_CHECKINGS);
			break;
		case R.id.menu_day_show_details:
			// Sauvegarde du jour affiché pour synchroniser les vues
			ViewSynchronizer.getInstance().setCurrentDay(mSelectedDay);
			
			// Modification de l'onglet courant
			switchTab(MainActivity.TAB_DAY_POS, mSelectedDay, DayActivity.PAGE_DETAILS);
			break;
		case R.id.menu_day_show_week:
			// Sauvegarde du jour affiché pour synchroniser les vues
			ViewSynchronizer.getInstance().setCurrentDay(mSelectedDay);
			
			// Modification de l'onglet courant
			switchTab(MainActivity.TAB_WEEK_POS, mSelectedDay, WeekActivity.PAGE_DAYS);
			break;
		case R.id.menu_day_delete:
			deleteDay(mSelectedDay);
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	@Override
	public void populateView(final long date) {
		final int year = DateUtils.extractYear(date);
		final int month = DateUtils.extractMonth(date);
		mAdapter.showMonth(year, month);
		populateView();
	}
	
	/**
	 * Remplit la vue avec les données du mois courant dans l'adapter
	 */
	public void populateView() {
		// Récupération des jours du mois demandé
		final int year = mAdapter.getYear();
		final int month = mAdapter.getMonth();
		final long firstDay = DateUtils.getDayId(year, month, 1);
		final long lastDay = DateUtils.getDayId(year, month, mAdapter.getNumberOfDaysInMonth());
		mDb.fetchDays(firstDay, lastDay, mMonthDays);
		
		// Ajout des jours dans l'adapteur
		items.clear();
		for (DayBean day : mMonthDays) {
			items.put(DateUtils.extractDayOfMonth(day.date), day);
		}
		
		// Mise à jour de l'affichage
		mAdapter.setItems(items);
		mAdapter.notifyDataSetChanged();

		mWorkTime.set(1, month, year);
		populateCommon(
			mWorkTime.format(DateUtils.FORMAT_DATE_MONTH),
    		0,
    		100);
	}

	/**
     * Affiche le mois précédant.
     * @param btn
     */
    public void showPrevious(final View btn) {
    	mAdapter.previousMonth();
    	populateView();
    	
    	// Sauvegarde du jour courant dans le synchroniseur de vues pour accorder
    	// toutes les vues sur le même jour
    	// On fait bien ça après le popuplateView car mAdapter y aura été mis à jour.
    	final long firstDayId = DateUtils.getDayId(mAdapter.getYear(), mAdapter.getMonth(), 1);
    	ViewSynchronizer.getInstance().setCurrentDay(firstDayId);
    }
    
    /**
     * Affiche le mois suivant.
     * @param btn
     */
    public void showNext(final View btn) {
    	mAdapter.nextMonth();
    	populateView();
    	
    	// Sauvegarde du jour courant dans le synchroniseur de vues pour accorder
    	// toutes les vues sur le même jour
    	// On fait bien ça après le popuplateView car mAdapter y aura été mis à jour.
    	final long firstDayId = DateUtils.getDayId(mAdapter.getYear(), mAdapter.getMonth(), 1);
    	ViewSynchronizer.getInstance().setCurrentDay(firstDayId);
    }
    
    @Override
	public void onDeleteDay(final long deletedDate) {
		// Si le jour supprimé est dans le mois actuellement affiché,
		// on rafraîchit la vue.
		for (DayBean day : mMonthDays) {
			if (day.date == deletedDate) {
				populateView(deletedDate);
				break;
			}
		}
	}
}
