package com.examples.ffmpeg4android_demo_native;

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.netcompss.loader.LoadJNI;

import com.wzhscript.*;

/**
 *  To run this Demo Make sure you have on your device this folder:
 *  /sdcard/videokit, 
 *  and you have in this folder a video file called in.mp4
 * @author elih
 *
 */
public class SimpleExample extends Activity {
	
	final String workFolder = "/sdcard/videokit";
	final String demoVideoPath = workFolder + "/in.mp4";
	
	
	@Override
	  public void onCreate(Bundle savedInstanceState) {
	      super.onCreate(savedInstanceState);
	      

	      setContentView(R.layout.ffmpeg_demo_client);
	      
	      
	      
	      Button invoke =  (Button)findViewById(R.id.invokeButton);
	      invoke.setOnClickListener(new OnClickListener() {
				public void onClick(View v){
					Log.i(Prefs.TAG, "run clicked.");
					if (GeneralUtils.checkIfFileExistAndNotEmpty(demoVideoPath)) {
						new TranscdingBackground(SimpleExample.this).execute();
					}
					else {
						Toast.makeText(getApplicationContext(), demoVideoPath + " not found", Toast.LENGTH_LONG).show();
					}
				}
			});
	}
	
	 public class TranscdingBackground extends AsyncTask<String, Integer, Integer>
	 {
	 	
	 	ProgressDialog progressDialog;
	 	Activity _act;
	 	
	 	public TranscdingBackground (Activity act) {
	 		_act = act;
	 	}
	
	 	
	 	
	 	@Override
	 	protected void onPreExecute() {
	 		progressDialog = new ProgressDialog(_act);
	 		progressDialog.setMessage("FFmpeg4Android Transcoding in progress...");
			progressDialog.show();
	 		
	 	}

	 	protected Integer doInBackground(String... paths) {
	 		Log.i(Prefs.TAG, "doInBackground started...");
	 		
	 		Transition transition = new FadeTransition();
	 		
	 		PowerManager powerManager = (PowerManager)_act.getSystemService(_act.POWER_SERVICE);
			WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "VK_LOCK"); 
			Log.d(Prefs.TAG, "Acquire wake lock");
			wakeLock.acquire();
			
			LoadJNI vk = new LoadJNI();
			try {
				String video1 = workFolder + "/part600x480.mp4";
				String video2 = workFolder + "/part1280x720.mp4";
				String final_video = workFolder + "/out_joint.mp4";
				transition.combineVideo(getApplicationContext(), video1, video2, final_video, 2);
			} catch (Throwable e) {
				Log.e(Prefs.TAG, "vk run exeption.", e);
			}
			finally {
				if (wakeLock.isHeld())
					wakeLock.release();
				else{
					Log.i(Prefs.TAG, "Wake lock is already released, doing nothing");
				}
			}
			Log.i(Prefs.TAG, "doInBackground finished");
	 		return Integer.valueOf(0);
	 	}

	 	protected void onProgressUpdate(Integer... progress) {
		}

	 	@Override
	 	protected void onCancelled() {
	 		Log.i(Prefs.TAG, "onCancelled");
	 		//progressDialog.dismiss();
	 		super.onCancelled();
	 	}


	 	@Override
	 	protected void onPostExecute(Integer result) {
	 		Log.i(Prefs.TAG, "onPostExecute");
	 		progressDialog.dismiss();
	 		super.onPostExecute(result);
	 		final String status = GeneralUtils.getReturnCodeFromLog(workFolder + "/vk.log");
	 		
	 		SimpleExample.this.runOnUiThread(new Runnable() {
				  public void run() {
					  Toast.makeText(SimpleExample.this, status, Toast.LENGTH_LONG).show();
					  if (status.equals("Transcoding Status: Failed")) {
						  Toast.makeText(SimpleExample.this, "Check: " + workFolder + "/vk.log" + " for more information.", Toast.LENGTH_LONG).show();
					  }
				  }
				});

	 	}
	
	 }
	 

}
