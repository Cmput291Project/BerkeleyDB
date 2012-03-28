/* Project 1: CMPUT 301 - Winter 2012 - University of Alberta



    Copyright (C) 2012 Hieu Ngo <hcngo@ualberta.ca>

		This program reuses components from Notepad tutorial on android dev guide website.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    aint with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cmput301W12.android.project;

import java.sql.Timestamp;
import java.util.Set;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * @author Hieu Ngo
 * @date Mar 15, 2012
 * Simple database access helper class. Defines the basic CRUD operations
 * for the database, and gives the ability to list all entries as well as
 * retrieve or modify a specific field. 
 */
public class DbAdapter {

	public static final String PHOTOID = "Photo_ID";
	public static final String LOCATION = "Location";
	public static final String TIMESTAMP = "Time_Stamp";
	public static final String PHOTONAME = "PhotoName";
	public static final String PHOTOANNOTATION = "PhotoAnnotation";

	public static final String GROUPID = "Group_ID";
	public static final String GROUPNAME = "GroupName";

	public static final String SKINCONDITIONID = "SkinConditionID";
	public static final String SKINNAME = "SkinName";

	public static final String ALARMID = "Alarm_ID";
	public static final String ALARMTIME = "Alarm_TimeStamp";
	public static final String ALARMNOTE = "Alarm_note";

	private static final String TAG = "DbAdapter";
	private static final String DATABASE_NAME = "skinObserver";



	private static final String PHOTO_TABLE	 = "PhotoTable";
	public static final String GROUP_TABLE = "GroupTable";
	public static final String SKIN_TABLE = "SkinConditionTable";
	public static final String PHOTOGROUP_TABLE = "Photo_GroupTable";
	public static final String PHOTOSKIN_TABLE = "Photo_SkinConditionTable";
	public static final String ALARM_TABLE = "AlarmTable";

	private static final int DATABASE_VERSION = 3;

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	private final Context mCtx;
	public static final int INVALID_ID = -1;

	private static DbAdapter dbAdap = null;


	/**
	 * Database creation sql statement
	 */
	private static final String CREATE_PHOTO_TABLE = 
			"create table " + PHOTO_TABLE + " ( " + 
					PHOTOID + " integer primary key, " + 
					LOCATION + " text not null, " +
					TIMESTAMP + " text, " +
					PHOTONAME + " text, " +
					PHOTOANNOTATION + " text, " + 
					" unique( " + LOCATION + ")" +	")";

	private static final String CREATE_GROUP_TABLE = 
			"create table " + GROUP_TABLE + " ( " + 
					GROUPID + " integer primary key, " + 
					GROUPNAME + " text not null, " +
					"unique( " + GROUPNAME + ") " + ")";

	private static final String CREATE_SKIN_TABLE = 
			"create table " + SKIN_TABLE + " ( " +
					SKINCONDITIONID + " integer primary key, " + 
					SKINNAME + " text not null, " +
					"unique( " + SKINNAME + ") " + ")";


	private static final String CREATE_PHOTOGROUP_TABLE = 
			"create table " + PHOTOGROUP_TABLE + 
			" ( " + PHOTOID + " integer not null CONSTRAINT fk_PHOTOID_PHOTOGROUP REFERENCES " + 
			PHOTO_TABLE + " ( " + PHOTOID + " ) " + " ON DELETE CASCADE, "  + 
			GROUPID + " integer not null CONSTRAINT fk_GROUPID REFERENCES  " +
			GROUP_TABLE + " ( " + GROUPID + " ) " + " ON DELETE CASCADE, " + 
			" primary key( " + PHOTOID + ", " + GROUPID + " ) ) " ;

	private static final String CREATE_PHOTOSKIN_TABLE = 
			"create table " + PHOTOSKIN_TABLE + 
			" ( " + PHOTOID + " integer not null CONSTRAINT fk_PHOTOID_PHOTOSKIN REFERENCES " + 
			PHOTO_TABLE + " ( " + PHOTOID + " ) " + " ON DELETE CASCADE, "  + 
			SKINCONDITIONID + " integer not null CONSTRAINT fk_SKINCONDITIONID REFERENCES  " +
			SKIN_TABLE + " ( " + SKINCONDITIONID + " ) " + " ON DELETE CASCADE, " + 
			" primary key( " + PHOTOID + ", " + SKINCONDITIONID + " ) ) " ;

	private static final String CREATE_ALARM_TABLE = 
			"create table " + ALARM_TABLE + 
			" ( " + ALARMID + " integer primary key, " + 
			ALARMTIME + " text not null, " + 
			ALARMNOTE + " text " + " )";

	private static final String CREATE_TRIGGER_PHOTOGROUP_INSERT = 
			"create trigger trig_PHOTOID_PHOTOGROUP_INSERT " +
					" before insert on " + PHOTOGROUP_TABLE + " for each row begin " + 
					" select raise(rollback, 'insert on table " + PHOTOGROUP_TABLE + 
					" violates foreign key constraint fk_PHOTOID_PHOTOGROUP' ) " + 
					" where (select  " + PHOTOID + " from " + PHOTO_TABLE + " where " + PHOTOID + " = NEW. " +
					PHOTOID + " ) " + " IS NULL; " +
					" END";

	private static final String CREATE_TRIGGER_PHOTOGROUP_UPDATE = 
			"create trigger trig_PHOTOID_PHOTOGROUP_UPDATE " +
					" before update on " + PHOTOGROUP_TABLE + " for each row begin" + 
					" select raise(rollback, ' update on table " + PHOTOGROUP_TABLE + 
					" violates foreign key constraint fk_PHOTOID_PHOTOGROUP ' ) " + 
					" where (select  " + PHOTOID + " from " + PHOTO_TABLE + " where " + PHOTOID + " = NEW." +
					PHOTOID + " ) " + " IS NULL; " +
					" END";

	private static final String CREATE_TRIGGER_PHOTOGROUP_DELETECASCADE = 
			"create trigger trig_PHOTOID_PHOTOGROUP_DELETECASCADE " + 
					" before delete on " + PHOTO_TABLE + " for each row begin " +
					" delete from " + PHOTOGROUP_TABLE + " where " + PHOTOID + " = OLD." + PHOTOID + ";" +
					" END";

	private static final String CREATE_TRIGGER_PHOTOGROUP_UPDATECASCADE = 
			"create trigger trig_PHOTOID_PHOTOGROUP_UPDATECASCADE " +
					" after update on " + PHOTO_TABLE + " for each row begin " + 
					" update " + PHOTOGROUP_TABLE + " set " + PHOTOID + " = "+  "NEW." + PHOTOID + 
					" where " + PHOTOID + " = " + "OLD." + PHOTOID + ";" +
					" END"; 

	private static final String CREATE_TRIGGER_PHOTOSKIN_INSERT = 
			"create trigger trig_PHOTOID_PHOTOSKIN_INSERT " +
					" before insert on " + PHOTOSKIN_TABLE + " for each row begin " + 
					" select raise(rollback, ' insert on table " + PHOTOSKIN_TABLE + 
					" violates foreign key constraint fk_PHOTOID_PHOTOSKIN ' ) " + 
					" where (select  " + PHOTOID + " from " + PHOTO_TABLE + " where " + PHOTOID + " = NEW." +
					PHOTOID + " ) " + " IS NULL; " +
					" END";

	private static final String CREATE_TRIGGER_PHOTOSKIN_UPDATE = 
			"create trigger trig_PHOTOID_PHOTOSKIN_UPDATE " +
					" before update on " + PHOTOSKIN_TABLE + " for each row begin " + 
					" select raise(rollback, ' update on table " + PHOTOSKIN_TABLE + 
					" violates foreign key constraint fk_PHOTOID_PHOTOSKIN ' ) " + 
					" where (select  " + PHOTOID + " from " + PHOTO_TABLE + " where " + PHOTOID + " = NEW." +
					PHOTOID + " ) " + " IS NULL; " +
					" END";

	private static final String CREATE_TRIGGER_PHOTOSKIN_DELETECASCADE = 
			"create trigger trig_PHOTOID_PHOTOSKIN_DELETECASCADE " + 
					" before delete on " + PHOTO_TABLE + " for each row begin " +
					" delete from " + PHOTOSKIN_TABLE + " where " + PHOTOID + " = OLD." + PHOTOID + ";" + 
					" END";

	private static final String CREATE_TRIGGER_PHOTOSKIN_UPDATECASCADE = 
			"create trigger trig_PHOTOID_PHOTOSKIN_UPDATECASCADE " +
					" after update on " + PHOTO_TABLE + " for each row begin " + 
					" update " + PHOTOSKIN_TABLE + " set " + PHOTOID + " = "+ "NEW." + PHOTOID + 
					" where " + PHOTOID + " = " + "OLD." + PHOTOID + ";" +
					" END";

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_PHOTO_TABLE);
			db.execSQL(CREATE_GROUP_TABLE);
			db.execSQL(CREATE_SKIN_TABLE);
			db.execSQL(CREATE_PHOTOGROUP_TABLE);
			db.execSQL(CREATE_PHOTOSKIN_TABLE);
			db.execSQL(CREATE_ALARM_TABLE);

			db.execSQL(CREATE_TRIGGER_PHOTOGROUP_INSERT);
			db.execSQL(CREATE_TRIGGER_PHOTOGROUP_UPDATE);
			db.execSQL(CREATE_TRIGGER_PHOTOGROUP_DELETECASCADE);
			db.execSQL(CREATE_TRIGGER_PHOTOGROUP_UPDATECASCADE);
			db.execSQL(CREATE_TRIGGER_PHOTOSKIN_INSERT);
			db.execSQL(CREATE_TRIGGER_PHOTOSKIN_UPDATE);
			db.execSQL(CREATE_TRIGGER_PHOTOSKIN_DELETECASCADE);
			db.execSQL(CREATE_TRIGGER_PHOTOSKIN_UPDATECASCADE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + PHOTO_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + GROUP_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + SKIN_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + PHOTOGROUP_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + PHOTOSKIN_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + ALARM_TABLE );
			onCreate(db);
		}
	}

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx the Context within which to work
	 */
	private DbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	public static DbAdapter getDbAdapter(Context ctx){
		if(dbAdap == null){
			dbAdap = new DbAdapter(ctx);
		}
		return dbAdap;
	}

	/**
	 * Open the skinObserver database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException if the database could be neither opened or created
	 */
	public DbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}


	public static String returnIdColumn(OptionType option){
		String id = "";
		switch(option){
		case PHOTO:
			id = PHOTOID;
			break;
		case GROUP:
			id = GROUPID;
			break;
		case SKINCONDITION:
			id = SKINCONDITIONID;
			break;
		case PHOTOGROUP:
			id = "ROWID";
			break;
		case PHOTOSKIN:
			id = "ROWID";
			break;
		case ALARM:
			id = ALARMID;
			break;
		}
		return id;
	}

	public static String returnTableName(OptionType option){
		String tableName = "";
		switch(option){
		case PHOTO:
			tableName = PHOTO_TABLE;
			break;
		case GROUP:
			tableName = GROUP_TABLE;
			break;
		case SKINCONDITION:
			tableName = SKIN_TABLE;
			break;
		case PHOTOGROUP:
			tableName = PHOTOGROUP_TABLE;
			break;
		case PHOTOSKIN:
			tableName = PHOTOSKIN_TABLE;
			break;
		case ALARM:
			tableName = ALARM_TABLE;
			break;
		}
		return tableName;
	}

	public static String returnItemName(OptionType option){
		String itemName = "";
		if(option == OptionType.GROUP){
			itemName = GROUPNAME;
		}else if(option == OptionType.SKINCONDITION){
			itemName = SKINNAME;
		}else if(option == OptionType.PHOTO){
			itemName = PHOTONAME;
		}

		return itemName;
	}


	/**
	 * Add a new photo entry using the location, timeStamp and name provided. 
	 * If the new photo entry is successfully added, return the new PHOTOID for that entry, otherwise return
	 * -1 to indicate failure.
	 * @return PHOTOID or -1 if failed
	 */
	public int addPhoto(String location, Timestamp timeStamp, String name, String annotation) {
		ContentValues initialValues = new ContentValues();

		initialValues.put(LOCATION, location);
		initialValues.put(TIMESTAMP, timeStamp.toString());
		initialValues.put(PHOTONAME, name);
		initialValues.put(PHOTOANNOTATION, annotation);

		return (int) mDb.insert(PHOTO_TABLE, null, initialValues);
	}

	/**
	 * Add a new group entry using the name provided. 
	 * If the new group entry is successfully added, return the new GROUPID for that entry, otherwise return
	 * -1 to indicate failure.
	 * @return GROUPID or -1 if failed
	 */
	public int addGroup(String name) {
		ContentValues initialValues = new ContentValues();

		initialValues.put(GROUPNAME, name);

		return (int) mDb.insert(GROUP_TABLE, null, initialValues);
	}

	/**
	 * Add a new skin condition entry using the name provided. 
	 * If the new skin condition entry is successfully added, return the new SKINCONDITIONID for that entry, otherwise return
	 * -1 to indicate failure.
	 * @return SKINCONDITIONID or -1 if failed
	 */
	public int addSkinCondition(String name) {
		ContentValues initialValues = new ContentValues();

		initialValues.put(SKINNAME, name);

		return (int) mDb.insert(SKIN_TABLE, null, initialValues);
	}

	/**
	 * Add a new photo - group entry using the PHOTOID and GROUPID provided. 
	 * If the new photo - group entry is successfully added, return the row id for that entry, otherwise return
	 * -1 to indicate failure.
	 * @return row id or -1 if failed
	 */
	public int addPhotoGroup(int photoId, int groupId) {
		ContentValues initialValues = new ContentValues();

		initialValues.put(PHOTOID, photoId);
		initialValues.put(GROUPID, groupId);

		return (int) mDb.insert(PHOTOGROUP_TABLE, null, initialValues);
	}

	/**
	 * Add a new photo - skin condition entry using the PHOTOID and SKINCONDITIONID provided. 
	 * If the new photo - skin condition is successfully added, return the row id for that entry, otherwise return
	 * -1 to indicate failure.
	 * @return row id or -1 if failed
	 */
	public int addPhotoSkinCondition(int photoId, int skinConditionId) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(PHOTOID, photoId);
		initialValues.put(SKINCONDITIONID, skinConditionId);
		return (int) mDb.insert(PHOTOSKIN_TABLE, null, initialValues);
	}

	/**
	 * Add a new alarm
	 * If the new alarm is successfully added, return the row id for that alarm, 
	 * otherwise return -1 to indicate failure.
	 * @param timeStamp
	 * @param note
	 * @return
	 */
	public int addAlarm(Timestamp timeStamp, String note){
		ContentValues cv = new ContentValues();
		cv.put(ALARMTIME, timeStamp.toString());
		cv.put(ALARMNOTE, note);
		return (int) mDb.insert(ALARM_TABLE, null, cv);
	}

	/**
	 * Search for photo with the provided location.
	 * @param location
	 * @return a Cursor that has PHOTOID column containing the photoId.
	 */
	public Cursor searchForPhoto(String location){
		Cursor mCursor = mDb.query(true, PHOTO_TABLE, 
				null, LOCATION + "  =  " + "'" + location + "'", null, null, null, null, null);
		if(mCursor != null){
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/**
	 * Search for the container(group or skin condition) 
	 * @param name
	 * @param option
	 * @return
	 */
	public Cursor searchForContainer(String name, OptionType option){
		Cursor mCursor = mDb.query(true, DbAdapter.returnTableName(option), 
				null, DbAdapter.returnItemName(option) + "  =  " + "'" + name + "'", null, null, null, null, null);
		if(mCursor != null){
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/**
	 * Use OptionType.GROUP in the third argument for search a Photo - group row.
	 * Use OptionType.SKINCONDITION in the third argument for search a photo - skin condition row.
	 * @param photoId
	 * @param itemId
	 * @param option
	 * @return
	 */
	public Cursor searchForPhotoContainer(int photoId, int itemId, OptionType option){
		String itemIdName = "";
		if(option == OptionType.PHOTOGROUP){
			itemIdName = GROUPID;
		} else if (option == OptionType.PHOTOSKIN){
			itemIdName = SKINCONDITIONID;
		}
		Cursor mCursor = mDb.query(true, DbAdapter.returnTableName(option), 
				null, PHOTOID + "  =  " + photoId + " and " + itemIdName + " = " + itemId, null, null, null, null, null);
		if(mCursor != null){
			mCursor.moveToFirst();
		}
		return mCursor;
	}


	public int updatePhoto(int photoId, String newLocation, Timestamp newTimeStamp, 
			String newName, String newAnnotation ){

		ContentValues initialValues = new ContentValues();

		if(newLocation != null){
			initialValues.put(LOCATION, newLocation);
		}

		if(newTimeStamp != null){
			initialValues.put(TIMESTAMP, newTimeStamp.toString());
		}

		if(newName != null){
			initialValues.put(PHOTONAME, newName);
		}

		if(newAnnotation != null){
			initialValues.put(PHOTOANNOTATION, newAnnotation);
		}

		return mDb.update(PHOTO_TABLE, initialValues , 
				DbAdapter.returnIdColumn(OptionType.PHOTO) + " = " + photoId, null);
	}
	/**
	 * Update group
	 * @param rowId
	 * @param newName
	 * @return
	 */
	public int updateGroup(int groupId, String newName ){
		ContentValues initialValues = new ContentValues();

		if(newName != null){
			initialValues.put(GROUPNAME, newName);
		}

		return mDb.update(GROUP_TABLE, initialValues , 
				DbAdapter.returnIdColumn(OptionType.GROUP) + " = " + groupId, null);
	}

	/**
	 * Update skin
	 * @param rowId
	 * @param newName
	 * @return
	 */
	public int updateSkin(int skinId, String newName ){
		ContentValues initialValues = new ContentValues();

		if(newName != null){
			initialValues.put(SKINNAME, newName);
		}

		return mDb.update(SKIN_TABLE, initialValues ,
				DbAdapter.returnIdColumn(OptionType.SKINCONDITION) + " = " + skinId, null);
	}

	/**
	 * This method is unlikely to be used.
	 * @param rowId
	 * @param photoId
	 * @param groupId
	 * @return
	 */

	public int updatePhotoGroup(int rowId, int photoId, int groupId){
		ContentValues cv = new ContentValues();
		cv.put(PHOTOID, photoId);
		cv.put(GROUPID,groupId);
		return mDb.update(PHOTOGROUP_TABLE, cv, 
				DbAdapter.returnIdColumn(OptionType.PHOTOGROUP) + " = " + rowId, null);
	}

	public int updatePhotoSkin(int rowId, int photoId, int skinId	){
		ContentValues cv = new ContentValues();
		cv.put(PHOTOID, photoId);
		cv.put(SKINCONDITIONID,skinId);
		return mDb.update(PHOTOSKIN_TABLE, cv, 
				DbAdapter.returnIdColumn(OptionType.PHOTOSKIN) + " = " + rowId, null);
	}

	/**
	 * update the alarm with new timeStamp and new note.
	 * @param alarmId
	 * @param timeStamp
	 * @param note
	 * @return
	 */
	public int updateAlarm(int alarmId, Timestamp timeStamp, String note){
		ContentValues cv = new ContentValues();
		if(timeStamp != null){
			cv.put(ALARMTIME, timeStamp.toString());
		}
		if(note != null){
			cv.put(ALARMNOTE, note);
		}
		return mDb.update(ALARM_TABLE, cv, ALARMID + " = " + alarmId, null);
	}

	// delete section.
	public int deleteEntry(int rowId, OptionType option) {
		String id = DbAdapter.returnIdColumn(option);
		return mDb.delete(DbAdapter.returnTableName(option), id + " = " + rowId, null) ;
	}

	public int disconnectAPhotoFromManyContainers(int photoId, OptionType option){
		if(option != OptionType.PHOTOGROUP && option != OptionType.PHOTOSKIN){
			return 0;
		} else {
			String table = DbAdapter.returnTableName(option);
			return this.mDb.delete(table, PHOTOID + " = " + photoId, null);
		}
	}

	public int disconnectAPhotoFromManyContainers(int photoId, Set<Integer> setOfIDs, OptionType option){
		int count = 0;
		if(option != OptionType.PHOTOGROUP && option != OptionType.PHOTOSKIN){
			return 0;
		} else {
			String itemIdName = "";
			if(option == OptionType.PHOTOGROUP){
				itemIdName = GROUPID;
			} else {
				itemIdName = SKINCONDITIONID;
			}
			String table = DbAdapter.returnTableName(option);
			for(Integer id : setOfIDs){
				count += this.mDb.delete(table, PHOTOID + " = " + photoId + 
						" and " + itemIdName + " = " + id  , null);
			}
			return count;
		}
	}

	public int disconnectAContainerFromManyPhotos(int containerId, OptionType option){
		if(option != OptionType.PHOTOGROUP && option != OptionType.PHOTOSKIN){
			return 0;
		} else {
			String table = DbAdapter.returnTableName(option);
			String itemIdName = "";
			if(option == OptionType.PHOTOGROUP){
				itemIdName = GROUPID;
			} else if (option == OptionType.PHOTOSKIN){
				itemIdName = SKINCONDITIONID;
			}
			return this.mDb.delete(table, itemIdName + " = " + containerId, null);
		}
	}

	public int disconnectAContainerFromManyPhotos(int containerId, Set<Integer> setOfIDs, OptionType option){
		int count = 0;
		if(option != OptionType.PHOTOGROUP && option != OptionType.PHOTOSKIN){
			return 0;
		} else {
			String table = DbAdapter.returnTableName(option);
			String itemIdName = "";
			if(option == OptionType.PHOTOGROUP){
				itemIdName = GROUPID;
			} else if (option == OptionType.PHOTOSKIN){
				itemIdName = SKINCONDITIONID;
			}
			for(Integer id : setOfIDs){
				count += this.mDb.delete(table, itemIdName + " = " + containerId + 
						" and " + PHOTOID + " = " + id, null);
			}
			return count;
		}
	}

	public Cursor fetchAllContainers(OptionType option){
		if(option != OptionType.GROUP && option != OptionType.SKINCONDITION){
			option = null;
		}
		return this.fetchAllEntries(option);
	}

	public Cursor fetchAllPhotos(){
		return this.fetchAllEntries(OptionType.PHOTO);
	}

	public Cursor fetchAllAlarms(){
		return this.fetchAllEntries(OptionType.ALARM);
	}


	/**
	 * fetch all containers associated with the indicated photo. option = OptionType. PHOTOGROUP for
	 * retrieving all groups associated with the photo, option = OptionType.PHOTOSKIN for retrieving all
	 * skin conditions associated with the photo.
	 * @param photoId
	 * @param table
	 * @return
	 */
	public Cursor fetchAllContainersOfAPhoto(int photoId, OptionType option){
		if(photoId == DbAdapter.INVALID_ID){
			if(option == OptionType.PHOTOGROUP){
				String sql = "select * from " + GROUP_TABLE;
				return mDb.rawQuery(sql, null);
			} else if (option == OptionType.PHOTOSKIN){
				String sql = "select * from " + SKIN_TABLE;
				return mDb.rawQuery(sql, null);
			} else{
				return null;
			}
		}else {
			String itemIdName = "";
			String itemTable = "";
			String itemName = "";
			if(option == OptionType.PHOTOGROUP){
				itemIdName = GROUPID;
				itemTable = GROUP_TABLE;
				itemName = GROUPNAME;
			}else if(option == OptionType.PHOTOSKIN){
				itemTable = SKIN_TABLE;
				itemIdName = SKINCONDITIONID;
				itemName = SKINNAME;
			}
			String lookUpTable = DbAdapter.returnTableName(option);
			String sql = "select " + itemTable + "." + itemIdName + 
					" as " + itemIdName + ", "  + itemName +
					" from " + itemTable + " , " + lookUpTable  +
					" where " + itemTable + "." + itemIdName + " = " 
					+ lookUpTable + "." + itemIdName 
					+ " and " + PHOTOID + " = " + photoId;

			Cursor mCursor = mDb.rawQuery(sql, null);
			return mCursor;
		}
	}

	/**
	 * fetch all photos associated with the indicated container ( group or skin condition ). table = PHOTOGROUP_TALBE
	 * for retrieving all photos associated with the group, table = PHOTOSKIN_TABLE for retrieving all
	 * photos associated with the skin condition.
	 * @param containerId
	 * @param table
	 * @return
	 */
	public Cursor fetchAllPhotosOfAContainer(int containerId, OptionType option){
		String itemIdName = "";
		String lookUpTable = DbAdapter.returnTableName(option);

		if(option == OptionType.PHOTOGROUP){
			itemIdName = GROUPID;
		}else if(option == OptionType.PHOTOSKIN){
			itemIdName = SKINCONDITIONID;
		}

		String sql = " select " + PHOTO_TABLE + "." + PHOTOID + " as " + PHOTOID + " , " + LOCATION + " , " + 
				TIMESTAMP + " , " + PHOTONAME + " , " + PHOTOANNOTATION  + 
				" from " + PHOTO_TABLE + " , " + lookUpTable + 
				" where " + PHOTO_TABLE + "." + PHOTOID + " = " + lookUpTable + "." + PHOTOID +
				" and " + itemIdName + " = " + containerId;


		//		String preparedStatement = "select ?s.?s as ?s, ?s , ?s , ?s, ?s from ?s , ?s " +
		//				" where ?s" + "." + "?s = ?s" + "." + "?s and ?s = ?s";
		//		String[] args = {PHOTO_TABLE, PHOTOID, PHOTOID, LOCATION, TIMESTAMP, PHOTONAME, 
		//				PHOTOANNOTATION, PHOTO_TABLE, lookUpTable, PHOTO_TABLE, PHOTOID, lookUpTable, PHOTOID , 
		//				itemIdName, containerId + ""};
		Cursor mCursor = mDb.rawQuery(sql, null);
		return mCursor;

	}

	/**
	 * Return a Cursor over the list of all entries in the indicated table
	 * 
	 * @return Cursor over all entries
	 */
	public Cursor fetchAllEntries(OptionType option) {
		String table = DbAdapter.returnTableName(option);
		return mDb.query(table, null, 
				null, null, null, null, null);
	}

	public Cursor fetchAnEntry(int rowId, OptionType option) throws SQLException {

		Cursor mCursor =

				mDb.query(true, DbAdapter.returnTableName(option), null, 
						DbAdapter.returnIdColumn(option) + " = " + rowId, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}
}
