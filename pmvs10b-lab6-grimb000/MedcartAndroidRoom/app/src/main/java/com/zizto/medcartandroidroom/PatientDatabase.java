package com.zizto.medcartandroidroom;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Patient.class}, version = 3)
public abstract class PatientDatabase extends RoomDatabase {
    public abstract PatientDao patientDao();

    private static volatile PatientDatabase INSTANCE;

    public static PatientDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (PatientDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    PatientDatabase.class,
                                    "PatientDB")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}