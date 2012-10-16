package fr.redmoon.tictac.bus.services;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.text.format.Time;
import android.widget.Toast;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.FlexUtils;
import fr.redmoon.tictac.bus.PreferencesUtils;
import fr.redmoon.tictac.bus.StandardDayTypes;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.bus.export.tocalendar.CalendarAccess;
import fr.redmoon.tictac.db.DbAdapter;
import fr.redmoon.tictac.gui.activities.WidgetDisplayTimePickerActivity;
import fr.redmoon.tictac.gui.widgets.WidgetProvider;

public class AddCheckingService extends Service {
	
	private static int mOldOrientation = 0;
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (newConfig.orientation != mOldOrientation) {
			WidgetProvider.updateWidgets(this);
			mOldOrientation = newConfig.orientation;
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Chargement des pr�f�rences
		PreferencesUtils.updatePreferencesBean(this);
		
		// Enregistre le pointage en base
		if (PreferencesBean.instance.widgetDisplayTimePicker) {
			final Intent intent2 = new Intent(getBaseContext(), WidgetDisplayTimePickerActivity.class);
			intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        getApplication().startActivity(intent2);
		} else {
			doClockin();
			
			// Mise � jour de l'image dans le(s) widget(s)
			WidgetProvider.updateWidgets(getApplicationContext());
		}
		
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	/**
     * Ajoute un pointage � l'heure courante
     * @param btn Bouton qui a �t� cliqu�.
     */
    private int doClockin() {
        // R�cup�ration du jour courant.
        final Time now = TimeUtils.getNowTime();
        final long today = DateUtils.getDayId(now);
        final DayBean day = new DayBean();
        day.date = today;
        DbAdapter.getInstance().fetchDay(today, day);
        
    	// On met � jour le nombre de pointages. Ca sera utilis� comme valeur de retour
    	// pour mettre � jour l'image du widget.
        int checkingsCount = day.checkings.size();
        	
    	// On ajoute le pointage en base si n�cessaire.
    	final Integer checking = now.hour * 100 + now.minute;
    	if (!day.checkings.contains(checking)) {
    		// Ajout du pointage dans le jour
    		day.checkings.add(checking);
    		
   			// Ajout ou mise � jour du jour dans la base
	    	if (day.isValid) {
	    		DbAdapter.getInstance().updateDay(day);
	    		
	    		// Ajout du pointage dans le calendrier
	    		if (PreferencesBean.instance.syncCalendar) {
	    			CalendarAccess.getInstance().createWorkEvents(day.date, day.checkings);
	    		}
	    	} else {
	    		// Le jour sera cr��. On vient d'ajouter un pointage, donc c'est
	    		// un jour de type "normal"
	    		day.typeMorning = StandardDayTypes.normal.name();
	    		day.typeAfternoon = StandardDayTypes.normal.name();
	    		
	    		DbAdapter.getInstance().createDay(day);
	    		
	    		// Ajout des �v�nements dans le calendrier
	    		if (PreferencesBean.instance.syncCalendar) {
	    			CalendarAccess.getInstance().createEvents(day);
	    		}
	    	}
	    	// Mise � jour de l'HV.
	    	final FlexUtils flexUtils = new FlexUtils();
	    	flexUtils.updateFlex(day.date);
	    	
	    	// Incr�ment du nombre de jour si l'ajout en base s'est correctement d�roul�.
	    	// Quelle que soit l'op�ration effectu�e, isValid a �t� mis � jour.
	    	if (day.isValid) {
	    		checkingsCount ++;
	    		Toast.makeText(getApplicationContext(), "Pointage � " + TimeUtils.formatTime(checking) + " enregistr� !", Toast.LENGTH_LONG).show();
	    	} else {
	    		Toast.makeText(this, "Oups ! Le pointage n'a pas �t� enregistr�. Merci de r�essayer.", Toast.LENGTH_LONG).show();
	    	}
    	} else {
    		// Le pointage existe d�j� : affichage d'un message � l'utilisateur
    		Toast.makeText(this, "Impossible de pointer � " + now.hour + ":" + now.minute + " car ce pointage existe d�j� !", Toast.LENGTH_LONG).show();
    	}
    	
    	return checkingsCount;
    }
}