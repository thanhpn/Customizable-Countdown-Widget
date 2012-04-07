package com.corradodev.customizable_countdown_widget;

import java.util.Calendar;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
//import android.util.Log;
import android.widget.RemoteViews;

public class widget_service extends Service {
	//private static final String TAG = "widget_service";
	@Override
    public void onStart(Intent intent, int startId) {
		//Get App ID
		int mAppWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
		//Log.v(TAG, "Service Start:" + mAppWidgetId);
		
    	//This is to load configuration when its clicked
        Intent clickIntent = new Intent(getBaseContext(), configure.class);
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), mAppWidgetId, clickIntent, 0);
        
		//Get Shared preferences
		SharedPreferences mPrefs = getApplicationContext().getSharedPreferences(configure.PREFS_NAME, 0);
        String mCountdownDate = mPrefs.getString("Date-" + mAppWidgetId, "");
        String mTitle = mPrefs.getString("Title-" + mAppWidgetId, "");
        String mImage = mPrefs.getString("Image-" + mAppWidgetId, "");
        String mColor = mPrefs.getString("Color-" + mAppWidgetId, "");
        String mWidgetSize = mPrefs.getString("WidgetSize-" + mAppWidgetId, "");
        //String mWidgetSize = mPrefs.getString("WidgetSize-" + mAppWidgetId, "");
        //If countdown date exists then update view
        if(mCountdownDate != "")
        {
        	//Log.v(TAG, "Service Updated");
        	//Calculate days to countdown
        	Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            String[] mDateArray = mCountdownDate.split("-");
            // Set the date for both of the calendar instance
            //Current time and date
            cal1.set(cal1.get(Calendar.YEAR), cal1.get(Calendar.MONTH), cal1.get(Calendar.DAY_OF_MONTH));//From Added one to date so the countdown doesn't include today
            //Midnight on date
            cal2.set(Integer.parseInt(mDateArray[2]), Integer.parseInt(mDateArray[0])-1, Integer.parseInt(mDateArray[1]),0,0,0);//To
            long mDiffDays = daysBetween(cal1,cal2);
            //Log.v(TAG, "Current Date: " + cal1.getTime());
            //Log.v(TAG, "Picked Date: " + cal2.getTime());
            //Log.v(TAG, "Diff Days:" + mDiffDays);
            
        	// Create Remote View
        	RemoteViews remoteView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.widget);
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
			
			if(mImage != "")
	        {
				byte[] theByteArray = Base64Coder.decode(mImage);
				Bitmap bitmap=BitmapFactory.decodeByteArray(theByteArray, 0, theByteArray.length);
				remoteView.setImageViewBitmap(R.id.widget_background, bitmap);
	        }
			int color=Integer.parseInt(mColor);
			remoteView.setTextColor(R.id.widget_title, color);
			remoteView.setTextColor(R.id.widget_date, color);
			remoteView.setTextColor(R.id.widget_days, color);
			if(mWidgetSize =="Small")
			{
				remoteView.setFloat(R.id.widget_title, "setTextSize", 16);
				remoteView.setFloat(R.id.widget_date, "setTextSize", 16);
				remoteView.setFloat(R.id.widget_days, "setTextSize", 16);
			}
			remoteView.setTextViewText(R.id.widget_title, mTitle);
			remoteView.setTextViewText(R.id.widget_date, mCountdownDate);
			remoteView.setTextViewText(R.id.widget_days, Long.toString(mDiffDays)+ " Days");
			remoteView.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);//Make layout clickable
			
			// apply changes to widget
			appWidgetManager.updateAppWidget(mAppWidgetId, remoteView);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't need to bind to this service
        return null;
    }
    //Function to calculate dates between 2 dates
	public static long daysBetween(Calendar startDate, Calendar endDate) {  
		  Calendar date = (Calendar) startDate.clone();  
		  long daysBetween = 0;  
		  while (date.before(endDate)) {  
		    date.add(Calendar.DAY_OF_MONTH, 1);  
		    daysBetween++;  
		  }  
		  return daysBetween;  
	} 
}