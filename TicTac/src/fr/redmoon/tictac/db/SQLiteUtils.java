package fr.redmoon.tictac.db;

public class SQLiteUtils {
	public static final String DATATYPE_TEXT = "text";
	public static final String DATATYPE_INTEGER = "integer";
	public static final String DATATYPE_REAL = "real";
	public static final String DATATYPE_BLOC = "blob";
	
	public static final String CONSTRAINT_NONE = "";
	public static final String CONSTRAINT_PRIMARY_KEY = "primary key";
	public static final String CONSTRAINT_AUTOINCREMENT = "autoincrement";
	public static final String CONSTRAINT_PRIMARY_KEY_AUTOINCREMENT = CONSTRAINT_PRIMARY_KEY + " " + CONSTRAINT_AUTOINCREMENT;
	public static final String CONSTRAINT_NOT_NULL = "not null";
	// L'espace � la fin doit �tre pr�sent pour permettre au dev de faire une simple concat�nation avec la valeur qu'il souhaite
	// utiliser par d�faut
	public static final String CONSTRAINT_DEFAULT = "default ";
}
