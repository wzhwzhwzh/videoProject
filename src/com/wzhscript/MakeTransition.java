package com.wzhscript;

import android.content.Context;

public class MakeTransition {
	private Transition transition;

	public MakeTransition() {
		// TODO Auto-generated constructor stub
	}

	public void combineVideoWithTransition(Context ctx, String type, String video1, String video2, String out_joint, int duration){
		if(type.equals("fade")){
			transition = new FadeTransition();
		}else if(type.equals("slideLeft")){
			transition = new SlideLeftTransition();
		}else if(type.equals("slideRight")){
			transition = new SlideRightTransition();
		}else if(type.equals("slideTop")){
			transition = new SlideTopTransition();
		}else if(type.equals("slideDown")){
			transition = new SlideDownTransition();
		}
		transition.combineVideo(ctx, video1, video2, out_joint, duration);
	}
}
