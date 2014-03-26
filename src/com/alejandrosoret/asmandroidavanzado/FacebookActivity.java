package com.alejandrosoret.asmandroidavanzado;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;

public class FacebookActivity extends android.support.v4.app.FragmentActivity {
	
	private static final String FACEBOOK_PUBLISH_PERMISSION = "publish_actions";
	
	public static final String imageParams = "ImageParams";
	public static final String locationParams = "LocationParams";
	
	private Bitmap mImage = null;
	private String mLocation = null;
	
	private AlertDialog mDialog = null;
	private Session.StatusCallback statusCallback = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_facebook);
		
		Bundle params = getIntent().getExtras();
		byte[] byteArray = params.getByteArray(imageParams);
		mImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
		mLocation = params.getString(locationParams, "");
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
			.setTitle(R.string.publishOnFacebook_title)
			.setMessage(R.string.publishOnFacebook_message)
			.setIcon(R.drawable.com_facebook_logo);
		mDialog = builder.create();
		
		Session session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) session = Session.restoreSession(this, null, (StatusCallback) this, savedInstanceState);
			if (session == null) session = new Session(this);
			Session.setActiveSession(session);
			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
				statusCallback = new Session.StatusCallback() {
					@Override
					public void call(Session session, SessionState state, Exception exception) {
						if(exception != null) {
							Log.d( "Error OnCreate():", exception.getMessage());
							ShowErrorDialogAndFinish();
						}
					}
				};
				session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback)); 
			}
		}
		
	}

    @Override
    protected void onStart() {
         super.onStart();
         if (statusCallback == null) {
        	 statusCallback = new Session.StatusCallback() {
					@Override
					public void call(Session session, SessionState state, Exception exception) {
						if(exception != null) {
							Log.d( "Error OnStart():", exception.getMessage());
							ShowErrorDialogAndFinish();
						}
					}
				};
         }
         Session.getActiveSession().addCallback(statusCallback);
         Post();
    }
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult( requestCode, resultCode, data );
         Session.getActiveSession().onActivityResult( this, requestCode, resultCode, data );
         if( requestCode == 64206 && resultCode == 0 ) this.finish();
    }   
    
    @Override
    protected void onStop() {
         super.onStop();
         Session.getActiveSession().removeCallback(statusCallback);
    }
    
    private void ShowErrorDialogAndFinish() {
    	mDialog.dismiss();
    	new AlertDialog.Builder(this)
    		.setTitle(R.string.publishOnFacebook_message)
    		.setMessage(R.string.publishOnFacebook_message)
    		.setIcon(R.drawable.com_facebook_logo)
    		.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			})
			.show();
    }
    
    private void Post() {
    	Session session = Session.getActiveSession();
    	if (session.isOpened()) {
    		mDialog.show();
    		doPublish();
    	}
    	else {
    		doLogin();
    	}
    }
    
    private void doLogin() {
    	Session session = Session.getActiveSession();
    	if (!session.isOpened() && !session.isClosed()) {
    		session.openForRead(new Session.OpenRequest(this).setCallback(new Session.StatusCallback() {
				@Override
				public void call(Session session, SessionState state, Exception exception) {
					if(exception != null) {
						Log.d( "Error doLogin():", exception.getMessage());
						ShowErrorDialogAndFinish();
					}
				}
			}));
    	}
    }
    
    private void doLogout() {

    }
    
    private void doPublish() {
    	Session session = Session.getActiveSession();
    	if (!session.getPermissions().contains(FACEBOOK_PUBLISH_PERMISSION)) {
    		mDialog.setMessage(getString(R.string.connectingToFacebook));
    		Session.NewPermissionsRequest request = new Session.NewPermissionsRequest(this, FACEBOOK_PUBLISH_PERMISSION);
    		request.setCallback(new Session.StatusCallback() {
				@Override
				public void call(Session session, SessionState state, Exception exception) {
					if (session.isOpened()) {
						doPublish();
					}
					else if (exception != null) {
						Log.d( "Error doPublish():", exception.getMessage());
						doLogout();
					}
				}
			});
    		session.requestNewPublishPermissions(request);
    	}
    	else doPublishMessage();
    }
    
    private void doPublishMessage() {
    	mDialog.setMessage(getString(R.string.publishOnFacebook_title));
    	String Message = "Picture Info:\n" + mLocation;
    	
    	Request requestMessage = Request.newStatusUpdateRequest(Session.getActiveSession(), Message, new Request.Callback() {
			@Override
			public void onCompleted(Response response) {
				if (response.getError() == null) {
					doPublishImage();
				}
				else {
					doLogout();
				}
			}
		});
    	requestMessage.executeAsync();
    }
    
    private void doPublishImage() {
    	mDialog.setMessage(getString(R.string.publishOnFacebook_title));
    	Request requestPicture = Request.newUploadPhotoRequest(Session.getActiveSession(), mImage, new Request.Callback() {
			@Override
			public void onCompleted(Response response) {
				if (response.getError() == null) {
					doLogout();
				}
				else {
					doLogout();
				}
			}
		});
    	requestPicture.executeAsync();
    }
}
