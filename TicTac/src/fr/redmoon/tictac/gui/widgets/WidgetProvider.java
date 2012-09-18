package fr.redmoon.tictac.gui.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.services.AddCheckingService;
import fr.redmoon.tictac.db.DbAdapter;

public class WidgetProvider extends AppWidgetProvider {
    public static final String URI_SCHEME = "images_widget";

    @Override
    public void onEnabled(Context context) {
        // This is only called once, regardless of the number of widgets of this
        // type
        // We do not have any global initialization
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        
        // Mise à jour de l'image dans les widgets
        updateClockinImage(context,appWidgetIds, getCheckingCount(context));

        // Build the intent to call the service
        final Intent intent = new Intent(context.getApplicationContext(), AddCheckingService.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        
		// Pour réagir à un clic il faut utiliser une pending intent car le onClickListener
		// est exécuté par l'application Home
		final PendingIntent pendingIntent = PendingIntent.getService(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.btn_checkin, pendingIntent);

		// Mise à jour des widgets avec le nouvel intent
		appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
		
		// DBG Cela semble inutile. Une fois confirmé, à supprimer.
        //super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
    	// DBG Très sale ! Ca permet de faire fonctionner le widget lorsque l'orientation de l'écran change
    	// mais c'est sale car on le fait à chaque notification et pas uniquement quand l'orientation change.
//    	Log.d("Welltime", "OnReceive:Action > " + intent.getAction());
    	onUpdate(context, AppWidgetManager.getInstance(context), getAppWidgetIds(context));
    	super.onReceive(context, intent);
    }

    /**
     * Retourne l'image de statut à afficher en fonction du nombre de pointages du
     * jour :
     * 	- nombre pair : l'utilisateur n'est pas en train de travailler.
     * 	- nombre impair : l'utilisateur est en train de travailler.
     * @param context
     * @return
     */
    private static int getCheckingCount(final Context context) {
    	// Ouverture d'un accès à la base.
    	final DbAdapter db = new DbAdapter(context);
        db.openDatabase();
        
        // Récupération du jour courant.
        final long today = DateUtils.getCurrentDayId();
        final DayBean day = new DayBean();
        db.fetchDay(today, day);
        db.closeDatabase();
        
        // Compte le nombre de pointages
        return day.checkings.size();
	}
    
    public static int[] getAppWidgetIds(final Context context) {
    	// Récupère les Id des widgets
		final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		return appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
    }

    /**
     * Mise à jour de l'image dans le(s) widget(s)
     * @param context
     * @param checkingsCount 
     */
	public static void updateClockinImage(final Context context, final int[] appWidgetIds, final int checkingsCount) {
		// L'utilisateur est en train de travailler si on a un nombre impair de pointages.
		int img = R.drawable.clockin_working;
		if (checkingsCount % 2 == 0) {
			img = R.drawable.clockin_notworking;
		}
		
		// Mise à jour de l'image
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		if (appWidgetIds.length > 0) {
			for (int widgetId : appWidgetIds) {
				RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
				remoteViews.setImageViewResource(R.id.btn_checkin, img);
				appWidgetManager.updateAppWidget(widgetId, remoteViews);
			}
		}
	}
	
	/**
     * Mise à jour de l'image de statut dans le(s) widget(s)
     * @param context
     * @param checkingsCount 
     */
	public static void updateClockinImage(final Context context) {
		// Récupère le nombre de pointage pour cette journée
		final int count = getCheckingCount(context);
		
		// Récupère les identifiants des widgets
		final int[] appWidgetIds = getAppWidgetIds(context);
		
		// Mise à jour de l'image de statut
		updateClockinImage(context, appWidgetIds, count);
	}

//	@Override
//    public void onDeleted(Context context, int[] appWidgetIds) {
//        Log.d(LOG_TAG, "onDelete()");
//
//        for (int appWidgetId : appWidgetIds) {
//
//            // stop alarm
//            Intent widgetUpdate = new Intent();
//            widgetUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//            widgetUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
//            widgetUpdate.setData(Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://widget/id/"), String.valueOf(appWidgetId)));
//            PendingIntent newPending = PendingIntent.getBroadcast(context, 0, widgetUpdate, PendingIntent.FLAG_UPDATE_CURRENT);
//
//            AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//            alarms.cancel(newPending);
//
//            // remove preference
//            Log.d(LOG_TAG, "Removing preference for id " + appWidgetId);
//            SharedPreferences config = context.getSharedPreferences(WidgetConfigurationActivity.PREFS_NAME, 0);
//            SharedPreferences.Editor configEditor = config.edit();
//
//            configEditor.remove(String.format(WidgetConfigurationActivity.PREFS_UPDATE_RATE_FIELD_PATTERN, appWidgetId));
//            configEditor.commit();
//        }
//
//        super.onDeleted(context, appWidgetIds);
//    }

//    @Override
//    public void onReceive(Context context, Intent intent) {
//
//        final String action = intent.getAction();
//        Log.d(LOG_TAG, "OnReceive:Action: " + action);
//        if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
//            final int appWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
//            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
//                this.onDeleted(context, new int[] { appWidgetId });
//            }
//        } else if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
//
//            if (!URI_SCHEME.equals(intent.getScheme())) {
//                // if the scheme doesn't match, that means it wasn't from the
//                // alarm
//                // either it's the first time in (even before the configuration
//                // is done) or after a reboot or update
//
//                final int[] appWidgetIds = intent.getExtras().getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
//
//                for (int appWidgetId : appWidgetIds) {
//
//                    // get the user settings for how long to schedule the update
//                    // time for
//                    SharedPreferences config = context.getSharedPreferences(WidgetConfigurationActivity.PREFS_NAME, 0);
//                    int updateRateSeconds = config.getInt(String.format(WidgetConfigurationActivity.PREFS_UPDATE_RATE_FIELD_PATTERN, appWidgetId), -1);
//                    if (updateRateSeconds != -1) {
//                        Log.i(LOG_TAG, "Starting recurring alarm for id " + appWidgetId);
//                        Intent widgetUpdate = new Intent();
//                        widgetUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//                        widgetUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { appWidgetId });
//
//                        // make this pending intent unique by adding a scheme to
//                        // it
//                        widgetUpdate.setData(Uri.withAppendedPath(Uri.parse(WidgetProvider.URI_SCHEME + "://widget/id/"), String.valueOf(appWidgetId)));
//                        PendingIntent newPending = PendingIntent.getBroadcast(context, 0, widgetUpdate, PendingIntent.FLAG_UPDATE_CURRENT);
//
//                        // schedule the updating
//                        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//                        alarms.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), updateRateSeconds * 1000, newPending);
//                    }
//                }
//            }
//            super.onReceive(context, intent);
//        } else {
//            super.onReceive(context, intent);
//        }
//    }
}
