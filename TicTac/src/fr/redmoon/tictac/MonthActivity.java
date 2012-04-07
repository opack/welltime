package fr.redmoon.tictac;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import fr.redmoon.tictac.TicTacActivity.OnDayDeletionListener;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.gui.calendar.CalendarAdapter;

public class MonthActivity extends TicTacActivity implements OnDayDeletionListener {
	
	private CalendarAdapter mAdapter;
	private SparseArray<DayBean> items;
	private List<DayBean> mMonthDays;
	
	private long mSelectedDay;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    mMonthDays = new ArrayList<DayBean>();
	    items = new SparseArray<DayBean>();
	    mAdapter = new CalendarAdapter(this, DateUtils.extractYear(mToday), DateUtils.extractMonth(mToday));
	    mSelectedDay = -1;
	    
	    // Création de l'interface graphique
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
		
		// On veut être informé si la vue "Semaine" supprime un jour
		WeekActivity.registerDayDeletionListener(this);
		
		// Affichage du mois courant. Inutile de passer des paramètres car l'adapteur
		// vient juste d'être créé avec les infos (année, mois) du mois courant.
		populateView();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.month_contextual, menu);
		
		// Mise à jour du jour sélectionné et du menu (en fonction de l'existence du jour en base)
		mSelectedDay = (Long)v.getTag();
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_day_show_checkings:
			switchTab(MainActivity.TAB_DAY_POS, mSelectedDay, R.id.day_checkings);
			break;
		case R.id.menu_day_show_details:
			switchTab(MainActivity.TAB_DAY_POS, mSelectedDay, R.id.day_details);
			break;
		case R.id.menu_day_show_week:
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
    }
    
    /**
     * Affiche le mois suivant.
     * @param btn
     */
    public void showNext(final View btn) {
    	mAdapter.nextMonth();
    	populateView();
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
