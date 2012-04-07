package fr.redmoon.tictac;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

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
import fr.redmoon.tictac.gui.widgets.WidgetProvider;

/**
 * G�re les fonctionnalit�s communes � toutes les activit�s de l'application :
 * 	- disposer d'un acc�s en base
 *  - disposer d'un DayBean et d'un Time de travail
 *  - afficher des bo�tes de dialogue
 */
public abstract class TicTacActivity extends Activity {
	
	public interface OnDayDeletionListener{
		/**
		 * Appel�e lorsqu'un jour a �t� supprim�
		 * @param date
		 */
		void onDeleteDay(long date);
	}
	private static List<OnDayDeletionListener> sDayDeletionListeners;
	
	/**
	 * Temps minimum entre deux flips de vue (changement de jour...)
	 */
	private static final int MIN_VIEW_FLIP_INTERVAL = 1000;
	
	// Objets charg�s de g�rer "physiquement" la bascule d'une vue � l'autre
	protected ViewFlipperManager mViewFlipperManager;
	private ViewSwitcherGestureListener mGestureListener;
	private GestureDetector mGestureDetector;
	protected View mCurrentView; // Vue actuellement affich�e
	private long mLastViewFlipTime; // Heure du dernier changement de vue, pour �viter des changements trop rapproch�s.
	private int[] mFlipViews;// Vues qu'on doit flipper, la seconde �tant la vue de d�tails � inflater.
	
	protected long mToday = -1;
	protected final Time mWorkTime = new Time();
	
	protected DbAdapter mDb;
	
	protected DayBean mWorkDayBean;
	protected Calendar mWorkCal;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Ouverture de l'acc�s � la base de donn�es
        mDb = new DbAdapter(this);
        mDb.openDatabase();
        
        // Cr�ation du bean de travail
        mWorkDayBean = new DayBean();
        
        // R�cup�ration de la date du jour
        mToday = DateUtils.getCurrentDayId(mWorkTime);
        mWorkCal = new GregorianCalendar(DateUtils.extractYear(mToday), DateUtils.extractMonth(mToday), DateUtils.extractDayOfMonth(mToday));
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
		// Cr�ation de la vue de d�tail
		final ViewFlipper flipper = (ViewFlipper)findViewById(R.id.view_flipper);
		for (int layout : layoutsToInflate) {
			View details = View.inflate(this, layout, null);
			flipper.addView(details);
		}
		
    	// Cr�ation du d�tecteur de gestes
    	mGestureListener = new ViewSwitcherGestureListener();
    	mGestureDetector = new GestureDetector(mGestureListener);
    	
    	// Cr�ation du gestionnaire de ViewFlipper
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
		// Si on n'a pas g�r� l'�v�nement, on le laisse continuer son chemin
		super.dispatchTouchEvent(event);
		return true;
    };
    
    /**
	 * G�re le changement de vue suite � un sweep. On affiche soit le d�tail
	 * des pointages, soit celui du jour.
	 * @param lastDirection
	 * @param viewId
	 */
	final protected void switchDetailsView(final Direction lastDirection, final int viewId) {
		if (viewId > 0) {
			// Une vue est sp�cifi�e : on va switch�e vers celle-l�
			mCurrentView = findViewById(viewId);
		} else {
			// Aucune vue sp�cifi�e : on switch vers l'autre
			switch (lastDirection) {
				// Qu'on glisse vers la gauche ou la droite, on va switcher
				// entre les d�tails et les pointages
				case left:
				case right:
					pointToNextDisplay();
					break;
				default:
					return;
			}
		}
		
		// Affichage des donn�es
		populateView(mWorkDayBean.date);
		mViewFlipperManager.flipView(lastDirection, mCurrentView.getId());
	}
	
	final protected void switchDetailsView(final Direction lastDirection) {
		switch (lastDirection) {
			// Qu'on glisse vers la gauche ou la droite, on va switcher
			// entre les d�tails et les pointages
			case left:
			case right:
				pointToNextDisplay();
				break;
			default:
				return;
		}
		
		// Affichage des donn�es
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
     * Met � jour le bean de travail avec les donn�es du jour indiqu�, et rafra�chit l'interface.
     * @param day
     */
    public abstract void populateView(final long day);
    
	/**
	 * Modifie l'onglet actuellement affich�
	 * @param indexTabToSwitchTo
	 */
	protected void switchTab(final int tabIndexToSwitchTo, final long date, final int viewId){
        MainActivity mainActivity = (MainActivity)this.getParent();
        mainActivity.switchTab(tabIndexToSwitchTo, date, viewId);
	}

	/**
	 * Propose une bo�te de dialogue de saisie de temps et met � jour le temps additionnel
	 * du jour s�lectionn�
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
	 * Propose une bo�te de dialogue de saisie de temps et met � jour le pointage indiqu�
	 * du jour s�lectionn�
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
	 * Propose une bo�te de dialogue de saisie de date pour cr�er un nouveau
	 * jour � la date s�lectionn�e
	 */
	protected void promptAddDay() {
		final Bundle args = new Bundle();
		args.putLong(DialogArgs.DATE, mToday);
		showDialog(DialogTypes.DATEPICKER_ADD_DAY, args);
	}
	
	/**
	 * Propose une bo�te de dialogue de saisie de date pour afficher
	 * les informations (pointages ou semaine) de la date s�lectionn�e
	 */
	protected void promptShowDay() {
		final Bundle args = new Bundle();
		args.putLong(DialogArgs.DATE, mToday);
		showDialog(DialogTypes.DATEPICKER_SHOW_DAY, args);
	}
	
	/**
	 * Propose une bo�te de dialogue de saisie de temps pour cr�er un nouveau
	 * pointage � la date s�lectionn�e
	 * @param date 
	 */
	protected void promptAddChecking(final long date) {
		final Bundle args = new Bundle();
		args.putLong(DialogArgs.DATE, date);
		args.putInt(DialogArgs.TIME, 0);// Par d�faut, on met l'heure � 00:00
		showDialog(DialogTypes.TIMEPICKER_ADD_CHECKING, args);
	}
	
	/**
	 * Propose une bo�te de dialogue de saisie de texte et met � jour la note du jour
	 * s�lectionn�
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
	 * Affiche une bo�te de dialogue avec un bouton "oui" et un bouton "non".
	 */
	protected void showConfirmDialog(final CharSequence message, final DialogInterface.OnClickListener positiveListener, final DialogInterface.OnClickListener negativeListener) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		// D�finition des param�tres g�n�raux
		builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle(R.string.app_name);
        builder.setMessage(message);
        
        // D�finition des boutons
        builder.setPositiveButton(R.string.btn_yes, positiveListener);
        builder.setNegativeButton(R.string.btn_no, negativeListener);
        
        // Affichage
        builder.show();
	}
	
	/**
	 * Met � jour un TextView avec une string compl�t�e avec les valeurs
	 * de la variable texts.
	 * @param viewId
	 * @param stringId Identifiant d'une chaine poss�dant des param�tres
	 * @param texts
	 */
	protected void setText(final int viewId, final int stringId, final Object... texts) {
		setText(viewId, getString(stringId, texts));
	}
	
	/**
	 * Met � jour un TextView avec la chaine pass�e en param�tre.
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
     * Mise � jour des composants communs � l'affichage "Pointages" et "D�tails"
     * @param day
     */
	protected void populateCommon(final CharSequence strCurrent, final int total, final int max) {
		// Affichage de la p�riode de la semaine
        final TextView txtCurrent = (TextView)findViewById(R.id.txt_current);
        txtCurrent.setText(strCurrent);
        
        // Affichage du temps effectu� et du temps restant
        final TextView txtTotal = (TextView)findViewById(R.id.txt_total);
        txtTotal.setText(TimeUtils.formatMinutes(total));
        
        final int remaining = total - max;
		final TextView txtRemaining = (TextView)findViewById(R.id.txt_remaining);
		txtRemaining.setText(TimeUtils.formatMinutesWithSign(remaining));
		if (remaining < 0) {
			// Il reste du temps � faire : on �crit en rouge
			txtRemaining.setTextColor(Color.RED);
		} else if (remaining == 0) {
			// On a fait pile le temps demand� : on �crit en noir
			txtRemaining.setTextColor(Color.BLACK);
		} else {
			// Du temps HV a �t� cumul� : on �crit en vert
			txtRemaining.setTextColor(Color.GREEN);
		}
        
        // Mise � jour de la barre de progression
        final ProgressBar barTotal = (ProgressBar)findViewById(R.id.bar_total);
        barTotal.setMax(max);
        // Lorsqu'on g�rera les objectifs, le max devra �tre adapt� pour refl�ter l'objectif
        // et le secondary indiquera le weekMin. Pour l'instant on consid�re donc que l'objectif
        // est le dayMin.
        //barTotal.setSecondaryProgress(PreferencesBean.instance.weekMin);
        barTotal.setProgress(total);
	}
	
	/**
	 * Affiche un message demandant la confirmation de la suppression,
	 * puis supprime le jour indiqu� le cas �ch�ant.
	 * @param date
	 */
	protected void deleteDay(final long date) {
		// Cr�ation des �l�ments de la bo�te de dialogue
		final CharSequence message = getString(R.string.dlg_msg_confirm_day_deletion, DateUtils.formatDateDDMM(date));
		final DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	// Suppression du jour en base
            	mDb.deleteDay(date);
            	
            	// Mise � jour de l'affichage
            	populateView(date);
//            	mWeek.remove(selectedDay);
//            	if (!mWeek.isEmpty()) {
//            		// Il reste au moins un jour : on affiche la semaine correspondante.
//            		populateView(mWeek.get(0).date);
//            	} else {
//            		// La semaine est vide : on affiche la semaine pr�c�dente
//            		long previous = mDb.fetchPreviousDay(date);
//            		if (previous == -1) {
//            			// S'il n'y a pas de jour pr�c�dent, alors on affiche la semaine d'aujourd'hui.
//            			previous = mToday;
//            		}
//            		populateView(previous);
//            	}
            	
            	// Si on a supprim� le jour d'aujourd'hui, on met � jour le widget
            	if (mToday == date) {
            		WidgetProvider.updateClockinImage(TicTacActivity.this);
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

