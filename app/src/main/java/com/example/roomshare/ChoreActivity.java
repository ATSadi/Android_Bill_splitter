package com.example.roomshare;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.roomshare.data.Chore;
import com.example.roomshare.data.RoomEntity;
import com.example.roomshare.data.Roommate;
import com.example.roomshare.databinding.ActivityChoreBinding;

import java.util.ArrayList;
import java.util.List;

public class ChoreActivity extends AppCompatActivity {
    private ActivityChoreBinding binding;
    private DatabaseHelper dbHelper;
    private List<RoomEntity> rooms = new ArrayList<>();
    private List<Roommate> roommates = new ArrayList<>();
    private List<Chore> chores = new ArrayList<>();
    private ArrayAdapter<String> roomAdapter;
    private ArrayAdapter<String> roommateAdapter;
    private ArrayAdapter<String> choreAdapter;
    private Long currentRoomId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChoreBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);
        setupUI();
        loadRooms();
    }

    private void setupUI() {
        roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRoom.setAdapter(roomAdapter);

        roommateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        roommateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerAssignee.setAdapter(roommateAdapter);

        choreAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, new ArrayList<>());
        binding.listChores.setAdapter(choreAdapter);
        binding.listChores.setChoiceMode(android.widget.AbsListView.CHOICE_MODE_SINGLE);

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

        binding.btnAddChore.setOnClickListener(v -> addChore());
        binding.btnMarkComplete.setOnClickListener(v -> markChoreComplete());
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
                    updateRoommateSpinner();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> showStatus("Error loading roommates: " + e.getMessage()));
            }
        });

        dbHelper.getChores(currentRoomId, new DatabaseHelper.Callback<List<Chore>>() {
            @Override
            public void onSuccess(List<Chore> result) {
                runOnUiThread(() -> {
                    chores = result;
                    updateChoreList();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> showStatus("Error loading chores: " + e.getMessage()));
            }
        });
    }

    private void updateRoommateSpinner() {
        roommateAdapter.clear();
        roommateAdapter.add("Unassigned");
        for (Roommate roommate : roommates) {
            roommateAdapter.add(roommate.name);
        }
        roommateAdapter.notifyDataSetChanged();
    }

    private void addChore() {
        if (currentRoomId == null) {
            showStatus("Please select a room first");
            return;
        }

        String name = binding.editChoreName.getText().toString().trim();
        if (name.isEmpty()) {
            showStatus("Chore name is required");
            return;
        }

        int selectedPosition = binding.spinnerAssignee.getSelectedItemPosition();
        Long assignedToId = (selectedPosition > 0 && selectedPosition <= roommates.size()) 
            ? roommates.get(selectedPosition - 1).id : null;

        dbHelper.addChore(name, assignedToId, currentRoomId, new DatabaseHelper.Callback<Long>() {
            @Override
            public void onSuccess(Long result) {
                runOnUiThread(() -> {
                    showStatus("Chore added successfully");
                    binding.editChoreName.setText("");
                    binding.spinnerAssignee.setSelection(0);
                    loadRoomData();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> showStatus("Error adding chore: " + e.getMessage()));
            }
        });
    }

    private void markChoreComplete() {
        if (currentRoomId == null) {
            showStatus("Please select a room first");
            return;
        }

        int selectedPosition = binding.listChores.getCheckedItemPosition();
        if (selectedPosition < 0 || selectedPosition >= chores.size()) {
            showStatus("Please select a chore to mark as complete");
            return;
        }

        Chore selectedChore = chores.get(selectedPosition);

        dbHelper.markChoreComplete(selectedChore.id, currentRoomId, new DatabaseHelper.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    showStatus("Chore marked as complete");
                    loadRoomData();
                    binding.listChores.clearChoices();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> showStatus("Error marking chore complete: " + e.getMessage()));
            }
        });
    }

    private void updateChoreList() {
        choreAdapter.clear();
        for (Chore chore : chores) {
            String status = (chore.completed == 1) ? "[X]" : "[ ]";
            String assignedTo = "Unassigned";
            if (chore.assignedToId != null) {
                for (Roommate roommate : roommates) {
                    if (roommate.id == chore.assignedToId) {
                        assignedTo = roommate.name;
                        break;
                    }
                }
            }
            choreAdapter.add(status + " " + chore.name + " - Assigned to: " + assignedTo);
        }
        choreAdapter.notifyDataSetChanged();
    }

    private void showStatus(String message) {
        binding.textStatus.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

