package com.corradodev.customizable_countdown_widget;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff.Mode;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
//import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Toast;
import android.app.DatePickerDialog;
import android.widget.EditText;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

import com.corradodev.customizable_countdown_widget.ColorPickerDialog;
import com.corradodev.customizable_countdown_widget.ColorPickerDialog.OnColorPickedListener;

public class configure extends Activity{
	//private static final String TAG = "configure";
	private int mAppWidgetId;
    private int mSelectedYear;
    private int mSelectedMonth;
    private int mSelectedDay;
	private int mSelectedColor;
	private String mImage;
	private Context self = this;
    private EditText mTxtDate;
	private ImageView mImgSelectedColor;
	private String mWidgetSize="Large";
	public static final String PREFS_NAME = "customizable_countdown_widget";
	
	private Uri mImageCaptureUri;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set the result to CANCELED.  if they press the back or cancel button.
        setResult(RESULT_CANCELED);

        // Set the view layout resource to use.
        setContentView(R.layout.configure);
        
        // Find the widget id from the intent. 
        Intent mIntent = getIntent();
        
        Bundle mExtras = mIntent.getExtras();
        if (mExtras != null) {
            mAppWidgetId = mExtras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        //Log.d(TAG, Integer.toString(mAppWidgetId));
        
        // If they gave us an intent without the widget id, close
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
        
        //Get Size of widget
        AppWidgetProviderInfo providerInfo = AppWidgetManager.getInstance(getBaseContext()).getAppWidgetInfo(mAppWidgetId);
        if(providerInfo.minWidth == 110)
        {
        	mWidgetSize="Small";
        }
        
        //Prefill boxes if editing a widget
		SharedPreferences mPrefs = getApplicationContext().getSharedPreferences(configure.PREFS_NAME, 0);
        String mPrefDate = mPrefs.getString("Date-" + mAppWidgetId, "");
        String mPrefTitle = mPrefs.getString("Title-" + mAppWidgetId, "");
        String mPrefColor = mPrefs.getString("Color-" + mAppWidgetId, "");
        mImage = mPrefs.getString("Image-" + mAppWidgetId, "");
        mTxtDate = (EditText)findViewById(R.id.countdown_date);
        if(mPrefDate !="")
        {
        	mTxtDate.setText(mPrefDate);
            EditText mTxtTitle = (EditText)findViewById(R.id.configure_title);
        	mTxtTitle.setText(mPrefTitle);
        	
        	String[] mPrefDateArray = mPrefDate.split("-");
        	mSelectedYear=Integer.parseInt(mPrefDateArray[2]);
        	mSelectedMonth=Integer.parseInt(mPrefDateArray[0])-1;//Subtract 1 because we are sending this date to the datepicker which starts at 0
        	mSelectedDay=Integer.parseInt(mPrefDateArray[1]);
        }
        else{
        	Calendar mCal = Calendar.getInstance();
        	mSelectedYear=mCal.get(Calendar.YEAR);
        	mSelectedMonth=mCal.get(Calendar.MONTH);
        	mSelectedDay=mCal.get(Calendar.DAY_OF_MONTH);
        	mTxtDate.setText(mSelectedMonth+1 + "-" + mSelectedDay + "-" +mSelectedYear);
        }
        //Color Picker
        Button mBtnColor = (Button) findViewById(R.id.color_picker);
        mImgSelectedColor=(ImageView)findViewById(R.id.color_picked_image);
        //Default to black
        if(mPrefDate ==""){
        	mSelectedColor=android.graphics.Color.BLACK;
        }
        else{
        	mSelectedColor=Integer.parseInt(mPrefColor);
        }
        mImgSelectedColor.setBackgroundColor(mSelectedColor);
        
        // Bind the action for the click listeners
        mBtnColor.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	ColorPickerDialog mColorDialog = new ColorPickerDialog(configure.this);
            	mColorDialog.setOnColorPickedListener(mColorPickerListener);
            	mColorDialog.show();
            }
        });
    	mTxtDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	showDialog(0);
            }
        });
        findViewById(R.id.image_button).setOnClickListener(imageOnClickListener);
        findViewById(R.id.ok_button).setOnClickListener(okOnClickListener);
        findViewById(R.id.cancel_button).setOnClickListener(cancelOnClickListener); 
    }
    
//Opens Date Dialog
    @Override
    protected Dialog onCreateDialog(int id) {
        return new DatePickerDialog(this,mDateSetListener,mSelectedYear, mSelectedMonth, mSelectedDay);
    }
    
//When a date is picked
    private DatePickerDialog.OnDateSetListener mDateSetListener =
        new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mSelectedYear = year;
                mSelectedMonth = monthOfYear;
                mSelectedDay = dayOfMonth;
                mTxtDate.setText(mSelectedMonth+1+ "-" +mSelectedDay+"-"+mSelectedYear);//Add 1 for display as month starts at 0
            }
    };
        
// When the image button is clicked
    View.OnClickListener imageOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
			Intent mIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(Intent.createChooser(mIntent, "Complete action using"), 0);
        }
    };
   
//image activity results
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 0){
			if (resultCode == Activity.RESULT_OK) {
				if (requestCode == 1)
				{
					mImageCaptureUri = data.getData();
				}
				Intent mIntent = new Intent("com.android.camera.action.CROP");
				mIntent.setClassName("com.android.gallery", "com.android.camera.CropImage");
				mIntent.setData(mImageCaptureUri);//Image to crop
				//(Number of cells * 74dip) - 2dip
				mIntent.putExtra("noFaceDetection", true);
				if(mWidgetSize=="Small")
				{
					mIntent.putExtra("outputX", 265);
					mIntent.putExtra("outputY", 265);
					mIntent.putExtra("aspectX", 1);
					mIntent.putExtra("aspectY", 1);
				}
				else
				{
					mIntent.putExtra("outputX", 424);
					mIntent.putExtra("outputY", 265);
					mIntent.putExtra("aspectX", 8);
					mIntent.putExtra("aspectY", 5);
				}
				mIntent.putExtra("return-data", true);
				startActivityForResult(mIntent, 1);
			}
		}
		//Encode image and put into preferences
		if (requestCode == 1){
		    if (resultCode == Activity.RESULT_OK) {
				final Bundle extras = data.getExtras();
				if (extras != null) {
					Bitmap photo = extras.getParcelable("data");
					//photo=getRoundedCornerBitmap(photo);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();  
					photo.compress(Bitmap.CompressFormat.JPEG, 100, baos); //photo is the bitmap object   
					byte[] b = baos.toByteArray();  
					char[] cvalue=Base64Coder.encode(b);
					mImage = new String(cvalue);
				}
		    }
		}
	}
    
//Color picked
    private OnColorPickedListener mColorPickerListener = new OnColorPickedListener(){
   	 public void colorPicked(int color) {
   		mSelectedColor=color;
   		 mImgSelectedColor.setBackgroundColor(mSelectedColor);
        }
   };
   
 // When the cancel button is clicked
    View.OnClickListener cancelOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
        	//Just close the window
            finish();  
        }
    };
    
// When the ok button is clicked
    View.OnClickListener okOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            EditText mTxtTitle=(EditText)findViewById(R.id.configure_title);
            
            //Save Data to shared preferences
			SharedPreferences mPrefs = self.getSharedPreferences(PREFS_NAME, 0);
			SharedPreferences.Editor edit = mPrefs.edit();
			edit.putString("Date-" + mAppWidgetId, mTxtDate.getText().toString());
			edit.putString("Title-" + mAppWidgetId, mTxtTitle.getText().toString());
			edit.putString("Color-" + mAppWidgetId, Integer.toString(mSelectedColor));
			edit.putString("Image-" + mAppWidgetId, mImage);
			edit.putString("WidgetSize-" + mAppWidgetId, mWidgetSize);
			edit.commit();
			
			// fire an update to display initial state of the widget
			PendingIntent updatepending = customizable_countdown_widget.makeControlPendingIntent(self,"update", mAppWidgetId);
			try {
				updatepending.send();
			} catch (CanceledException e) {
				Toast.makeText(getApplicationContext(), "Update of Widget Failed", Toast.LENGTH_SHORT).show();
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
    
    //Function to round corners of background image
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
            bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 12;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
      }
}
