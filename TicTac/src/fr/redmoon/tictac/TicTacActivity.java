package fr.redmoon.tictac;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.Time;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.db.DbAdapter;
import fr.redmoon.tictac.gui.dialogs.DialogArgs;
import fr.redmoon.tictac.gui.dialogs.DialogTypes;
import fr.redmoon.tictac.gui.sweep.Direction;
import fr.redmoon.tictac.gui.sweep.ViewFlipperManager;
import fr.redmoon.tictac.gui.sweep.ViewSwitcherGestureListener;

/**
 * Gère les fonctionnalités communes à toutes les activités de l'application :
 * 	- disposer d'un accès en base
 *  - disposer d'un DayBean et d'un Time de travail
 *  - afficher des boîtes de dialogue
 */
public abstract class TicTacActivity extends Activity {
	/**
	 * Temps minimum entre deux flips de vue (changement de jour...)
	 */
	private static final int MIN_VIEW_FLIP_INTERVAL = 1000;
	
	// Objets chargés de gérer "physiquement" la bascule d'une vue à l'autre
	protected ViewFlipperManager mViewFlipperManager;
	private ViewSwitcherGestureListener mGestureListener;
	private GestureDetector mGestureDetector;
	protected View mCurrentView; // Vue actuellement affichée
	private long mLastViewFlipTime; // Heure du dernier changement de vue, pour éviter des changements trop rapprochés.
	private int[] mFlipViews;// Vues qu'on doit flipper, la seconde étant la vue de détails à inflater.
	
	protected long mToday = -1;
	protected final Time mWorkTime = new Time();
	
	protected DbAdapter mDb;
	
	protected DayBean mWorkDayBean;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Ouverture de l'accès à la base de données
        mDb = new DbAdapter(this);
        mDb.openDatabase();
        
        // Création du bean de travail
        mWorkDayBean = new DayBean();
        
        // Récupération de la date du jour
        mToday = DateUtils.getCurrentDayId(mWorkTime);
    }
	
	@Override
	protected void onDestroy() {
		mDb.closeDatabase();
		super.onDestroy();
	}
	
    public DbAdapter getDbAdapter() {
		return mDb;
	}

//  @Override
//	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
//		super.onCreateContextMenu(menu, v, menuInfo);
//		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.menu.day_contextual, menu);
//	}
//	
//	@Override
//	public boolean onContextItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case R.id.menu_checking_edit:
//			
//			return true;
//		case R.id.menu_checking_delete:
//			deleteChecking(mContextMenuDayDate);
//			return true;
//		}
//		return super.onContextItemSelected(item);
//	}
    
	/**
	 * Initialise le gestionnaire de flip.
	 * @param flipViewsIds Identifiants des vues qu'on va switcher
	 * @param layoutsToInflate Identifiants des layouts qu'on va inflater
	 */
	protected void initSweep(final int[] flipViewsIds, final int[] layoutsToInflate) {
		// Création de la vue de détail
		final ViewFlipper flipper = (ViewFlipper)findViewById(R.id.view_flipper);
		for (int layout : layoutsToInflate) {
			View details = View.inflate(this, layout, null);
			flipper.addView(details);
		}
		
    	// Création du détecteur de gestes
    	mGestureListener = new ViewSwitcherGestureListener();
    	mGestureDetector = new GestureDetector(mGestureListener);
    	
    	// Création du gestionnaire de ViewFlipper
    	mFlipViews = flipViewsIds;
    	mViewFlipperManager = new ViewFlipperManager(
    		this,
    		mFlipViews
    	);
    	
    	mLastViewFlipTime = 0;
    	mCurrentView = findViewById(flipViewsIds[0]);
    }
	
	@Override
    public boolean dispatchTouchEvent(MotionEvent event) {
    	boolean eventConsumed = false;
		final long now = System.currentTimeMillis();
		if (now - mLastViewFlipTime > MIN_VIEW_FLIP_INTERVAL) {
			mGestureListener.resetLastDirection();
			mGestureDetector.onTouchEvent(event);
			Direction lastDirection = mGestureListener.getLastDirection();
			eventConsumed = lastDirection == Direction.left || lastDirection == Direction.right; 
			if (eventConsumed) {
				switchDetailsView(lastDirection);
				mLastViewFlipTime = now;
				return true;
			}
		}
		// Si on n'a pas géré l'évènement, on le laisse continuer son chemin
		super.dispatchTouchEvent(event);
		return true;
    };
    
    /**
	 * Gère le changement de vue suite à un sweep. On affiche soit le détail
	 * des pointages, soit celui du jour.
	 * @param lastDirection
	 * @param viewId
	 */
	final protected void switchDetailsView(final Direction lastDirection, final int viewId) {
		if (viewId > 0) {
			// Une vue est spécifiée : on va switchée vers celle-là
			mCurrentView = findViewById(viewId);
		} else {
			// Aucune vue spécifiée : on switch vers l'autre
			switch (lastDirection) {
				// Qu'on glisse vers la gauche ou la droite, on va switcher
				// entre les détails et les pointages
				case left:
				case right:
					pointToNextDisplay();
					break;
				default:
					return;
			}
		}
		
		// Affichage des données
		populateView(mWorkDayBean.date);
		mViewFlipperManager.flipView(lastDirection, mCurrentView.getId());
	}
	
	final protected void switchDetailsView(final Direction lastDirection) {
		switch (lastDirection) {
			// Qu'on glisse vers la gauche ou la droite, on va switcher
			// entre les détails et les pointages
			case left:
			case right:
				pointToNextDisplay();
				break;
			default:
				return;
		}
		
		// Affichage des données
		populateView(mWorkDayBean.date);
		mViewFlipperManager.flipView(lastDirection, mCurrentView.getId());
	}
	
	final private void pointToNextDisplay() {
		if (mCurrentView.getId() == mFlipViews[0]) {
			mCurrentView = findViewById(mFlipViews[1]);
		} else {
			mCurrentView = findViewById(mFlipViews[0]);
		}
	}
	
    /**
     * Met à jour le bean de travail avec les données du jour indiqué, et rafraîchit l'interface.
     * @param day
     */
    public abstract void populateView(final long day);
    
	protected void switchTab(final int tabIndexToSwitchTo, final long date){
        switchTab(tabIndexToSwitchTo, date, 0);
	}
	
	/**
	 * Modifie l'onglet actuellement affiché
	 * @param indexTabToSwitchTo
	 */
	protected void switchTab(final int tabIndexToSwitchTo, final long date, final int viewId){
        MainActivity mainActivity = (MainActivity)this.getParent();
        mainActivity.switchTab(tabIndexToSwitchTo, date, viewId);
	}

	/**
	 * Propose une boîte de dialogue de saisie de temps et met à jour le temps additionnel
	 * du jour sélectionné
	 * @param activity
	 * @param db
	 * @param day
	 */
	protected void promptEditExtraTime(final long date, final int extra) {
		final Bundle args = new Bundle();
		args.putLong(DialogArgs.DATE, date);
		args.putInt(DialogArgs.TIME, extra);
		showDialog(DialogTypes.TIMEPICKER_EDIT_EXTRA, args);
	}
	
	/**
	 * Propose une boîte de dialogue de saisie de temps et met à jour le pointage indiqué
	 * du jour sélectionné
	 * @param activity
	 * @param db
	 * @param day
	 */
	protected void promptEditChecking(final long date, final int checkingToEdit) {
		final Bundle args = new Bundle();
		args.putLong(DialogArgs.DATE, date);
		args.putInt(DialogArgs.TIME, checkingToEdit);
		showDialog(DialogTypes.TIMEPICKER_EDIT_CHECKING, args);
	}
	
	/**
	 * Propose une boîte de dialogue de saisie de date pour créer un nouveau
	 * jour à la date sélectionnée
	 */
	protected void promptAddDay() {
		final Bundle args = new Bundle();
		args.putLong(DialogArgs.DATE, mToday);
		showDialog(DialogTypes.DATEPICKER_ADD_DAY, args);
	}
	
	/**
	 * Propose une boîte de dialogue de saisie de date pour afficher
	 * les informations (pointages ou semaine) de la date sélectionnée
	 */
	protected void promptShowDay() {
		final Bundle args = new Bundle();
		args.putLong(DialogArgs.DATE, mToday);
		showDialog(DialogTypes.DATEPICKER_SHOW_DAY, args);
	}
	
	/**
	 * Propose une boîte de dialogue de saisie de temps pour créer un nouveau
	 * pointage à la date sélectionnée
	 * @param date 
	 */
	protected void promptAddChecking(final long date) {
		final Bundle args = new Bundle();
		args.putLong(DialogArgs.DATE, date);
		args.putInt(DialogArgs.TIME, 0);// Par défaut, on met l'heure à 00:00
		showDialog(DialogTypes.TIMEPICKER_ADD_CHECKING, args);
	}
	
	/**
	 * Propose une boîte de dialogue de saisie de texte et met à jour la note du jour
	 * sélectionné
	 * @param activity
	 * @param db
	 * @param day
	 */
	protected void promptEditNote(final long date, final String initialNote) {
		final Bundle args = new Bundle();
		args.putLong(DialogArgs.DATE, date);
		args.putString(DialogArgs.NOTE, initialNote);
		showDialog(DialogTypes.TEXTINPUT_EDIT_NOTE, args);
	}
	
	/**
	 * Affiche une boîte de dialogue avec un bouton "oui" et un bouton "non".
	 */
	protected void showConfirmDialog(final CharSequence message, final DialogInterface.OnClickListener positiveListener, final DialogInterface.OnClickListener negativeListener) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		// Définition des paramètres généraux
		builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle(R.string.app_name);
        builder.setMessage(message);
        
        // Définition des boutons
        builder.setPositiveButton(R.string.btn_yes, positiveListener);
        builder.setNegativeButton(R.string.btn_no, negativeListener);
        
        // Affichage
        builder.show();
	}
	
	/**
	 * Met à jour un TextView avec une string complétée avec les valeurs
	 * de la variable texts.
	 * @param viewId
	 * @param stringId Identifiant d'une chaine possédant des paramètres
	 * @param texts
	 */
	protected void setText(final int viewId, final int stringId, final Object... texts) {
		setText(viewId, getString(stringId, texts));
	}
	
	/**
	 * Met à jour un TextView avec la chaine passée en paramètre.
	 * @param viewId
	 * @param text
	 */
	protected void setText(final int viewId, final String text) {
		final TextView label = (TextView)findViewById(viewId);
		label.setText(text);
	}
	
	public long getToday() {
		return mToday;
	}

	/**
     * Mise à jour des composants communs à l'affichage "Pointages" et "Détails"
     * @param day
     */
	protected void populateCommon(final CharSequence strCurrent, final int total, final int max) {
		// Affichage de la période de la semaine
        final TextView txtCurrent = (TextView)findViewById(R.id.txt_current);
        txtCurrent.setText(strCurrent);
        
        // Affichage du temps effectué et du temps restant
        final TextView txtTotal = (TextView)findViewById(R.id.txt_total);
        txtTotal.setText(TimeUtils.formatMinutes(total));
        
        final int remaining = total - max;
		final TextView txtRemaining = (TextView)findViewById(R.id.txt_remaining);
		txtRemaining.setText(TimeUtils.formatMinutesWithSign(remaining));
		if (remaining < 0) {
			// Il reste du temps à faire : on écrit en rouge
			txtRemaining.setTextColor(Color.RED);
		} else if (remaining == 0) {
			// On a fait pile le temps demandé : on écrit en noir
			txtRemaining.setTextColor(Color.BLACK);
		} else {
			// Du temps HV a été cumulé : on écrit en vert
			txtRemaining.setTextColor(Color.GREEN);
		}
        
        // Mise à jour de la barre de progression
        final ProgressBar barTotal = (ProgressBar)findViewById(R.id.bar_total);
        barTotal.setMax(max);
        // Lorsqu'on gèrera les objectifs, le max devra être adapté pour refléter l'objectif
        // et le secondary indiquera le weekMin. Pour l'instant on considère donc que l'objectif
        // est le dayMin.
        //barTotal.setSecondaryProgress(PreferencesBean.instance.weekMin);
        barTotal.setProgress(total);
	}
}

