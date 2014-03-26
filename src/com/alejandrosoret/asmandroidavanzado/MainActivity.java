package com.alejandrosoret.asmandroidavanzado;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alejandrosoret.asmandroidavanzado.ShakeDetector.OnShakeListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends android.support.v4.app.FragmentActivity {

	private static final int TAKE_PICTURE_RQ = 69;

	private Button mTakePicture = null;
	private Button mDiscardPicture = null;
	private Bitmap mCurrentPicture = null;
	private ImageView mImage = null;
	private TextView mLocationInfo = null;
	private Button mPublishOnFacebook = null;
	private LocationManager mLocationManager = null;
	private GoogleMap mMap = null;

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private ShakeDetector mShakeDetector;

	private Location mCurrentLocation = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Pillamos las vistas
		mTakePicture = (Button) findViewById(R.id.btn_take_picture);
		mDiscardPicture = (Button) findViewById(R.id.btn_discard_picture);
		mImage = (ImageView) findViewById(R.id.picture);
		mLocationInfo = (TextView) findViewById(R.id.location_info);
		mPublishOnFacebook = (Button) findViewById(R.id.btn_publish_on_facebook);
		mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.locationMap)).getMap();

		activateControls(false);

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// ShakeDetector initialization
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mShakeDetector = new ShakeDetector();
		mShakeDetector.setOnShakeListener(new OnShakeListener() {
			@Override
			public void onShake(int count) {
				activateControls(false);
			}
		});

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
		
		// Botón de Féisbul
		mPublishOnFacebook.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				publishOnFacebook();
			}
		});

	}

	@Override
	public void onResume() {
		super.onResume();
		// Add the following line to register the Session Manager Listener onResume
		mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
	}

	@Override
	public void onPause() {
		// Add the following line to unregister the Sensor Manager onPause
		mSensorManager.unregisterListener(mShakeDetector);
		super.onPause();
	}

	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		if (requestCode == TAKE_PICTURE_RQ) {
			if (resultCode == RESULT_OK) {
				Bundle extras = data.getExtras();
				mCurrentPicture = (Bitmap) extras.get("data");
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
		mDiscardPicture.setEnabled(b);
		if (!b) mCurrentPicture = null;
		mImage.setImageBitmap(mCurrentPicture);
		if (!b) mLocationInfo.setText(R.string.unavailable);
		mLocationInfo.setText( ( b ) ? GetLocationInfo() : "" );
		mPublishOnFacebook.setEnabled(b);
		if (!b) mMap.clear();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public String GetLocationInfo() {
		String LocationInfo = "";
		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			mCurrentLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if(mCurrentLocation != null) {
				LocationInfo = "Latitud: " + mCurrentLocation.getLatitude() + "\r\nLongitud: "	+ mCurrentLocation.getLongitude();
				new FindLocationInfo().execute(mCurrentLocation);
			}
		}
		return LocationInfo;
	}

	private class FindLocationInfo extends AsyncTask<Location, Void, String> {

		@Override
		protected String doInBackground(Location... params) {
			String LocationString = null;
			Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

			Location CurrentLocation = params[0];
			List<Address> addresses = null;
			try { addresses = geocoder.getFromLocation(CurrentLocation.getLatitude(), CurrentLocation.getLongitude(), 1); }
			catch (IOException e) {
				Log.d("Geocoder error", e.getMessage());
			}

			if (addresses != null && addresses.size() > 0 ) {
				Address address = addresses.get(0);
				LocationString = String.format("%s, %s, %s",
						address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
								address.getLocality(),
								address.getCountryName());
			}
			
			return LocationString;
		}

		@Override
		protected void onPostExecute(String LocationInfo) {
			if (LocationInfo != null) {
				mLocationInfo.append ("\r\n" + LocationInfo);
				LatLng position = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
				mMap.addMarker(new MarkerOptions().position(position).title("Marker"));
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 10));
			}
			else {
				mLocationInfo.append ("\r\n" + "Error al detectar la localización");
			}
		}

	}
	
	public void publishOnFacebook() {
		
		if (mCurrentPicture != null) {
			new AlertDialog.Builder(this)
				.setTitle(R.string.publishOnFacebook_title)
				.setMessage(R.string.publishOnFacebook_message)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						mCurrentPicture.compress(Bitmap.CompressFormat.PNG, 100, stream);
						byte[] byteArray = stream.toByteArray();
						
						Intent intent = new Intent(MainActivity.this, FacebookActivity.class);
						intent.putExtra(FacebookActivity.imageParams, byteArray);
						intent.putExtra(FacebookActivity.locationParams, mCurrentLocation);
						startActivity(intent);
					}
				})
				.setNegativeButton(R.string.no, null)
				.setIcon(R.drawable.com_facebook_logo)
				.show();
		}
		
	}

}
