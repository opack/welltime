package fr.redmoon.tictac.bus.bean;
public class DayType {
	public String label;
	public int time;	// minutes
	public int color;
	
	public DayType(final String _label, final int _time, final int _color) {
		label = _label;
		time = _time;
		color = _color;
	}
}