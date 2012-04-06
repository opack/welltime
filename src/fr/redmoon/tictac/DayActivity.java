package fr.redmoon.tictac;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
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
import fr.redmoon.tictac.WeekActivity.OnDayDeletionListener;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.gui.dialogs.DayDialogDelegate;
import fr.redmoon.tictac.gui.listadapter.DayAdapter;
import fr.redmoon.tictac.gui.widgets.WidgetProvider;

public class DayActivity extends TicTacActivity implements OnDayDeletionListener {
	private int mCheckingToEdit;
	private DayDialogDelegate mDialogDelegate;
	
	// Ci-dessous suivent les objets instanciés une unique fois pour des raisons de performance.
	// On les crée au démarrage de l'application et on les réutilise avec des màj pour éviter
	// d'autres instanciations.
	private ListView mLstCheckings;
	private final List<String[]> mCheckingsArray = new ArrayList<String[]>();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mDialogDelegate = new DayDialogDelegate(this);
        
        // Création de l'interface graphique
        setContentView(R.layout.view);
        
        // Initialisation du gestionnaire de sweep
        initSweep(new int[]{R.id.list, R.id.day_details}, R.layout.day_details);
        
        // Remplissage de la liste des jours dans le détail
        final Spinner spinner = (Spinner)findViewById(R.id.day_type);
 	    final ArrayAdapter<CharSequence> dayTypeAdapter = ArrayAdapter.createFromResource(this, R.array.dayTypesEntries, android.R.layout.simple_spinner_item);
 	    dayTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    spinner.setAdapter(dayTypeAdapter);
 	    spinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				updateDayType(pos);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
 	    });
        
        // Création de l'adapteur affichant les pointages. Pour l'instant, aucun pointage.
        final ListAdapter adapter = new DayAdapter(this, R.layout.day_item, mCheckingsArray);
        mLstCheckings = (ListView)findViewById(R.id.list);
        mLstCheckings.setAdapter(adapter);
        
        // Affichage du jour courant
        mWorkDayBean.date = mToday;
        
        // Ajout de l'activité actuelle comme écouteur de la suppression de jours
        WeekActivity.registerDayDeletionListener(this);
    }
    
    @Override
    protected void onResume() {
    	populateView(mWorkDayBean.date); 	// On passe la date et non pas juste le bean pour
											// s'assurer qu'une lecture des données en base
											// sera effectuée afin d'initialiser le bean.
    	super.onResume();
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
		case R.id.menu_day_show_week:
			showWeek();
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
	
	@Override
	protected Dialog onCreateDialog(final int id) {
		return mDialogDelegate.createDialog(id);
	}
	
	@Override
	protected void onPrepareDialog(final int id, final Dialog dialog, final Bundle args) {
		super.onPrepareDialog(id, dialog, args);
		mDialogDelegate.prepareDialog(id, dialog, args);
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
		    		mDb.createDay(mWorkDayBean);
		    	}
		    	
		    	// Rafraîchissement de l'interface
		    	if (mWorkDayBean.isValid) {
		    		populateView(mWorkDayBean);
		    		
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
	 * Bascule vers l'activité "Semaine" en affichant la semaine du jour courant.
	 * @param date
	 */
	private void showWeek() {
		switchTab(1, mWorkDayBean.date);
	}
    
    /**
     * Affiche le jour précédant le jour courant.
     * @param btn
     */
    public void showPrevious(final View btn) {
    	// On récupère le jour en base
    	mDb.fetchPreviousDay(mWorkDayBean.date, mWorkDayBean);
    	
    	// Rafraîchit l'interface graphique.
    	populateView(mWorkDayBean);
    }
    
    /**
     * Affiche le jour suivant le jour courant.
     * @param btn
     */
    public void showNext(final View btn) {
    	// On récupère le jour en base
    	mDb.fetchNextDay(mWorkDayBean.date, mWorkDayBean);
    	
    	// Faut-il préparer le jour d'aujourd'hui ? Oui, si :
    	//  - on a essayé de récupérer un jour qui n'a pas été trouvé
    	//  - ce jour est avant aujourd'hui
    	if (!mWorkDayBean.isValid
    	&& mWorkDayBean.date < mToday) {
    		mWorkDayBean.reset();
    		mWorkDayBean.date = mToday;
    	}
    	
    	// Rafraîchit l'interface graphique.
    	populateView(mWorkDayBean);
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
    	// On affiche le nom du jour
    	DateUtils.fillTime(day.date, mWorkTime);
        mWorkTime.normalize(true);
        final TextView txtCurrentDay = (TextView)findViewById(R.id.txt_current);
		txtCurrentDay.setText(mWorkTime.format(DateUtils.FORMAT_DATE_DETAILS));
		
		// Affichage du temps effectué.
		final int dayTotal = TimeUtils.computeTotal(day);
        final TextView txtDayStats = (TextView)findViewById(R.id.txt_stats);
        txtDayStats.setText(getResources().getString(
			R.string.day_stats,
			TimeUtils.formatMinutes(dayTotal)
		));
        
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
        
        // On colore le fond de l'écran avec la couleur du type de jour
        final int dayColor = PreferencesBean.getColorByDayType(day.type);
        findViewById(R.id.txt_current).setBackgroundColor(dayColor);
        
        // Masquage de l'image indiquant la présence d'une note si
        // aucune note n'est disponible.
        int noteBtnVisibility = View.VISIBLE;
        if (day.note == null || day.note.isEmpty()) {
        	noteBtnVisibility = View.INVISIBLE;
        }
        findViewById(R.id.img_note).setVisibility(noteBtnVisibility);
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
		final Spinner spinner = (Spinner)findViewById(R.id.day_type);
		spinner.setSelection(day.type);
		setText(R.id.day_work_time, TimeUtils.formatMinutes(workTime));
		setText(R.id.day_extra_time, TimeUtils.formatTime(day.extra));
		setText(R.id.day_lost_time, TimeUtils.formatMinutes(lost));
		final EditText note = (EditText)findViewById(R.id.day_note);
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
		final DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	// Suppression du jour en base
            	mDb.deleteChecking(date, time);
            	
            	// Mise à jour de l'affichage
        		populateView(date);
            	
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
	
	public void updateDayType(final int dayType) {
		if (mDb.updateDayType(mWorkDayBean.date, dayType)) {
			// Mise à jour de l'affichage
			populateView(mWorkDayBean.date);
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

