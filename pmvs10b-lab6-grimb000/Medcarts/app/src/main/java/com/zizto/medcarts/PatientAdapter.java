package com.zizto.medcarts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.ViewHolder> {
    private List<Patient> patients = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_patient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Patient patient = patients.get(position);
        holder.tvFIO.setText(patient.getFio());

        String details = String.format("Рост: %d см | Вес: %.1f кг | Карта: %s" +
                        "\nДиагноз: %s | Группа крови: %s",
                patient.getHeight(), patient.getWeight(), patient.getCardNumber(),
                patient.getDiagnosis(), patient.getBloodGroup());
        holder.tvDetails.setText(details);
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    public void setPatients(List<Patient> patients) {
        this.patients = patients;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFIO, tvDetails;

        ViewHolder(View itemView) {
            super(itemView);
            tvFIO = itemView.findViewById(R.id.tvFIO);
            tvDetails = itemView.findViewById(R.id.tvDetails);
        }
    }
}