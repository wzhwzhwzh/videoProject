package com.wzhscript;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.examples.ffmpeg4android_demo_native.GeneralUtils;

public class FadeFlyTTransition extends Transition {

	public FadeFlyTTransition(Activity _act, String folder) {
		super(_act, folder);
		// TODO Auto-generated constructor stub
	}

	public FadeFlyTTransition(Activity _act) {
		super(_act);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void generateTransitionImages(String tmpFolder,
			String imgFormat1, String imgFormat2, String imgFormat3,
			int transDur) {
		int i = 1, alpha;
		int height1, height2, width, height;
		int frames = transDur*frameRate;
		String img1, img2;
		Canvas canvas;
		Bitmap bitmap1, bitmap2, combined;
		Paint paint;
	    
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		
		img1 = String.format(imgFormat1, i);
		img2 = String.format(imgFormat2, i);
		bitmap1 = bitmap2 = null;
		
		bitmap1 = BitmapFactory.decodeFile(tmpFolder+img1, options);
		width = bitmap1.getWidth();
		height = bitmap1.getHeight();
		paint = new Paint();
		
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
		    alpha = (int)(255*(1 - i*1.0/frames));//compute current alpha
		    paint.setAlpha(alpha);		    
		    height2 = (int)(height*i*1.0/frames);//compute current height
		    height2 = height2 == 0 ? 1 : height2;//ensure height2 > 0
		    
		    canvas.drawBitmap(bitmap1, 0f, 0f, paint);
		    bitmap2 = Bitmap.createBitmap(bitmap2, 0, height - height2, width, height2);
		    canvas.drawBitmap(bitmap2, 0f, 0f, null); 
		    
		    saveImg(Bitmap.CompressFormat.JPEG, tmpFolder+String.format(imgFormat3, i), combined);
		    
		    //Log.d(Prefs.TAG, "frame " + i + " of transition, alpha:" + alpha);
		    
		    i++;
		    img1 = String.format(imgFormat1, i);
		    img2 = String.format(imgFormat2, i);
		}
	}

}
