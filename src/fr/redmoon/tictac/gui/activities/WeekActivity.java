package fr.redmoon.tictac.gui.activities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.FlexUtils;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.bus.bean.WeekBean;
import fr.redmoon.tictac.db.DbAdapter;
import fr.redmoon.tictac.gui.ViewSynchronizer;
import fr.redmoon.tictac.gui.activities.TicTacActivity.OnDayDeletionListener;
import fr.redmoon.tictac.gui.adapters.WeekAdapter;
import fr.redmoon.tictac.gui.adapters.WeekAdapterEntry;
import fr.redmoon.tictac.gui.dialogs.DialogArgs;
import fr.redmoon.tictac.gui.dialogs.EditFlexTimeFragment;
import fr.redmoon.tictac.gui.quickactions.ActionItem;
import fr.redmoon.tictac.gui.quickactions.QuickAction;

public class WeekActivity extends TicTacActivity implements OnDayDeletionListener {
	public static final int PAGE_DAYS = 0;
	public static final int PAGE_DETAILS = 1;
	
	// Quick Action IDs
	private static final int QAID_SHOW_CHECKINGS = 0;
	private static final int QAID_SHOW_DETAILS = 1;
	private static final int QAID_DELETE_DAY = 2;

	private List<DayBean> mWeekDays;
	private WeekBean mWeekData;
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
    	
        // Création des beans de travail
        mWeekDays = new ArrayList<DayBean>();
        mWeekData = new WeekBean();
        
        // Initialisation de l'affichage
        setContentView(R.layout.view_common_frame);
        findViewById(R.id.btn_checkin).setVisibility(View.INVISIBLE);
        
        // Initialisation du gestionnaire de pages
        final View pageDays = View.inflate(this, R.layout.view_week_days, null);
        final View pageDetails = View.inflate(this, R.layout.view_week_details, null);
        initPages(getResources().getStringArray(R.array.week_page_titles), pageDays, pageDetails);
        
        // Création de l'adapteur affichant les jours. Pour l'instant aucun jour.
        final ListAdapter adapter = new WeekAdapter(this, R.layout.itm_week_day, mDaysArray);
        mLstDays = (ListView)pageDays.findViewById(R.id.list);
        mLstDays.setAdapter(adapter);
        
        // Création des QuickActions
    	ActionItem showCheckingsItem = new ActionItem(QAID_SHOW_CHECKINGS, getString(R.string.menu_day_show_checkings), getResources().getDrawable(android.R.drawable.ic_menu_recent_history));
		ActionItem showDetailsItem = new ActionItem(QAID_SHOW_DETAILS, getString(R.string.menu_day_show_details), getResources().getDrawable(android.R.drawable.ic_menu_info_details));
		ActionItem deleteDayItem = new ActionItem(QAID_DELETE_DAY, getString(R.string.menu_day_delete), getResources().getDrawable(android.R.drawable.ic_menu_delete));
		
		final QuickAction mQuickAction 	= new QuickAction(this);
		mQuickAction.addActionItem(showCheckingsItem);
		mQuickAction.addActionItem(showDetailsItem);
		mQuickAction.addActionItem(deleteDayItem);
		
		mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
			@Override
			public void onItemClick(QuickAction quickAction, int pos, int actionId) {
				final DayBean day = mWeekDays.get(mSelectedDay);
				switch (actionId) {
				case QAID_SHOW_CHECKINGS:
					// Sauvegarde du jour affiché pour synchroniser les vues
					ViewSynchronizer.getInstance().setCurrentDay(day.date);
					
					// Modification de l'onglet courant
					switchTab(MainActivity.TAB_DAY_POS, day.date, DayActivity.PAGE_CHECKINGS);
					break;
				case QAID_SHOW_DETAILS:
					// Sauvegarde du jour affiché pour synchroniser les vues
					ViewSynchronizer.getInstance().setCurrentDay(day.date);
					
					// Modification de l'onglet courant
					switchTab(MainActivity.TAB_DAY_POS, day.date, DayActivity.PAGE_DETAILS);
					break;
				case QAID_DELETE_DAY:
					deleteDay(day.date);
					break;
				}
			}
		});
		
		mLstDays.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// Mise à jour du jour sélectionné et du menu (en fonction de l'existence du jour en base)
				mSelectedDay = (Integer)view.getTag();
				
				// Si le jour n'existe pas en base, on masque certaines options
				final DayBean day = mWeekDays.get(mSelectedDay);
				final boolean isDayExisting = DbAdapter.getInstance().isDayExisting(day.date);
				mQuickAction.setActionVisible(QAID_SHOW_CHECKINGS, isDayExisting);
				mQuickAction.setActionVisible(QAID_DELETE_DAY, isDayExisting);
				
				mQuickAction.show(view);
			}
		});
        
        // Affichage du jour courant
        mMonday = mToday; 	// On passe la date et non pas juste le bean pour
        					// s'assurer qu'une lecture des données en base
        					// sera effectuée afin d'initialiser le bean.
        
        // On veut être informé si la vue "Mois" supprime un jour
        TicTacActivity.registerDayDeletionListener(this);
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
    
	public void showPrevious(final View btn) {
		// On se place sur le lundi de la semaine, et on recule d'un jour
    	mWorkCal.set(DateUtils.extractYear(mMonday), DateUtils.extractMonth(mMonday), DateUtils.extractDayOfMonth(mMonday));
    	mWorkCal.add(Calendar.DAY_OF_YEAR, -7);
    	
    	// Maintenant on affiche la semaine de ce jour
    	populateView(DateUtils.getDayId(mWorkCal));
    	
    	// Sauvegarde du jour courant dans le synchroniseur de vues pour accorder
    	// toutes les vues sur le même jour
    	// On fait bien ça après le popuplateView car mMonday y aura été mis à jour
    	// avec le nouveau lundi.
    	ViewSynchronizer.getInstance().setCurrentDay(mMonday);
    }
    
    public void showNext(final View btn) {
    	// On se place sur le dimanche de la semaine, et on avance d'un jour
    	mWorkCal.set(DateUtils.extractYear(mSunday), DateUtils.extractMonth(mSunday), DateUtils.extractDayOfMonth(mSunday));
    	mWorkCal.add(Calendar.DAY_OF_YEAR, 1);
    	
    	// Maintenant on affiche la semaine de ce jour
    	populateView(DateUtils.getDayId(mWorkCal));
    	

    	// Sauvegarde du jour courant dans le synchroniseur de vues pour accorder
    	// toutes les vues sur le même jour.
    	// On fait bien ça après le popuplateView car mMonday y aura été mis à jour
    	// avec le nouveau lundi.
    	ViewSynchronizer.getInstance().setCurrentDay(mMonday);
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
    	DbAdapter.getInstance().fetchDays(mMonday, mSunday, mWeekDays);

    	// On conserve le jour de travail (notamment pour le sweep)
    	final Set<Long> daysById = new HashSet<Long>();
    	for (DayBean day : mWeekDays) {
    		daysById.add(day.date);
    		
    		if (day.date == date) {
    			mWorkDayBean = day;
    		}
    	}
    	
    	// Ajout de jours permettant de remplir les trous de la semaine
    	mWorkCal.set(monday.year, monday.month, monday.monthDay);
    	long dayId;
    	for (int curDay = 0; curDay < FlexUtils.NB_DAYS_IN_WEEK; curDay++) {
    		// Si le jour n'est pas en base, on en crée un faux
    		dayId = DateUtils.getDayId(mWorkCal);
    		if (!daysById.contains(dayId)) {
    			// Le nouveau jour créé sera noté comme non valide et de type
    			// "non travaillé".
    			DayBean fillin = new DayBean();
    			fillin.date = dayId;
    			mWeekDays.add(curDay, fillin);
    			
    			if (dayId == date) {
        			mWorkDayBean = fillin;
        		}
    		}
    		
    		// On passe au jour suivant
    		mWorkCal.add(Calendar.DAY_OF_YEAR, 1);
    	}
    	
    	// Rafraîchit l'interface graphique.
    	// populateCommon est appelée après populateDays car populateDays 
    	// parcours la liste des jours et initialise des variables de travail 
    	// qui nous permettront de ne pas parcourir cette liste deux fois.
    	populateDays(monday, sunday, mWeekDays);
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
            dayInfos.morningDayType = day.typeMorning;
            dayInfos.afternoonDayType = day.typeAfternoon;
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
		if (mWeekDays.isEmpty()) {
			return;
		}
		
		final DayBean firstDay = mWeekDays.get(0);
		DateUtils.fillTime(firstDay.date, mWorkTime);
		mWorkTime.normalize(true);
		
		// Calcule le temps ecrêté
		final int lost = Math.max(0, mWeekWorked - PreferencesBean.instance.weekMax);
		
		// Récupération du temps HV en début de semaine
		DbAdapter.getInstance().fetchLastFlexTime(mMonday, mWeekData);
		
		// Calcul du nouvel HV en ajoutant le temps effectué cette semaine
		// à l'HV en début de semaine
		final FlexUtils flexUtils = new FlexUtils();
		final int curWeekFlex = flexUtils.computeFlexTime(mWeekWorked, mWeekData.flexTime);
		
        // Mise à jour des composants graphiques
		final View pageDetails = getPage(PAGE_DETAILS);
		setText(pageDetails, R.id.btn_monday_flex_time, R.string.week_monday_flex_time, DateUtils.formatDateDDMM(mWeekData.date));
		setText(pageDetails, R.id.txt_monday_flex_time, TimeUtils.formatMinutes(mWeekData.flexTime));
		setText(pageDetails, R.id.txt_week_current_flex_time, TimeUtils.formatMinutes(curWeekFlex));
		setText(pageDetails, R.id.txt_week_work_time, TimeUtils.formatMinutes(mWeekWorked));
		setText(pageDetails, R.id.txt_week_lost_time, TimeUtils.formatMinutes(lost));
	}
    
    public void updateFlexTime(final View btn) {
    	final Bundle args = new Bundle();
		args.putLong(DialogArgs.DATE.name(), mMonday);
		args.putInt(DialogArgs.TIME.name(), mWeekData.flexTime);

		final DialogFragment newFragment = new EditFlexTimeFragment();
		newFragment.setArguments(args);
	    newFragment.show(getSupportFragmentManager(), EditFlexTimeFragment.TAG);
    }

	@Override
	public void onDeleteDay(final long deletedDate) {
		// Si le jour supprimé est dans la semaine actuellement affichée,
		// on rafraîchit la vue.
		for (DayBean day : mWeekDays) {
			if (day.date == deletedDate) {
				populateView(deletedDate);
				break;
			}
		}
	}
}