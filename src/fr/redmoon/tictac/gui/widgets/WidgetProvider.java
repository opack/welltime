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
        // Mise à jour de l'affichage des widgets
        updateWidgets(context);
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
    
    /**
     * Mise à jour de l'image de statut dans le(s) widget(s)
     * @param context
     * @param checkingsCount 
     */
	public static void updateWidgets(final Context context) {
		// Récupère les identifiants des widgets
		final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
		
		// Récupération du jour courant
    	final DayBean day = getCurrentDay(context);
    	
        // Mise à jour de l'image dans les widgets
    	// L'utilisateur est en train de travailler si on a un nombre impair de pointages.
		int img = R.drawable.clockin_working;
		if (day.checkings.size() % 2 == 0) {
			img = R.drawable.clockin_notworking;
		}
		
		// Pour réagir à un clic il faut utiliser une pending intent car le onClickListener
		// est exécuté par l'application Home
		final Intent intent = new Intent(context.getApplicationContext(), AddCheckingService.class);
		final PendingIntent pendingIntent = PendingIntent.getService(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		// Mise à jour de l'image
		if (appWidgetIds.length > 0) {
			for (int widgetId : appWidgetIds) {
				RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
				remoteViews.setOnClickPendingIntent(R.id.btn_checkin, pendingIntent);
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
