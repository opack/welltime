package fr.redmoon.tictac;

import java.util.Calendar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.MonthDisplayHelper;
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
	
	public Calendar month;
	public CalendarAdapter adapter;
	public Handler handler;
	public SparseArray<DayBean> items; // container to store some random calendar items

	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    // Création de l'interface graphique
        setContentView(R.layout.view);
        findViewById(R.id.lyt_btn_bar).setVisibility(View.GONE);
        findViewById(R.id.img_note).setVisibility(View.INVISIBLE);
	    
	    // Initialisation du gestionnaire de sweep
        initSweep(
        	new int[]{R.id.month_calendar, R.id.month_details}, 
        	new int[]{R.layout.month_calendar, R.layout.month_details});
        
	    month = Calendar.getInstance();
	    onNewIntent(getIntent());

	    items = new SparseArray<DayBean>();
	    adapter = new CalendarAdapter(this, month);

	    GridView gridview = (GridView) findViewById(R.id.gridview);
	    gridview.setAdapter(adapter);

	    handler = new Handler();
	    handler.post(calendarUpdater);

		gridview.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		    	TextView date = (TextView)v.findViewById(R.id.date);
		        if(date instanceof TextView && !date.getText().equals("")) {
		        	String day = date.getText().toString();
		        	if(day.length()==1) {
		        		day = "0"+day;
		        	}
		        	// return chosen date as string format 
		        	Toast.makeText(
		        		MonthActivity.this, 
		        		android.text.format.DateFormat.format("yyyy-MM", month)+"-"+day,
		        		Toast.LENGTH_SHORT)
		        	.show();
		        }
		    }
		});
		
		populateView(-1);
	}

	public void onNewIntent(Intent intent) {
		String date = intent.getStringExtra("date");
		if (date != null) {
			String[] dateArr = date.split("-"); // date format is yyyy-mm-dd
			month.set(Integer.parseInt(dateArr[0]), Integer.parseInt(dateArr[1]), Integer.parseInt(dateArr[2]));
		} else {
			// Aucune date n'a été passée, on prend la date du jour
			month.setTimeInMillis(System.currentTimeMillis());
		}
		
	}

	public Runnable calendarUpdater = new Runnable() {

		@Override
		public void run() {
			items.clear();
			// format random values. You can implement a dedicated class to provide real values
			items.put(1, null);
			DayBean dayNoNote = new DayBean();
			items.put(2, dayNoNote);
			DayBean dayWithNote = new DayBean();
			dayWithNote.note = "DBG";
			items.put(3, dayWithNote);
			
			
			adapter.setItems(items);
			adapter.notifyDataSetChanged();
		}
	};	

	@Override
	public void populateView(long day) {
		// DBG day vaut -1 et est ignoré
		
		adapter.refreshDays();
		adapter.notifyDataSetChanged();				
		handler.post(calendarUpdater); // generate some random calendar items				

		final MonthDisplayHelper helper = adapter.getMonthDisplayHelper();
		mWorkTime.set(1, helper.getMonth(), helper.getYear());
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
    	adapter.previousMonth();
    	populateView(-1);
    }
    
    /**
     * Affiche le mois suivant.
     * @param btn
     */
    public void showNext(final View btn) {
    	adapter.nextMonth();
    	populateView(-1);
    }
}
