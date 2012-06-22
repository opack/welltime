package fr.redmoon.tictac.gui;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.drawable.shapes.Shape;
import fr.redmoon.tictac.bus.bean.PreferencesBean;

public class DayBiColorDrawableHelper {
	
	private static final DayBiColorDrawableHelper INSTANCE = new DayBiColorDrawableHelper();
	
	private Path mUpperTriangle;
	private Path mLowerTriangle;
	
	private DayBiColorDrawableHelper() {
		initPaths();
	}
	
	public void initPaths() {
		final Point upLeft = new Point(0, 0);
		final Point upRight = new Point(100, 0);
		final Point downLeft = new Point(0, 100);
		final Point downRight = new Point(100, 100);

		mUpperTriangle = new Path();
		mUpperTriangle.setFillType(Path.FillType.EVEN_ODD);
		mUpperTriangle.moveTo(upLeft.x, upLeft.y);
		mUpperTriangle.lineTo(upRight.x, upRight.y);
		mUpperTriangle.lineTo(downLeft.x, downLeft.y);
		mUpperTriangle.lineTo(upLeft.x, upLeft.y);
		mUpperTriangle.close();
        
		mLowerTriangle = new Path();
		mLowerTriangle.setFillType(Path.FillType.EVEN_ODD);
		mLowerTriangle.moveTo(upRight.x, upRight.y);
		mLowerTriangle.lineTo(downRight.x, downRight.y);
		mLowerTriangle.lineTo(downLeft.x, downLeft.y);
		mLowerTriangle.lineTo(upRight.x, upRight.y);
		mLowerTriangle.close();
	}
	
	public Drawable getDrawableForDayTypes(final int morningDayType, final int afternoonDayType) {
		final int morningColor = PreferencesBean.getColorByDayType(morningDayType);
		final int afternoonColor = PreferencesBean.getColorByDayType(afternoonDayType);
		return getDrawableForColors(morningColor, afternoonColor);
	}
	
	public Drawable getDrawableForColors(final int upperColor, final int lowerColor) {
		final Shape shape = new PathShape(mUpperTriangle, 100, 100);
        ShapeDrawable upperPart = new ShapeDrawable(shape);
        Paint paint = upperPart.getPaint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		paint.setStrokeWidth(2);
		paint.setColor(upperColor);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setAntiAlias(true);
		
		final Shape shape2 = new PathShape(mLowerTriangle, 100, 100);
        ShapeDrawable lowerPart = new ShapeDrawable(shape2);
        Paint paint2 = lowerPart.getPaint();
        paint2.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint2.setStrokeWidth(2);
        paint2.setColor(lowerColor);
        paint2.setStyle(Paint.Style.FILL_AND_STROKE);
        paint2.setAntiAlias(true);
        
        final Drawable bothParts = new LayerDrawable(new Drawable[]{upperPart, lowerPart});
        return bothParts;
	}
	
	public static DayBiColorDrawableHelper getInstance() {
		return INSTANCE;
	}

}
