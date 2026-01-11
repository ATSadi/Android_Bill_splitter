package com.example.roomshare;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.roomshare.data.RoomEntity;
import com.example.roomshare.databinding.ActivityReportBinding;

import java.util.ArrayList;
import java.util.List;

public class ReportActivity extends AppCompatActivity {
    private ActivityReportBinding binding;
    private DatabaseHelper dbHelper;
    private List<RoomEntity> rooms = new ArrayList<>();
    private ArrayAdapter<String> roomAdapter;
    private Long currentRoomId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);
        setupUI();
        loadRooms();
    }

    private void setupUI() {
        roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRoom.setAdapter(roomAdapter);

        binding.spinnerRoom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < rooms.size()) {
                    currentRoomId = rooms.get(position).id;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.btnGenerateReport.setOnClickListener(v -> generateReport());
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void loadRooms() {
        dbHelper.getAllRooms(new DatabaseHelper.Callback<List<RoomEntity>>() {
            @Override
            public void onSuccess(List<RoomEntity> result) {
                runOnUiThread(() -> {
                    rooms = result;
                    roomAdapter.clear();
                    for (RoomEntity room : rooms) {
                        roomAdapter.add(room.name);
                    }
                    roomAdapter.notifyDataSetChanged();

                    if (!rooms.isEmpty()) {
                        currentRoomId = rooms.get(0).id;
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> showStatus("Error loading rooms: " + e.getMessage()));
            }
        });
    }

    private void generateReport() {
        if (currentRoomId == null) {
            showStatus("Please select a room first");
            return;
        }

        dbHelper.generateReport(currentRoomId, new DatabaseHelper.Callback<DatabaseHelper.Report>() {
            @Override
            public void onSuccess(DatabaseHelper.Report result) {
                runOnUiThread(() -> {
                    displayReport(result);
                    showStatus("Report generated successfully");
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> showStatus("Error generating report: " + e.getMessage()));
            }
        });
    }

    private void displayReport(DatabaseHelper.Report report) {
        StringBuilder sb = new StringBuilder();

        sb.append("=== CHORE STATISTICS ===\n\n");
        sb.append("Total Chores: ").append(report.totalChores).append("\n");
        sb.append("Completed: ").append(report.completedChores).append("\n");
        sb.append("Pending: ").append(report.pendingChores).append("\n\n");

        sb.append("=== EXPENSE STATISTICS ===\n\n");
        sb.append("Total Expenses: ").append(report.totalExpenses).append("\n");
        sb.append("Total Amount: $").append(String.format("%.2f", report.totalAmount)).append("\n");
        sb.append("Average per Expense: $").append(String.format("%.2f", report.avgPerExpense)).append("\n\n");

        sb.append("=== FAIRNESS METRICS ===\n\n");
        if (!report.choresPerRoommate.isEmpty()) {
            for (DatabaseHelper.Pair<String, Integer> pair : report.choresPerRoommate) {
                sb.append(pair.first).append(" completed ").append(pair.second).append(" chores\n");
            }
        } else {
            sb.append("No completed chores to display\n");
        }

        binding.textReportDisplay.setText(sb.toString());
    }

    private void showStatus(String message) {
        binding.textStatus.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

