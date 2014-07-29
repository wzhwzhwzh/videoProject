package com.wzhscript;

import android.app.Activity;
import android.os.Environment;

public class MakeTransition {
	private Transition transition;
	private String workFolder;
	private Activity act;
	public MakeTransition(Activity _act) {
		act = _act;
		workFolder = Environment.getExternalStorageDirectory() + "/videokit";
	}
	public MakeTransition(Activity _act, String folder) {
		act = _act;
		workFolder = folder;
	}

	/**
	 * @param ctx
	 * @param type
	 * @param video1
	 * @param video2
	 * @param out_joint
	 * @param duration
	 */
	public void concateVideosWithTransEffect(String type, String video1, String video2, String out_joint, int duration){
		//fade
		if(type.equals("fade")){
			
			transition = new FadeTransition(act, workFolder);
			
		}
		//slide
		else if(type.equals("slideLeft")){
			
			transition = new SlideLeftTransition(act, workFolder);
			
		}else if(type.equals("slideRight")){
			
			transition = new SlideRightTransition(act, workFolder);
			
		}else if(type.equals("slideTop")){
			
			transition = new SlideTopTransition(act, workFolder);
			
		}else if(type.equals("slideDown")){
			
			transition = new SlideDownTransition(act, workFolder);
			
		}
		//fly in
		else if(type.equals("flyInLeft")){
			
			transition = new FlyInLeftTransition(act, workFolder);
			
		}else if(type.equals("flyInRight")){
			
			transition = new FlyInRightTransition(act, workFolder);
			
		}else if(type.equals("flyInTop")){
			
			transition = new FlyInTopTransition(act, workFolder);
			
		}else if(type.equals("flyInDown")){
			
			transition = new FlyInDownTransition(act, workFolder);
			
		}
		//fly out
		else if(type.equals("flyOutLeft")){
			
			transition = new FlyOutLeftTransition(act, workFolder);
			
		}else if(type.equals("flyOutRight")){
			
			transition = new FlyOutRightTransition(act, workFolder);
			
		}else if(type.equals("flyOutTop")){
			
			transition = new FlyOutTopTransition(act, workFolder);
		
		}else if(type.equals("flyOutDown")){
			
			transition = new FlyOutDownTransition(act, workFolder);
			
		}
		//fade fly
		else if(type.equals("fadeFlyLeft")){
			
			transition = new FadeFlyLTransition(act, workFolder);
			
		}else if(type.equals("fadeFlyRight")){
			
			transition = new FadeFlyRTransition(act, workFolder);
			
		}else if(type.equals("fadeFlyTop")){
			
			transition = new FadeFlyTTransition(act, workFolder);
			
		}else if(type.equals("fadeFlyDown")){
			
			transition = new FadeFlyDTransition(act, workFolder);
			
		}
		//scale
		else if(type.equals("scale")){
			
			transition = new ScaleTransition(act, workFolder);
			
		}else if(type.equals("scaleDown")){
			
			transition = new ScaleDownTransition(act, workFolder);
			
		}else if(type.equals("scaleUp")){
			
			transition = new ScaleUpTransition(act, workFolder);
			
		}
		
		
		transition.combineVideo(video1, video2, out_joint, duration);
	}
}
