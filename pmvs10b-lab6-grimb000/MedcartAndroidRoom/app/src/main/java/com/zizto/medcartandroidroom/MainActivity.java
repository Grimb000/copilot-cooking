package com.zizto.medcartandroidroom;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private PatientRepository repository;
    private PatientAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = new PatientRepository(getApplication());

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PatientAdapter();
        recyclerView.setAdapter(adapter);

        repository.getAllPatients().observe(this, patients -> {
            adapter.setPatients(patients);
            if (patients.isEmpty()) addInitialData();
        });

        findViewById(R.id.btnRead).setOnClickListener(this);
        findViewById(R.id.btnAdd).setOnClickListener(this);
        findViewById(R.id.btnUpdate).setOnClickListener(this);
        findViewById(R.id.btnFilter).setOnClickListener(this);
        findViewById(R.id.btnStats).setOnClickListener(this);
        findViewById(R.id.btnGroupByBlood).setOnClickListener(this);
    }

    private void addInitialData() {
        repository.insert(new Patient("А А А", 180, 75.5f, "C001", "Грипп", "II+"));
        repository.insert(new Patient("Б Б Б", 165, 77.0f, "C002", "ОРВИ", "I+"));
        repository.insert(new Patient("В В В", 170, 66.0f, "C003", "Грипп", "II+"));
        repository.insert(new Patient("Г Г Г", 175, 68.0f, "C004", "Грипп", "I+"));
        repository.insert(new Patient("Д Д Д", 200, 69.0f, "C005", "ОРВИ", "I+"));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btnRead) {
            repository.getSortedByWeightDesc().observe(this, adapter::setPatients);

        } else if (id == R.id.btnAdd) {
            String f = ((EditText)findViewById(R.id.etFIO)).getText().toString();
            int h = Integer.parseInt(((EditText)findViewById(R.id.etHeight)).getText().toString());
            float w = Float.parseFloat(((EditText)findViewById(R.id.etWeight)).getText().toString());
            repository.insert(new Patient(f, h, w, ((EditText)findViewById(R.id.etCardNumber)).getText().toString(),
                    ((EditText)findViewById(R.id.etDiagnosis)).getText().toString(),
                    ((EditText)findViewById(R.id.etBloodGroup)).getText().toString()));
            Toast.makeText(this, "Добавлено", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.btnUpdate) {
            repository.updateFirstWeight(99.9f);

        } else if (id == R.id.btnFilter) {
            String diag = ((EditText)findViewById(R.id.etDiagnosis)).getText().toString();
            repository.getByDiagnosis(diag).observe(this, adapter::setPatients);

        } else if (id == R.id.btnStats) {
            repository.getStatistics().observe(this, stats -> {
                String text = String.format("Всего: %d\nСредний вес: %.1f", stats.total, stats.avgWeight);
                new AlertDialog.Builder(this).setMessage(text).show();
            });

        } else if (id == R.id.btnGroupByBlood) {
            repository.getBloodGroupCounts().observe(this, counts -> {
                StringBuilder sb = new StringBuilder();
                for (BloodGroupCount c : counts) {
                    sb.append(c.bloodGroup).append(": ").append(c.count).append("\n");
                }
                new AlertDialog.Builder(this).setMessage(sb.toString()).show();
            });
        }
    }
}