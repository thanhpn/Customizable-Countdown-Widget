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
import android.util.Log;
import android.widget.ImageView.ScaleType;
import android.widget.RemoteViews;
import android.graphics.Color;

public class widget_service extends Service {
	private static final String TAG = "widget_service";
	@Override
    public void onStart(Intent intent, int startId) {
		Log.v(TAG, "Service Start Begin");
        
        
		//Get App ID
		int mAppWidgetId = intent.getExtras().getInt(
				AppWidgetManager.EXTRA_APPWIDGET_ID);
		Log.v(TAG, Integer.toString(mAppWidgetId));
		
    	//This is to load configuration when its clicked
        Intent clickIntent = new Intent(getBaseContext(), configure.class);
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), mAppWidgetId, clickIntent, 0);
        
		//Get Shared preferences
		SharedPreferences prefs = getApplicationContext().getSharedPreferences(configure.PREFS_NAME, 0);
        String CountdownDate = prefs.getString("Date-" + mAppWidgetId, "");
        String Title = prefs.getString("Title-" + mAppWidgetId, "");
        String Image_String = prefs.getString("Image-" + mAppWidgetId, "");
        String ImageLandScape_String = prefs.getString("ImageLandScape-" + mAppWidgetId, "");
        String Color_position = prefs.getString("Color-" + mAppWidgetId, "");
        //If countdown date exists then update view
        if(CountdownDate != "")
        {
        	Log.v(TAG, "Service View Updated");
        	
        	//Calculate days to countdown
        	Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            String[] Countdown_array = CountdownDate.split("-");
            // Set the date for both of the calendar instance
            cal1.set(cal1.get(Calendar.YEAR), cal1.get(Calendar.MONTH) + 1, cal1.get(Calendar.DAY_OF_MONTH));//From
            cal2.set(Integer.parseInt(Countdown_array[2]), Integer.parseInt(Countdown_array[0]), Integer.parseInt(Countdown_array[1]));//To

            // Get the represented date in milliseconds
            long milis1 = cal1.getTimeInMillis();
            long milis2 = cal2.getTimeInMillis();
            
            // Calculate difference in milliseconds
            long diff = milis2 - milis1;
            long diffDays=0;
            // Calculate difference in days
            if (diff > 0)//If countdown still counting
            {
            	diffDays = diff / (24 * 60 * 60 * 1000);
            }

            

			
            
        	// Create Remote View
        	RemoteViews remoteView = new RemoteViews(getApplicationContext()
				.getPackageName(), R.layout.widget);
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
			
			if(Image_String != "")
	        {
				byte[] theByteArray = Base64Coder.decode(Image_String);
				Bitmap bitmap=BitmapFactory.decodeByteArray(theByteArray, 0, theByteArray.length);
				remoteView.setImageViewBitmap(R.id.widget_background, bitmap);
				//byte[] theByteArrayx = Base64Coder.decode(ImageLandScape_String);
				//Bitmap bitmapx=BitmapFactory.decodeByteArray(theByteArrayx, 0, theByteArrayx.length);
				//remoteView.setImageViewBitmap(R.id.widget_background, bitmapx);
	        }
			String Color_hex = getResources().getStringArray(R.array.colors_hex)[Integer.parseInt(Color_position)];
			int color=Color.parseColor(Color_hex);
			remoteView.setTextColor(R.id.widget_title, color);
			remoteView.setTextColor(R.id.widget_date, color);
			remoteView.setTextColor(R.id.widget_days, color);
			remoteView.setTextViewText(R.id.widget_title, Title);
			remoteView.setTextViewText(R.id.widget_date, CountdownDate);
			remoteView.setTextViewText(R.id.widget_days, Long.toString(diffDays)+ " Days Left");
			remoteView.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);//Make layout clickable
			
			// apply changes to widget
			appWidgetManager.updateAppWidget(mAppWidgetId, remoteView);
        }
		Log.v(TAG, "Service Start End");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't need to bind to this service
        return null;
    }
}
