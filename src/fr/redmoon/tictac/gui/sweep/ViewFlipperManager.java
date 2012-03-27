package fr.redmoon.tictac.gui.sweep;

import android.app.Activity;
import android.util.SparseIntArray;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ViewFlipper;
import fr.redmoon.tictac.R;

public class ViewFlipperManager {
	private final Animation slideLeftIn;
	private final Animation slideLeftOut;
	private final Animation slideRightIn;
    private final Animation slideRightOut;
    private final ViewFlipper viewFlipper;
    private final SparseIntArray views;
    
    public ViewFlipperManager(final Activity activity, final int... viewIds) {
    	viewFlipper = (ViewFlipper)activity.findViewById(R.id.view_flipper);
        slideLeftIn = AnimationUtils.loadAnimation(activity, R.anim.slide_left_in);
        slideLeftOut = AnimationUtils.loadAnimation(activity, R.anim.slide_left_out);
        slideRightIn = AnimationUtils.loadAnimation(activity, R.anim.slide_right_in);
        slideRightOut = AnimationUtils.loadAnimation(activity, R.anim.slide_right_out);
        views = new SparseIntArray();
        for (int index = 0; index < viewIds.length; index ++) {
        	views.put(viewIds[index], index);
        }
	}
    
    public void flipView(final Direction direction, final int childViewId) {
		Animation in = null;
		Animation out = null;
		
		switch (direction) {
		case left:
			in = slideLeftIn;
			out = slideLeftOut;
			break;
		case right:
			in = slideRightIn;
			out = slideRightOut;
			break;
		}		
		
		if (in != null && out != null) {
			viewFlipper.setInAnimation(in);
	        viewFlipper.setOutAnimation(out);
	        viewFlipper.setDisplayedChild(getChildViewIndex(childViewId));
		}
	}
    
    private int getChildViewIndex(final int id) {
    	return views.get(id);
    }
}
