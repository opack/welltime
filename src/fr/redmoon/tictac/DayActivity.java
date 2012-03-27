package fr.redmoon.tictac;

import java.util.ArrayList;
import java.util.List;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import fr.redmoon.tictac.WeekActivity.OnDayDeletionListener;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.DayTypes;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.gui.listadapter.DayAdapter;
import fr.redmoon.tictac.gui.widgets.WidgetProvider;

public class DayActivity extends TicTacActivity implements OnDayDeletionListener {
	private int mCheckingToEdit;
	
	// Ci-dessous suivent les objets instanci�s une unique fois pour des raisons de performance.
	// On les cr�e au d�marrage de l'application et on les r�utilise avec des m�j pour �viter
	// d'autres instanciations.
	private ListView mLstCheckings;
	private final List<String[]> mCheckingsArray = new ArrayList<String[]>();
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Cr�ation de l'interface graphique
        setContentView(R.layout.view);
     		
        // Initialisation du gestionnaire de sweep
        initSweep(new int[]{R.id.list, R.id.day_details}, R.layout.day_details);
        
        // Cr�ation de l'adapteur affichant les pointages. Pour l'instant, aucun pointage.
        final ListAdapter adapter = new DayAdapter(this, R.layout.day_item, mCheckingsArray);
        mLstCheckings = (ListView)findViewById(R.id.list);
        mLstCheckings.setAdapter(adapter);
        
        // Affichage du jour courant
        mWorkDayBean.date = mToday;
        
        // Ajout de l'activit� actuelle comme �couteur de la suppression de jours
        WeekActivity.registerDayDeletionListener(this);
    }
    
    @Override
    protected void onResume() {
    	populateView(mWorkDayBean.date); 	// On passe la date et non pas juste le bean pour
											// s'assurer qu'une lecture des donn�es en base
											// sera effectu�e afin d'initialiser le bean.
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
		
		// Mise � jour du jour s�lectionn� et du menu (en fonction de l'existence du jour en base)
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
     * Ajoute un pointage � l'heure courante
     * @param btn Bouton qui a �t� cliqu�.
     */
    public void doClockin(final View btn) {
    	// Cr�ation du pointage � l'heure courante
    	TimeUtils.setToNow(mWorkTime);
    	
    	if (mToday == mWorkDayBean.date) {    	
	    	final Integer clockin = mWorkTime.hour * 100 + mWorkTime.minute;
	    	if (mWorkDayBean.checkings.contains(clockin)){
	    		// Le pointage existe d�j� : affichage d'un message � l'utilisateur
	    		Toast.makeText(this, "Impossible de pointer � " + TimeUtils.formatTime(clockin) + " car ce pointage existe d�j� !", Toast.LENGTH_SHORT).show();
	    	} else {
	    		// Ajout du pointage dans la liste des horaires
	    		mWorkDayBean.checkings.add(clockin);
	    	
		    	// Ajout ou mise � jour du jour dans la base
		    	if (mWorkDayBean.isValid) {
		    		mDb.updateDay(mWorkDayBean);
		    	} else {
		    		mDb.createDay(mWorkDayBean);
		    	}
		    	
		    	// Rafra�chissement de l'interface
		    	if (mWorkDayBean.isValid) {
		    		populateView(mWorkDayBean);
		    		
		    		// Mise � jour des widgets
					WidgetProvider.updateClockinImage(
						this,
						WidgetProvider.getAppWidgetIds(this),
						mWorkDayBean.checkings.size());
		    	} else {
		    		Toast.makeText(this, "Oups ! Le pointage n'a pas �t� enregistr�. Merci de r�essayer.", Toast.LENGTH_SHORT).show();
		    	}
	    	}
    	} else {
    		// On n'autorise les pointages que le jour courant
    		Toast.makeText(this, "D�sol�, on ne pointe que pour aujourd'hui !", Toast.LENGTH_SHORT).show();
    	}
    }
    
    /**
	 * Bascule vers l'activit� "Semaine" en affichant la semaine du jour courant.
	 * @param date
	 */
	private void showWeek() {
		switchTab(1, mWorkDayBean.date);
	}
    
    /**
     * Affiche le jour pr�c�dant le jour courant.
     * @param btn
     */
    public void showPrevious(final View btn) {
    	// On r�cup�re le jour en base
    	mDb.fetchPreviousDay(mWorkDayBean.date, mWorkDayBean);
    	
    	// Rafra�chit l'interface graphique.
    	populateView(mWorkDayBean);
    }
    
    /**
     * Affiche le jour suivant le jour courant.
     * @param btn
     */
    public void showNext(final View btn) {
    	// On r�cup�re le jour en base
    	mDb.fetchNextDay(mWorkDayBean.date, mWorkDayBean);
    	
    	// Faut-il pr�parer le jour d'aujourd'hui ? Oui, si :
    	//  - on a essay� de r�cup�rer un jour qui n'a pas �t� trouv�
    	//  - ce jour est avant aujourd'hui
    	if (!mWorkDayBean.isValid
    	&& mWorkDayBean.date < mToday) {
    		mWorkDayBean.reset();
    		mWorkDayBean.date = mToday;
    	}
    	
    	// Rafra�chit l'interface graphique.
    	populateView(mWorkDayBean);
    }
    
    /**
     * Affiche la note et en propose l'�cition
     * @param btn
     */
    public void showNote(final View btn) {
    	promptEditNote(mWorkDayBean.date, mWorkDayBean.note);
    }
    
    /**
     * Met � jour le bean de travail avec les donn�es du jour indiqu�, et rafra�chit l'interface.
     * @param day
     */
    public void populateView(final long day) {
    	// On r�cup�re le jour en base
    	mDb.fetchDay(day, mWorkDayBean);
    	
    	// Rafra�chit l'interface graphique.
    	populateView(mWorkDayBean);
    }
    
    /**
     * Rafra�chit l'interface graphique en utilisant le bean fournit en param�tre, donc sans acc�der
     * � la base de donn�es.
     * @param day
     */
    private void populateView(final DayBean day) {
    	populateCommon(day);
    	populateCheckings(day);
    	populateDetails(day);
    }
    
    /**
     * Mise � jour des composants communs � l'affichage "Pointages" et "D�tails"
     * @param day
     */
    private void populateCommon(final DayBean day) {
    	// On affiche le nom du jour
    	DateUtils.fillTime(day.date, mWorkTime);
        mWorkTime.normalize(true);
        final TextView txtCurrentDay = (TextView)findViewById(R.id.txt_current);
		txtCurrentDay.setText(mWorkTime.format(DateUtils.FORMAT_DATE_DETAILS));
		
		// Affichage du temps effectu�.
		final int dayTotal = TimeUtils.computeTotal(day);
        final TextView txtDayStats = (TextView)findViewById(R.id.txt_stats);
        txtDayStats.setText(getResources().getString(
			R.string.day_stats,
			TimeUtils.formatMinutes(dayTotal)
		));
        
        // Le bouton de pointage n'est affich� que pour aujourd'hui
        final ImageView image = (ImageView)findViewById(R.id.btn_checkin);
        if (mToday == day.date) {
        	// Activation et d�grisage de l'image
        	image.setEnabled(true);
        	image.setColorFilter(null);
        } else {
        	// D�sactivation et grisage de l'image
        	image.setEnabled(false);
    		ColorMatrix cm = new ColorMatrix();
    		cm.setSaturation(0);
    		image.setColorFilter(new ColorMatrixColorFilter(cm));
        }
        
        // On colore le fond de l'�cran avec la couleur du type de jour
        final int dayColor = PreferencesBean.getColorByDayType(day.type);
        findViewById(R.id.txt_current).setBackgroundColor(dayColor);
        
        // Masquage de l'image indiquant la pr�sence d'une note si
        // aucune note n'est disponible.
        int noteBtnVisibility = View.VISIBLE;
        if (day.note == null || day.note.isEmpty()) {
        	noteBtnVisibility = View.INVISIBLE;
        }
        findViewById(R.id.img_note).setVisibility(noteBtnVisibility);
    }
    
    /**
     * Mise � jour de la vue "Pointages"
     * @param day
     */
    private void populateCheckings(final DayBean day) {
    	// On cr�e le tableau qui est attendu par l'adapter d'affichage
    	mCheckingsArray.clear();
    	if (day.checkings != null && !day.checkings.isEmpty()) {
    		String[] inOutCheckings = null;
        	boolean isInChecking = true;
	    	for (int checking : day.checkings) {
	    		if (isInChecking) {
	    			// Cr�ation d'un nouveau tableau de 2 horaires
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
    	
        // On affiche les pointages en rafra�chissant la ListView de pointages
    	// avec son propre adapter. Ca para�t barbare, mais il y a des traitements
    	// dans le setAdapter qui sont r�alis�s pour rafra�chir la liste, et il
    	// n'y pas de m�thode "reset", "refresh" ou autre qui permette d'ex�cuter
    	// ces traitements.
    	// Note : la m�thode invalidate ne doit pas �tre utilis�e ici car elle se
    	// contente de redessiner la partie de la liste qui aurait besoin d'un
    	// nouveau draw. Typiquement, s'il y a quelques items de plus ils seront
    	// dessin�s, mais l'espace utilis� par les items existants ne sera pas
    	// rafra�chit.
    	mLstCheckings.setAdapter(mLstCheckings.getAdapter());
    }
    
    /**
     * Mise � jour de la vue "D�tails"
     * @param day
     */
    protected void populateDetails(final DayBean day) {
    	DateUtils.fillTime(day.date, mWorkTime);
		mWorkTime.normalize(true);
		
		// Calcule les temps utilis�s pour les d�tails
		final int workTime = TimeUtils.computeTotal(day, false);
		int total = workTime;
		final int lost = Math.max(0, total - PreferencesBean.instance.dayMax);
		total = workTime - lost;
	
		// Mise � jour des composants graphiques
		setText(R.id.day_type, DayTypes.values()[day.type].getLabel(this));
		setText(R.id.day_work_time, TimeUtils.formatMinutes(workTime));
		setText(R.id.day_extra_time, TimeUtils.formatTime(day.extra));
		setText(R.id.day_lost_time, TimeUtils.formatMinutes(lost));
		final EditText note = (EditText)findViewById(R.id.day_note);
		note.setText(day.note);
    }
    
    /**
	 * Affiche un message demandant la confirmation de la suppression,
	 * puis supprime le pointage indiqu� le cas �ch�ant.
	 * @param time
	 */
	private void deleteChecking(final long date, final int time) {
		// Cr�ation des �l�ments de la bo�te de dialogue
		final CharSequence message = getString(R.string.dlg_msg_confirm_checking_deletion, TimeUtils.formatTime(time));
		final DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	// Suppression du jour en base
            	mDb.deleteChecking(date, time);
            	
            	// Mise � jour de l'affichage
        		populateView(date);
            	
            	// Si on a supprim� un pointage d'aujourd'hui, on met � jour le widget
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
        
        // Affichage de la bo�te de dialogue
		showConfirmDialog(message, positiveListener, negativeListener);
	}
	
	public void promptEditExtraTime(final View btn) {
		promptEditExtraTime(mWorkDayBean.date, mWorkDayBean.extra);
	}
	
	public void promptEditDayType(final View btn) {
		promptEditDayType(mWorkDayBean.date, mWorkDayBean.type);
	}
	
	public void promptEditNote(final View btn) {
		promptEditNote(mWorkDayBean.date, mWorkDayBean.note);
	}

	@Override
	public void onDeleteDay(long date) {
		// Si le jour supprim� est le jour actuellement affich�,
		// on rafra�chit la vue.
		if (mWorkDayBean.date == date) {
			populateView(date);
		}
	}
}

