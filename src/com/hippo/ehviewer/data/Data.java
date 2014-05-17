package com.hippo.ehviewer.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.SparseArray;

/**
 * @author Hippo
 * 
 */

public class Data {
    private static final String TAG = "Data";
    
    private static final int mVersion = 1;
    private static final String DB_NAME = "data";
    
    private static final String TABLE_GALLERY = "gallery";
    private static final String TABLE_READ = "read";
    private static final String TABLE_LOCAL_FAVOURITE = "local_favourite";
    private static final String TABLE_TAG = "tag";
    private static final String TABLE_DOWNLOAD = "download";
    
    private static final String COLUMN_GID = "gid";
    private static final String COLUMN_TOKEN = "token";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_POSTED = "posted";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_THUMB = "thumb";
    private static final String COLUMN_UPLOADER = "uploader";
    private static final String COLUMN_RATING = "rating";
    private static final String COLUMN_REFERENCE = "reference";
    
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_MODE = "mode";
    private static final String COLUMN_SEARCH = "search";
    private static final String COLUMN_ADVANCE = "advance";
    private static final String COLUMN_MIN_RATING = "min_rating";
    private static final String COLUMN_TAG = "tag";
    
    private static final String COLUMN_STATE = "state";
    private static final String COLUMN_PAGES = "pages";
    private static final String COLUMN_DETAIL = "detail";
    private static final String COLUMN_START_PAGE = "start_page";
    
    private SparseArray<GalleryInfo> mGallerys;
    
    private long mTagRowNum;
    private List<Tag> mTags;
    private List<GalleryInfo> mLocalFavourites;
    private List<GalleryInfo> mReads;
    private long mDownloadRowNum;
    private List<DownloadInfo> mDownloads;
    
    private Context mContext;
    
    private DBHelper mDBHelper;
    private SQLiteDatabase mDatabase;
    
    public Data(Context context) {
        mContext = context;
        mDBHelper = new DBHelper(mContext);
        mDatabase = mDBHelper.getWritableDatabase();
        
        mGallerys = new SparseArray<GalleryInfo>();
        
        getTags();
        getReads();
        getLocalFavourites();
        getDownloads();
    }
    
    @Override
    public void finalize() {
        mDBHelper.close();
    }
    
    /****** gallery ******/
    
    /**
     * Get galleryInfo in table, if do not exits return null
     * 
     * @param gid
     * @return
     */
    public GalleryInfo getGallery(int gid) {
        GalleryInfo galleryInfo = mGallerys.get(gid);
        if (galleryInfo == null) {
            Cursor cursor = mDatabase.rawQuery("select * from " + TABLE_GALLERY + " where " + COLUMN_GID + "=?",
                    new String[]{String.valueOf(gid)});
            if (cursor.moveToFirst()) {
                galleryInfo = new GalleryInfo();
                galleryInfo.gid = gid;
                galleryInfo.token = cursor.getString(1);
                galleryInfo.title = cursor.getString(2);
                galleryInfo.posted = cursor.getString(3);
                galleryInfo.category = cursor.getInt(4);
                galleryInfo.thumb = cursor.getString(5);
                galleryInfo.uploader = cursor.getString(6);
                galleryInfo.rating = cursor.getFloat(7);
                
                // add to map
                mGallerys.put(gid, galleryInfo);
            }
            cursor.close();
        }
        return galleryInfo;
    }
    
    @SuppressWarnings("unused")
    private synchronized boolean containsGallery(int gid) {
        boolean re = false;
        Cursor cursor = mDatabase.rawQuery("select * from "
                + TABLE_GALLERY + " where " + COLUMN_GID + "=?",
                new String[]{String.valueOf(gid)});
        if (cursor.moveToFirst())
            re = true;
        cursor.close();
        return re;
    }
    
    /**
     * If do not contain the gallery return -1;
     * @param gid
     * @return
     */
    private synchronized int getGalleryReference(int gid) {
        int reference = -1;
        Cursor cursor = mDatabase.rawQuery("select " + COLUMN_REFERENCE + " from "
                + TABLE_GALLERY + " where " + COLUMN_GID + "=?",
                new String[]{String.valueOf(gid)});
        if (cursor.moveToFirst()) {
            reference = cursor.getInt(0);
        }
        cursor.close();
        return reference;
    }
    
    /**
     * 
     * If exits same gid, add the reference by 1.
     * If exits no same gid, add it to table reference is 1
     * 
     * @param galleryInfo
     */
    private void addGallery(GalleryInfo galleryInfo) {
        boolean isInsert;
        int gid = galleryInfo.gid;
        // add to map
        mGallerys.put(gid, galleryInfo);
        
        // add to sql or update and add reference by 1
        int reference;
        if ((reference = getGalleryReference(gid)) != -1) {
            isInsert = false;
            reference++;
        }
        else {
            isInsert = true;
            reference = 1;
        }
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_GID, gid);
        values.put(COLUMN_TOKEN, galleryInfo.token);
        values.put(COLUMN_TITLE, galleryInfo.title);
        values.put(COLUMN_POSTED, galleryInfo.posted);
        values.put(COLUMN_CATEGORY, galleryInfo.category);
        values.put(COLUMN_THUMB, galleryInfo.thumb);
        values.put(COLUMN_UPLOADER, galleryInfo.uploader);
        values.put(COLUMN_RATING, galleryInfo.rating);
        values.put(COLUMN_REFERENCE, reference);
        
        if (isInsert) { // Set
            mDatabase.insert(TABLE_GALLERY, null, values);
        } else { // Update
            mDatabase.update(TABLE_GALLERY, values, COLUMN_GID + "=?", new String[]{String.valueOf(gid)});
        }
    }
    
    /**
     * True if update
     * 
     * @param gid
     * @return
     */
    private boolean updateGallery(GalleryInfo galleryInfo) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_GID, galleryInfo.gid);
        values.put(COLUMN_TOKEN, galleryInfo.token);
        values.put(COLUMN_TITLE, galleryInfo.title);
        values.put(COLUMN_POSTED, galleryInfo.posted);
        values.put(COLUMN_CATEGORY, galleryInfo.category);
        values.put(COLUMN_THUMB, galleryInfo.thumb);
        values.put(COLUMN_UPLOADER, galleryInfo.uploader);
        values.put(COLUMN_RATING, galleryInfo.rating);
        
        return mDatabase.update(TABLE_GALLERY, values,
                COLUMN_GID + "=?", new String[]{String.valueOf(galleryInfo.gid)}) != 0;
    }
    
    /**
     * decrease reference by 1, only delete when reference is 0
     * @param gid
     */
    private boolean deleteGallery(int gid) {
        int reference = getGalleryReference(gid);
        if (reference == -1) {
            Log.w(TAG, "Can't get reference when deleteGallery, gid is " + gid);
            return false;
        }
        reference--;
        if (reference == 0) { // delete gallery
            // delete in sql
            mDatabase.delete(TABLE_GALLERY, COLUMN_GID + "=?", new String[]{String.valueOf(gid)});
            // delete in list
            mGallerys.remove(gid);
        } else { // update reference
            ContentValues values = new ContentValues();
            values.put(COLUMN_REFERENCE, reference);
            mDatabase.update(TABLE_GALLERY, values, COLUMN_GID + "=?", new String[]{String.valueOf(gid)});
        }
        return true;
    }
    
    /****** download ******/
    private synchronized void getDownloads() {
        mDownloads = new ArrayList<DownloadInfo>();
        mDownloadRowNum = 0;
        
        Cursor cursor = mDatabase.rawQuery("select * from " + TABLE_DOWNLOAD
                + " order by " + COLUMN_ID + " asc", null);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                int gid = cursor.getInt(1);
                GalleryInfo galleryInfo = getGallery(gid);
                int state = cursor.getInt(2);
                int pages = cursor.getInt(3);
                byte[] detail = cursor.getBlob(4);
                int startPage = cursor.getInt(5);
                DownloadInfo downloadInfo = new DownloadInfo(
                        galleryInfo, state, pages, detail, startPage);
                mDownloads.add(downloadInfo);
                
                cursor.moveToNext();
                mDownloadRowNum++;
            }
        }
        cursor.close();
    }
    
    /**
     * True if contain this item
     * 
     * @param gid
     * @return
     */
    public synchronized boolean containsDownload(int gid) {
        boolean re = false;
        Cursor cursor = mDatabase.rawQuery("select * from "
                + TABLE_DOWNLOAD + " where " + COLUMN_GID + "=?",
                new String[]{String.valueOf(gid)});
        if (cursor.moveToFirst())
            re = true;
        cursor.close();
        return re;
    }
    
    public synchronized boolean addDownload(DownloadInfo downloadInfo) {
        GalleryInfo galleryInfo = downloadInfo.getGalleryInfo();
        int gid = galleryInfo.gid;
        // Delete from read
        deleteRead(gid);
        // Add to download
        if (containsDownload(gid)) {
            updateGallery(galleryInfo);
            return false;
        } else {
            addGallery(galleryInfo);
            
            // add to sql
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, mDownloadRowNum);
            values.put(COLUMN_GID, gid);
            values.put(COLUMN_STATE, downloadInfo.getState());
            values.put(COLUMN_DETAIL, downloadInfo.getDetail());
            values.put(COLUMN_START_PAGE, downloadInfo.getStartPage());
            if (mDatabase.insert(TABLE_DOWNLOAD, null,
                    values) != -1) { // If add ok
                mDownloadRowNum++;
                mDownloads.add(downloadInfo);
                return true;
            } else { // If add fail
                deleteGallery(gid);
                return false;
            }
        }
    }
    
    public synchronized boolean deleteDownload(int id) {
        int deleteNum = mDatabase.delete(TABLE_DOWNLOAD, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        if (deleteNum > 1)
            Log.e(TAG, "WTF? more than one id is " + id);
        
        if (deleteNum == 0)
            return false;
        if (id < 0 || id >= mDownloads.size()) {
            Log.e(TAG, "id is out of bounds, id is " + id + ", mDownloads.size() is " + mDownloads.size());
            return false;
        }
        
        // delete from sql
        ContentValues values = new ContentValues();
        for (int i = id + 1; i < mDownloadRowNum; i++) {
            values.put(COLUMN_ID, i - 1);
            mDatabase.update(TABLE_DOWNLOAD, values, COLUMN_ID + "=?", new String[]{String.valueOf(i)});
        }
        mDownloadRowNum -= deleteNum;
        
        // delete from list
        DownloadInfo downloadInfo = mDownloads.remove(id);
        
        // sub reference
        if (downloadInfo != null) {
            deleteGallery(downloadInfo.getGalleryInfo().gid);
        } else {
            Log.e(TAG, id + " of mDownloads is null");
        }
        
        return true;
    }
    
    public synchronized List<DownloadInfo> getAllDownloads() {
        return mDownloads;
    }
    
    public synchronized DownloadInfo getDownload(int location) {
        return mDownloads.get(location);
    }
    
    public synchronized void setDownload(int location, DownloadInfo downloadInfo) {
        if (location == mDownloadRowNum) {
            addDownload(downloadInfo);
        } else {
            // Update list
            mDownloads.set(location, downloadInfo);
            
            // Update sql
            GalleryInfo galleryInfo = downloadInfo.getGalleryInfo();
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, mDownloadRowNum);
            values.put(COLUMN_GID, galleryInfo.gid);
            values.put(COLUMN_STATE, downloadInfo.getState());
            values.put(COLUMN_DETAIL, downloadInfo.getDetail());
            values.put(COLUMN_START_PAGE, downloadInfo.getStartPage());
            mDatabase.update(TABLE_DOWNLOAD, values, COLUMN_ID + "=?", new String[]{String.valueOf(location)});
            
            // Update gallery
            updateGallery(galleryInfo);
        }
    }
    
    public synchronized void notifyDownloadChange(int location) {
        setDownload(location, mDownloads.get(location));
    }
    
    public synchronized boolean swapDownload(int indexOne, int indexTwo) {
        if (indexOne < 0 || indexOne > mDownloadRowNum
                || indexTwo < 0 || indexTwo > mDownloadRowNum)
            return false;
        
        if (indexOne == indexTwo)
            return true;
        
        // Update list
        DownloadInfo temp = mDownloads.get(indexOne);
        mDownloads.set(indexOne, mDownloads.get(indexTwo));
        mDownloads.set(indexTwo, temp);
        // Update sql
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, -1);
        mDatabase.update(TABLE_DOWNLOAD, values, COLUMN_ID + "=?", new String[]{String.valueOf(indexOne)});
        values.put(COLUMN_ID, indexOne);
        mDatabase.update(TABLE_DOWNLOAD, values, COLUMN_ID + "=?", new String[]{String.valueOf(indexTwo)});
        values.put(COLUMN_ID, indexTwo);
        mDatabase.update(TABLE_DOWNLOAD, values, COLUMN_ID + "=?", new String[]{String.valueOf(-1)});
        return true;
    }
    
    /****** read ******/
    private synchronized void getReads() {
        mReads = new ArrayList<GalleryInfo>();
        Cursor cursor = mDatabase.rawQuery("select * from "
                + TABLE_READ, null);
        
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                int gid = cursor.getInt(0);
                GalleryInfo galleryInfo = getGallery(gid);
                mReads.add(galleryInfo);
                cursor.moveToNext();
            }
        }
        cursor.close();
    }
    
    public synchronized List<GalleryInfo> getAllReads() {
        return mReads;
    }
    
    public synchronized boolean containsRead(int gid) {
        boolean re = false;
        Cursor cursor = mDatabase.rawQuery("select * from "
                + TABLE_READ + " where " + COLUMN_GID + "=?",
                new String[]{String.valueOf(gid)});
        if (cursor.moveToFirst())
            re = true;
        cursor.close();
        return re;
    }
    
    public synchronized void addRead(GalleryInfo galleryInfo) {
        int gid = galleryInfo.gid;
        // Do not add to read if it in download
        if (containsDownload(gid))
            return;
        
        if (containsRead(gid)) {
            updateGallery(galleryInfo);
        } else {
            addGallery(galleryInfo);
            // add to sql
            ContentValues values = new ContentValues();
            values.put(COLUMN_GID, galleryInfo.gid);
            mDatabase.insert(TABLE_READ, null, values);
            // add to list
            mReads.add(galleryInfo);
        }
    }
    
    public synchronized void deleteRead(int gid) {
        // delete from list
        for (GalleryInfo  galleryInfo : mReads) {
            if (galleryInfo.gid == gid) {
                mReads.remove(galleryInfo);
                break;
            }
        }
        
        // delete from sql
        mDatabase.delete(TABLE_READ, COLUMN_GID + "=?", new String[]{String.valueOf(gid)});
        deleteGallery(gid);
    }
    
    public synchronized void deleteAllReads() {
        // delete from list
        mReads.clear();
        
        // delete from sql
        Cursor cursor = mDatabase.rawQuery("select * from "
                + TABLE_READ, null);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                int gid = cursor.getInt(0);
                deleteGallery(gid);
                cursor.moveToNext();
            }
        }
        cursor.close();
        mDatabase.delete(TABLE_READ, null, null);
    }
    
    /****** local favourite ******/
    private synchronized void getLocalFavourites() {
        mLocalFavourites = new ArrayList<GalleryInfo>();
        Cursor cursor = mDatabase.rawQuery("select * from "
                + TABLE_LOCAL_FAVOURITE, null);
        
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                int gid = cursor.getInt(0);
                GalleryInfo galleryInfo = getGallery(gid);
                mLocalFavourites.add(galleryInfo);
                cursor.moveToNext();
            }
        }
        cursor.close();
    }
    
    public synchronized boolean containsLocalFavourite(int gid) {
        boolean re = false;
        Cursor cursor = mDatabase.rawQuery("select * from "
                + TABLE_LOCAL_FAVOURITE + " where " + COLUMN_GID + "=?",
                new String[]{String.valueOf(gid)});
        if (cursor.moveToFirst())
            re = true;
        cursor.close();
        return re;
    }
    
    /**
     * galleryInfo can't be null
     * @param galleryInfo
     */
    public synchronized void addLocalFavourite(GalleryInfo galleryInfo) {
        int gid = galleryInfo.gid;
        
        if (containsLocalFavourite(gid)) {
            updateGallery(galleryInfo);
        } else {
            addGallery(galleryInfo);
            // add to sql
            ContentValues values = new ContentValues();
            values.put(COLUMN_GID, galleryInfo.gid);
            mDatabase.insert(TABLE_LOCAL_FAVOURITE, null, values);
            // add to list
            mLocalFavourites.add(galleryInfo);
        }
    }
    
    public synchronized void deleteLocalFavourite(int gid) {
        // delete from list
        for (GalleryInfo  galleryInfo : mLocalFavourites) {
            if (galleryInfo.gid == gid) {
                mLocalFavourites.remove(galleryInfo);
                break;
            }
        }
        
        // delete from sql
        mDatabase.delete(TABLE_LOCAL_FAVOURITE, COLUMN_GID + "=?", new String[]{String.valueOf(gid)});
        deleteGallery(gid);
    }
    
    public synchronized List<GalleryInfo> getAllLocalFavourites() {
        return mLocalFavourites;
    }
    
    /******  tag ******/
    private synchronized void getTags() {
        mTags = new ArrayList<Tag>();
        mTagRowNum = 0;
        Cursor cursor = mDatabase.rawQuery("select * from " + TABLE_TAG
                + " order by " + COLUMN_ID + " asc", null);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                
                String name = cursor.getString(1);
                int mode = cursor.getInt(2);
                int category = cursor.getInt(3);
                String search = cursor.getString(4);
                int advance = cursor.getInt(5);
                int min_rating = cursor.getInt(6);
                String tagStr = cursor.getString(7);
                
                Tag tag = new Tag(name, category, search);
                tag.setAdvance(advance, min_rating);
                tag.setTag(tagStr);
                tag.setMode(mode);
                
                mTags.add(tag);
                
                cursor.moveToNext();
                mTagRowNum++;
            }
        }
        cursor.close();
    }
    
    public synchronized boolean addTag(Tag tag) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, mTagRowNum);
        values.put(COLUMN_NAME, tag.getName());
        values.put(COLUMN_MODE, tag.getName());
        values.put(COLUMN_CATEGORY, tag.getType());
        values.put(COLUMN_SEARCH, tag.getSearch());
        values.put(COLUMN_ADVANCE, tag.getAdvanceType());
        values.put(COLUMN_MIN_RATING, tag.getMinRating());
        values.put(COLUMN_TAG, tag.getTag());
        
        boolean ok = mDatabase.insert(
                TABLE_TAG, null, values) != -1;
        if (ok) {
            mTagRowNum++;
            mTags.add(tag);
        }
        return ok;
    }
    
    public synchronized boolean deleteTag(int id) {
        int deleteNum = mDatabase.delete(TABLE_TAG, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        if (deleteNum > 1)
            Log.e(TAG, "WTF? more than one id is " + id);
        
        if (deleteNum == 0)
            return false;
        if (id < 0 || id >= mTags.size()) {
            Log.e(TAG, "id is out of bounds, id is " + id + ", mTags.size() is " + mTags.size());
            return false;
        }
        
        // Update sql
        ContentValues values = new ContentValues();
        for (int i = id + 1; i < mTagRowNum; i++) {
            values.put(COLUMN_ID, i - 1);
            mDatabase.update(TABLE_TAG, values, COLUMN_ID + "=?", new String[]{String.valueOf(i)});
        }
        mTagRowNum -= deleteNum;
        
        // Update list
        mTags.remove(id);
        
        return true;
    }
    
    public synchronized List<Tag> getAllTags() {
        return mTags;
    }
    
    public synchronized Tag getTag(int location) {
        return mTags.get(location);
    }
    
    public synchronized List<String> getAllTagNames() {
        List<String> names = new ArrayList<String>();
        for (Tag tag : mTags)
            names.add(tag.getName());
        return names;
    }
    
    /**
     * location >= 0 && location <= tagRowNum please
     * @param location
     * @param tag
     */
    public synchronized void setTag(int location, Tag tag) {
        if (location == mTagRowNum) {
            addTag(tag);
        } else {
            // Update list
            mTags.set(location, tag);
            
            // Update sql
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, tag.getName());
            values.put(COLUMN_CATEGORY, tag.getType());
            values.put(COLUMN_SEARCH, tag.getSearch());
            values.put(COLUMN_ADVANCE, tag.getAdvanceType());
            values.put(COLUMN_MIN_RATING, tag.getMinRating());
            mDatabase.update(TABLE_TAG, values, COLUMN_ID + "=?", new String[]{String.valueOf(location)});
        }
    }
    
    public synchronized boolean swapTag(int indexOne, int indexTwo) {
        if (indexOne < 0 || indexOne > mTagRowNum
                || indexTwo < 0 || indexTwo > mTagRowNum)
            return false;
        
        if (indexOne == indexTwo)
            return true;
        
        // Update list
        Tag temp = mTags.get(indexOne);
        mTags.set(indexOne, mTags.get(indexTwo));
        mTags.set(indexTwo, temp);
        // Update sql
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, -1);
        mDatabase.update(TABLE_TAG, values, COLUMN_ID + "=?", new String[]{String.valueOf(indexOne)});
        values.put(COLUMN_ID, indexOne);
        mDatabase.update(TABLE_TAG, values, COLUMN_ID + "=?", new String[]{String.valueOf(indexTwo)});
        values.put(COLUMN_ID, indexTwo);
        mDatabase.update(TABLE_TAG, values, COLUMN_ID + "=?", new String[]{String.valueOf(-1)});
        return true;
    }
    
    public class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, DB_NAME, null, mVersion);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // gallery
            String CreateGallery = "create table " + TABLE_GALLERY + "("
                    + COLUMN_GID + " integer primary key,"
                    + COLUMN_TOKEN + " text,"
                    + COLUMN_TITLE + " text,"
                    + COLUMN_POSTED + " text,"
                    + COLUMN_CATEGORY + " integer,"
                    + COLUMN_THUMB + " text,"
                    + COLUMN_UPLOADER + " text,"
                    + COLUMN_RATING + " float,"
                    + COLUMN_REFERENCE + " integer);";
            db.execSQL(CreateGallery);
            
            // read
            String CreateRead = "create table "
                    + TABLE_READ + "("
                    + COLUMN_GID + " integer primary key,"
                    + "foreign key(" + COLUMN_GID + ") references " + TABLE_GALLERY + "(" + COLUMN_GID + "));";
            db.execSQL(CreateRead);
            
            // local favourite
            String CreateLocalFavourite = "create table "
                    + TABLE_LOCAL_FAVOURITE + "("
                    + COLUMN_GID + " integer primary key,"
                    + "foreign key(" + COLUMN_GID + ") references " + TABLE_GALLERY + "(" + COLUMN_GID + "));";
            db.execSQL(CreateLocalFavourite);
            
            // tag
            String CreateTag = "create table "
                    + TABLE_TAG + "("
                    + COLUMN_ID + " integer primary key,"
                    + COLUMN_NAME + " text,"
                    + COLUMN_MODE + " integer,"
                    + COLUMN_CATEGORY + " integer,"
                    + COLUMN_SEARCH + " text,"
                    + COLUMN_ADVANCE + " integer,"
                    + COLUMN_MIN_RATING + " integer,"
                    + COLUMN_TAG + " text);";
            db.execSQL(CreateTag);
            
            // download
            String CreateDownload = "create table "
                    + TABLE_DOWNLOAD + "("
                    + COLUMN_ID + " integer primary key,"
                    + COLUMN_GID + " integer unique,"
                    + COLUMN_STATE + " integer,"
                    + COLUMN_PAGES + " integer,"
                    + COLUMN_DETAIL + " blob,"
                    + COLUMN_START_PAGE + " integer,"
                    + "foreign key(" + COLUMN_GID + ") references " + TABLE_GALLERY + "(" + COLUMN_GID + "));";
            db.execSQL(CreateDownload);
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists " + TABLE_GALLERY);
            onCreate(db);
        }
    }
}
