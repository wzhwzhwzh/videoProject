package com.wzhscript;

import java.io.File;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.examples.ffmpeg4android_demo_native.GeneralUtils;
import com.examples.ffmpeg4android_demo_native.Prefs;
import com.netcompss.loader.LoadJNI;

public abstract class Transition {
	final String workFolder = "/sdcard/videokit";//TODO: get the work folder dynamically
	final int frameRate = 25;


	public void combineVideo(Context ctx, String video1, String video2, String final_video, int transDur){
		String imgDir = workFolder + "img/";
		String imgFormat1 = "image1-%d.jpeg";
		String imgFormat2 = "image2-%d.jpeg";
		String transFormat3 = "transition-%d.jpeg";
		String part1 = workFolder + "transTemp1.mp4";
		String part2 = workFolder + "transTemp2.mp4";
		String transPart = workFolder + "transition.mp4";
		String transVideo = workFolder + "transition_temp.mp4";
		String audio1 = workFolder + "audio1.mp3";
		String audio2 = workFolder + "audio2.mp3";
		String transAudio = workFolder + "audio.mp3";		
		int duration1 = 0;
		
 		// delete previous log
 		GeneralUtils.deleteFileUtil(workFolder + "/vk.log");
 		
		LoadJNI vk = new LoadJNI();
		//get the duration of video1
		int msec = MediaPlayer.create(ctx, Uri.fromFile(new File(video1))).getDuration();
		duration1 = (int)TimeUnit.MILLISECONDS.toSeconds(msec);
		
		//get the first part of video1
		String commandStr = "ffmpeg -y -i " + video1 + " -strict experimental -t "+ (duration1 - transDur) +" " + part1;
		vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, ctx);
		Log.d(Prefs.TAG, "get the first part of video1...");
		
		//get the second part of video2
		commandStr = "ffmpeg -y -i "+video2+" -strict experimental -ss " + transDur + " " + part2;
		vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, ctx);
		Log.d(Prefs.TAG, "get the second part of video2...");
		
		//get frames of transition
		commandStr = "ffmpeg -y -i " + video1 + " -ss "+ (duration1 - transDur) +" -r "+ frameRate +" "+ imgDir + imgFormat1;
		vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, ctx);
		
		commandStr = "ffmpeg -y -i " + video2 + " -t "+ transDur +" -r "+ frameRate +" "+ imgDir + imgFormat2;
		vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, ctx);
		Log.d(Prefs.TAG, "get frames of transition...");
		
		//picture processing
		generateTransitionImages(imgDir, imgFormat1, imgFormat2, transFormat3, transDur);
		
		//get transition audio
		commandStr = "ffmpeg -i "+ video1 +" -ss 3 -f mp3 " + audio1;
		vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, ctx);
		
		commandStr = "ffmpeg -i " + video2 + " -t 2 -f mp3 " + audio2;
		vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, ctx);
		
		commandStr = "ffmpeg -i "+ audio1 +" -i "+ audio2 +" -filter_complex amix=inputs=2:duration=shortest "+ transAudio;
		vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, ctx);
		Log.d(Prefs.TAG, "get transition audio");
		
		//get transition video
		commandStr = "ffmpeg -y -f image2 -i "+ transFormat3 +" "+ transVideo;
		vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, ctx);
		
		commandStr = "ffmpeg -i "+ transVideo +" -i "+ transAudio +" -vcodec copy -acodec copy "+ transPart;
		vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, ctx);
		Log.d(Prefs.TAG, "get transition video");
		
		//get full video
		String[] complexCommand = {"ffmpeg","-y","-i", part1, "-i", transPart, "-i", part2, "-strict","experimental", "-filter_complex", "[0:0] [0:1] [1:0] [1:1] [2:0] [2:1] concat=n=3:v=1:a=1", final_video};
		vk.run(complexCommand, workFolder, ctx);
		Log.d(Prefs.TAG, "get full video");
	
	}
	
	/**generateTransitionImages 
	 * @param imgFormat1
	 * @param imgFormat2
	 * @param imgFormat3
	 */
	protected abstract void generateTransitionImages(String imgDir, String imgFormat1, String imgFormat2, String imgFormat3, int transDur);
}
