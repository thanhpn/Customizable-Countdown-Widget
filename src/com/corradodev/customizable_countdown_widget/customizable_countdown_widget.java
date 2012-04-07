package com.corradodev.customizable_countdown_widget;

import java.util.Calendar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
//import android.util.Log;

public class customizable_countdown_widget extends AppWidgetProvider {
	//private static final String TAG = "customizable_countdown_widget";
	
	//When first instance of the widget is defined
	@Override
	public void onEnabled(Context context)
	{
		super.onEnabled(context);
	}
	
	//When a widget is updated by what is defined in meta data on how often this happens
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		//Log.v(TAG, "AppWidgetProvider onUpdate");
		
		//Calculate milliseconds until next day midnight+1 minute so it does not count current date
		Calendar midnight = Calendar.getInstance();
		midnight.add(Calendar.DAY_OF_MONTH, 1);
		midnight.set(Calendar.HOUR_OF_DAY,0);
		midnight.set(Calendar.MINUTE,1);
		midnight.set(Calendar.SECOND,0);
		midnight.set(Calendar.MILLISECOND,0);
		
		long midnightMilli= midnight.getTimeInMillis();
		//Log.v(TAG, "Midnight Next Day in Milliseconds(+1 min):" + midnightMilli);
		for (int mAppWidgetId : appWidgetIds) {
			PendingIntent updatepending = customizable_countdown_widget.makeControlPendingIntent(context,"update", mAppWidgetId);
			setAlarm(context, mAppWidgetId, midnightMilli);
			try {
				updatepending.send();
			} catch (CanceledException e) {
				e.printStackTrace();
			}
		}
		super.onUpdate(context, appWidgetManager, appWidgetIds); 
    }	

	//This happens before all widget actions
	@Override
	public void onReceive(Context context, Intent intent) {
		// v1.5 fix that doesn't call onDelete Action
		final String action = intent.getAction();
		if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
			final int appWidgetId = intent.getExtras().getInt(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
			if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				this.onDeleted(context, new int[] { appWidgetId });
			}
		} else {
			super.onReceive(context, intent);
		}
	}
	
	// When a widget is deleted
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		//Delete all alarms when widget is deleted
        for (int appWidgetId : appWidgetIds) {       
            setAlarm(context, appWidgetId, 0);
        }
		super.onDeleted(context, appWidgetIds);
	}
	
	//When all widgets are deleted
	@Override
	public void onDisabled(Context context) {
		//Ensure all services are stopped when disabled
		context.stopService(new Intent(context,widget_service.class));
		super.onDisabled(context);
		//Log.v(TAG, "Kill Service because all widgets are deleted");
	}
	
	//Sets up Pending intent
	public static PendingIntent makeControlPendingIntent(Context context, String command, int appWidgetId) {
        Intent active = new Intent(context,widget_service.class);
        active.setAction(command);
        active.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        //this Uri data is to make the PendingIntent unique, so it wont be updated by FLAG_UPDATE_CURRENT
        //so if there are multiple widget instances they wont override each other
        Uri data = Uri.withAppendedPath(Uri.parse("countdownwidget://widget/id/#"+command+appWidgetId), String.valueOf(appWidgetId));
        active.setData(data);
        return(PendingIntent.getService(context, 0, active, PendingIntent.FLAG_UPDATE_CURRENT));
    }
	
	//Removes and adds alarm
	public static void setAlarm(Context context, int appWidgetId, long midnightMilli) {
        PendingIntent newPending = makeControlPendingIntent(context,"update",appWidgetId);
        
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (midnightMilli != 0) {
            alarms.setRepeating(1, midnightMilli,AlarmManager.INTERVAL_DAY, newPending);
            //Log.v(TAG, "Setup Alarm:"+appWidgetId);
        } else {
        	// on a negative updateRate stop the refreshing 
            alarms.cancel(newPending);
            //Log.v(TAG, "Cancel Alarm:"+appWidgetId);
        }
    }
}
