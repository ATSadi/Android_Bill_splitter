package com.example.roomshare;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.roomshare.data.RoomEntity;
import com.example.roomshare.databinding.ActivityHistoryBinding;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private ActivityHistoryBinding binding;
    private DatabaseHelper dbHelper;
    private List<RoomEntity> rooms = new ArrayList<>();
    private ArrayAdapter<String> roomAdapter;
    private Long currentRoomId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
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
                    loadHistory();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.btnFilter.setOnClickListener(v -> loadHistory());
        binding.btnClear.setOnClickListener(v -> {
            binding.editFilter.setText("");
            loadHistory();
        });
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
                        loadHistory();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> showStatus("Error loading rooms: " + e.getMessage()));
            }
        });
    }

    private void loadHistory() {
        if (currentRoomId == null) {
            showStatus("Please select a room first");
            return;
        }

        String filterText = binding.editFilter.getText().toString().trim();

        dbHelper.getHistory(currentRoomId, filterText.isEmpty() ? null : filterText, 
                new DatabaseHelper.Callback<List<DatabaseHelper.HistoryItem>>() {
            @Override
            public void onSuccess(List<DatabaseHelper.HistoryItem> result) {
                runOnUiThread(() -> {
                    displayHistory(result);
                    showStatus("History loaded successfully");
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> showStatus("Error loading history: " + e.getMessage()));
            }
        });
    }

    private void displayHistory(List<DatabaseHelper.HistoryItem> history) {
        StringBuilder text = new StringBuilder();
        for (DatabaseHelper.HistoryItem item : history) {
            if (item instanceof DatabaseHelper.HistoryItem.ChoreItem) {
                DatabaseHelper.HistoryItem.ChoreItem choreItem = (DatabaseHelper.HistoryItem.ChoreItem) item;
                String status = (choreItem.chore.completed == 1) ? "[X]" : "[ ]";
                String statusText = (choreItem.chore.completed == 1) ? "Completed" : "Pending";
                text.append(status).append(" ").append(choreItem.chore.name)
                    .append(" - Assigned to: ").append(choreItem.assignedTo)
                    .append(" (").append(statusText).append(")\n");
            } else if (item instanceof DatabaseHelper.HistoryItem.ExpenseItem) {
                DatabaseHelper.HistoryItem.ExpenseItem expenseItem = (DatabaseHelper.HistoryItem.ExpenseItem) item;
                text.append(expenseItem.expense.name).append(" - Paid by: ")
                    .append(expenseItem.payer).append("\n");
            }
        }
        binding.textHistoryList.setText(text.length() > 0 ? text.toString() : "No history found");
    }

    private void showStatus(String message) {
        binding.textStatus.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

