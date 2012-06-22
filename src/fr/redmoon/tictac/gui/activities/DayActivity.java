package fr.redmoon.tictac.gui.activities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.R.array;
import fr.redmoon.tictac.R.color;
import fr.redmoon.tictac.R.id;
import fr.redmoon.tictac.R.layout;
import fr.redmoon.tictac.R.menu;
import fr.redmoon.tictac.R.string;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.DayTypes;
import fr.redmoon.tictac.bus.FlexUtils;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.bus.export.CalendarAccess;
import fr.redmoon.tictac.gui.DayBiColorDrawableHelper;
import fr.redmoon.tictac.gui.ViewSynchronizer;
import fr.redmoon.tictac.gui.activities.TicTacActivity.OnDayDeletionListener;
import fr.redmoon.tictac.gui.dialogs.DayDialogDelegate;
import fr.redmoon.tictac.gui.listadapter.DayAdapter;
import fr.redmoon.tictac.gui.widgets.WidgetProvider;

public class DayActivity extends TicTacActivity implements OnDayDeletionListener {
	public static final int PAGE_CHECKINGS = 0;
	public static final int PAGE_DETAILS = 1;

	private int mCheckingToEdit;
	
	// Ci-dessous suivent les objets instanciés une unique fois pour des raisons de performance.
	// On les crée au démarrage de l'application et on les réutilise avec des màj pour éviter
	// d'autres instanciations.
	private ListView mLstCheckings;
	private final List<String[]> mCheckingsArray = new ArrayList<String[]>();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setDialogDelegate(new DayDialogDelegate(this));
        
        // Création de l'interface graphique
        setContentView(R.layout.view_common_frame);
        
        // Initialisation du gestionnaire de pages
        final View pageCheckings = View.inflate(this, R.layout.view_day_checkings, null);
        final View pageDetails = View.inflate(this, R.layout.view_day_details, null);
        initPages(pageCheckings, pageDetails);
        
        // Remplissage de la liste des jours dans le détail
        Spinner spinner = (Spinner)pageDetails.findViewById(R.id.day_morning_type);
 	    ArrayAdapter<CharSequence> dayTypeAdapter = ArrayAdapter.createFromResource(this, R.array.dayTypesEntries, android.R.layout.simple_spinner_item);
 	    dayTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    spinner.setAdapter(dayTypeAdapter);
 	    spinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				updateMorningDayType(pos);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
 	    });
 	    
 	    spinner = (Spinner)pageDetails.findViewById(R.id.day_afternoon_type);
	    dayTypeAdapter = ArrayAdapter.createFromResource(this, R.array.dayTypesEntries, android.R.layout.simple_spinner_item);
	    dayTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner.setAdapter(dayTypeAdapter);
	    spinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				updateAfternoonDayType(pos);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
	    });
        
        // Création de l'adapteur affichant les pointages. Pour l'instant, aucun pointage.
        final ListAdapter adapter = new DayAdapter(this, R.layout.itm_day_checking, mCheckingsArray);
        mLstCheckings = (ListView)pageCheckings.findViewById(R.id.list);
        mLstCheckings.setAdapter(adapter);
        final View emptyView = pageCheckings.findViewById(R.id.no_checkings);
    	mLstCheckings.setEmptyView(emptyView);
    	
        // Affichage du jour courant
        mWorkDayBean.date = mToday;
        
        // On veut être informé si la vue "Semaine" ou "Mois" supprime un jour
        TicTacActivity.registerDayDeletionListener(this);
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.day_main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_day_add_checking:
			promptAddChecking(mWorkDayBean.date);
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
		inflater.inflate(R.menu.day_contextual, menu);
		
		// Mise à jour du jour sélectionné et du menu (en fonction de l'existence du jour en base)
		mCheckingToEdit = (Integer)v.getTag();
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_checking_edit:
	    	promptEditChecking(mWorkDayBean.date, mCheckingToEdit);
			return true;
		case R.id.menu_checking_delete:
			deleteChecking(mWorkDayBean.date, mCheckingToEdit);
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
    /**
     * Ajoute un pointage à l'heure courante
     * @param btn Bouton qui a été cliqué.
     */
    public void doClockin(final View btn) {
    	// Création du pointage à l'heure courante
    	TimeUtils.setToNow(mWorkTime);
    	
    	if (mToday == mWorkDayBean.date) {    	
	    	final Integer clockin = mWorkTime.hour * 100 + mWorkTime.minute;
	    	if (mWorkDayBean.checkings.contains(clockin)){
	    		// Le pointage existe déjà : affichage d'un message à l'utilisateur
	    		Toast.makeText(this, "Impossible de pointer à " + TimeUtils.formatTime(clockin) + " car ce pointage existe déjà !", Toast.LENGTH_SHORT).show();
	    	} else {
	    		// Ajout du pointage dans la liste des horaires
	    		mWorkDayBean.checkings.add(clockin);
	    	
		    	// Ajout ou mise à jour du jour dans la base
		    	if (mWorkDayBean.isValid) {
		    		mDb.updateDay(mWorkDayBean);
		    	} else {
		    		// Le jour sera créé. On vient d'ajouter un pointage, donc c'est
		    		// un jour de type "normal"
		    		mWorkDayBean.typeMorning = DayTypes.normal.ordinal();
		    		mWorkDayBean.typeAfternoon = DayTypes.normal.ordinal();
		    		
		    		mDb.createDay(mWorkDayBean);
		    	}
		    	// Mise à jour de l'HV.
		    	final FlexUtils flexUtils = new FlexUtils(mDb);
		    	flexUtils.updateFlex(mWorkDayBean.date);
		    	
		    	// Rafraîchissement de l'interface
		    	if (mWorkDayBean.isValid) {
		    		populateView(mWorkDayBean);
		    		
		    		// Ajout du pointage dans le calendrier
					CalendarAccess.getInstance().createWorkingEvents(mWorkDayBean.date, mWorkDayBean.checkings);
		    		
		    		// Mise à jour des widgets
					WidgetProvider.updateClockinImage(
						this,
						WidgetProvider.getAppWidgetIds(this),
						mWorkDayBean.checkings.size());
		    	} else {
		    		Toast.makeText(this, "Oups ! Le pointage n'a pas été enregistré. Merci de réessayer.", Toast.LENGTH_SHORT).show();
		    	}
	    	}
    	} else {
    		// On n'autorise les pointages que le jour courant
    		Toast.makeText(this, "Désolé, on ne pointe que pour aujourd'hui !", Toast.LENGTH_SHORT).show();
    	}
    }
    
    /**
     * Affiche le jour précédant le jour courant.
     * @param btn
     */
    public void showPrevious(final View btn) {
    	// On se place sur le dimanche de la semaine, et on avance d'un jour
    	mWorkCal.set(DateUtils.extractYear(mWorkDayBean.date), DateUtils.extractMonth(mWorkDayBean.date), DateUtils.extractDayOfMonth(mWorkDayBean.date));
    	final int dayOfWeek = mWorkCal.get(Calendar.DAY_OF_WEEK);
    	if (Calendar.TUESDAY <= dayOfWeek && dayOfWeek <= Calendar.FRIDAY) {
    		// Si le jour est entre mardi et vendredi, on peut reculer d'un jour : on sera toujours
    		// sur un jour de semaine travaillé (lundi - vendredi)
    		mWorkCal.add(Calendar.DAY_OF_YEAR, -1);
    	} else {
    		// On est dimanche ou lundi, et on ne peut pas avancer d'un seul jour sinon on tombe le
    		// week-end. On va donc se placer sur le vendredi puis reculer le 7 jours
    		mWorkCal.add(Calendar.DAY_OF_YEAR, Calendar.FRIDAY - dayOfWeek - 7);
    	}
    	
    	// Maintenant on affiche la semaine de ce jour
    	final long dayId = DateUtils.getDayId(mWorkCal);
    	populateView(dayId);
    	
    	// Sauvegarde du jour courant dans le synchroniseur de vues pour accorder
    	// toutes les vues sur le même jour
    	ViewSynchronizer.getInstance().setCurrentDay(dayId);
    }
    
    /**
     * Affiche le jour suivant le jour courant.
     * @param btn
     */
    public void showNext(final View btn) {
    	// On se place sur le jour courant, et on avance d'un jour
    	mWorkCal.set(DateUtils.extractYear(mWorkDayBean.date), DateUtils.extractMonth(mWorkDayBean.date), DateUtils.extractDayOfMonth(mWorkDayBean.date));
    	final int dayOfWeek = mWorkCal.get(Calendar.DAY_OF_WEEK);
    	if (Calendar.SUNDAY <= dayOfWeek && dayOfWeek <= Calendar.THURSDAY) {
    		// Si le jour est entre dimanche et jeudi, on peut avancer d'un jour : on sera toujours
    		// sur un jour de semaine travaillé (lundi - vendredi)
    		mWorkCal.add(Calendar.DAY_OF_YEAR, 1);
    	} else {
    		// On est vendredi ou samedi, et on ne peut pas avancer d'un seul jour sinon on tombe le
    		// week-end. On va donc avancer d'assez de jours pour tomber sur le lundi.
    		mWorkCal.add(Calendar.DAY_OF_YEAR, 9 - dayOfWeek);
    	}
    	
    	// Maintenant on affiche la semaine de ce jour
    	final long dayId = DateUtils.getDayId(mWorkCal);
    	populateView(DateUtils.getDayId(mWorkCal));
    	
    	// Sauvegarde du jour courant dans le synchroniseur de vues pour accorder
    	// toutes les vues sur le même jour
    	ViewSynchronizer.getInstance().setCurrentDay(dayId);
    }
    
    /**
     * Met à jour le bean de travail avec les données du jour indiqué, et rafraîchit l'interface.
     * @param day
     */
    public void populateView(final long day) {
    	// On récupère le jour en base
    	mDb.fetchDay(day, mWorkDayBean);
    	
    	// Rafraîchit l'interface graphique.
    	populateView(mWorkDayBean);
    }
    
    /**
     * Rafraîchit l'interface graphique en utilisant le bean fournit en paramètre, donc sans accéder
     * à la base de données.
     * @param day
     */
    private void populateView(final DayBean day) {
    	populateCommon(day);
		populateCheckings(day);
		populateDetails(day);
    }
    
    /**
     * Mise à jour des composants communs à l'affichage "Pointages" et "Détails"
     * @param day
     */
    private void populateCommon(final DayBean day) {
    	// Affichage du jour courant, du temps effectué et du temps restant
    	DateUtils.fillTime(day.date, mWorkTime);
        mWorkTime.normalize(true);
        final String strCurrent = mWorkTime.format(DateUtils.FORMAT_DATE_DETAILS);
		final int total = TimeUtils.computeTotal(day);
        populateCommon(
        		strCurrent,
        		total,
        		PreferencesBean.instance.dayMin);
        final TextView txtCurrent = (TextView)findViewById(R.id.txt_current);
        if (day.note != null && day.note.length() != 0) {
        	txtCurrent.setTextColor(getResources().getColor(R.color.calendar_day_with_note));
        } else {
        	txtCurrent.setTextColor(Color.BLACK);
        }
        
        // Le bouton de pointage n'est affiché que pour aujourd'hui
        final ImageView image = (ImageView)findViewById(R.id.btn_checkin);
        if (mToday == day.date) {
        	// Activation et dégrisage de l'image
        	image.setEnabled(true);
        	image.setColorFilter(null);
        } else {
        	// Désactivation et grisage de l'image
        	image.setEnabled(false);
    		ColorMatrix cm = new ColorMatrix();
    		cm.setSaturation(0);
    		image.setColorFilter(new ColorMatrixColorFilter(cm));
        }
        
        // On colore le fond de la date avec la couleur du type de jour
        final Drawable background = DayBiColorDrawableHelper.getInstance().getDrawableForDayTypes(day.typeMorning, day.typeAfternoon);
        findViewById(R.id.txt_current).setBackgroundDrawable(background);
    }
    
    /**
     * Mise à jour de la vue "Pointages"
     * @param day
     */
    private void populateCheckings(final DayBean day) {
    	// On crée le tableau qui est attendu par l'adapter d'affichage
    	mCheckingsArray.clear();
    	if (day.checkings != null && !day.checkings.isEmpty()) {
    		String[] inOutCheckings = null;
        	boolean isInChecking = true;
	    	for (int checking : day.checkings) {
	    		if (isInChecking) {
	    			// Création d'un nouveau tableau de 2 horaires
	    			inOutCheckings = new String[2];
	    			mCheckingsArray.add(inOutCheckings);
	    			
	    			// Remplissage du premier pointage
	    			inOutCheckings[0] = TimeUtils.formatTime(checking);
	    		} else {
	    			// Remplissage du second pointage.
	    			inOutCheckings[1] = TimeUtils.formatTime(checking);
	    		}
	    		isInChecking = !isInChecking;
	    	}
    	}
    	
        // On affiche les pointages en rafraîchissant la ListView de pointages
    	// avec son propre adapter. Ca paraît barbare, mais il y a des traitements
    	// dans le setAdapter qui sont réalisés pour rafraîchir la liste, et il
    	// n'y pas de méthode "reset", "refresh" ou autre qui permette d'exécuter
    	// ces traitements.
    	// Note : la méthode invalidate ne doit pas être utilisée ici car elle se
    	// contente de redessiner la partie de la liste qui aurait besoin d'un
    	// nouveau draw. Typiquement, s'il y a quelques items de plus ils seront
    	// dessinés, mais l'espace utilisé par les items existants ne sera pas
    	// rafraîchit.
    	mLstCheckings.setAdapter(mLstCheckings.getAdapter());
    }
    
    /**
     * Mise à jour de la vue "Détails"
     * @param day
     */
    protected void populateDetails(final DayBean day) {
    	DateUtils.fillTime(day.date, mWorkTime);
		mWorkTime.normalize(true);
		
		// Calcule les temps utilisés pour les détails
		final int workTime = TimeUtils.computeTotal(day, false);
		int total = workTime;
		final int lost = Math.max(0, total - PreferencesBean.instance.dayMax);
		total = workTime - lost;
	
		// Mise à jour des composants graphiques
		final View pageDetails = getPage(PAGE_DETAILS);
		final Spinner spnMorning = (Spinner)pageDetails.findViewById(R.id.day_morning_type);
		spnMorning.setSelection(day.typeMorning);
		final Spinner spnAfternoon = (Spinner)pageDetails.findViewById(R.id.day_afternoon_type);
		spnAfternoon.setSelection(day.typeAfternoon);
		setText(pageDetails, R.id.day_work_time, TimeUtils.formatMinutes(workTime));
		setText(pageDetails, R.id.day_extra_time, TimeUtils.formatTime(day.extra));
		setText(pageDetails, R.id.day_lost_time, TimeUtils.formatMinutes(lost));
		final EditText note = (EditText)pageDetails.findViewById(R.id.day_note);
		note.setText(day.note);
    }
    
    /**
	 * Affiche un message demandant la confirmation de la suppression,
	 * puis supprime le pointage indiqué le cas échéant.
	 * @param time
	 */
	private void deleteChecking(final long date, final int time) {
		// Création des éléments de la boîte de dialogue
		final CharSequence message = getString(R.string.dlg_msg_confirm_checking_deletion, TimeUtils.formatTime(time));
		final DayBean workBean = mWorkDayBean;
		final DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	// Suppression du jour en base
            	mDb.deleteChecking(date, time);
            	
            	// Mise à jour de l'HV.
				final FlexUtils flexUtils = new FlexUtils(mDb);
				flexUtils.updateFlex(date);
            	
            	// Mise à jour de l'affichage
        		populateView(date);
        		
        		// Suppression du pointage dans le calendrier
				CalendarAccess.getInstance().createWorkingEvents(workBean.date, workBean.checkings);
            	
            	// Si on a supprimé un pointage d'aujourd'hui, on met à jour le widget
            	if (mToday == date) {
            		WidgetProvider.updateClockinImage(DayActivity.this);
            	}
            }
        };
        final DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
        	@Override
    		public void onClick(DialogInterface dialog, int id) {
            	dialog.cancel();
        	}
        };
        
        // Affichage de la boîte de dialogue
		showConfirmDialog(message, positiveListener, negativeListener);
	}
	
	public void promptEditExtraTime(final View btn) {
		promptEditExtraTime(mWorkDayBean.date, mWorkDayBean.extra);
	}
	
	public void updateMorningDayType(final int type) {
		updateDayType(type, mWorkDayBean.typeAfternoon);
	}
	
	public void updateAfternoonDayType(final int type) {
		updateDayType(mWorkDayBean.typeMorning, type);
	}
	
	public void updateDayType(final int typeMorning, final int typeAfternoon) {
		// On teste si les types sont bien différents car lors de l'initialisation du spinner cette
		// méthode sera appelée comme si l'utilisateur avait choisit une valeur. Or dans ce cas
		// on ne veut pas créer un jour car le type de jour n'aura pas bougé.
		if (typeAfternoon != mWorkDayBean.typeAfternoon || typeMorning != mWorkDayBean.typeMorning) {
			if (mDb.updateDayType(mWorkDayBean.date, typeMorning, typeAfternoon)) {
				// Mise à jour de l'HV.
				final FlexUtils flexUtils = new FlexUtils(mDb);
		    	flexUtils.updateFlex(mWorkDayBean.date);
				
				// Mise à jour de l'affichage
				populateView(mWorkDayBean.date);
			}
		}
	}
	
	public void promptEditNote(final View btn) {
		promptEditNote(mWorkDayBean.date, mWorkDayBean.note);
	}

	@Override
	public void onDeleteDay(long date) {
		// Si le jour supprimé est le jour actuellement affiché,
		// on rafraîchit la vue.
		if (mWorkDayBean.date == date) {
			populateView(date);
		}
	}
}

