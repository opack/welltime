package fr.redmoon.tictac;

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
import fr.redmoon.tictac.TicTacActivity.OnDayDeletionListener;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.gui.ViewSynchronizer;
import fr.redmoon.tictac.gui.calendar.CalendarAdapter;
import fr.redmoon.tictac.gui.dialogs.AbsDialogDelegate;
import fr.redmoon.tictac.gui.dialogs.MonthDialogDelegate;

public class MonthActivity extends TicTacActivity implements OnDayDeletionListener {
	
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
	    
	    // Cr�ation de l'interface graphique
        setContentView(R.layout.view);
        findViewById(R.id.lyt_btn_bar).setVisibility(View.INVISIBLE);
        findViewById(R.id.img_note).setVisibility(View.GONE);
	    
	    // Initialisation du gestionnaire de sweep
        initSweep(
        	new int[]{R.id.month_calendar, R.id.month_details}, 
        	new int[]{R.layout.month_calendar, R.layout.month_details});
        
	    // Initialisation du GridView du calendrier
	    GridView gridview = (GridView) findViewById(R.id.gridview);
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
		
		// On veut �tre inform� si la vue "Semaine" supprime un jour
		WeekActivity.registerDayDeletionListener(this);
		
		// Affichage du mois courant. Inutile de passer des param�tres car l'adapteur
		// vient juste d'�tre cr�� avec les infos (ann�e, mois) du mois courant.
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
		
		// Mise � jour du jour s�lectionn� et du menu (en fonction de l'existence du jour en base)
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
			// Sauvegarde du jour affich� pour synchroniser les vues
			ViewSynchronizer.getInstance().setCurrentDay(mSelectedDay);
			
			// Modification de l'onglet courant
			switchTab(MainActivity.TAB_DAY_POS, mSelectedDay, R.id.day_checkings);
			break;
		case R.id.menu_day_show_details:
			// Sauvegarde du jour affich� pour synchroniser les vues
			ViewSynchronizer.getInstance().setCurrentDay(mSelectedDay);
			
			// Modification de l'onglet courant
			switchTab(MainActivity.TAB_DAY_POS, mSelectedDay, R.id.day_details);
			break;
		case R.id.menu_day_show_week:
			// Sauvegarde du jour affich� pour synchroniser les vues
			ViewSynchronizer.getInstance().setCurrentDay(mSelectedDay);
			
			// Modification de l'onglet courant
			switchTab(MainActivity.TAB_WEEK_POS, mSelectedDay, R.id.week_days);
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
	 * Remplit la vue avec les donn�es du mois courant dans l'adapter
	 */
	public void populateView() {
		// R�cup�ration des jours du mois demand�
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
		
		// Mise � jour de l'affichage
		mAdapter.setItems(items);
		mAdapter.notifyDataSetChanged();

		mWorkTime.set(1, month, year);
		populateCommon(
			mWorkTime.format(DateUtils.FORMAT_DATE_MONTH),
    		0,
    		100);
	}

	/**
     * Affiche le mois pr�c�dant.
     * @param btn
     */
    public void showPrevious(final View btn) {
    	mAdapter.previousMonth();
    	populateView();
    	
    	// Sauvegarde du jour courant dans le synchroniseur de vues pour accorder
    	// toutes les vues sur le m�me jour
    	// On fait bien �a apr�s le popuplateView car mAdapter y aura �t� mis � jour.
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
    	// toutes les vues sur le m�me jour
    	// On fait bien �a apr�s le popuplateView car mAdapter y aura �t� mis � jour.
    	final long firstDayId = DateUtils.getDayId(mAdapter.getYear(), mAdapter.getMonth(), 1);
    	ViewSynchronizer.getInstance().setCurrentDay(firstDayId);
    }
    
    @Override
	public void onDeleteDay(final long deletedDate) {
		// Si le jour supprim� est dans le mois actuellement affich�,
		// on rafra�chit la vue.
		for (DayBean day : mMonthDays) {
			if (day.date == deletedDate) {
				populateView(deletedDate);
				break;
			}
		}
	}
}
