package fr.redmoon.tictac;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.Time;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewFlipper;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.db.DbAdapter;
import fr.redmoon.tictac.gui.datepicker.DatePickerDialogHelper;
import fr.redmoon.tictac.gui.daytypes.UpdateDayTypeListener;
import fr.redmoon.tictac.gui.listeners.AddCheckingListener;
import fr.redmoon.tictac.gui.listeners.AddDayListener;
import fr.redmoon.tictac.gui.listeners.EditNoteListener;
import fr.redmoon.tictac.gui.listeners.ShowDayListener;
import fr.redmoon.tictac.gui.listeners.UpdateCheckingListener;
import fr.redmoon.tictac.gui.listeners.UpdateExtraListener;
import fr.redmoon.tictac.gui.sweep.Direction;
import fr.redmoon.tictac.gui.sweep.ViewFlipperManager;
import fr.redmoon.tictac.gui.sweep.ViewSwitcherGestureListener;
import fr.redmoon.tictac.gui.timepicker.TimePickerDialogHelper;

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
	
	/**
	 * Constante identifiant les boîtes de dialogue
	 */
	protected static final int DIALOG_TIMEPICKER_ADD_CHECKING = 0;
	protected static final int DIALOG_TIMEPICKER_EDIT_CHECKING = 1;
	protected static final int DIALOG_TIMEPICKER_EDIT_EXTRA = 2;
	protected static final int DIALOG_DATEPICKER_ADD_DAY = 3;
	protected static final int DIALOG_DATEPICKER_SHOW_DAY = 4;
	
	/**
	 * Constante identifiant un argument spécifiant une date
	 */
	protected static final String DATE = "DATE";
	
	/**
	 * Constante identifiant un argument spécifiant un horaire
	 */
	protected static final String TIME = "TIME";
	
	/**
	 * Constante identifiant un argument spécifiant un type de jour
	 */
	protected static final String DAY_TYPE = "DAY_TYPE";
	
	/**
	 * Constante identifiant un argument spécifiant une note
	 */
	protected static final String NOTE = "NOTE";
	
	// Objets chargés de gérer "physiquement" la bascule d'une vue à l'autre
	protected ViewFlipperManager mViewFlipperManager;
	private ViewSwitcherGestureListener mGestureListener;
	private GestureDetector mGestureDetector;
	protected View mCurrentView; // Vue actuellement affichée
	private long mLastViewFlipTime; // Heure du dernier changement de vue, pour éviter des changements trop rapprochés.
	private int[] mFlipViews;// Vues qu'on doit flipper, la seconde étant la vue de détails à inflater.
	
	// Listeners pour les boîtes de dialogue
	private AddCheckingListener mAddCheckingListener;
	private UpdateCheckingListener mUpdateCheckingListener;
	private UpdateExtraListener mUpdateExtraListener;
	private AddDayListener mAddDayListener;
	private ShowDayListener mShowDayListener;
	private UpdateDayTypeListener mUpdateDayTypeListener;
	private EditNoteListener mEditNoteListener;
	
	protected long mToday = -1;
	protected final Time mWorkTime = new Time();
	
	protected DbAdapter mDb;
	
	protected DayBean mWorkDayBean;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Ouverture de l'accès à la base de données
        mDb = new DbAdapter(this);
        mDb.openDatabase();
        
        // Préparation des listeners pour les boîtes de dialogue
        mAddCheckingListener = new AddCheckingListener(this);
        mUpdateCheckingListener = new UpdateCheckingListener(this);
        mUpdateExtraListener = new UpdateExtraListener(this);
        mAddDayListener = new AddDayListener(this);
        mShowDayListener = new ShowDayListener(this);
        mUpdateDayTypeListener = new UpdateDayTypeListener(this);
        mEditNoteListener = new EditNoteListener(this);
        
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

	@Override
	protected Dialog onCreateDialog(final int id) {
    	switch (id) {
    	case DIALOG_TIMEPICKER_ADD_CHECKING:
    		return TimePickerDialogHelper.createDialog(this, mAddCheckingListener);
		case DIALOG_TIMEPICKER_EDIT_CHECKING:
			return TimePickerDialogHelper.createDialog(this, mUpdateCheckingListener);
		case DIALOG_TIMEPICKER_EDIT_EXTRA:
			return TimePickerDialogHelper.createDialog(this, mUpdateExtraListener);
		case DIALOG_DATEPICKER_ADD_DAY:
			return DatePickerDialogHelper.createDialog(this, mAddDayListener, DateUtils.extractYear(mToday), DateUtils.extractMonth(mToday), DateUtils.extractDayOfMonth(mToday));
		case DIALOG_DATEPICKER_SHOW_DAY:
			return DatePickerDialogHelper.createDialog(this, mShowDayListener, DateUtils.extractYear(mToday), DateUtils.extractMonth(mToday), DateUtils.extractDayOfMonth(mToday));
		}
		return null;
	}
	
	@Override
	protected void onPrepareDialog(final int id, final Dialog dialog, final Bundle args) {
		super.onPrepareDialog(id, dialog, args);
		switch (id) {
			case DIALOG_TIMEPICKER_ADD_CHECKING:
				mAddCheckingListener.prepare(args.getLong(DATE));
				TimePickerDialogHelper.prepare((TimePickerDialog) dialog, args.getInt(TIME));
				break;
			case DIALOG_TIMEPICKER_EDIT_CHECKING:
				mUpdateCheckingListener.prepare(args.getLong(DATE), args.getInt(TIME));
				TimePickerDialogHelper.prepare((TimePickerDialog) dialog, args.getInt(TIME));
				break;
			case DIALOG_TIMEPICKER_EDIT_EXTRA:
				mUpdateExtraListener.prepare(args.getLong(DATE));
				TimePickerDialogHelper.prepare((TimePickerDialog) dialog, args.getInt(TIME));
				break;
			case DIALOG_DATEPICKER_ADD_DAY:
			case DIALOG_DATEPICKER_SHOW_DAY:
				// C'est un peu inutile car on passe toujours le jour courant.
				final long date = args.getLong(DATE);
				DatePickerDialogHelper.prepare((DatePickerDialog) dialog, DateUtils.extractYear(date), DateUtils.extractMonth(date), DateUtils.extractDayOfMonth(date));
				break;
		}
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
	 * @param flipViews Tableau de 2 vues, dont la seconde est la vue de détails à inflater
	 */
	protected void initSweep(final int[] flipViews, final int... detailsLayoutsToInflate) {
		// Création de la vue de détail
		final ViewFlipper flipper = (ViewFlipper)findViewById(R.id.view_flipper);
		View details = null;
		for (int layoutToInflate : detailsLayoutsToInflate) {
	 		details = View.inflate(this, layoutToInflate, null);
	 		flipper.addView(details);
		}
		
    	// Création du détecteur de gestes
    	mGestureListener = new ViewSwitcherGestureListener();
    	mGestureDetector = new GestureDetector(mGestureListener);
    	
    	// Création du gestionnaire de ViewFlipper
    	mFlipViews = flipViews;
    	mViewFlipperManager = new ViewFlipperManager(
    		this,
    		mFlipViews
    	);
    	
    	mLastViewFlipTime = 0;
    	mCurrentView = findViewById(R.id.list);
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
		args.putLong(DATE, date);
		args.putInt(TIME, extra);
		showDialog(DIALOG_TIMEPICKER_EDIT_EXTRA, args);
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
		args.putLong(DATE, date);
		args.putInt(TIME, checkingToEdit);
		showDialog(DIALOG_TIMEPICKER_EDIT_CHECKING, args);
	}
	
	/**
	 * Propose une boîte de dialogue de saisie de date pour créer un nouveau
	 * jour à la date sélectionnée
	 */
	protected void promptAddDay() {
		final Bundle args = new Bundle();
		args.putLong(DATE, mToday);
		showDialog(DIALOG_DATEPICKER_ADD_DAY, args);
	}
	
	/**
	 * Propose une boîte de dialogue de saisie de date pour afficher
	 * les informations (pointages ou semaine) de la date sélectionnée
	 */
	protected void promptShowDay() {
		final Bundle args = new Bundle();
		args.putLong(DATE, mToday);
		showDialog(DIALOG_DATEPICKER_SHOW_DAY, args);
	}
	
	/**
	 * Propose une boîte de dialogue de saisie de temps pour créer un nouveau
	 * pointage à la date sélectionnée
	 * @param date 
	 */
	protected void promptAddChecking(final long date) {
		final Bundle args = new Bundle();
		args.putLong(DATE, date);
		args.putInt(TIME, 0);// Par défaut, on met l'heure à 00:00
		showDialog(DIALOG_TIMEPICKER_ADD_CHECKING, args);
	}
	
	/**
	 * Affiche une boîte de dialogue permettant la saisie du type de jour
	 * et met à jour la base le cas échéant.
	 */
	protected void promptEditDayType(final long date, final int currentType) {
		// Prépare le listener
		mUpdateDayTypeListener.prepare(date);

		// Création de la boîte de dialogue
		// On ne passe pas par le mécanisme onCreateDialog/onPrepareDialg car on ne pourrait alors
		// pas initialiser la valeur actuelle dans la liste.
		// En effet, soit on crée la dialog dans le onCreateDialog (logique) mais on n'a alors pas
		// encore le type de jour courant.
		// Soit on le fait dans le onPrepare (bizarre, mais là au moins on a les infos permettant
		// de préparer la dlg, comme le type actuel du jour) mais là on n'a plus moyen de créer la
		// dlg puisqu'à ce niveau on ne peut que paramétrer une dialogue existante. Et comme il ne
		// semble pas y avoir moyen de faire un setSelected ou un truc dans le genre, on l'a dans
		// le ...
		// Donc on fait ce qui suit, et d'ailleurs c'est même limite plus clair.
		// Seul éceuil, Android ne gère pas la vie de notre boîte de dialogue, mais on s'en fiche
		// puisqu'on la recrée à chaque fois.
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.dlg_title_edit_day_type);
		builder.setSingleChoiceItems(R.array.dayTypesEntries, currentType, mUpdateDayTypeListener);
		builder.setNeutralButton(R.string.btn_close, new DialogInterface.OnClickListener() {
        	@Override
    		public void onClick(DialogInterface dialog, int id) {
            	dialog.dismiss();
        	}
        });
		
		// Affiche la jolie boî-boîte
		builder.show();
	}
	
	/**
	 * Propose une boîte de dialogue de saisie de texte et met à jour la note du jour
	 * sélectionné
	 * @param activity
	 * @param db
	 * @param day
	 */
	protected void promptEditNote(final long date, final String initialNote) {
		// Prépare le listener
		mEditNoteListener.prepare(date);

		// Création de la boîte de dialogue		
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(R.string.dlg_title_edit_day_note);
		final EditText input = new EditText(this);
		input.setLines(3);
		input.setGravity(Gravity.TOP);
		input.setText(initialNote);
		if (initialNote != null) {
			input.setSelection(initialNote.length());
		}
		alert.setView(input);
		alert.setPositiveButton(R.string.btn_save, new DialogInterface.OnClickListener() {
			@Override
    		public void onClick(DialogInterface dialog, int id) {
				mEditNoteListener.onNoteSaved(input.getText().toString());
        	}
		});
		alert.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
			@Override
    		public void onClick(DialogInterface dialog, int id) {
            	dialog.dismiss();
        	}
		});
		alert.show();
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
}

