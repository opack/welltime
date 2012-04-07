package fr.redmoon.tictac.gui.calendar;

import java.util.Calendar;

import android.content.Context;
import android.graphics.Color;
import android.util.MonthDisplayHelper;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.bean.DayBean;

public class CalendarAdapter extends BaseAdapter {
	private final Context mContext;

	private final MonthDisplayHelper mHelper;
    private final Calendar mCalendar;
    private final Calendar mSelectedDate;
    private SparseArray<DayBean> mDaysData;
    public String[] days;
    private final String[] mDaysShortNames;
    
    public CalendarAdapter(final Context _context, final Calendar monthCalendar) {
    	mContext = _context;
    	mDaysShortNames = mContext.getResources().getStringArray(R.array.days_short_names);
    	
    	mHelper = new MonthDisplayHelper(
    		monthCalendar.get(Calendar.YEAR), 
    		monthCalendar.get(Calendar.MONTH), 
    		Calendar.MONDAY);
    	mSelectedDate = (Calendar)monthCalendar.clone();
    	mCalendar = monthCalendar;    	
        mCalendar.set(Calendar.DAY_OF_MONTH, 1);
        
        this.mDaysData = new SparseArray<DayBean>();
        refreshDays();
    }
    
    public void setItems(final SparseArray<DayBean> items) {
    	this.mDaysData = items;
    }

    public int getCount() {
    	// 6 lignes de 7 jours + 1 ligne d'entête + 1 colonne d'entete,
    	// soit 7 lignes de 8 colonnes
        return 56;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return position / 8;
    }

    public View getView(final int position, final View convertView, final ViewGroup parent) {
        // Récupération de la vue si possible, sinon inflate
    	View v = convertView;
    	// Si la vue n'est pas recyclée, on initialise son contenu
        if (convertView == null) {
        	LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.calendar_item, null);
        }
        
        
        // Détermine à quelle ligne et colonne appartient cette vue
        // Une ligne contient 7 colonnes de jours + 1 colonne d'entête, soit 8 valeurs.
    	final int row = position / 8;
        final int col = position % 8;
        
        // Traitement de la vue appartenant à l'entête de ligne ou colonne
        if (row == 0) {
        	adaptRowHeaderView(col, v);
        } else if (col == 0) {
        	adaptColHeaderView(row, v);
        }
        // Vue correspondant à un jour dans le calendrier
        else {
        	// Pour que le MonthDisplayHelper ne soit pas perdu, on décale le numéro de ligne et colonne
        	// de façon à lui masquer le fait qu'on a ajouté une ligne et une colonne d'entête
        	adaptDayView(row - 1, col - 1, v);
        }
        return v;
    }
    
    private void adaptRowHeaderView(final int col, final View view) {
    	final TextView dayView = (TextView)view.findViewById(R.id.date);
    	if (col == 0) {
    		// La colonne 0 est celle où sont affichées les semaines. On n'aura
    		// donc pas à écrire de nom de jour dessus. On vide la case
    		dayView.setText("");
	    	view.setBackgroundColor(R.color.app_background);
    	} else {
	    	dayView.setText(mDaysShortNames[col - 1]);
	    	dayView.setTextColor(Color.WHITE);
	    	view.setBackgroundResource(R.drawable.item_calendar_background_hdr);
    	}
	}

	private void adaptColHeaderView(int row, View view) {
		final TextView dayView = (TextView)view.findViewById(R.id.date);
		if (row == 0) {
    		// La ligne 0 est celle où sont affichées les noms des jours. On n'aura
    		// donc pas à écrire de numéro de semaine dessus.
			dayView.setText("");
	    	view.setBackgroundColor(R.color.app_background);
    	} else {
	    	dayView.setText("S00");
	    	dayView.setTextColor(Color.WHITE);
			view.setBackgroundResource(R.drawable.item_calendar_background_hdr);
    	}
	}

	/**
     * Méthode appelée pour mettre à une jour une vue correspondant à un jour
     * dans le mois
     * @param row
     * @param col
     * @param dayView
     */
    private void adaptDayView(final int row, final int col, View view) {
    	final int position = row * 7 + col;
    	final TextView dayView = (TextView)view.findViewById(R.id.date);
    	
    	// Les jours du mois précédent/suivant sont affichés différemment
        if(!mHelper.isWithinCurrentMonth(row, col)) {
        	view.setBackgroundResource(R.drawable.item_calendar_background_out);
        	dayView.setTextColor(Color.BLACK);
        }
    	// Mise en valeur du jour courant
        else if (mCalendar.get(Calendar.YEAR) == mSelectedDate.get(Calendar.YEAR)
        	&& mCalendar.get(Calendar.MONTH)== mSelectedDate.get(Calendar.MONTH)
        	&& days[position].equals(String.valueOf(mSelectedDate.get(Calendar.DAY_OF_MONTH))) ) {
    		view.setBackgroundResource(R.drawable.item_calendar_background_sel);
    		dayView.setTextColor(Color.WHITE);
    	}
        // Affichage simple d'un jour standard
        else {
    		view.setBackgroundResource(R.drawable.list_item_calendar_background_std);
    		dayView.setTextColor(Color.BLUE);
        }
        dayView.setText(days[position]);
        
        // Afficher une icone indiquant la présence d'une note
        ImageView iw = (ImageView)view.findViewById(R.id.note);
        final DayBean dayData = mDaysData.get(position);
        if (dayData != null && dayData.note != null && !dayData.note.isEmpty()) {
        	// Une note existe pour ce jour
        	iw.setVisibility(View.VISIBLE);
        } else {
        	// Il n'y a pas de note pour ce jour
        	iw.setVisibility(View.INVISIBLE);
        }
	}

	public void refreshDays()
    {
    	// clear items
    	mDaysData.clear();
    	
    	days = new String[42];// 6 lignes de 7 jours
    	int[] row;
    	int curDay = 0;
    	for (int curRow = 0; curRow < 6; curRow++) {
    		row = mHelper.getDigitsForRow(curRow);
    		for (int dayNum : row) {
    			days[curDay] = String.valueOf(dayNum);
    			curDay++;
    		}
    	}
    }

	public void previousMonth() {
		mHelper.previousMonth();
	}

	public void nextMonth() {
		mHelper.nextMonth();
	}

	public MonthDisplayHelper getMonthDisplayHelper() {
		return mHelper;
	}
}