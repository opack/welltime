package fr.redmoon.tictac.bus.services;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.IBinder;
import android.text.format.Time;
import android.widget.Toast;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.DayTypes;
import fr.redmoon.tictac.bus.FlexUtils;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.db.DbAdapter;
import fr.redmoon.tictac.gui.widgets.WidgetProvider;

public class AddCheckingService extends Service {
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Enregistre le pointage en base
		final int checkingsCount = doClockin();
		
		// Mise � jour de l'image dans le(s) widget(s)
		WidgetProvider.updateClockinImage(
			getApplicationContext(),
			intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS),
			checkingsCount);
		
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
    	// Ouverture d'un acc�s � la base.
    	final DbAdapter db = new DbAdapter(this);
        db.openDatabase();
        
        // R�cup�ration du jour courant.
        final Time now = TimeUtils.getNowTime();
        final long today = DateUtils.getDayId(now);
        final DayBean day = new DayBean();
        day.date = today;
        db.fetchDay(today, day);
        
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
	    		db.updateDay(day);
	    	} else {
	    		// Le jour sera cr��. On vient d'ajouter un pointage, donc c'est
	    		// un jour de type "normal"
	    		day.type = DayTypes.normal.ordinal();
	    		
	    		db.createDay(day);
	    		
	    		// Si aucun enregistrement pour cette semaine existe, on
		    	// en cr�e un et on met � jour le temps HV depuis le
		    	// dernier enregistrement avant cette date jusqu'au dernier
		    	// jour en base.
		    	final FlexUtils flexUtils = new FlexUtils(db);
		    	flexUtils.updateFlexIfNeeded(day.date);
	    	}
	    	
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
    	db.closeDatabase();
    	
    	return checkingsCount;
    }
    
    @Override
    public void onDestroy() {
    	Toast.makeText(this, "Destruction du service", Toast.LENGTH_SHORT).show();
    	super.onDestroy();
    }
}