package com.wzhscript;

import android.content.Context;

public class MakeTransition {
	private Transition transition;

	public MakeTransition() {
		// TODO Auto-generated constructor stub
	}

	public void combineVideoWithTransition(Context ctx, String type, String video1, String video2, String out_joint, int duration){
		//fade
		if(type.equals("fade")){
			
			transition = new FadeTransition();
			
		}else if(type.equals("fadeIn")){
			
			transition = new FadeInTransition();
			
		}else if(type.equals("fadeOut")){
			
			transition = new FadeOutTransition();
			
		}
		//slide
		else if(type.equals("slideLeft")){
			
			transition = new SlideLeftTransition();
			
		}else if(type.equals("slideRight")){
			
			transition = new SlideRightTransition();
			
		}else if(type.equals("slideTop")){
			
			transition = new SlideTopTransition();
			
		}else if(type.equals("slideDown")){
			
			transition = new SlideDownTransition();
			
		}
		//fly in
		else if(type.equals("flyInLeft")){
			
			transition = new FlyInLeftTransition();
			
		}else if(type.equals("flyInRight")){
			
			transition = new FlyInRightTransition();
			
		}else if(type.equals("flyInTop")){
			
			transition = new FlyInTopTransition();
			
		}else if(type.equals("flyInDown")){
			
			transition = new FlyInDownTransition();
			
		}
		//fly out
		else if(type.equals("flyOutLeft")){
			
			transition = new FlyOutLeftTransition();
			
		}else if(type.equals("flyOutRight")){
			
			transition = new FlyOutRightTransition();
			
		}else if(type.equals("flyOutTop")){
			
			transition = new FlyOutTopTransition();
		
		}else if(type.equals("flyOutDown")){
			
			transition = new FlyOutDownTransition();
			
		}
		//fade fly
		else if(type.equals("fadeFlyLeft")){
			
			transition = new FadeFlyLTransition();
			
		}else if(type.equals("fadeFlyRight")){
			
			transition = new FadeFlyRTransition();
			
		}else if(type.equals("fadeFlyTop")){
			
			transition = new FadeFlyTTransition();
			
		}else if(type.equals("fadeFlyDown")){
			
			transition = new FadeFlyDTransition();
			
		}
		//scale
		else if(type.equals("scale")){
			
			transition = new ScaleTransition();
			
		}else if(type.equals("scaleDown")){
			
			transition = new ScaleDownTransition();
			
		}else if(type.equals("scaleUp")){
			
			transition = new ScaleUpTransition();
			
		}
		
		
		transition.combineVideo(ctx, video1, video2, out_joint, duration);
	}
}
