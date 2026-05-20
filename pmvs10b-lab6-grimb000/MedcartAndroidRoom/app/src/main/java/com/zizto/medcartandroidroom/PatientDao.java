package com.zizto.medcartandroidroom;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface PatientDao {

    @Query("SELECT * FROM patients")
    LiveData<List<Patient>> getAllPatients();
    @Query("SELECT * FROM patients ORDER BY fio ASC")
    LiveData<List<Patient>> getAllSortedByNameAsc();
    @Query("SELECT * FROM patients ORDER BY fio DESC")
    LiveData<List<Patient>> getAllSortedByNameDesc();
    @Query("SELECT * FROM patients ORDER BY weight ASC")
    LiveData<List<Patient>> getAllSortedByWeightAsc();
    @Query("SELECT * FROM patients ORDER BY weight DESC")
    LiveData<List<Patient>> getAllSortedByWeightDesc();
    @Query("SELECT * FROM patients ORDER BY height ASC")
    LiveData<List<Patient>> getAllSortedByHeightAsc();

    @Query("SELECT * FROM patients WHERE diagnosis LIKE '%' || :search || '%'")
    LiveData<List<Patient>> getPatientsByDiagnosis(String search);

    @Query("SELECT * FROM patients WHERE blood_group = :group")
    LiveData<List<Patient>> getPatientsByBloodGroup(String group);

    @Query("SELECT COUNT(*) as total, " +
            "AVG(weight) as avgWeight, " +
            "MIN(weight) as minWeight, " +
            "MAX(weight) as maxWeight, " +
            "AVG(height) as avgHeight " +
            "FROM patients")
    LiveData<StatisticsResult> getStatistics();
    @Query("SELECT blood_group, COUNT(*) as count FROM patients GROUP BY blood_group")
    LiveData<List<BloodGroupCount>> getBloodGroupCounts();

    @Insert
    void insert(Patient patient);

    @Update
    void update(Patient patient);

    @Delete
    void delete(Patient patient);

    @Query("UPDATE patients SET weight = :newWeight WHERE id = (SELECT MIN(id) FROM patients)")
    void updateFirstPatientWeight(float newWeight);
}