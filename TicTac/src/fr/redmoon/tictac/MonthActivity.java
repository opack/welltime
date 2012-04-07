package fr.redmoon.tictac;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.gui.calendar.CalendarAdapter;

public class MonthActivity extends TicTacActivity {
	
	public CalendarAdapter mAdapter;
	public SparseArray<DayBean> items; // container to store some random calendar items
	public List<DayBean> mMonthDays;

	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    mMonthDays = new ArrayList<DayBean>();
	    items = new SparseArray<DayBean>();
	    mAdapter = new CalendarAdapter(this, DateUtils.extractYear(mToday), DateUtils.extractMonth(mToday));
	    
	    // Création de l'interface graphique
        setContentView(R.layout.view);
        findViewById(R.id.lyt_btn_bar).setVisibility(View.GONE);
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
		
		// Affichage du mois courant. Inutile de passer des paramètres car l'adapteur
		// vient juste d'être créé avec les infos (année, mois) du mois courant.
		populateView();
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
}
