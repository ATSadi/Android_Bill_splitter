package com.example.roomshare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.roomshare.data.RoomEntity;
import com.example.roomshare.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private DatabaseHelper dbHelper;
    private List<RoomEntity> rooms = new ArrayList<>();
    private ArrayAdapter<String> roomAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);

        setupUI();
        loadRooms();
    }

    private void setupUI() {
        binding.titleText.setText("Welcome to RoomShare!");

        binding.btnSetup.setOnClickListener(v -> 
            startActivity(new Intent(this, SetupActivity.class)));

        binding.btnChoreBoard.setOnClickListener(v -> 
            startActivity(new Intent(this, ChoreActivity.class)));

        binding.btnBillTracker.setOnClickListener(v -> 
            startActivity(new Intent(this, BillActivity.class)));

        binding.btnBalance.setOnClickListener(v -> 
            startActivity(new Intent(this, BalanceActivity.class)));

        binding.btnHistory.setOnClickListener(v -> 
            startActivity(new Intent(this, HistoryActivity.class)));

        binding.btnReports.setOnClickListener(v -> 
            startActivity(new Intent(this, ReportActivity.class)));

        binding.btnDeleteRoom.setOnClickListener(v -> deleteSelectedRoom());

        roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRooms.setAdapter(roomAdapter);
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
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> 
                    Toast.makeText(MainActivity.this, "Error loading rooms: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void deleteSelectedRoom() {
        int selectedPosition = binding.spinnerRooms.getSelectedItemPosition();
        if (selectedPosition < 0 || rooms.isEmpty()) {
            Toast.makeText(this, "Please select a room to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        RoomEntity selectedRoom = rooms.get(selectedPosition);

        new AlertDialog.Builder(this)
            .setTitle("Delete Room")
            .setMessage("Are you sure you want to delete '" + selectedRoom.name + "'? This will delete all related data.")
            .setPositiveButton("Delete", (dialog, which) -> {
                dbHelper.deleteRoom(selectedRoom.id, new DatabaseHelper.Callback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "Room deleted successfully", Toast.LENGTH_SHORT).show();
                            loadRooms();
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> 
                            Toast.makeText(MainActivity.this, "Error deleting room: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show());
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRooms();
    }
}

