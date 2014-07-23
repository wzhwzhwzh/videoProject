package com.wzhscript;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
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
	final String workFolder = "/sdcard/videokit/";//TODO: get the work folder dynamically
	final int frameRate = 25;


	public void combineVideo(Context ctx, String video1, String video2, String final_video, int transDur){
		String imgDir = workFolder + "img/";
		String tmpFolder = workFolder;
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
		
		String tmp_video1 = tmpFolder + "temp_video1.mp4", tmp_video2 = tmpFolder + "temp_video2.mp4";
		paddingAndResizing(ctx, vk, video1, video2, tmp_video1, tmp_video2);
		
		//get the duration of video1
		int msec = MediaPlayer.create(ctx, Uri.fromFile(new File(tmp_video1))).getDuration();
		duration1 = (int)TimeUnit.MILLISECONDS.toSeconds(msec);
		
		//get the first part of video1
		String commandStr = "ffmpeg -y -i " + tmp_video1 + " -strict experimental -t "+ (duration1 - transDur) +" " + part1;
		vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, ctx);
		Log.d(Prefs.TAG, "get the first part of video1...");
		
		//get the second part of video2
		commandStr = "ffmpeg -y -i "+tmp_video2+" -strict experimental -ss " + transDur + " " + part2;
		vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, ctx);
		Log.d(Prefs.TAG, "get the second part of video2...");
		
		//get frames of transition
		commandStr = "ffmpeg -y -i " + tmp_video1 + " -ss "+ (duration1 - transDur) +" -r "+ frameRate +" "+ imgDir + imgFormat1;
		vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, ctx);
		
		commandStr = "ffmpeg -y -i " + tmp_video2 + " -t "+ transDur +" -r "+ frameRate +" "+ imgDir + imgFormat2;
		vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, ctx);
		Log.d(Prefs.TAG, "get frames of transition...");
		
		//picture processing
		generateTransitionImages(imgDir, imgFormat1, imgFormat2, transFormat3, transDur);
		
		//get transition audio
		commandStr = "ffmpeg -i "+ tmp_video1 +" -ss 3 -f mp3 " + audio1;
		vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, ctx);
		
		commandStr = "ffmpeg -i " + tmp_video2 + " -t 2 -f mp3 " + audio2;
		vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, ctx);
		
		commandStr = "ffmpeg -i "+ audio1 +" -i "+ audio2 +" -filter_complex amix=inputs=2:duration=shortest "+ transAudio;
		vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, ctx);
		Log.d(Prefs.TAG, "get transition audio");
		
		//get transition video
		commandStr = "ffmpeg -y -f image2 -i "+ imgDir + transFormat3 +" "+ transVideo;
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
	
	
	/** make sure temp_video1 and temp_video2 have the same aspect ratio and resolution
	 * @param ctx
	 * @param video1, source video path
	 * @param video2, source video path
	 * @param temp_video1, target video path
	 * @param temp_video2, targei video path
	 */
	private void paddingAndResizing(Context ctx, LoadJNI vk, String video1, String video2, String temp_video1, String temp_video2){
		Map resolution1, resolution2;
		double ratio1, ratio2, maxRatio;
		double width1, height1, width2, height2, targetWidth, targetHeight;
		
		resolution1 = getVideoResolution(ctx, vk, video1);
		resolution2 = getVideoResolution(ctx, vk, video2);

		width1 = Double.parseDouble(resolution1.get("width").toString());
		width2 = Double.parseDouble(resolution2.get("width").toString());
		height1 = Double.parseDouble(resolution1.get("height").toString());
		height2 = Double.parseDouble(resolution2.get("height").toString());
		
		//get maxRatio
		ratio1 = width1/height1;
		ratio2 = width2/height2;		
		maxRatio = Math.max(ratio1, ratio2);
		maxRatio = Math.max(maxRatio, 4.0/3);	

		//get target width and height
		targetWidth = Math.min(width1, maxRatio*height1);
		targetWidth = Math.min(targetWidth, width2);
		targetWidth = Math.min(targetWidth, maxRatio*height2);
		
		targetHeight = targetWidth/maxRatio;
		
		//padding and resizing
		if(width1==targetWidth&&height1==targetHeight){
			
			temp_video1 = video1;
			
		}else if(ratio1 == maxRatio && height1 != targetHeight){
			
			resizeVideo(ctx, vk, targetWidth + "x" + targetHeight, video1, temp_video1);
			
		}else if(ratio1 < maxRatio && height1 == targetHeight){//resizing isn't needed as the width1 will equal targetWidth after padding
			
			paddingVideo(ctx, vk, "pad=ih*"+ maxRatio +":ih:(ow-iw)/2:0", video1, temp_video1);
			
		}else if(ratio1 < maxRatio && height1 != targetHeight){
			
			String temp = workFolder + "tmp_wider_video.mp4";
			paddingVideo(ctx, vk, "pad=ih*"+ maxRatio +":ih:(ow-iw)/2:0", video1, temp);
			resizeVideo(ctx, vk, targetWidth + "x" + targetHeight, temp, temp_video1);
			
		}

		
		if(width2==targetHeight&&height2==targetHeight){
			
			temp_video2 = video2;
			
		}else if(ratio2 == maxRatio && height2 != targetHeight){
			
			resizeVideo(ctx, vk, targetWidth + "x" + targetHeight, video2, temp_video2);
			
		}else if(ratio2 < maxRatio && height2 == targetHeight){//resizing isn't needed as the width1 will equal targetWidth after padding
			
			paddingVideo(ctx, vk, "pad=ih*"+ maxRatio +":ih:(ow-iw)/2:0", video2, temp_video1);
			
		}else if(ratio2 < maxRatio && height2 != targetHeight){
			
			String temp = workFolder + "tmp_wider_video.mp4";
			paddingVideo(ctx, vk, "pad=ih*"+ maxRatio +":ih:(ow-iw)/2:0", video2, temp);
			resizeVideo(ctx, vk, targetWidth + "x" + targetHeight, temp, temp_video2);
			
		}
	}
	
	/** get video resolution width*height
	 * @param ctx
	 * @param vk
	 * @param video, the path of video
	 * @return
	 */
	private Map getVideoResolution(Context ctx, LoadJNI vk, String video){
		Map<String, Integer> resolution = new HashMap();
		String tempImage = "tmp_image.jpeg";
		String commandStr = "ffmpeg -y -i " + video +" -r "+ frameRate +" -vframes 1 "+ workFolder + tempImage;
		vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, ctx);
		
		Bitmap bitmap = BitmapFactory.decodeFile(workFolder+tempImage);
		resolution.put("width", bitmap.getWidth());
		resolution.put("height", bitmap.getHeight());
			
		return resolution;
	}
	
	/**resize video
	 * @param ctx
	 * @param vk
	 * @param size
	 * @param fromVideo
	 * @param toVideo
	 */
	private void resizeVideo(Context ctx, LoadJNI vk, String size, String fromVideo, String toVideo){
		String commandStr = "ffmpeg -y -i " + fromVideo +" -r "+ frameRate +" -strict experimental -s " + size + " "+ toVideo;
		vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, ctx);		
	}
	
	/**resize video
	 * @param ctx
	 * @param vk
	 * @param pad
	 * @param fromVideo
	 * @param toVideo
	 */
	private void paddingVideo(Context ctx, LoadJNI vk, String pad, String fromVideo, String toVideo){
		String commandStr = "ffmpeg -i "+ fromVideo +" -strict experimental -vf pad="+ pad +" "+ toVideo;
		vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, ctx);
	}
}
