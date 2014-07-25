package com.wzhscript;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.examples.ffmpeg4android_demo_native.GeneralUtils;

public class FlyOutTopTransition extends Transition {

	public FlyOutTopTransition() {
	}

	@Override
	protected void generateTransitionImages(String tmpFolder,
			String imgFormat1, String imgFormat2, String imgFormat3,
			int transDur) {
		int i = 1;
		int height1, height2, width, height;
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
		    height1 = (int)(height*(1 - i*1.0/frames));//compute current height
		    height1 = height1 == 0 ? 1 : height1;//ensure height1 > 0

		    canvas.drawBitmap(bitmap2, 0f, 0f, null); 		    
		    bitmap1 = Bitmap.createBitmap(bitmap1, 0, height - height1, width, height1);
		    canvas.drawBitmap(bitmap1, 0f, 0f, null);
		    
		    saveImg(Bitmap.CompressFormat.JPEG, tmpFolder+String.format(imgFormat3, i), combined);
		    
		    //Log.d(Prefs.TAG, "frame " + i + " of transition, alpha:" + alpha);
		    
		    i++;
		    img1 = String.format(imgFormat1, i);
		    img2 = String.format(imgFormat2, i);
		}
	}

}
