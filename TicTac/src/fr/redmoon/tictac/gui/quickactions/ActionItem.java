package fr.redmoon.tictac.gui.quickactions;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class ActionItem {
	private Drawable icon;
	private Bitmap thumb;
	private String title;
	private int actionId = -1;
    private boolean selected;
    private boolean sticky;

    /**
     * Constructor
     * 
     * @param actionId  Action id for case statements
     * @param title     Title
     * @param icon      Icon to use
     */
    public ActionItem(int actionId, String title, Drawable icon) {
        this.title = title;
        this.icon = icon;
        this.actionId = actionId;
    }
    
    /**
     * Constructor
     */
    public ActionItem() {
        this(-1, null, null);
    }
    
    /**
     * Constructor
     * 
     * @param actionId  Action id of the item
     * @param title     Text to show for the item
     */
    public ActionItem(int actionId, String title) {
        this(actionId, title, null);
    }
    
    /**
     * Constructor
     * 
     * @param icon {@link Drawable} action icon
     */
    public ActionItem(Drawable icon) {
        this(-1, null, icon);
    }
    
    /**
     * Constructor
     * 
     * @param actionId  Action ID of item
     * @param icon      {@link Drawable} action icon
     */
    public ActionItem(int actionId, Drawable icon) {
        this(actionId, null, icon);
    }

	/**
	 * Set action title
	 * 
	 * @param title action title
	 */
    protected void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Get action title
	 * 
	 * @return action title
	 */
	protected String getTitle() {
		return this.title;
	}

	/**
	 * Set action icon
	 * 
	 * @param icon {@link Drawable} action icon
	 */
	protected void setIcon(Drawable icon) {
		this.icon = icon;
	}

	/**
	 * Get action icon
	 * @return  {@link Drawable} action icon
	 */
	protected Drawable getIcon() {
		return this.icon;
	}

	 /**
     * Set action id
     * 
     * @param actionId  Action id for this action
     */
	protected void setActionId(int actionId) {
        this.actionId = actionId;
    }
    
    /**
     * @return  Our action id
     */
    protected int getActionId() {
        return actionId;
    }
    
    /**
     * Set sticky status of button
     * 
     * @param sticky  true for sticky, pop up sends event but does not disappear
     */
    protected void setSticky(boolean sticky) {
        this.sticky = sticky;
    }
    
    /**
     * @return  true if button is sticky, menu stays visible after press
     */
    protected boolean isSticky() {
        return sticky;
    }
    
	/**
	 * Set selected flag;
	 * 
	 * @param selected Flag to indicate the item is selected
	 */
    protected void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * Check if item is selected
	 * 
	 * @return true or false
	 */
    protected boolean isSelected() {
		return this.selected;
	}

	/**
	 * Set thumb
	 * 
	 * @param thumb Thumb image
	 */
    protected void setThumb(Bitmap thumb) {
		this.thumb = thumb;
	}

	/**
	 * Get thumb image
	 * 
	 * @return Thumb image
	 */
    protected Bitmap getThumb() {
		return this.thumb;
	}
}