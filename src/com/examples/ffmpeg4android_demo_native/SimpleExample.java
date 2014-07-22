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
	 		String tmpDir = "/sdcard/videokit/img/";
	 		Log.i(Prefs.TAG, "doInBackground started...");
	 		
	 		// delete previous log
	 		GeneralUtils.deleteFileUtil(workFolder + "/vk.log");
	 		
	 		PowerManager powerManager = (PowerManager)_act.getSystemService(_act.POWER_SERVICE);
			WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "VK_LOCK"); 
			Log.d(Prefs.TAG, "Acquire wake lock");
			wakeLock.acquire();
	 		
	 		//String[] complexCommand = {"ffmpeg","-threads", "0", "-y" ,"-i", "/sdcard/videokit/in.mp4","-strict","experimental", "-vf", "crop=iw/2:ih:0:0,split[tmp],pad=2*iw[left]; [tmp]hflip[right]; [left][right] overlay=W/2", "-vcodec", "mpeg4", "-vb", "20M", "-r", "23.956", "/sdcard/videokit/out.mp4"};
	 		//String commandStr = "ffmpeg -y -i /sdcard/videokit/in.mp4 -strict experimental -s 320x240 -r 30 -aspect 3:4 -ab 48000 -ac 2 -ar 22050 -vcodec mpeg4 -b 2097152 /sdcard/videokit/out.mp4";

//			EditText commandText = (EditText)findViewById(R.id.CommandText);
//			String commandStr = commandText.getText().toString();
			
			LoadJNI vk = new LoadJNI();
			try {
				//get the first part of video1
				String commandStr = "ffmpeg -y -i /sdcard/videokit/part1.mp4 -strict experimental -t 3 /sdcard/videokit/transTemp1.mp4";
				vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, getApplicationContext());
				Log.d(Prefs.TAG, "get the first part of video1...");
				
				//get the second part of video2
				commandStr = "ffmpeg -y -i /sdcard/videokit/part2.mp4 -strict experimental -ss 2 /sdcard/videokit/transTemp2.mp4";
				vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, getApplicationContext());
				Log.d(Prefs.TAG, "get the second part of video2...");
				
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inPreferredConfig = Bitmap.Config.ARGB_8888;
				
				//get frames of transition
				commandStr = "ffmpeg -y -i /sdcard/videokit/part1.mp4 -ss 3 -r 25 /sdcard/videokit/img/image1-%d.jpeg";
				vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, getApplicationContext());
				
				commandStr = "ffmpeg -y -i /sdcard/videokit/part2.mp4 -t 2 -r 25 /sdcard/videokit/img/image2-%d.jpeg";
				vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, getApplicationContext());
				Log.d(Prefs.TAG, "get frames of transition...");
				
				//picture processing
				int i = 1, alpha;
				double sum;
				String img1 = String.format("image1-%d.jpeg", i);
				String img2 = String.format("image2-%d.jpeg", i);
				Canvas canvas;
				Bitmap bitmap1 = null, bitmap2 = null, combined;
				while(GeneralUtils.checkIfFileExistAndNotEmpty(tmpDir+img1)||GeneralUtils.checkIfFileExistAndNotEmpty(tmpDir+img2)){
					Log.d(Prefs.TAG, "start frame processing:" + i);
					if(GeneralUtils.checkIfFileExistAndNotEmpty(tmpDir+img1)){
						bitmap1 = BitmapFactory.decodeFile(tmpDir+img1, options);
					}
					if(GeneralUtils.checkIfFileExistAndNotEmpty(tmpDir+img2)){
						bitmap2 = BitmapFactory.decodeFile(tmpDir+img2, options);
					}
					
					
					combined = Bitmap.createBitmap(bitmap2.getWidth(), bitmap2.getHeight() , Bitmap.Config.ARGB_8888);
					canvas = new Canvas(combined); 

	    		    Paint paint1 = new Paint();
	    		    Paint paint2 = new Paint();
	    		    sum = i>50? i:50;
	    		    alpha = (int)(255*(1 - i/sum));
	    		    paint1.setAlpha(alpha);//TODO: 50 should be variable
	    		    paint2.setAlpha(255 - alpha);

	    		    canvas.drawBitmap(bitmap1, 0f, 0f, paint1);
	    		    canvas.drawBitmap(bitmap2, 0f, 0f, paint2); 
	    		    
	    		    GeneralUtils.saveImg(Bitmap.CompressFormat.JPEG, tmpDir+"transition-"+i+".jpeg", combined);
	    		    
	    		    Log.d(Prefs.TAG, "frame " + i + " of transition, alpha:" + alpha);
	    		    i++;
	    		    img1 = String.format("image1-%d.jpeg", i);
	    		    img2 = String.format("image2-%d.jpeg", i);
				}
				
				//get transition audio
				commandStr = "ffmpeg -i /sdcard/videokit/part1.mp4 -ss 3 -f mp3 /sdcard/videokit/audio1.mp3";
				vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, getApplicationContext());
				
				commandStr = "ffmpeg -i /sdcard/videokit/part2.mp4 -t 2 -f mp3 /sdcard/videokit/audio2.mp3";
				vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, getApplicationContext());
				
				commandStr = "ffmpeg -i /sdcard/videokit/audio1.mp3 -i /sdcard/videokit/audio2.mp3 -filter_complex amix=inputs=2:duration=shortest /sdcard/videokit/audio.mp3";
				vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, getApplicationContext());
				Log.d(Prefs.TAG, "get transition audio");
				
				//get transition video
				commandStr = "ffmpeg -y -f image2 -i /sdcard/videokit/img/transition-%d.jpeg /sdcard/videokit/transition_temp.mp4";
				vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, getApplicationContext());
				
				commandStr = "ffmpeg -i /sdcard/videokit/transition_temp.mp4 -i /sdcard/videokit/audio.mp3 -vcodec copy -acodec copy /sdcard/videokit/transition.mp4";
				vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, getApplicationContext());
				Log.d(Prefs.TAG, "get transition video");
				
				//get full video
				String[] complexCommand = {"ffmpeg","-y","-i", "/sdcard/videokit/transTemp1.mp4", "-i", "/sdcard/videokit/transition.mp4", "-strict","experimental", "-filter_complex", "[0:0] [0:1] [1:0] [1:1] concat=n=2:v=1:a=1", "/sdcard/videokit/out_joint_temp.mp4"};
				vk.run(complexCommand, workFolder, getApplicationContext());
				
				String[] complexCommand2 = {"ffmpeg","-y","-i", "/sdcard/videokit/out_joint_temp.mp4", "-i", "/sdcard/videokit/transTemp2.mp4", "-strict","experimental", "-filter_complex", "[0:0] [0:1] [1:0] [1:1] concat=n=2:v=1:a=1", "/sdcard/videokit/out_joint.mp4"};
				vk.run(complexCommand2, workFolder, getApplicationContext());
				Log.d(Prefs.TAG, "get full video");
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
