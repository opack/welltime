package fr.redmoon.tictac;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.Time;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.PreferenceKeys;
import fr.redmoon.tictac.bus.PreferencesUtils;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.gui.dialogs.WeekDialogDelegate;
import fr.redmoon.tictac.gui.listadapter.WeekAdapter;
import fr.redmoon.tictac.gui.widgets.WidgetProvider;

public class WeekActivity extends TicTacActivity {
	
	public interface OnDayDeletionListener{
		/**
		 * Appel�e lorsqu'un jour a �t� supprim�
		 * @param date
		 */
		void onDeleteDay(long date);
	}
	
	private WeekDialogDelegate mDialogDelegate;
	
	private List<DayBean> mWeek;
	private int mSelectedDay;
	
	private int mWeekWorked;
	
	private long mMonday;
	private long mSunday;
	
	// Ci-dessous suivent les objets instanci�s une unique fois pour des raisons de performance.
	// On les cr�e au d�marrage de l'application et on les r�utilise avec des m�j pour �viter
	// d'autres instanciations.
	private ListView mLstDays;
	private final List<String[]> mDaysArray = new ArrayList<String[]>();
	private static List<OnDayDeletionListener> sDayDeletionListeners;
	
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	mDialogDelegate = new WeekDialogDelegate(this);

        // Cr�ation du bean de travail
        mWeek = new ArrayList<DayBean>();
        
        // Initialisation de l'affichage
        setContentView(R.layout.view);
        findViewById(R.id.btn_checkin).setVisibility(View.INVISIBLE);
        findViewById(R.id.img_note).setVisibility(View.INVISIBLE);
        
        // Initialisation du gestionnaire de sweep
        initSweep(new int[]{R.id.list, R.id.week_details}, R.layout.week_details);
        
        // Cr�ation de l'adapteur affichant les jours. Pour l'instant aucun jour.
        final ListAdapter adapter = new WeekAdapter(this, R.layout.week_item, mDaysArray);
        mLstDays = (ListView)findViewById(R.id.list);
        mLstDays.setAdapter(adapter);
        
        // Affichage du jour courant
        mMonday = mToday; 	// On passe la date et non pas juste le bean pour
        					// s'assurer qu'une lecture des donn�es en base
        					// sera effectu�e afin d'initialiser le bean.
    }
    
    @Override
    protected void onResume() {
    	populateView(mMonday); 	// On passe la date et non pas juste le bean pour
								// s'assurer qu'une lecture des donn�es en base
								// sera effectu�e afin d'initialiser le bean.
    	super.onResume();
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
		inflater.inflate(R.menu.week_contextual, menu);
		
		// Mise � jour du jour s�lectionn� et du menu (en fonction de l'existence du jour en base)
		mSelectedDay = (Integer)v.getTag();
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final DayBean day = mWeek.get(mSelectedDay);
		switch (item.getItemId()) {
		case R.id.menu_day_show_checkings:
			showDayCheckings(day.date);
			return true;
		case R.id.menu_day_show_details:
			showDayDetails(day);
			return true;
		case R.id.menu_day_delete:
			deleteDay(day.date, mSelectedDay);
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	@Override
	protected Dialog onCreateDialog(final int id) {
		return mDialogDelegate.createDialog(id);
	}
	
	@Override
	protected void onPrepareDialog(final int id, final Dialog dialog, final Bundle args) {
		super.onPrepareDialog(id, dialog, args);
		mDialogDelegate.prepareDialog(id, dialog, args);
	}
    
	private void showDayDetails(final DayBean day) {
		switchTab(0, day.date, R.id.day_details);
	}

	/**
	 * Bascule vers l'activit� "Jour" en affichant le jour s�lectionn�.
	 * @param date
	 */
	private void showDayCheckings(final long date) {
		switchTab(0, date, R.id.list);
	}

	public void showPrevious(final View btn) {
    	// On r�cup�re en base le jour pr�c�dent le lundi actuellement affich�
    	mDb.fetchPreviousDay(mMonday, mWorkDayBean);
    	if (mWorkDayBean.isValid) {
	    	// Rafra�chit l'interface graphique.
	    	populateView(mWorkDayBean.date);
    	}
    }
    
    public void showNext(final View btn) {
    	// On r�cup�re en base le jour pr�c�dent le lundi actuellement affich�
    	mDb.fetchNextDay(mSunday, mWorkDayBean);
    	
    	// Faut-il pr�parer la semaine d'aujourd'hui ?
    	// Faut-il pr�parer le jour d'aujourd'hui ? Oui, si :
    	//  - on a essay� de r�cup�rer un jour qui n'a pas �t� trouv�
    	//  - ce jour est avant aujourd'hui
    	if (!mWorkDayBean.isValid
    	&& mWorkDayBean.date < mToday) {
    		mWorkDayBean.reset();
    		mWorkDayBean.date = mToday;
    	}
    	
    	// Rafra�chit l'interface graphique.
    	populateView(mWorkDayBean.date);
    }
    
    /**
     * Met � jour la liste de beans de travail avec les donn�es des jours de la semaine
     * comprenant le jour indiqu�, et rafra�chit l'interface.
     * @param day
     */
    public void populateView(final long date) {
    	// On d�termine les jours correspondant � cette semaine.
    	// Typiquement, le lundi et le dimanche de la semaine.
    	DateUtils.fillTime(date, mWorkTime);
    	mWorkTime.normalize(true);
    	final Time monday = new Time();
    	DateUtils.getDateOfDayOfWeek(mWorkTime, Time.MONDAY, monday);
    	final Time sunday = new Time();
    	DateUtils.getDateOfDayOfWeek(mWorkTime, Time.SUNDAY, sunday);
    	
    	// On r�cup�re le jour en base
    	mMonday = DateUtils.getDayId(monday);
    	mSunday = DateUtils.getDayId(sunday);
    	mDb.fetchDays(mMonday, mSunday, mWeek);

    	// On conserve le jour de travail (notamment pour le sweep)
    	for (DayBean day : mWeek) {
    		if (day.date == date) {
    			mWorkDayBean = day;
    			break;
    		}
    	}
    	
    	// Rafra�chit l'interface graphique.
    	// populateCommon est appel�e apr�s populateDays car populateDays 
    	// parcours la liste des jours et initialise des variables de travail 
    	// qui nous permettront de ne pas parcourir cette liste deux fois.
    	populateDays(monday, sunday, mWeek);
    	populateCommon(monday, sunday);
    	populateDetails();
    }
    
    /**
     * Mise � jour des composants communs � l'affichage "Pointages" et "D�tails"
     * @param day
     */
    private void populateCommon(final Time monday, final Time sunday) {
    	// Affichage de la p�riode de la semaine
        final TextView txtCurrentWeek = (TextView)findViewById(R.id.txt_current);
        txtCurrentWeek.setText(getResources().getString(
			R.string.current_week,
			monday.getWeekNumber(),
			monday.format(DateUtils.FORMAT_DATE_LONG),
			sunday.format(DateUtils.FORMAT_DATE_LONG)
		));
        
        // Affichage du temps effectu�
    	final int weekTotal = Math.min(mWeekWorked, PreferencesBean.instance.weekMax);
        final TextView txtWeekStats = (TextView)findViewById(R.id.txt_stats);
        txtWeekStats.setText(getResources().getString(
			R.string.week_stats,
			TimeUtils.formatMinutes(weekTotal)
		));
    }
    
    /**
     * Rafra�chit l'interface graphique en utilisant le bean fournit en param�tre, donc sans acc�der
     * � la base de donn�es.
     * @param week 
     * @param sunday 
     * @param monday 
     * @param day
     */
    private void populateDays(final Time monday, final Time sunday, final List<DayBean> week) {
    	mWeekWorked = 0;
    	
    	// On cr�e le tableau qui est attendu par l'adapter d'affichage
    	mDaysArray.clear();
    	String[] dayInfos = null;
    	for (DayBean day : week) {
    		// Cr�ation du tableau contenant les infos du jour
    		dayInfos = new String[3];
    		mDaysArray.add(dayInfos);
    		
    		// Remplissage de la date du jour
			dayInfos[0] = DateUtils.formatDateDDMM(day.date);
    		
			// Calcul du temps effectu�
    		final int dayTotal = TimeUtils.computeTotal(day);
    		dayInfos[1] = TimeUtils.formatMinutes(dayTotal);
    		
    		// Mise � jour du total de la semaine
    		mWeekWorked += dayTotal;
    		
    		// Sauvegarde de la couleur de fond de ce jour
            dayInfos[2] = String.valueOf(PreferencesBean.getColorByDayType(day.type));
    	}
    	
        // Affichage des jours.
    	// Note : une explication d�taill�e sur la raison conduisant � faire
    	// un get(set()) est donn�e dans DayActivity.populateView(DayBean).
        mLstDays.setAdapter(mLstDays.getAdapter());
    }
    
    /**
     * Mise � jour de la vue "D�tails". Cette m�thode travaille avec les r�sultats
     * de populateDays, qui doit donc �tre appel�e avant.
     */
    private void populateDetails() {
		if (!mWeek.isEmpty()) {
			final DayBean firstDay = mWeek.get(0);
			DateUtils.fillTime(firstDay.date, mWorkTime);
			mWorkTime.normalize(true);
			
			// Calcule le temps ecr�t�
			final int lost = Math.max(0, mWeekWorked - PreferencesBean.instance.weekMax);
			
			// Calcul du temps HV en d�but de semaine si la date d'init HV est ant�rieure � la date de d�but de la semaine
	        String flexString = TimeUtils.UNKNOWN_TIME_STRING;
	        final long mondayDate = DateUtils.getDayId(mWorkTime);
	        if (PreferencesBean.instance.flexCurDate < mondayDate) {
	        	final int flex = updateFlex(PreferencesBean.instance.flexInitDate, PreferencesBean.instance.flexInitTime, mondayDate);
	        	flexString = TimeUtils.formatMinutes(flex);
	        } else {
	        	flexString = TimeUtils.formatMinutes(PreferencesBean.instance.flexCurTime);
	        }
			
	        // Mise � jour des composants graphiques
			setText(R.id.txt_week_flex_time, R.string.week_flex_time, DateUtils.formatDateDDMM(firstDay.date), flexString);
			setText(R.id.txt_week_work_time, R.string.week_work_time, TimeUtils.formatMinutes(mWeekWorked));
			setText(R.id.txt_week_lost_time, R.string.week_lost_time, TimeUtils.formatMinutes(lost));
		}
	}

	/**
	 * Met � jour l'HV en le calculant entre la date d'HV initiale
	 * et le jour courant.
	 * @return
	 */
	private int updateFlex(final long initialDay, final int initialTime, final long endDay) {
		// On r�cup�re le lundi de la semaine courante. C'est la date � laquelle l'HV doit
		// �tre � jour.
		TimeUtils.parseDate(endDay, mWorkTime);
		DateUtils.getDateOfDayOfWeek(mWorkTime, Time.MONDAY, mWorkTime);
		final long currentMonday = DateUtils.getDayId(mWorkTime);
		
		// On se place sur le lundi. L'HV est consid�r� � la semaine � partir du lundi.
		// C'est inutile de chercher le lundi ici puisque calculateWeekFlex le fera de toutes
		// fa�on, mais comme on est oblig�s d'avoir un Time pour initialiser notre Calendar,
		// autant le faire sur un lundi.
		TimeUtils.parseDate(initialDay, mWorkTime);
		DateUtils.getDateOfDayOfWeek(mWorkTime, Time.MONDAY, mWorkTime);
		final Calendar calendar = new GregorianCalendar(mWorkTime.year, mWorkTime.month, mWorkTime.monthDay); // On sauvegarde le lundi pour passer facilement d'une semaine � l'autre

		long curDate = DateUtils.getDayId(calendar);
		int flexCurTime = initialTime;
		while (curDate < currentMonday) {
			// On calcule l'H.V. de la semaine et on l'ajoute au total
			flexCurTime += computeWeekFlex(curDate);
			
			// On passe � la semaine suivante
			calendar.add(Calendar.DAY_OF_MONTH, 7);
			curDate = DateUtils.getDayId(calendar);
		}
		
		// On borne le temps additionnel
		flexCurTime = Math.max(flexCurTime, PreferencesBean.instance.flexMin);
		flexCurTime = Math.min(flexCurTime, PreferencesBean.instance.flexMax);
		
		// On enregistre le temps additionnel et la date � laquelle il a �t� calcul�
		PreferencesBean.instance.flexCurDate = currentMonday;
		PreferencesUtils.savePreference(this, PreferenceKeys.flexCurDate.getKey(), PreferencesBean.instance.flexCurDate);
		PreferencesBean.instance.flexCurTime = flexCurTime;
		PreferencesUtils.savePreference(this, PreferenceKeys.flexCurTime.getKey(), PreferencesBean.instance.flexCurTime);
		
		return flexCurTime;
	}
	
	/**
	 * Retourne le temps total effectu� au cours de la semaine contenant le jour indiqu�.
	 * @param aDay
	 * @return
	 */
	private int computeWeekFlex(final long aDay) {
		// On r�cup�re le lundi correspondant au jour indiqu�
		TimeUtils.parseDate(aDay, mWorkTime);
		DateUtils.getDateOfDayOfWeek(mWorkTime, Time.MONDAY, mWorkTime);
		final long firstDay = DateUtils.getDayId(mWorkTime);
		
		// On prend le dernier jour de la semaine, donc 6 jours plus tard.
		// On fait un min pour dire : si le dimanche trouv� est apr�s ajourd'hui, alors on prend plut�t la date d'aujourd'hui.
		// On peut penser que �a ne sert � rien car aujourd'hui sera toujours le dernier jour. En r�alit� c'est faux car on
		// pourra pointer en avance des jours de vacances par exemple.
		DateUtils.getDateOfDayOfWeek(mWorkTime, Time.SUNDAY, mWorkTime);
		final long lastDay = Math.min(mToday, DateUtils.getDayId(mWorkTime));
		
		// On r�cup�re les jours de la semaine
		final List<DayBean> days = new ArrayList<DayBean>();
		mDb.fetchDays(firstDay, lastDay, days);

		// On calcule le temps effectu�.
		int flex = 0;
		int dayTotal = 0;
		for (final DayBean day : days) {
			dayTotal = TimeUtils.computeTotal(day);
			if (dayTotal > PreferencesBean.instance.dayMax) {
				dayTotal = PreferencesBean.instance.dayMax;
			}
			flex += dayTotal - PreferencesBean.instance.dayMin;
		}
		
		return flex;
	}
	
	/**
	 * Affiche un message demandant la confirmation de la suppression,
	 * puis supprime le jour indiqu� le cas �ch�ant.
	 * @param date
	 */
	private void deleteDay(final long date, final int selectedDay) {
		// Cr�ation des �l�ments de la bo�te de dialogue
		final CharSequence message = getString(R.string.dlg_msg_confirm_day_deletion, DateUtils.formatDateDDMM(date));
		final DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	// Suppression du jour en base
            	mDb.deleteDay(date);
            	
            	// Mise � jour de l'affichage
            	mWeek.remove(selectedDay);
            	if (!mWeek.isEmpty()) {
            		// Il reste au moins un jour : on affiche la semaine correspondante.
            		populateView(mWeek.get(0).date);
            	} else {
            		// La semaine est vide : on affiche la semaine pr�c�dente
            		long previous = mDb.fetchPreviousDay(date);
            		if (previous == -1) {
            			// S'il n'y a pas de jour pr�c�dent, alors on affiche la semaine d'aujourd'hui.
            			previous = mToday;
            		}
            		populateView(previous);
            	}
            	
            	// Si on a supprim� le jour d'aujourd'hui, on met � jour le widget
            	if (mToday == date) {
            		WidgetProvider.updateClockinImage(WeekActivity.this);
            	}
            	
            	// Si on a supprim� le jour actuellement affich� dans la vue "Jour", on l'en informe
            	// pour qu'elle mette � jour son affichage
            	fireOnDeleteDay(date);
            }
        };
        final DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
        	@Override
    		public void onClick(DialogInterface dialog, int id) {
            	dialog.cancel();
        	}
        };
        
        // Affichage de la bo�te de dialogue
		showConfirmDialog(message, positiveListener, negativeListener);
	}
	
	public static void registerDayDeletionListener(final OnDayDeletionListener listener) {
		if (sDayDeletionListeners == null) {
			sDayDeletionListeners = new ArrayList<OnDayDeletionListener>();
		}
		sDayDeletionListeners.add(listener);
	}
	
	/**
	 * Notifie les listeners qu'un jour a �t� supprim�
	 * @param date
	 */
	private static void fireOnDeleteDay(final long date) {
		for (OnDayDeletionListener listener : sDayDeletionListeners) {
			listener.onDeleteDay(date);
		}
	}
}