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
import android.widget.TextView;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.bean.PreferencesBean;

public class CalendarAdapter extends BaseAdapter {
	private final Context mContext;

	private MonthDisplayHelper mHelper;
    private SparseArray<DayBean> mDaysData;
    public String[] days;
    private final String[] mDaysShortNames;
    
    public CalendarAdapter(final Context _context, final int year, final int month) {
    	mContext = _context;
    	mDaysShortNames = mContext.getResources().getStringArray(R.array.days_short_names);
    	
    	showMonth(year, month);
        
        this.mDaysData = new SparseArray<DayBean>();
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
        return 0;
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
	    	view.setBackgroundColor(mContext.getResources().getColor(R.color.app_background));
    	} else {
	    	dayView.setText(mDaysShortNames[col - 1]);
	    	dayView.setTextColor(Color.WHITE);
	    	view.setBackgroundColor(mContext.getResources().getColor(R.color.light_blue));
    	}
	}

	private void adaptColHeaderView(int row, View view) {
		final TextView dayView = (TextView)view.findViewById(R.id.date);
		if (row == 0) {
    		// La ligne 0 est celle où sont affichées les noms des jours. On n'aura
    		// donc pas à écrire de numéro de semaine dessus.
			dayView.setText("");
	    	view.setBackgroundColor(mContext.getResources().getColor(R.color.app_background));
    	} else {
	    	dayView.setText("00");
	    	dayView.setTextColor(Color.WHITE);
	    	view.setBackgroundColor(mContext.getResources().getColor(R.color.light_blue));
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
    	final int position = row * 7 + col - mHelper.getOffset() + 1;
    	final TextView dayLabel = (TextView)view.findViewById(R.id.date);
    	final DayBean dayData = mDaysData.get(position);
    	
    	final String dayNumber = String.valueOf(mHelper.getDayAt(row, col));
    	dayLabel.setText(dayNumber);
    	// Coloration du fond des cases :
    	// Les jours du mois précédent/suivant sont affichés en gris
        if(!mHelper.isWithinCurrentMonth(row, col)) {
        	dayLabel.setTextColor(Color.LTGRAY);
        	view.setBackgroundColor(mContext.getResources().getColor(R.color.calendar_day_out_of_month));
        }
        // Les jours normaux prennent la couleur du type du jour
        // ou gris si aucun nour n'a été pointé
        else {
	        dayLabel.setTextColor(mContext.getResources().getColor(R.color.calendar_day_standard));
	        // Coloration du fond avec la couleur du type de jour
	        if (dayData != null) {
	        	final int dayColor = PreferencesBean.getColorByDayType(dayData.type);
	        	view.setBackgroundColor(dayColor);
	        } else {
	        	view.setBackgroundColor(mContext.getResources().getColor(R.color.calendar_day_not_worked));
	        }
        }
        
        // Affichage d'une icone indiquant la présence d'une note
        if (dayData != null && dayData.note != null && !dayData.note.isEmpty()) {
        	// Une note existe pour ce jour : on affiche le libellé du jour en jaune
        	dayLabel.setTextColor(mContext.getResources().getColor(R.color.calendar_day_with_note));
        }
	}

	public int getMonth() {
		return mHelper.getMonth();
	}

	public int getYear() {
		return mHelper.getYear();
	}

	public void nextMonth() {
		mHelper.nextMonth();
	}

	public void previousMonth() {
		mHelper.previousMonth();
	}

	public int getNumberOfDaysInMonth() {
		return mHelper.getNumberOfDaysInMonth();
	}

	public void showMonth(final int year, final int month) {
		mHelper = new MonthDisplayHelper(
    		year, 
    		month, 
    		Calendar.MONDAY);		
	}
}