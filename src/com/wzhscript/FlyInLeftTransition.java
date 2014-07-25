package com.wzhscript;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;

import com.examples.ffmpeg4android_demo_native.GeneralUtils;
import com.examples.ffmpeg4android_demo_native.Prefs;

public class FlyInLeftTransition extends Transition {

	public FlyInLeftTransition() {
	}

	@Override
	protected void generateTransitionImages(String tmpFolder,
			String imgFormat1, String imgFormat2, String imgFormat3,
			int transDur) {
		int i = 1;
		int width1, width2, width, height;
		int frames = transDur*frameRate;
		String img1, img2;
		Canvas canvas;
		Bitmap bitmap1, bitmap2, combined;
	    
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		
		img1 = String.format(imgFormat1, i);
		img2 = String.format(imgFormat2, i);
		bitmap1 = bitmap2 = null;
		
		bitmap1 = BitmapFactory.decodeFile(tmpFolder+img1, options);
		width = bitmap1.getWidth();
		height = bitmap1.getHeight();
		
		while(GeneralUtils.checkIfFileExistAndNotEmpty(tmpFolder+img1)||GeneralUtils.checkIfFileExistAndNotEmpty(tmpFolder+img2)){
			//Log.d(Prefs.TAG, "start frame processing:" + i);
			
			//get origin images
			if(GeneralUtils.checkIfFileExistAndNotEmpty(tmpFolder+img1)){
				bitmap1 = BitmapFactory.decodeFile(tmpFolder+img1, options);
			}
			
			if(GeneralUtils.checkIfFileExistAndNotEmpty(tmpFolder+img2)){
				bitmap2 = BitmapFactory.decodeFile(tmpFolder+img2, options);
			}else{
				break;//optimizing: avoid stuck between transition and next part video
			}
			//paint transition image
			combined = Bitmap.createBitmap(bitmap2.getWidth(), bitmap2.getHeight() , Bitmap.Config.ARGB_8888);
			canvas = new Canvas(combined); 
			
			
		    frames = i>frames? i:frames;//ensure i <= frames
		    width2 = (int)(width*i*1.0/frames);//compute current width
		    width2 = width2 == 0 ? 1 : width2;//ensure width2 > 0
//		    width1 = width;

//		    Log.d(Prefs.TAG, "frame " + i + " of transition, width1:" + width1 + " width2:" + width2);
//		    bitmap1 = Bitmap.createBitmap(bitmap1, 0, 0, width1, height);
		    canvas.drawBitmap(bitmap1, 0f, 0f, null);
		    bitmap2 = Bitmap.createBitmap(bitmap2, width - width2, 0, width2, height);
		    canvas.drawBitmap(bitmap2, 0f, 0f, null); 
		    
		    saveImg(Bitmap.CompressFormat.JPEG, tmpFolder+String.format(imgFormat3, i), combined);
		    
		    //Log.d(Prefs.TAG, "frame " + i + " of transition, alpha:" + alpha);
		    
		    i++;
		    img1 = String.format(imgFormat1, i);
		    img2 = String.format(imgFormat2, i);
		}
	}

}