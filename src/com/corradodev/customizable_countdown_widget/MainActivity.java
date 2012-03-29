package com.corradodev.customizable_countdown_widget;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Button button = (Button)findViewById(R.id.button1);

	    button.setOnClickListener(new OnClickListener() {
	      public void onClick(View arg0) {
	        Intent viewIntent =
	          new Intent("android.intent.action.VIEW",
	            Uri.parse("http://www.youtube.com/watch?v=ZMsqSQzV6nw"));
	          startActivity(viewIntent);
	      }
	    });
    }
}