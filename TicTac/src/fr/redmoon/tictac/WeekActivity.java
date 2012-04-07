package fr.redmoon.tictac;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Dialog;
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
import fr.redmoon.tictac.TicTacActivity.OnDayDeletionListener;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.PreferenceKeys;
import fr.redmoon.tictac.bus.PreferencesUtils;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.gui.dialogs.WeekDialogDelegate;
import fr.redmoon.tictac.gui.listadapter.WeekAdapter;
import fr.redmoon.tictac.gui.listadapter.WeekAdapterEntry;

public class WeekActivity extends TicTacActivity implements OnDayDeletionListener {
	
	private WeekDialogDelegate mDialogDelegate;
	
	private List<DayBean> mWeek;
	private int mSelectedDay;
	
	private int mWeekWorked;
	
	private long mMonday;
	private long mSunday;
	
	// Ci-dessous suivent les objets instanciés une unique fois pour des raisons de performance.
	// On les crée au démarrage de l'application et on les réutilise avec des màj pour éviter
	// d'autres instanciations.
	private ListView mLstDays;
	private final List<WeekAdapterEntry> mDaysArray = new ArrayList<WeekAdapterEntry>();
	
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	mDialogDelegate = new WeekDialogDelegate(this);

        // Création du bean de travail
        mWeek = new ArrayList<DayBean>();
        
        // Initialisation de l'affichage
        setContentView(R.layout.view);
        findViewById(R.id.btn_checkin).setVisibility(View.INVISIBLE);
        findViewById(R.id.img_note).setVisibility(View.INVISIBLE);
        
        // Initialisation du gestionnaire de sweep
        initSweep(
            	new int[]{R.id.week_days, R.id.week_details},
            	new int[]{R.layout.week_days, R.layout.week_details});
        
        // Création de l'adapteur affichant les jours. Pour l'instant aucun jour.
        final ListAdapter adapter = new WeekAdapter(this, R.layout.week_item, mDaysArray);
        mLstDays = (ListView)findViewById(R.id.list);
        mLstDays.setAdapter(adapter);
        
        // Affichage du jour courant
        mMonday = mToday; 	// On passe la date et non pas juste le bean pour
        					// s'assurer qu'une lecture des données en base
        					// sera effectuée afin d'initialiser le bean.
        
        // On veut être informé si la vue "Mois" supprime un jour
        MonthActivity.registerDayDeletionListener(this);
    }
    
    @Override
    protected void onResume() {
    	populateView(mMonday); 	// On passe la date et non pas juste le bean pour
								// s'assurer qu'une lecture des données en base
								// sera effectuée afin d'initialiser le bean.
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
		case R.id.menu_show_month:
			switchTab(MainActivity.TAB_MONTH_POS, mWorkDayBean.date, R.id.month_calendar);
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}
    
    @Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.week_contextual, menu);
		
		// Mise à jour du jour sélectionné et du menu (en fonction de l'existence du jour en base)
		mSelectedDay = (Integer)v.getTag();
		
		// Si le jour n'existe pas en base, on masque certaines options
		final DayBean day = mWeek.get(mSelectedDay);
		if (!mDb.isDayExisting(day.date)) {
			menu.findItem(R.id.menu_day_delete).setVisible(false);
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final DayBean day = mWeek.get(mSelectedDay);
		switch (item.getItemId()) {
		case R.id.menu_day_show_checkings:
			switchTab(MainActivity.TAB_DAY_POS, day.date, R.id.day_checkings);
			return true;
		case R.id.menu_day_show_details:
			switchTab(MainActivity.TAB_DAY_POS, day.date, R.id.day_details);
			return true;
		case R.id.menu_day_delete:
			deleteDay(day.date);
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
    
	public void showPrevious(final View btn) {
		// On se place sur le lundi de la semaine, et on recule d'un jour
    	mWorkCal.set(DateUtils.extractYear(mMonday), DateUtils.extractMonth(mMonday), DateUtils.extractDayOfMonth(mMonday));
    	mWorkCal.add(Calendar.DAY_OF_YEAR, -1);
    	
    	// Maintenant on affiche la semaine de ce jour
    	populateView(DateUtils.getDayId(mWorkCal));
    }
    
    public void showNext(final View btn) {
    	// On se place sur le dimanche de la semaine, et on avance d'un jour
    	mWorkCal.set(DateUtils.extractYear(mSunday), DateUtils.extractMonth(mSunday), DateUtils.extractDayOfMonth(mSunday));
    	mWorkCal.add(Calendar.DAY_OF_YEAR, 1);
    	
    	// Maintenant on affiche la semaine de ce jour
    	populateView(DateUtils.getDayId(mWorkCal));
    }
    
    /**
     * Met à jour la liste de beans de travail avec les données des jours de la semaine
     * comprenant le jour indiqué, et rafraîchit l'interface.
     * @param day
     */
    public void populateView(final long date) {
    	// On détermine les jours correspondant à cette semaine.
    	// Typiquement, le lundi et le dimanche de la semaine.
    	DateUtils.fillTime(date, mWorkTime);
    	mWorkTime.normalize(true);
    	final Time monday = new Time();
    	DateUtils.getDateOfDayOfWeek(mWorkTime, Time.MONDAY, monday);
    	final Time sunday = new Time();
    	DateUtils.getDateOfDayOfWeek(mWorkTime, Time.SUNDAY, sunday);
    	
    	// On récupère le jour en base
    	mMonday = DateUtils.getDayId(monday);
    	mSunday = DateUtils.getDayId(sunday);
    	mDb.fetchDays(mMonday, mSunday, mWeek);

    	// On conserve le jour de travail (notamment pour le sweep)
    	final Set<Long> daysById = new HashSet<Long>();
    	for (DayBean day : mWeek) {
    		daysById.add(day.date);
    		
    		if (day.date == date) {
    			mWorkDayBean = day;
    		}
    	}
    	
    	// Ajout de jours permettant de remplir les trous de la semaine
    	mWorkCal.set(monday.year, monday.month, monday.monthDay);
    	long dayId;
    	for (int curDay = 0; curDay < 5; curDay++) {
    		// Si le jour n'est pas en base, on en crée un faux
    		dayId = DateUtils.getDayId(mWorkCal);
    		if (!daysById.contains(dayId)) {
    			// Le nouveau jour créé sera noté comme non valide et de type
    			// "non travaillé".
    			DayBean fillin = new DayBean();
    			fillin.date = dayId;
    			mWeek.add(curDay, fillin);
    		}
    		
    		// On passe au jour suivant
    		mWorkCal.add(Calendar.DAY_OF_YEAR, 1);
    	}
    	
    	// Rafraîchit l'interface graphique.
    	// populateCommon est appelée après populateDays car populateDays 
    	// parcours la liste des jours et initialise des variables de travail 
    	// qui nous permettront de ne pas parcourir cette liste deux fois.
    	populateDays(monday, sunday, mWeek);
    	populateCommon(monday, sunday);
    	populateDetails();
    }
    
    /**
     * Mise à jour des composants communs à l'affichage "Pointages" et "Détails"
     * @param day
     */
    private void populateCommon(final Time monday, final Time sunday) {
        final String txtCurrent = getResources().getString(
    			R.string.current_week,
    			monday.getWeekNumber(),
    			monday.format(DateUtils.FORMAT_DATE_LONG),
    			sunday.format(DateUtils.FORMAT_DATE_LONG)
    		);
        final int total = Math.min(mWeekWorked, PreferencesBean.instance.weekMax);
        
        populateCommon(
    		txtCurrent,
    		total,
    		PreferencesBean.instance.weekMin);
    }
    
    /**
     * Rafraîchit l'interface graphique en utilisant le bean fournit en paramètre, donc sans accéder
     * à la base de données.
     * @param week 
     * @param sunday 
     * @param monday 
     * @param day
     */
    private void populateDays(final Time monday, final Time sunday, final List<DayBean> week) {
    	mWeekWorked = 0;
    	
    	// On crée le tableau qui est attendu par l'adapter d'affichage
    	mDaysArray.clear();
    	WeekAdapterEntry dayInfos = null;
    	for (DayBean day : week) {
    		// Création de la structure contenant les infos du jour
    		dayInfos = new WeekAdapterEntry();
    		mDaysArray.add(dayInfos);
    		dayInfos.isValid = day.isValid;
    		
    		// Remplissage de la date du jour
			dayInfos.date = DateUtils.formatDateDDMM(day.date);
    		
			// Calcul du temps effectué
			if (day.isValid) {
	    		final int dayTotal = TimeUtils.computeTotal(day);
	    		dayInfos.total = TimeUtils.formatMinutes(dayTotal);
	    		
	    		// Mise à jour du total de la semaine
	    		mWeekWorked += dayTotal;
			} else {
				dayInfos.total = TimeUtils.UNKNOWN_TIME_STRING;
			}
    		
    		// Sauvegarde de la couleur de fond de ce jour
            dayInfos.bkColor = PreferencesBean.getColorByDayType(day.type);
    	}
    	
        // Affichage des jours.
    	// Note : une explication détaillée sur la raison conduisant à faire
    	// un get(set()) est donnée dans DayActivity.populateView(DayBean).
        mLstDays.setAdapter(mLstDays.getAdapter());
    }
    
    /**
     * Mise à jour de la vue "Détails". Cette méthode travaille avec les résultats
     * de populateDays, qui doit donc être appelée avant.
     */
    private void populateDetails() {
		if (!mWeek.isEmpty()) {
			final DayBean firstDay = mWeek.get(0);
			DateUtils.fillTime(firstDay.date, mWorkTime);
			mWorkTime.normalize(true);
			
			// Calcule le temps ecrêté
			final int lost = Math.max(0, mWeekWorked - PreferencesBean.instance.weekMax);
			
			// Calcul du temps HV en début de semaine si la date d'init HV est antérieure à la date de début de la semaine
	        String flexString = TimeUtils.UNKNOWN_TIME_STRING;
	        final long mondayDate = DateUtils.getDayId(mWorkTime);
	        if (PreferencesBean.instance.flexCurDate < mondayDate) {
	        	final int flex = updateFlex(PreferencesBean.instance.flexInitDate, PreferencesBean.instance.flexInitTime, mondayDate);
	        	flexString = TimeUtils.formatMinutes(flex);
	        } else {
	        	flexString = TimeUtils.formatMinutes(PreferencesBean.instance.flexCurTime);
	        }
			
	        // Mise à jour des composants graphiques
			setText(R.id.txt_week_flex_time, R.string.week_flex_time, DateUtils.formatDateDDMM(firstDay.date), flexString);
			setText(R.id.txt_week_work_time, R.string.week_work_time, TimeUtils.formatMinutes(mWeekWorked));
			setText(R.id.txt_week_lost_time, R.string.week_lost_time, TimeUtils.formatMinutes(lost));
		}
	}

	/**
	 * Met à jour l'HV en le calculant entre la date d'HV initiale
	 * et le jour courant.
	 * @return
	 */
	private int updateFlex(final long initialDay, final int initialTime, final long endDay) {
		// On récupère le lundi de la semaine courante. C'est la date à laquelle l'HV doit
		// être à jour.
		TimeUtils.parseDate(endDay, mWorkTime);
		DateUtils.getDateOfDayOfWeek(mWorkTime, Time.MONDAY, mWorkTime);
		final long currentMonday = DateUtils.getDayId(mWorkTime);
		
		// On se place sur le lundi. L'HV est considéré à la semaine à partir du lundi.
		// C'est inutile de chercher le lundi ici puisque calculateWeekFlex le fera de toutes
		// façon, mais comme on est obligés d'avoir un Time pour initialiser notre Calendar,
		// autant le faire sur un lundi.
		TimeUtils.parseDate(initialDay, mWorkTime);
		DateUtils.getDateOfDayOfWeek(mWorkTime, Time.MONDAY, mWorkTime);
		final Calendar calendar = new GregorianCalendar(mWorkTime.year, mWorkTime.month, mWorkTime.monthDay); // On sauvegarde le lundi pour passer facilement d'une semaine à l'autre

		long curDate = DateUtils.getDayId(calendar);
		int flexCurTime = initialTime;
		while (curDate < currentMonday) {
			// On calcule l'H.V. de la semaine et on l'ajoute au total
			flexCurTime += computeWeekFlex(curDate);
			
			// On passe à la semaine suivante
			calendar.add(Calendar.DAY_OF_MONTH, 7);
			curDate = DateUtils.getDayId(calendar);
		}
		
		// On borne le temps additionnel
		flexCurTime = Math.max(flexCurTime, PreferencesBean.instance.flexMin);
		flexCurTime = Math.min(flexCurTime, PreferencesBean.instance.flexMax);
		
		// On enregistre le temps additionnel et la date à laquelle il a été calculé
		PreferencesBean.instance.flexCurDate = currentMonday;
		PreferencesUtils.savePreference(this, PreferenceKeys.flexCurDate.getKey(), PreferencesBean.instance.flexCurDate);
		PreferencesBean.instance.flexCurTime = flexCurTime;
		PreferencesUtils.savePreference(this, PreferenceKeys.flexCurTime.getKey(), PreferencesBean.instance.flexCurTime);
		
		return flexCurTime;
	}
	
	/**
	 * Retourne le temps total effectué au cours de la semaine contenant le jour indiqué.
	 * @param aDay
	 * @return
	 */
	private int computeWeekFlex(final long aDay) {
		// On récupère le lundi correspondant au jour indiqué
		TimeUtils.parseDate(aDay, mWorkTime);
		DateUtils.getDateOfDayOfWeek(mWorkTime, Time.MONDAY, mWorkTime);
		final long firstDay = DateUtils.getDayId(mWorkTime);
		
		// On prend le dernier jour de la semaine, donc 6 jours plus tard.
		// On fait un min pour dire : si le dimanche trouvé est après ajourd'hui, alors on prend plutôt la date d'aujourd'hui.
		// On peut penser que ça ne sert à rien car aujourd'hui sera toujours le dernier jour. En réalité c'est faux car on
		// pourra pointer en avance des jours de vacances par exemple.
		DateUtils.getDateOfDayOfWeek(mWorkTime, Time.SUNDAY, mWorkTime);
		final long lastDay = Math.min(mToday, DateUtils.getDayId(mWorkTime));
		
		// On récupère les jours de la semaine
		final List<DayBean> days = new ArrayList<DayBean>();
		mDb.fetchDays(firstDay, lastDay, days);

		// On calcule le temps effectué.
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
	
	@Override
	public void onDeleteDay(final long deletedDate) {
		// Si le jour supprimé est dans la semaine actuellement affichée,
		// on rafraîchit la vue.
		for (DayBean day : mWeek) {
			if (day.date == deletedDate) {
				populateView(deletedDate);
				break;
			}
		}
	}
}