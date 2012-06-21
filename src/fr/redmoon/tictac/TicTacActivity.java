package fr.redmoon.tictac;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.format.Time;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.FlexUtils;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.db.DbAdapter;
import fr.redmoon.tictac.gui.ViewSynchronizer;
import fr.redmoon.tictac.gui.dialogs.AbsDialogDelegate;
import fr.redmoon.tictac.gui.dialogs.DialogArgs;
import fr.redmoon.tictac.gui.dialogs.DialogTypes;
import fr.redmoon.tictac.gui.listadapter.TicTacPagerAdapter;
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
	
	protected long mToday = -1;
	protected final Time mWorkTime = new Time();
	
	protected DbAdapter mDb;
	
	protected DayBean mWorkDayBean;
	protected Calendar mWorkCal;

	protected AbsDialogDelegate mDialogDelegate;
	
	private ViewPager mPager;
	private View[] mPages;
	
	@Override
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
	public void onBackPressed() {
		// Affichage d'une bo�te de dialogue pour confirmer la sortie de l'application
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.dlg_msg_confirm_exit);
		builder.setCancelable(false);
		builder.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
        	   closeApp();
           }
       });
		builder.setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
           }
       });
		builder.show();
	}
	
	protected void closeApp() {
		ViewSynchronizer.getInstance().setCurrentDay(-1);
		super.onBackPressed();
	}
	
	@Override
    protected void onResume() {
    	// R�cup�ration de la date affich�e � l'onglet pr�c�dent
		long currentDate = ViewSynchronizer.getInstance().getCurrentDay();
		if (currentDate == -1) {
			currentDate = mToday;
		}
		
		// Affichage des donn�es de cette date.
    	populateView(currentDate);
    	    	
    	super.onResume();
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
    	if (mDialogDelegate != null) {
    		return mDialogDelegate.createDialog(id);
    	}
    	return super.onCreateDialog(id);
	}
	
	@Override
	protected void onPrepareDialog(final int id, final Dialog dialog, final Bundle args) {
		super.onPrepareDialog(id, dialog, args);
		if (mDialogDelegate != null) {
			mDialogDelegate.prepareDialog(id, dialog, args);
		}
	}
	
	protected void setDialogDelegate(final AbsDialogDelegate dialogDelegate) {
		mDialogDelegate = dialogDelegate;
	}
	
	protected void initPages(final View... pages) {
		// Initialise l'indicateur de pages s'il est pr�sent
        int curIndicator;
        final ViewGroup pageIndicator = (ViewGroup)findViewById(R.id.lyt_page_indicator);
        if (pageIndicator != null) {
        	View indicator = null;
        	for (curIndicator = 0; curIndicator < pages.length; curIndicator++) {
        		indicator = View.inflate(this, R.layout.itm_page_indicator_empty, pageIndicator);
        		if (curIndicator == 0) {
        			// Par d�faut, on s�lectionne le premier indicateur
        			indicator.findViewById(R.id.img_page_indicator).setBackgroundResource(R.drawable.page_indicator_full);
        		}
        	}
        }
        
		// Initialise le pager
		mPages = pages;
		final TicTacPagerAdapter adapter = new TicTacPagerAdapter(pages);
		mPager = (ViewPager)findViewById(R.id.view_pager);
        mPager.setAdapter(adapter);
        mPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				ImageView indicator = null;
				for (int curIndicator = 0; curIndicator < pages.length; curIndicator++) {
					indicator = (ImageView)pageIndicator.getChildAt(curIndicator);
					if (position == curIndicator) {
						indicator.setBackgroundResource(R.drawable.page_indicator_full);
					} else {
						indicator.setBackgroundResource(R.drawable.page_indicator_empty);
					}
	        	}
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}
			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});
	}
	
	public int getCurrentPage() {
		return mPager.getCurrentItem();
	}
	
	public View getPage(final int pageId) {
		//return mPager.getChildAt(pageId);
		return mPages[pageId];
	}
	
    /**
	 * G�re le changement de vue suite � un sweep. On affiche soit le d�tail
	 * des pointages, soit celui du jour.
	 * @param lastDirection
	 * @param pageId
	 */
	final protected void switchPage(final int pageId) {
		mPager.setCurrentItem(pageId);
		
		// Affichage des donn�es
		populateView(mWorkDayBean.date);
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
	protected void switchTab(final int tabIndexToSwitchTo, final long date, final int pageId){
        MainActivity mainActivity = (MainActivity)this.getParent();
        mainActivity.switchTab(tabIndexToSwitchTo, date, pageId);
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
	protected void setText(final View container, final int viewId, final int stringId, final Object... texts) {
		setText(container, viewId, getString(stringId, texts));
	}
	
	/**
	 * Met � jour un TextView avec la chaine pass�e en param�tre.
	 * @param viewId
	 * @param text
	 */
	protected void setText(final View container, final int viewId, final String text) {
		final TextView label = (TextView)container.findViewById(viewId);
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
            	
            	// Si la semaine de ce jour est vide, on la supprime
            	updateFlexAfterDeletion(date);
            	
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

			private void updateFlexAfterDeletion(final long date) {
				final Time time = new Time();
				// On r�cup�re le lundi correspondant au jour indiqu�
				TimeUtils.parseDate(date, time);
				DateUtils.getDateOfDayOfWeek(time, Time.MONDAY, time);
				final long firstDay = DateUtils.getDayId(time);
				
				DateUtils.getDateOfDayOfWeek(mWorkTime, Time.SUNDAY, mWorkTime);
				final long lastDay = DateUtils.getDayId(mWorkTime);
				
				// Si la semaine est vide, on supprime l'enregistrement correspondant
				if (mDb.countDaysBetween(firstDay, lastDay) == 0) {
					mDb.deleteFlexTime(firstDay);
				}
				
				// Puis on met � jour l'HV
				final FlexUtils flexUtils = new FlexUtils(mDb);
    			flexUtils.updateFlex(date);
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

