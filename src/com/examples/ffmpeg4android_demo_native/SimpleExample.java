package com.examples.ffmpeg4android_demo_native;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.netcompss.loader.LoadJNI;
import com.wzhscript.MakeTransition;

/**
 *  To run this Demo Make sure you have on your device this folder:
 *  /sdcard/videokit, 
 *  and you have in this folder a video file called in.mp4
 * @author elih
 *
 */
public class SimpleExample extends Activity implements OnItemSelectedListener {
	
	final String workFolder = "/sdcard/videokit";
	final String demoVideoPath = workFolder + "/in.mp4";
	
	
	@Override
	  public void onCreate(Bundle savedInstanceState) {
	      super.onCreate(savedInstanceState);
	      

	      setContentView(R.layout.ffmpeg_demo_client);
	      
		   Spinner spinner = (Spinner) findViewById(R.id.type_spinner);
		   // Create an ArrayAdapter using the string array and a default spinner layout
		   ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		           R.array.type_array, android.R.layout.simple_spinner_item);
		   // Specify the layout to use when the list of choices appears
		   adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		   // Apply the adapter to the spinner
		   spinner.setAdapter(adapter);
		   spinner.setOnItemSelectedListener(this);
	      
	      
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
	 		
	 		
	 		
	 		PowerManager powerManager = (PowerManager)_act.getSystemService(_act.POWER_SERVICE);
			WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "VK_LOCK"); 
			Log.d(Prefs.TAG, "Acquire wake lock");
			wakeLock.acquire();
			
			LoadJNI vk = new LoadJNI();
			try {
				EditText nameText1 = (EditText)findViewById(R.id.video1); 
				EditText nameText2 = (EditText)findViewById(R.id.video2);
				String video1 = workFolder + "/" + nameText1.getText();
				String video2 = workFolder + "/" + nameText2.getText();
				String final_video = workFolder + "/out_joint.mp4";
				MakeTransition transition = new MakeTransition(_act);
				Spinner spinner = (Spinner) findViewById(R.id.type_spinner);
				String type = spinner.getSelectedItem().toString();
				transition.concateVideosWithTransEffect(type, video1, video2, final_video, 2);
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

    public void onItemSelected(AdapterView<?> parent, View view, 
            int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }	
	 

}
