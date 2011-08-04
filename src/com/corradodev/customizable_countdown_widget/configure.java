package com.corradodev.customizable_countdown_widget;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

/*
 *  The configuration screen
 */
public class configure extends Activity {
	private int mAppWidgetId;
	private static final String TAG = "configure";
	private Context self = this;
	public static final String PREFS_NAME = "customizable_countdown_widget";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set the result to CANCELED.  if they press the back or cancel button.
        setResult(RESULT_CANCELED);

        // Set the view layout resource to use.
        setContentView(R.layout.configure);
        
        // Find the widget id from the intent. 
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        Log.d(TAG, Integer.toString(mAppWidgetId));
        
        // If they gave us an intent without the widget id, close
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
        
        //Prefill boxes if editing a widget
		SharedPreferences prefs = getApplicationContext().getSharedPreferences(configure.PREFS_NAME, 0);
        String CountdownDate = prefs.getString("Date-" + mAppWidgetId, "");
        String configure_title = prefs.getString("Title-" + mAppWidgetId, "");
        String Color_position = prefs.getString("Color-" + mAppWidgetId, "");
        if(CountdownDate !="")
        {
        	Log.d(TAG, "Prefill View");
        	String[] Countdown_array = CountdownDate.split("-");
        	DatePicker dp = (DatePicker)findViewById(R.id.countdown_datepicker);
        	dp.init(Integer.parseInt(Countdown_array[2]), Integer.parseInt(Countdown_array[0])-1, Integer.parseInt(Countdown_array[1]),null);
            EditText title = (EditText)findViewById(R.id.configure_title);
        	title.setText(configure_title);
        }
    	
        //Bind Colors to spinner
        Spinner hubSpinner = (Spinner) findViewById(R.id.color_spinner);
		ArrayAdapter adapter = ArrayAdapter.createFromResource( this, R.array.colors , android.R.layout.simple_spinner_item); 
        adapter.setDropDownViewResource(android.R.layout.select_dialog_item);
        hubSpinner.setAdapter(adapter);
        
        //If position exists set default value
        if(Color_position !="")
        {
        	hubSpinner.setSelection(Integer.parseInt(Color_position));
        }
        // Bind the action for the ok and cancel button.
        findViewById(R.id.image_button).setOnClickListener(imageOnClickListener);
        findViewById(R.id.ok_button).setOnClickListener(okOnClickListener);
        findViewById(R.id.cancel_button).setOnClickListener(cancelOnClickListener);
        
    }
	
    // When the image button is clicked
    View.OnClickListener imageOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
        	Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, 1);
        }
    };
    
 // When the ok button is clicked
    View.OnClickListener okOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            DatePicker mDatePicker = (DatePicker)findViewById(R.id.countdown_datepicker);
            EditText mTitle=(EditText)findViewById(R.id.configure_title);
            Spinner mColor=(Spinner)findViewById(R.id.color_spinner);
            int color_position = mColor.getSelectedItemPosition();
            //Save Date to shared preferences
            String dateCountdown = mDatePicker.getMonth()+1 + "-"+ mDatePicker.getDayOfMonth() + "-"+ mDatePicker.getYear();
			SharedPreferences prefs = self.getSharedPreferences(PREFS_NAME, 0);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putString("Date-" + mAppWidgetId, dateCountdown);
			edit.putString("Title-" + mAppWidgetId, mTitle.getText().toString());
			edit.putString("Color-" + mAppWidgetId, Integer.toString(color_position));
			edit.commit();
			Log.v("Click Date", dateCountdown);
			
			// fire an update to display initial state of the widget
			PendingIntent updatepending = customizable_countdown_widget
					.makeControlPendingIntent(self,"update", mAppWidgetId);
			try {
				updatepending.send();
			} catch (CanceledException e) {
				e.printStackTrace();
			}
			
			// change the result to OK
			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					mAppWidgetId);
			setResult(RESULT_OK, resultValue);
			
			//Close configuration window
            finish();
            
        }
    };
    
    // When the cancel button is clicked
    View.OnClickListener cancelOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
        	//Just close the window
            finish();  
        }
    };
    
    //image activity results
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      if (requestCode == 1)
        if (resultCode == Activity.RESULT_OK) {
        	Uri selectedImage = data.getData();
           final Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setData(selectedImage);//Image to crop
            /*LANDSCAPE(X,Y)(424,142)
             *PORTRAIT(X,Y)(320,200)*/
            
            intent.putExtra("outputX", 424);
            intent.putExtra("outputY", 265);
            intent.putExtra("aspectX", 8);
            intent.putExtra("aspectY", 5);
            intent.putExtra("return-data", true);
            Log.d(TAG, "1" + selectedImage);
            startActivityForResult(intent, 2);
        }
      if (requestCode == 2)
          if (resultCode == Activity.RESULT_OK) {
        	  Log.v("Cropped", "x");
              final Bundle extras = data.getExtras();
              if (extras != null) {
				Bitmap photo = extras.getParcelable("data");
				ByteArrayOutputStream baos = new ByteArrayOutputStream();  
				photo.compress(Bitmap.CompressFormat.JPEG, 100, baos); //photo is the bitmap object   
				byte[] b = baos.toByteArray();  
				char[] cvalue=Base64Coder.encode(b);
				String value = new String(cvalue);
				SharedPreferences prefs = self.getSharedPreferences(PREFS_NAME, 0);
				SharedPreferences.Editor edit = prefs.edit();
				edit.putString("Image-" + mAppWidgetId, value);
				edit.commit();
              }

          }


    }
}



