package com.example.roomshare;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.roomshare.data.Bill;
import com.example.roomshare.data.RoomEntity;
import com.example.roomshare.data.Roommate;
import com.example.roomshare.databinding.ActivitySetupBinding;

import java.util.ArrayList;
import java.util.List;

public class SetupActivity extends AppCompatActivity {
    private ActivitySetupBinding binding;
    private DatabaseHelper dbHelper;
    private List<RoomEntity> rooms = new ArrayList<>();
    private List<Roommate> roommates = new ArrayList<>();
    private List<Bill> bills = new ArrayList<>();
    private ArrayAdapter<String> roomAdapter;
    private Long currentRoomId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetupBinding.inflate(getLayoutInflater());
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
                    loadRoomData();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.btnAddRoom.setOnClickListener(v -> addRoom());
        binding.btnAddRoommate.setOnClickListener(v -> addRoommate());
        binding.btnAddBill.setOnClickListener(v -> addBill());
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
                        loadRoomData();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> showStatus("Error loading rooms: " + e.getMessage()));
            }
        });
    }

    private void loadRoomData() {
        if (currentRoomId == null) return;

        dbHelper.getRoommates(currentRoomId, new DatabaseHelper.Callback<List<Roommate>>() {
            @Override
            public void onSuccess(List<Roommate> result) {
                runOnUiThread(() -> {
                    roommates = result;
                    updateRoommateList();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> showStatus("Error loading roommates: " + e.getMessage()));
            }
        });

        dbHelper.getBills(currentRoomId, new DatabaseHelper.Callback<List<Bill>>() {
            @Override
            public void onSuccess(List<Bill> result) {
                runOnUiThread(() -> {
                    bills = result;
                    updateBillList();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> showStatus("Error loading bills: " + e.getMessage()));
            }
        });
    }

    private void addRoom() {
        String roomName = binding.editRoomName.getText().toString().trim();
        if (roomName.isEmpty()) {
            showStatus("Room name cannot be empty");
            return;
        }

        dbHelper.createOrGetRoom(roomName, new DatabaseHelper.Callback<Long>() {
            @Override
            public void onSuccess(Long roomId) {
                runOnUiThread(() -> {
                    showStatus("Room created/selected successfully");
                    binding.editRoomName.setText("");
                    loadRooms();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> showStatus("Error adding room: " + e.getMessage()));
            }
        });
    }

    private void addRoommate() {
        if (currentRoomId == null) {
            showStatus("Please select or create a room first");
            return;
        }

        String name = binding.editRoommateName.getText().toString().trim();
        if (name.isEmpty()) {
            showStatus("Roommate name is required");
            return;
        }

        String email = binding.editRoommateEmail.getText().toString().trim();
        String phone = binding.editRoommatePhone.getText().toString().trim();

        dbHelper.addRoommate(name, email.isEmpty() ? null : email, phone.isEmpty() ? null : phone, 
                currentRoomId, new DatabaseHelper.Callback<Long>() {
            @Override
            public void onSuccess(Long result) {
                runOnUiThread(() -> {
                    showStatus("Roommate added successfully");
                    binding.editRoommateName.setText("");
                    binding.editRoommateEmail.setText("");
                    binding.editRoommatePhone.setText("");
                    loadRoomData();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> showStatus("Error: " + e.getMessage()));
            }
        });
    }

    private void addBill() {
        if (currentRoomId == null) {
            showStatus("Please select or create a room first");
            return;
        }

        String name = binding.editBillName.getText().toString().trim();
        if (name.isEmpty()) {
            showStatus("Bill name is required");
            return;
        }

        String amountStr = binding.editBillAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            showStatus("Bill amount is required");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            showStatus("Invalid amount");
            return;
        }

        if (amount <= 0) {
            showStatus("Amount must be positive");
            return;
        }

        dbHelper.addBill(name, amount, currentRoomId, new DatabaseHelper.Callback<Long>() {
            @Override
            public void onSuccess(Long result) {
                runOnUiThread(() -> {
                    showStatus("Bill added successfully");
                    binding.editBillName.setText("");
                    binding.editBillAmount.setText("");
                    loadRoomData();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> showStatus("Error adding bill: " + e.getMessage()));
            }
        });
    }

    private void updateRoommateList() {
        StringBuilder text = new StringBuilder();
        for (Roommate roommate : roommates) {
            text.append(roommate.name);
            if (roommate.email != null) text.append(" - ").append(roommate.email);
            if (roommate.phone != null) text.append(" - ").append(roommate.phone);
            text.append("\n");
        }
        binding.textRoommatesList.setText(text.length() > 0 ? text.toString() : "No roommates added yet");
    }

    private void updateBillList() {
        StringBuilder text = new StringBuilder();
        for (Bill bill : bills) {
            text.append(bill.name).append(" - $").append(String.format("%.2f", bill.amount)).append("\n");
        }
        binding.textBillsList.setText(text.length() > 0 ? text.toString() : "No bills added yet");
    }

    private void showStatus(String message) {
        binding.textStatus.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

