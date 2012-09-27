package fr.redmoon.tictac.bus.bean;

import java.io.Serializable;

public class DayType implements Serializable {
	private static final long serialVersionUID = 6731017748779443301L;
	
	public final String id;
	public final String label;
	public final int time;	// minutes
	public final int color;
	
	public DayType(final String _id, final String _label, final int _time, final int _color) {
		id = _id;
		label = _label;
		time = _time;
		color = _color;
	}
	
	@Override
	public String toString() {
		return label;
	}
}