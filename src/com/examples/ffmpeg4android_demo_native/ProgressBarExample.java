package com.examples.ffmpeg4android_demo_native;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.netcompss.loader.LoadJNI;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ProgressBarExample extends Activity  {
	
	public ProgressDialog progressBar;
	
	final String workFolder = "/sdcard/videokit";
	final String demoVideoPath = workFolder + "/in.mp4";
	final String vkLogPath = workFolder + "/vk.log";
	
	private String _durationOfCurrent;
	private long _lastVklogSize = -1;
	private int _vkLogNoChangeCounter = 0;
	private SimpleDateFormat _simpleDateFormat;
	long _timeRef = -1;
	int  _prevProgress = 0;
	LoadJNI vk;
	
	
	@Override
	  public void onCreate(Bundle savedInstanceState) {
	      super.onCreate(savedInstanceState);

	      setContentView(R.layout.ffmpeg_demo_client);
	      Button invoke =  (Button)findViewById(R.id.invokeButton);
	      invoke.setOnClickListener(new OnClickListener() {
				public void onClick(View v){
					Log.i(Prefs.TAG, "run clicked.");
					runTranscoding();
				}
			});
	}
	
	private Handler handler = new Handler() {
        	@Override
        	public void handleMessage(Message msg) {
        		Log.i(Prefs.TAG, "Handler got message");
        		if (progressBar != null) {
        			progressBar.dismiss();
        			
        			// stopping the transcoding native
        			if (msg.what == -1) {
        				Log.i(Prefs.TAG, "Got cancel message, calling fexit");
        				vk.fExit(getApplicationContext());
        				
        			
        			}
        		}
            

        }
    };
	
	public void runTranscoding() {
		  progressBar = new ProgressDialog(ProgressBarExample.this);
		  progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		  progressBar.setTitle("FFmpeg4Android Direct JNI");
		  progressBar.setMessage("Press the cancel button to end the operation");
		  progressBar.setMax(100);
		  progressBar.setProgress(0);
		  
		  progressBar.setCancelable(false);
		  progressBar.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
		      @Override
		      public void onClick(DialogInterface dialog, int which) {
		    	  handler.sendEmptyMessage(-1);
		      }
		  });
		  
		  progressBar.show();
		  
		  
		 
		  
		  
	      //progressBar = ProgressDialog.show(MainActivity.this, "","Scanning Please Wait",true);
	      new Thread() {
	          public void run() {
	        	  Log.d(Prefs.TAG,"Worker started");
	              try {
	                  //sleep(5000);
	            	  runTranscodingUsingLoader();
	                  handler.sendEmptyMessage(0);

	              } catch(Exception e) {
	                  Log.e("threadmessage",e.getMessage());
	              }
	          }
	      }.start();
	      
	      // Progress update thread
	      new Thread() {
	          public void run() {
	        	  Log.d(Prefs.TAG,"Progress update started");
	        	  int i = 0;
	        	  int progress = -1;
	        	  try {
	        		  while (true) {
	        			  sleep(300);
	        			  progress = calcProgress();
	        			  if (progress != 0 && progress < 100) {
	        				  progressBar.setProgress(progress);
	        			  }
	        			  else if (progress == 100) {
	        				  Log.i(Prefs.TAG, "==== progress is 100, exiting Progress update thread");
	        				  initCalcParamsForNextInter();
	        				  break;
	        			  }
	        		  }

	        	  } catch(Exception e) {
	        		  Log.e("threadmessage",e.getMessage());
	        	  }
	          }
	      }.start();
	  }
	
	private void runTranscodingUsingLoader() {
		Log.i(Prefs.TAG, "runTranscodingUsingLoader started...");
		
		_simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SS");
		try {
			Date ref = _simpleDateFormat.parse("00:00:00.00");
			ref.setYear(112);
			_timeRef = ref.getTime();
		} catch (ParseException e) {
			Log.w(Prefs.TAG, "failed to set _timeRef");
		}
 		
 		// delete previous log
 		GeneralUtils.deleteFileUtil(vkLogPath);
 		
 		PowerManager powerManager = (PowerManager)ProgressBarExample.this.getSystemService(ProgressBarExample.this.POWER_SERVICE);
		WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "VK_LOCK"); 
		Log.d(Prefs.TAG, "Acquire wake lock");
		wakeLock.acquire();
 		
 		//String[] complexCommand = {"ffmpeg","-threads", "0", "-y" ,"-i", "/sdcard/videokit/in.mp4","-strict","experimental", "-vf", "crop=iw/2:ih:0:0,split[tmp],pad=2*iw[left]; [tmp]hflip[right]; [left][right] overlay=W/2", "-vcodec", "mpeg4", "-vb", "20M", "-r", "23.956", "/sdcard/videokit/out.mp4"};
 		//String commandStr = "ffmpeg -y -i /sdcard/videokit/sample.mp4 -strict experimental -s 320x240 -r 30 -aspect 3:4 -ab 48000 -ac 2 -ar 22050 -vcodec mpeg4 -b 2097152 /sdcard/videokit/out.mp4";
 		//String[] complexCommand = {"ffmpeg","-threads", "0", "-y" ,"-i", "/sdcard/videokit/sample.mp4","-strict","experimental", "-vf", "crop=iw/2:ih:0:0,split[tmp],pad=2*iw[left]; [tmp]hflip[right]; [left][right] overlay=W/2", "-vcodec", "mpeg4", "-vb", "20M", "-r", "23.956", "/sdcard/videokit/out.mp4"};
 		
 		EditText commandText = (EditText)findViewById(R.id.CommandText);
		String commandStr = commandText.getText().toString();
		
		
		vk = new LoadJNI();
		try {
			
			//vk.run(complexCommand, workFolder, getApplicationContext());
			vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, getApplicationContext());
			
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

		// finished Toast
		final String status = GeneralUtils.getReturnCodeFromLog(vkLogPath);
 		ProgressBarExample.this.runOnUiThread(new Runnable() {
			  public void run() {
				  Toast.makeText(ProgressBarExample.this, status, Toast.LENGTH_LONG).show();
				  if (status.equals("Transcoding Status: Failed")) {
					  Toast.makeText(ProgressBarExample.this, "Check: " + vkLogPath + " for more information.", Toast.LENGTH_LONG).show();
				  }
			  }
			});
		
		
		
		
	}
	
	private void initCalcParamsForNextInter() {
		Log.i(Prefs.TAG, "initCalcParamsForNextInter");
		
		Log.i(Prefs.TAG, "Init _lastVklogSize");
		_lastVklogSize = -1;

	}
	
	
	private int _durationOfCurrentWaitIndex = 0;
	private final int DURATION_OF_CURRENT_WAIT_INDEX_LIMIT = 6;
			
	private int calcProgress() {
		//Log.i(Prefs.TAG, "========calc progress=======");
		int progress  = 0;
		if (_durationOfCurrent == null) {
			String dur = GeneralUtils.getDutationFromVCLogRandomAccess(vkLogPath);
			Log.d(Prefs.TAG, "dur: " + dur);
			if (dur == null || dur.equals("") || dur.equals("null") ) {
				Log.i(Prefs.TAG, "dur is not good, not setting ");
				if (_durationOfCurrentWaitIndex < DURATION_OF_CURRENT_WAIT_INDEX_LIMIT) {
					Log.i(Prefs.TAG, "waiting for real duration, going out of calcProgress with 0");
					_durationOfCurrentWaitIndex ++;
					return 0;
				}
				else {
					Log.i(Prefs.TAG, "_durationOfCurrentWaitIndex is equal to: " + DURATION_OF_CURRENT_WAIT_INDEX_LIMIT + " reseting.");
					_durationOfCurrentWaitIndex = 0;
					Log.i(Prefs.TAG, "setting fake Prefs.durationOfCurrent");

					_durationOfCurrent = "00:03:00.00";
					Log.w(Prefs.TAG, "setting fake Prefs.durationOfCurrent (Cant get from file): " + _durationOfCurrent);

				}
			}
			else {
				_durationOfCurrent = GeneralUtils.getDutationFromVCLogRandomAccess(vkLogPath);
				Log.i(Prefs.TAG, "Got real duration: " + _durationOfCurrent);
			}
		}

		
		if (_durationOfCurrent != null) {
			long currentVkLogSize = -1;
			
			currentVkLogSize = GeneralUtils.getVKLogSizeRandomAccess(vkLogPath);
			//Log.d(Prefs.TAG, "currentVkLogSize: " + currentVkLogSize + " _lastVklogSize: " + _lastVklogSize);

			if (currentVkLogSize > _lastVklogSize) {
				_lastVklogSize = currentVkLogSize;
				_vkLogNoChangeCounter = 0;
			}
			else {
				Log.w(Prefs.TAG, "Looks like Vk log is not increasing in size");
				_vkLogNoChangeCounter++;
			}

			
			String currentTimeStr = GeneralUtils.readLastTimeFromVKLogUsingRandomAccess(vkLogPath);
			//Log.d(Prefs.TAG, "currentTimeStr: " + currentTimeStr);
			if (currentTimeStr.equals("exit")) {
				Log.d(Prefs.TAG, "============Found one of the exit tokens in the log============");
				return 100;
			}
			else if (currentTimeStr.equals("error") && _prevProgress == 0) {
				Log.d(Prefs.TAG, "============Found error in the log============");
				return 100;
			}
			else if (_vkLogNoChangeCounter > 16) {
				Log.e(Prefs.TAG, "VK log is not changing in size, and no exit token found");
				return 100;
			}
			try {
				Date durationDate = _simpleDateFormat.parse(_durationOfCurrent);
				Date currentTimeDate = _simpleDateFormat.parse(currentTimeStr);
				currentTimeDate.setYear(112);
				durationDate.setYear(112);
				//Log.d(Prefs.TAG, " durationDate: " + durationDate + " currentTimeDate: " + currentTimeDate);
				
				long durationLong = durationDate.getTime() - _timeRef;
				long currentTimeLong = currentTimeDate.getTime() - _timeRef;
				//Log.d(Prefs.TAG, " durationLong: " + durationLong + " currentTimeLong: " + currentTimeLong + " diff: " + (durationLong - currentTimeLong));
				progress  = Math.round(((float)currentTimeLong / durationLong) * 100);
				if (progress >= 100) {
					Log.w(Prefs.TAG, "progress is 100, but can't find exit in the log, probably fake progress, still running...");
					progress = 99;
				}
				_prevProgress = progress;

				
			} catch (ParseException e) {
				Log.w(Prefs.TAG, e.getMessage());
			}
		}
		
		return progress;
	}

}
