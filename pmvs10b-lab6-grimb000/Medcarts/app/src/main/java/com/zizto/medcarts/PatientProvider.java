package com.zizto.medcarts;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PatientProvider extends ContentProvider {

    private static final String TAG = "PatientProvider";

    private static final int PATIENTS = 100;
    private static final int PATIENT_ID = 101;
    private static final int STATS = 200;
    private static final int GROUP_BLOOD = 300;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(PatientContract.AUTHORITY, "patients", PATIENTS);
        uriMatcher.addURI(PatientContract.AUTHORITY, "patients/#", PATIENT_ID);
        uriMatcher.addURI(PatientContract.AUTHORITY, "patients/stats", STATS);
        uriMatcher.addURI(PatientContract.AUTHORITY, "patients/groupby/blood", GROUP_BLOOD);
    }

    private DatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;

        int match = uriMatcher.match(uri);

        switch (match) {
            case PATIENTS:
                SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
                qb.setTables("patients");
                cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case PATIENT_ID:
                String id = uri.getLastPathSegment();
                cursor = db.query("patients", projection, "id=?", new String[]{id},
                        null, null, sortOrder);
                break;

            case STATS:
                cursor = db.rawQuery(
                        "SELECT COUNT(*) as total, " +
                                "AVG(weight) as avg_weight, " +
                                "MIN(weight) as min_weight, " +
                                "MAX(weight) as max_weight, " +
                                "AVG(height) as avg_height " +
                                "FROM patients", null);
                break;

            case GROUP_BLOOD:
                cursor = db.query("patients",
                        new String[]{"blood_group", "COUNT(*) as count"},
                        null, null, "blood_group", null, "blood_group");
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = uriMatcher.match(uri);
        switch (match) {
            case PATIENTS:
                return "vnd.android.cursor.dir/vnd.zizto.patients";
            case PATIENT_ID:
                return "vnd.android.cursor.item/vnd.zizto.patient";
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (uriMatcher.match(uri) != PATIENTS) {
            throw new IllegalArgumentException("Invalid URI for insert: " + uri);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = db.insert("patients", null, values);

        if (id > 0) {
            Uri resultUri = Uri.withAppendedPath(PatientContract.CONTENT_URI, String.valueOf(id));
            getContext().getContentResolver().notifyChange(resultUri, null);
            return resultUri;
        }
        throw new android.database.SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;

        int match = uriMatcher.match(uri);
        if (match == PATIENT_ID) {
            String id = uri.getLastPathSegment();
            count = db.delete("patients", "id=?", new String[]{id});
        } else {
            count = db.delete("patients", selection, selectionArgs);
        }

        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;

        int match = uriMatcher.match(uri);
        if (match == PATIENT_ID) {
            String id = uri.getLastPathSegment();
            count = db.update("patients", values, "id=?", new String[]{id});
        } else {
            count = db.update("patients", values, selection, selectionArgs);
        }

        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "PatientDB";
        private static final int DATABASE_VERSION = 3;

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE patients (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "fio TEXT, " +
                    "height INTEGER, " +
                    "weight REAL, " +
                    "card_number TEXT, " +
                    "diagnosis TEXT, " +
                    "blood_group TEXT" +
                    ")");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < 2) {
                db.execSQL("ALTER TABLE patients ADD COLUMN diagnosis TEXT");
                db.execSQL("ALTER TABLE patients ADD COLUMN blood_group TEXT");
                db.execSQL("UPDATE patients SET diagnosis='Не указан', blood_group='Неизвестно'");
            }
        }
    }
}