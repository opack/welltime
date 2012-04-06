package fr.redmoon.tictac;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import fr.redmoon.tictac.gui.calendar.CalendarAdapter;

public class MonthActivity extends TicTacActivity {
	
	public Calendar month;
	public CalendarAdapter adapter;
	public Handler handler;
	public ArrayList<String> items; // container to store some random calendar items

	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    // Création de l'interface graphique
        setContentView(R.layout.view);
        findViewById(R.id.btn_checkin).setVisibility(View.INVISIBLE);
        findViewById(R.id.img_note).setVisibility(View.INVISIBLE);
	    
	    // Initialisation du gestionnaire de sweep
        initSweep(
        	new int[]{R.id.month_calendar, R.id.month_details}, 
        	new int[]{R.layout.month_calendar, R.layout.month_details});
        
	    month = Calendar.getInstance();
	    onNewIntent(getIntent());

	    items = new ArrayList<String>();
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
			for(int i=0;i<31;i++) {
				Random r = new Random();

				if(r.nextInt(10)>6)
				{
					items.add(Integer.toString(i));
				}
			}

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

		populateCommon(
			android.text.format.DateFormat.format("MMMM yyyy", month),
    		0,
    		100);
	}

	/**
     * Affiche le mois précédant.
     * @param btn
     */
    public void showPrevious(final View btn) {
    	if(month.get(Calendar.MONTH)== month.getActualMinimum(Calendar.MONTH)) {				
			month.set((month.get(Calendar.YEAR)-1),month.getActualMaximum(Calendar.MONTH),1);
		} else {
			month.set(Calendar.MONTH,month.get(Calendar.MONTH)-1);
		}
    	populateView(-1);
    }
    
    /**
     * Affiche le mois suivant.
     * @param btn
     */
    public void showNext(final View btn) {
    	if(month.get(Calendar.MONTH)== month.getActualMaximum(Calendar.MONTH)) {				
			month.set((month.get(Calendar.YEAR)+1),month.getActualMinimum(Calendar.MONTH),1);
		} else {
			month.set(Calendar.MONTH,month.get(Calendar.MONTH)+1);
		}
    	populateView(-1);
    }
}
