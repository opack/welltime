package fr.redmoon.tictac.gui;

/**
 * Chargé de la cohérence des données entre les vues. Ainsi, si on affiche un jour dans la vue "Jour",
 * le passage à la vue semaine se fera sur la semaine de ce jour, et idem pour le mois.
 * Dans l'ordre inverse, l'affichage de la vue "Mois" vers la vue "Semaine" se fera sur le premier jour
 * du mois, et le passage de la vue "Semaine" à la vue "Jour" se fera sur le premier jour de la semaine.
 */
public class ViewSynchronizer {
	private static ViewSynchronizer instance;
	
	private long mCurrentDay;
	
	private ViewSynchronizer(){
		mCurrentDay = -1;
	}
	
	public static ViewSynchronizer getInstance() {
		if (instance == null) {
			instance = new ViewSynchronizer();
		}
		return instance;
	}

	public long getCurrentDay() {
		return mCurrentDay;
	}

	public void setCurrentDay(long currentDay) {
		mCurrentDay = currentDay;
	}
}
