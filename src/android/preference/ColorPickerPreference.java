/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.preference;

import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

public class ColorPickerPreference extends DialogPreference {
	
    /**
	 * The default value for this preference
	 */
	private int defaultValue;
	
	/**
	 * The picked value
	 */
	private int pickedColor;
	
    /**
	 * @param context
	 * @param attrs
	 */
	public ColorPickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}
 
	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public ColorPickerPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}
	
	/**
	 * Initialize this preference
	 */
	private void initialize() {
		setPersistent(true);
	}
	
    @Override
	protected View onCreateDialogView() {
    	// Initialisation du contenu des composants en fonction de la valeur de préférence actuelle.
 		final int initialColor = getPersistedInt(defaultValue);
    	
    	// initialColor is the initially-selected color to be shown in the rectangle on the left of the arrow.
    	// for example, 0xff000000 is black, 0xff0000ff is blue. Please be aware of the initial 0xff which is the alpha.
    	AmbilWarnaDialog dialog = new AmbilWarnaDialog(getContext(), initialColor, new OnAmbilWarnaListener() {
    	        @Override
    	        public void onOk(AmbilWarnaDialog dialog, int color) {
    	                // color is the color selected by the user.
    	        }
    	                
    	        @Override
    	        public void onCancel(AmbilWarnaDialog dialog) {
    	                // cancel was selected by the user
    	        }
    	        
    	        @Override
    	        public void onColorChanged(int color) {
                    pickedColor = color;
                }
    	});
    	
        return dialog.getMainView();
    }
    
    @Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			persistInt(pickedColor);
		}
	}
 
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		final int value = a.getInt(index, 0);
		this.defaultValue = value;
		return value;
	}
}