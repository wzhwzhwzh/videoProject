package com.wzhscript;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.examples.ffmpeg4android_demo_native.GeneralUtils;
import com.examples.ffmpeg4android_demo_native.Prefs;

public class FadeTransition extends Transition {

	public FadeTransition() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void generateTransitionImages(String imgDir, String imgFormat1,
			String imgFormat2, String imgFormat3, int transDur) {
		int i = 1, alpha;
		int frames = transDur*frameRate;
		String img1, img2;
		Canvas canvas;
		Bitmap bitmap1, bitmap2, combined;
	    Paint paint1, paint2;
	    
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		
		img1 = String.format(imgFormat1, i);
		img2 = String.format(imgFormat2, i);
		bitmap1 = bitmap2 = null;
		paint1 = new Paint();
		paint2 = new Paint();
		while(GeneralUtils.checkIfFileExistAndNotEmpty(imgDir+img1)||GeneralUtils.checkIfFileExistAndNotEmpty(imgDir+img2)){
			//Log.d(Prefs.TAG, "start frame processing:" + i);
			
			//get origin images
			if(GeneralUtils.checkIfFileExistAndNotEmpty(imgDir+img1)){
				bitmap1 = BitmapFactory.decodeFile(imgDir+img1, options);
			}
			
			if(GeneralUtils.checkIfFileExistAndNotEmpty(imgDir+img2)){
				bitmap2 = BitmapFactory.decodeFile(imgDir+img2, options);
			}else{
				break;//optimizing: avoid stuck between transition and next part video
			}
			//paint transition image
			combined = Bitmap.createBitmap(bitmap2.getWidth(), bitmap2.getHeight() , Bitmap.Config.ARGB_8888);
			canvas = new Canvas(combined); 
			
			
		    frames = i>frames? i:frames;
		    alpha = (int)(255*(1 - i*1.0/frames));//compute current alpha
		    paint1.setAlpha(alpha);
		    paint2.setAlpha(255 - alpha);

		    canvas.drawBitmap(bitmap1, 0f, 0f, paint1);
		    canvas.drawBitmap(bitmap2, 0f, 0f, paint2); 
		    
		    saveImg(Bitmap.CompressFormat.JPEG, imgDir+String.format(imgFormat3, i), combined);
		    
		    //Log.d(Prefs.TAG, "frame " + i + " of transition, alpha:" + alpha);
		    
		    i++;
		    img1 = String.format(imgFormat1, i);
		    img2 = String.format(imgFormat2, i);
		}
	}

}
