package com.zizto.medcarts;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    final String LOG_TAG = "myLogs";
    Button btnAdd, btnRead, btnUpdate, btnFilter, btnStats, btnGroupByBlood;
    EditText etFIO, etHeight, etWeight, etCardNumber, etDiagnosis, etBloodGroup;
    Spinner spinnerSort;
    RecyclerView recyclerView;
    PatientAdapter adapter;

    String currentSortOrder = null;
    String currentSelection = null;
    String[] currentSelectionArgs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etFIO = findViewById(R.id.etFIO);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        etCardNumber = findViewById(R.id.etCardNumber);
        etDiagnosis = findViewById(R.id.etDiagnosis);
        etBloodGroup = findViewById(R.id.etBloodGroup);

        btnAdd = findViewById(R.id.btnAdd);
        btnRead = findViewById(R.id.btnRead);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnFilter = findViewById(R.id.btnFilter);
        btnStats = findViewById(R.id.btnStats);
        btnGroupByBlood = findViewById(R.id.btnGroupByBlood);

        btnAdd.setOnClickListener(this);
        btnRead.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);
        btnFilter.setOnClickListener(this);
        btnStats.setOnClickListener(this);
        btnGroupByBlood.setOnClickListener(this);

        spinnerSort = findViewById(R.id.spinnerSort);
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(this,
                R.array.sort_options, android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: currentSortOrder = null; break;
                    case 1: currentSortOrder = "fio ASC"; break;
                    case 2: currentSortOrder = "fio DESC"; break;
                    case 3: currentSortOrder = "weight ASC"; break;
                    case 4: currentSortOrder = "weight DESC"; break;
                    case 5: currentSortOrder = "height ASC"; break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PatientAdapter();
        recyclerView.setAdapter(adapter);

        addInitialPatients();
        loadPatientsList();
    }

    private void addInitialPatients() {
        Cursor cursor = getContentResolver().query(PatientContract.CONTENT_URI,
                new String[]{"COUNT(*) as count"}, null, null, null);
        if (cursor != null && cursor.moveToFirst() && cursor.getInt(0) > 0) {
            cursor.close();
            return;
        }
        if (cursor != null) cursor.close();

        addPatient("Иванов И.И.", 180, 75.5f, "МК-001", "Грипп", "II+");
        addPatient("Петрова М.С.", 165, 60.0f, "МК-002", "ОРВИ", "I+");
        addPatient("Сидоров А.П.", 175, 82.3f, "МК-003", "Здоров", "III-");
        addPatient("Козлова А.В.", 170, 65.0f, "МК-004", "Аллергия", "IV+");
        addPatient("Смирнов Д.О.", 185, 90.0f, "МК-005", "Простуда", "I-");

        Toast.makeText(this, "Добавлено 5 тестовых записей", Toast.LENGTH_SHORT).show();
    }

    private void addPatient(String fio, int height, float weight, String card, String diag, String blood) {
        ContentValues values = new ContentValues();
        values.put(PatientContract.COLUMN_FIO, fio);
        values.put(PatientContract.COLUMN_HEIGHT, height);
        values.put(PatientContract.COLUMN_WEIGHT, weight);
        values.put(PatientContract.COLUMN_CARD, card);
        values.put(PatientContract.COLUMN_DIAGNOSIS, diag);
        values.put(PatientContract.COLUMN_BLOOD_GROUP, blood);
        getContentResolver().insert(PatientContract.CONTENT_URI, values);
    }

    private void loadPatientsList() {
        Cursor cursor = getContentResolver().query(
                PatientContract.CONTENT_URI,
                null,
                currentSelection,
                currentSelectionArgs,
                currentSortOrder
        );

        List<Patient> patientList = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Patient p = new Patient(
                        cursor.getInt(cursor.getColumnIndexOrThrow(PatientContract._ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(PatientContract.COLUMN_FIO)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(PatientContract.COLUMN_HEIGHT)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(PatientContract.COLUMN_WEIGHT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(PatientContract.COLUMN_CARD)),
                        cursor.getString(cursor.getColumnIndexOrThrow(PatientContract.COLUMN_DIAGNOSIS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(PatientContract.COLUMN_BLOOD_GROUP))
                );
                patientList.add(p);
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter.setPatients(patientList);
        Log.d(LOG_TAG, "Загружено пациентов: " + patientList.size());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btnRead) {
            currentSelection = null;
            currentSelectionArgs = null;
            loadPatientsList();

        } else if (id == R.id.btnAdd) {
            ContentValues values = new ContentValues();
            values.put(PatientContract.COLUMN_FIO, etFIO.getText().toString());
            values.put(PatientContract.COLUMN_HEIGHT, Integer.parseInt(etHeight.getText().toString()));
            values.put(PatientContract.COLUMN_WEIGHT, Float.parseFloat(etWeight.getText().toString()));
            values.put(PatientContract.COLUMN_CARD, etCardNumber.getText().toString());
            values.put(PatientContract.COLUMN_DIAGNOSIS, etDiagnosis.getText().toString());
            values.put(PatientContract.COLUMN_BLOOD_GROUP, etBloodGroup.getText().toString());

            Uri newUri = getContentResolver().insert(PatientContract.CONTENT_URI, values);
            Toast.makeText(this, "Добавлено: " + newUri, Toast.LENGTH_SHORT).show();
            loadPatientsList();

        } else if (id == R.id.btnUpdate) {
            ContentValues values = new ContentValues();
            values.put(PatientContract.COLUMN_WEIGHT, 99.9f);

            int updated = getContentResolver().update(
                    PatientContract.CONTENT_URI,
                    values,
                    PatientContract._ID + " = (SELECT MIN(id) FROM patients)",
                    null
            );
            Toast.makeText(this, "Обновлено " + updated + " записей", Toast.LENGTH_SHORT).show();
            loadPatientsList();

        } else if (id == R.id.btnFilter) {
            String diagFilter = etDiagnosis.getText().toString();
            if (!diagFilter.isEmpty()) {
                currentSelection = PatientContract.COLUMN_DIAGNOSIS + " LIKE ?";
                currentSelectionArgs = new String[]{"%" + diagFilter + "%"};
                loadPatientsList();
            } else {
                Toast.makeText(this, "Введите диагноз для фильтра", Toast.LENGTH_SHORT).show();
            }

        } else if (id == R.id.btnGroupByBlood) {
            showGroupByBloodResult();

        } else if (id == R.id.btnStats) {
            showStatistics();
        }
    }

    private void showStatistics() {
        Uri statsUri = PatientContract.CONTENT_URI.buildUpon()
                .appendPath("stats")
                .build();

        Cursor cursor = getContentResolver().query(statsUri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int total = cursor.getInt(cursor.getColumnIndexOrThrow("total"));
            float avgWeight = cursor.getFloat(cursor.getColumnIndexOrThrow("avg_weight"));
            float minWeight = cursor.getFloat(cursor.getColumnIndexOrThrow("min_weight"));
            float maxWeight = cursor.getFloat(cursor.getColumnIndexOrThrow("max_weight"));
            float avgHeight = cursor.getFloat(cursor.getColumnIndexOrThrow("avg_height"));
            cursor.close();

            String stats = String.format(
                    "Всего пациентов: %d\n" +
                            "Средний вес: %.1f кг\n" +
                            "Мин. вес: %.1f кг\n" +
                            "Макс. вес: %.1f кг\n" +
                            "Средний рост: %.1f см",
                    total, avgWeight, minWeight, maxWeight, avgHeight
            );

            new AlertDialog.Builder(this)
                    .setTitle("Статистика")
                    .setMessage(stats)
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private void showGroupByBloodResult() {
        Uri groupUri = PatientContract.CONTENT_URI.buildUpon()
                .appendPath("groupby")
                .appendPath("blood")
                .build();

        Cursor cursor = getContentResolver().query(groupUri, null, null, null, null);

        StringBuilder sb = new StringBuilder("Распределение по группам крови:\n\n");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String group = cursor.getString(cursor.getColumnIndexOrThrow("blood_group"));
                int count = cursor.getInt(cursor.getColumnIndexOrThrow("count"));
                sb.append(group).append(": ").append(count).append(" чел.\n");
            }
            cursor.close();
        }

        new AlertDialog.Builder(this)
                .setTitle("Группировка")
                .setMessage(sb.toString())
                .setPositiveButton("OK", null)
                .show();
    }
}
