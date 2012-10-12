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
        
        // Mise à jour de l'affichage des widgets
        updateDisplay(context);

        // Build the intent to call the service
        final Intent intent = new Intent(context.getApplicationContext(), AddCheckingService.class);
        
		// Pour réagir à un clic il faut utiliser une pending intent car le onClickListener
		// est exécuté par l'application Home
		final PendingIntent pendingIntent = PendingIntent.getService(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.btn_checkin, pendingIntent);

		// Mise à jour des widgets avec le nouvel intent
		appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
    	// DBG Très sale ! Ca permet de faire fonctionner le widget lorsque l'orientation de l'écran change
    	// mais c'est sale car on le fait à chaque notification et pas uniquement quand l'orientation change.
//    	Log.d("Welltime", "OnReceive:Action > " + intent.getAction());
    	onUpdate(context, AppWidgetManager.getInstance(context), getAppWidgetIds(context));
    	super.onReceive(context, intent);
    }

    private static DayBean getCurrentDay(final Context context) {
    	// Ouverture d'un accès à la base.
    	final DbAdapter db = new DbAdapter(context);
        db.openDatabase();
        
        // Récupération du jour courant.
        final long today = DateUtils.getCurrentDayId();
        final DayBean day = new DayBean();
        db.fetchDay(today, day);
        db.closeDatabase();
        
        // Compte le nombre de pointages
        return day;
	}
    
    private static int[] getAppWidgetIds(final Context context) {
    	// Récupère les Id des widgets
		final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		return appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
    }
    
    /**
     * Mise à jour de l'image de statut dans le(s) widget(s)
     * @param context
     * @param checkingsCount 
     */
	public static void updateDisplay(final Context context) {
		// Récupère les identifiants des widgets
		final int[] appWidgetIds = getAppWidgetIds(context);
		
		// Récupération du jour courant
    	final DayBean day = getCurrentDay(context);
    	
        // Mise à jour de l'image dans les widgets
        updateClockinImage(context, appWidgetIds, day.checkings.size());
	}

    /**
     * Mise à jour de l'image dans le(s) widget(s)
     * @param context
     * @param checkingsCount 
     */
	private static void updateClockinImage(final Context context, final int[] appWidgetIds, final int checkingsCount) {
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
}
