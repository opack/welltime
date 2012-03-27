package fr.redmoon.tictac.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

public class TableHelper {

	private SQLiteDatabase mDb;
	private final String tableName;
	private final String[] columns;
	
	private final String createStatement;
    
	public TableHelper(
			String tableName,
			String[] columns,
			String[] columnTypes,
			String[] columnConstraints) {
		this.tableName = tableName;
		this.columns = columns;
		
		final StringBuilder sbCreateStatement = new StringBuilder();
		final StringBuilder sbPKConstraint = new StringBuilder("PRIMARY KEY (");
		boolean isFirstPK = true;
		
		sbCreateStatement.append("create table ").append(tableName).append(" (");
		for (int curCol = 0; curCol < columns.length; curCol++) {
			sbCreateStatement.append(columns[curCol])
			.append(" ").append(columnTypes[curCol]);
			
			// On conserve les PK pour la fin car c'est le seul moyen de gérer les clés composites
			if (SQLiteUtils.CONSTRAINT_PRIMARY_KEY.equals(columnConstraints[curCol])) {
				if (isFirstPK) {
					isFirstPK = false;
				} else {
					sbPKConstraint.append(", ");
				}
				sbPKConstraint.append(columns[curCol]);
			} else {
				sbCreateStatement.append(" ").append(columnConstraints[curCol]);
			}
			if (curCol < columns.length - 1) {
				sbCreateStatement.append(", ");
			}
		}
		
		sbPKConstraint.append(")");
		sbCreateStatement.append(", ").append(sbPKConstraint).append(");");
		createStatement = sbCreateStatement.toString();
	}
	
	public void setDB(final SQLiteDatabase db) {
		this.mDb = db;
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public String[] getColumns() {
		return columns;
	}
	
	public void createTable(final SQLiteDatabase db) {
		db.execSQL(createStatement);
	}
	
	public void dropTable(final SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + tableName);
	}
	
	public Cursor fetchAll() {
		return mDb.query(tableName, columns, null, null, null, null, null);
	}
	
	public Cursor fetch(final long id) {
		return fetchWhere(columns[0] + "=" + id);
	}
	
	public Cursor fetch(final String id) {
		return fetchWhere(columns[0] + "='" + id + "'");
	}
	
	public Cursor fetchWhere(final String whereClause) {
		return fetchWhere(whereClause, null);
	}
	
	public Cursor fetchWhere(final String whereClause, final String orderByClause) {
		final Cursor mCursor = mDb.query(
			true,
			tableName,
			columns,
			whereClause,
			null,
			null,
			null,
			orderByClause,
			null);
        
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
	}
	
	public boolean exists(final long id) {
		final Cursor result = fetch(id);
		final boolean exists = result.getCount() > 0;
		result.close();
		return exists;
	}
	
	public boolean exists(final String id) {
		final Cursor result = fetch(id);
		final boolean exists = result.getCount() > 0;
		result.close();
		return exists;
	}
	
	public void deleteAll() {
		mDb.execSQL("DELETE FROM " + tableName);
	}
	
	public boolean delete(final long id) {
		return mDb.delete(tableName, columns[0] + "=" + id, null) > 0;
	}
	
	public boolean delete(final String id) {
		return mDb.delete(tableName, columns[0] + "='" + id + "'", null) > 0;
	}
	
	public int deleteWhere(final String whereClause) {
		return mDb.delete(tableName, whereClause, null);
	}
	
	public long createRecord(final ContentValues data) {
		return mDb.insert(tableName, null, data);
	}
	
	public boolean updateRecord(final long id, final ContentValues data) {
		return mDb.update(tableName, data, columns[0] + "=" + id, null) > 0;
	}
	
	public boolean updateRecord(final String id, final ContentValues data) {
		return mDb.update(tableName, data, columns[0] + "='" + id + "'", null) > 0;
	}
	
	public boolean updateRecord(final long idCol1, final long idCol2, final ContentValues data) {
		return mDb.update(tableName, data, columns[0] + "=" + idCol1 + " and " + columns[1] + "=" + idCol2, null) > 0;
	}

	public long getCount() {
		return DatabaseUtils.queryNumEntries(mDb, tableName);
	}
}
