package fr.redmoon.tictac.gui.calendar;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.format.Time;
import android.util.MonthDisplayHelper;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.bean.PreferencesBean;

public class CalendarAdapter extends BaseAdapter {
	private final Activity mActivity;

	private MonthDisplayHelper mHelper;
    private SparseArray<DayBean> mDaysData;
    private final String[] mDaysShortNames;
    
    private final Time mWorkTime;
    private final Calendar mWorkCal;
    
    public CalendarAdapter(final Activity _activity, final int year, final int month) {
    	mActivity = _activity;
    	mDaysShortNames = mActivity.getResources().getStringArray(R.array.days_short_names);
    	
    	showMonth(year, month);
        
        mDaysData = new SparseArray<DayBean>();
        mWorkTime = new Time();
        mWorkCal = new GregorianCalendar();
    }
    
    public void setItems(final SparseArray<DayBean> items) {
    	this.mDaysData = items;
    }

    public int getCount() {
    	// 6 lignes de 7 jours + 1 ligne d'ent�te + 1 colonne d'entete,
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
        // R�cup�ration de la vue si possible, sinon inflate
    	View v = convertView;
    	// Si la vue n'est pas recycl�e, on initialise son contenu
        if (convertView == null) {
        	LayoutInflater vi = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.calendar_item, null);
        }
        
        
        // D�termine � quelle ligne et colonne appartient cette vue
        // Une ligne contient 7 colonnes de jours + 1 colonne d'ent�te, soit 8 valeurs.
    	final int row = position / 8;
        final int col = position % 8;
        
        // Traitement de la vue appartenant � l'ent�te de ligne ou colonne
        if (row == 0) {
        	adaptRowHeaderView(col, v);
        } else if (col == 0) {
        	adaptColHeaderView(row, v);
        }
        // Vue correspondant � un jour dans le calendrier
        else {
        	// Pour que le MonthDisplayHelper ne soit pas perdu, on d�cale le num�ro de ligne et colonne
        	// de fa�on � lui masquer le fait qu'on a ajout� une ligne et une colonne d'ent�te
        	adaptDayView(row - 1, col - 1, v);
        }
        
        // Ajout/retrait de ce jour comme proposant un menu contextuel suivant qu'il est dans
        // un ent�te ou non, ou dans le WE
        if (row == 0 || col == 0) {
            mActivity.unregisterForContextMenu(v);
        } else {
            mActivity.registerForContextMenu(v);
        }
        return v;
    }
    
    private void adaptRowHeaderView(final int col, final View view) {
    	final TextView dayView = (TextView)view.findViewById(R.id.day_num);
    	if (col == 0) {
    		// La colonne 0 est celle o� sont affich�es les semaines. On n'aura
    		// donc pas � �crire de nom de jour dessus. On vide la case
    		dayView.setText("");
	    	view.setBackgroundColor(mActivity.getResources().getColor(R.color.app_background));
    	} else {
	    	dayView.setText(mDaysShortNames[col - 1]);
	    	dayView.setTextColor(Color.WHITE);
	    	view.setBackgroundColor(mActivity.getResources().getColor(R.color.light_blue));
    	}
	}

	private void adaptColHeaderView(int row, View view) {
		final TextView dayView = (TextView)view.findViewById(R.id.day_num);
		if (row == 0) {
    		// La ligne 0 est celle o� sont affich�es les noms des jours. On n'aura
    		// donc pas � �crire de num�ro de semaine dessus.
			dayView.setText("");
	    	view.setBackgroundColor(mActivity.getResources().getColor(R.color.app_background));
    	} else {
    		// Pour d�terminer le num�ro de la semaine, on se base sur le premier jour du mois,
    		// auquel on ajoute 7 jours pour chaque ligne. Ca nous donne donc le 1er, le 8 etc...
    		// Chaque jour �tant forc�ment sur une ligne diff�rente, on peut en d�duire le num�ro
    		// de la semaine auquel il appartient.
    		// Le row-1 est n�cessaire car on a une ligne d'ent�te que l'on veut ignorer dans ce
    		// calcul.
    		final int dayNum = 1 + (row - 1) * 7;
    		mWorkTime.set(dayNum, mHelper.getMonth(), mHelper.getYear());
    		mWorkTime.normalize(true);
    		final int weekNumber = mWorkTime.getWeekNumber();
    		
	    	dayView.setText(String.valueOf(weekNumber));
	    	dayView.setTextColor(Color.WHITE);
	    	view.setBackgroundColor(mActivity.getResources().getColor(R.color.light_blue));
    	}
	}

	/**
     * M�thode appel�e pour mettre � une jour une vue correspondant � un jour
     * dans le mois
     * @param row
     * @param col
     * @param dayView
     */
    private void adaptDayView(final int row, final int col, View view) {
    	final int position = row * 7 + col - mHelper.getOffset() + 1;
    	final TextView dayLabel = (TextView)view.findViewById(R.id.day_num);
    	final DayBean dayData = mDaysData.get(position);
    	final boolean isWithinCurrentMonth = mHelper.isWithinCurrentMonth(row, col);
    	
    	// Ecriture du num�ro du jour
    	final int dayNumber = mHelper.getDayAt(row, col);
    	dayLabel.setText(String.valueOf(dayNumber));
    	
    	// Ajout de l'identifiant du jour, qui sera utilis� pour l'identifier si on utilise
        // le menu contextuel
    	// Si on est dans le mois courant, alors on d�duit l'identifiant du jour gr�ce au
    	// MonthDisplayHelper
    	long dayId = 0;
    	if (isWithinCurrentMonth) {
    		dayId = DateUtils.getDayId(mHelper.getYear(), mHelper.getMonth(), dayNumber);
    	}
    	// On est hors du mois. On va se servir d'un Calendar pour se positionner sur la date
		// courante et avancer ou reculer d'un mois, de fa�on � obtenir la bonne date.
    	else {
    		// On se place sur le premier jour du mois
    		mWorkCal.set(mHelper.getYear(), mHelper.getMonth(), 1);
    		// On se d�cale d'autant de jours que la position (donc �ventuellement n�gatif)
    		mWorkCal.add(Calendar.DAY_OF_YEAR, position);
    		// Il ne reste plus qu'� extraire le jour qu'a calcul� le Calendar
    		dayId = DateUtils.getDayId(mWorkCal.get(Calendar.YEAR), mWorkCal.get(Calendar.MONTH), dayNumber);
    	}
    	view.setTag(dayId);
    	
    	// Coloration du fond des cases :
    	// Les jours du mois pr�c�dent/suivant sont affich�s en gris
        if(!isWithinCurrentMonth) {
        	dayLabel.setTextColor(Color.LTGRAY);
        	view.setBackgroundColor(mActivity.getResources().getColor(R.color.calendar_day_out_of_month));
        }
        // Les jours normaux prennent la couleur du type du jour
        // ou gris si aucun nour n'a �t� point�
        else {
	        dayLabel.setTextColor(mActivity.getResources().getColor(R.color.calendar_day_standard));
	        // Coloration du fond avec la couleur du type de jour
	        if (dayData != null) {
	        	final int dayColor = PreferencesBean.getColorByDayType(dayData.type);
	        	view.setBackgroundColor(dayColor);
	        } else {
	        	view.setBackgroundColor(mActivity.getResources().getColor(R.color.calendar_day_not_worked));
	        }
        }
        
        // Coloration du texte pour indiquer la pr�sence d'une note
        if (dayData != null && dayData.note != null && !dayData.note.isEmpty()) {
        	// Une note existe pour ce jour : on affiche le libell� du jour en jaune
        	dayLabel.setTextColor(mActivity.getResources().getColor(R.color.calendar_day_with_note));
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