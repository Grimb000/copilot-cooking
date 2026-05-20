package com.zizto.medcartandroidroom;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PatientRepository {
    private final PatientDao patientDao;
    private final ExecutorService executor;

    public PatientRepository(Application application) {
        PatientDatabase db = PatientDatabase.getInstance(application);
        patientDao = db.patientDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Patient>> getAllPatients() { return patientDao.getAllPatients(); }
    public LiveData<List<Patient>> getSortedByNameAsc() { return patientDao.getAllSortedByNameAsc(); }
    public LiveData<List<Patient>> getSortedByWeightDesc() { return patientDao.getAllSortedByWeightDesc(); }
    public LiveData<List<Patient>> getByDiagnosis(String search) { return patientDao.getPatientsByDiagnosis(search); }
    public LiveData<StatisticsResult> getStatistics() { return patientDao.getStatistics(); }
    public LiveData<List<BloodGroupCount>> getBloodGroupCounts() { return patientDao.getBloodGroupCounts(); }

    public void insert(Patient patient) { executor.execute(() -> patientDao.insert(patient)); }
    public void updateFirstWeight(float weight) { executor.execute(() -> patientDao.updateFirstPatientWeight(weight)); }
}