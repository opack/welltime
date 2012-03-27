package fr.redmoon.tictac.gui.sweep;

import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class ViewSwitcherGestureListener extends SimpleOnGestureListener {
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 100;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	
    private Direction lastDirection;
    
    public ViewSwitcherGestureListener() {
        resetLastDirection();
    }
	
	public void resetLastDirection() {
		lastDirection = Direction.none;
	}
	
	@Override
	public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {
		lastDirection = Direction.none;
		try {
			// Cherche un "fling" horizontal
			if (Math.abs(e1.getY() - e2.getY()) < SWIPE_MAX_OFF_PATH) {
		        if (Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
		            if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE) {
		            	lastDirection = Direction.left;
		            }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE) {
		            	lastDirection = Direction.right;
		            }
		        }
			}
			// Cherche un "fling" vertical si on n'a pas trouvé d'horizontal
			if (lastDirection == Direction.none && Math.abs(e1.getX() - e2.getX()) < SWIPE_MAX_OFF_PATH) {
		        if (Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
		            if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE) {
		            	lastDirection = Direction.up;
		            }  else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE) {
		            	lastDirection = Direction.down;
		            }
		        }
			}
        } catch (Exception e) {
            // nothing
        }
        return lastDirection != Direction.none;
	}

	public Direction getLastDirection() {
		return lastDirection;
	}
}