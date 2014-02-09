package com.alejandrosoret.asmandroidavanzado;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private static final int TAKE_PICTURE_RQ = 69;
	
	private Button mTakePicture = null;
	private ImageView mImage = null;
	private TextView mLocationInfo = null;
	private Button mDiscardPicture = null;
	private Button mPublishOnFacebook = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Pillamos las vistas
        mTakePicture = (Button) findViewById(R.id.btn_take_picture);
        mImage = (ImageView) findViewById(R.id.picture);
        mLocationInfo = (TextView) findViewById(R.id.location_info);
        mDiscardPicture = (Button) findViewById(R.id.btn_discard_picture);
        mPublishOnFacebook = (Button) findViewById(R.id.btn_publish_on_facebook);
        
        activateControls(false);
        
        // Botón de hacer la foto
        mTakePicture.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(intent, TAKE_PICTURE_RQ);
			}
		});
        
        mDiscardPicture.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activateControls(false);
			}
		});
        
    }

	@Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	if (requestCode == TAKE_PICTURE_RQ) {
    		if (resultCode == RESULT_OK) {
    			Bundle extras = data.getExtras();
    			Bitmap bitmap = (Bitmap) extras.get("data");
    			mImage.setImageBitmap(bitmap);
    			activateControls(true);
    		}
    		else if (resultCode == RESULT_CANCELED); {
    			// El usuario canceló la captura de foto
    		}
    		/*else -- joder, no sé por qué me da error de sintaxis si pongo el else!!! */ {
    			// La captura falló, avisar al usuario
    		}
    	}
	}
    
    private void activateControls(boolean b) {
		mTakePicture.setEnabled(!b);
		if (!b) mImage.setImageBitmap(null);
		if (!b) mLocationInfo.setText(R.string.unavailable);
		mLocationInfo.setEnabled(b);
		mDiscardPicture.setEnabled(b);
		mPublishOnFacebook.setEnabled(b);
	}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
