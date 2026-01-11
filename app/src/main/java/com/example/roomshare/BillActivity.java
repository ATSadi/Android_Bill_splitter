package com.example.roomshare;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.roomshare.data.Expense;
import com.example.roomshare.data.RoomEntity;
import com.example.roomshare.data.Roommate;
import com.example.roomshare.databinding.ActivityBillBinding;

import java.util.ArrayList;
import java.util.List;

public class BillActivity extends AppCompatActivity {
    private ActivityBillBinding binding;
    private DatabaseHelper dbHelper;
    private List<RoomEntity> rooms = new ArrayList<>();
    private List<Roommate> roommates = new ArrayList<>();
    private List<Expense> expenses = new ArrayList<>();
    private ArrayAdapter<String> roomAdapter;
    private ArrayAdapter<String> payerAdapter;
    private Long currentRoomId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBillBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);
        setupUI();
        loadRooms();
    }

    private void setupUI() {
        roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRoom.setAdapter(roomAdapter);

        payerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        payerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerPayer.setAdapter(payerAdapter);

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

        binding.btnAddExpense.setOnClickListener(v -> addExpense());
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
                    updatePayerSpinner();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> showStatus("Error loading roommates: " + e.getMessage()));
            }
        });

        dbHelper.getExpenses(currentRoomId, new DatabaseHelper.Callback<List<Expense>>() {
            @Override
            public void onSuccess(List<Expense> result) {
                runOnUiThread(() -> {
                    expenses = result;
                    updateExpenseList();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> showStatus("Error loading expenses: " + e.getMessage()));
            }
        });
    }

    private void updatePayerSpinner() {
        payerAdapter.clear();
        for (Roommate roommate : roommates) {
            payerAdapter.add(roommate.name);
        }
        payerAdapter.notifyDataSetChanged();
    }

    private void addExpense() {
        if (currentRoomId == null) {
            showStatus("Please select a room first");
            return;
        }

        if (roommates.isEmpty()) {
            showStatus("Please add roommates first");
            return;
        }

        String name = binding.editExpenseName.getText().toString().trim();
        if (name.isEmpty()) {
            showStatus("Expense name is required");
            return;
        }

        String amountStr = binding.editExpenseAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            showStatus("Expense amount is required");
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

        int selectedPosition = binding.spinnerPayer.getSelectedItemPosition();
        if (selectedPosition < 0 || selectedPosition >= roommates.size()) {
            showStatus("Please select a payer");
            return;
        }

        long payerId = roommates.get(selectedPosition).id;

        String splitCountStr = binding.editSplitCount.getText().toString().trim();
        if (splitCountStr.isEmpty()) {
            showStatus("Split count is required");
            return;
        }

        int splitCount;
        try {
            splitCount = Integer.parseInt(splitCountStr);
        } catch (NumberFormatException e) {
            showStatus("Invalid split count");
            return;
        }

        if (splitCount <= 0) {
            showStatus("Split count must be positive");
            return;
        }

        dbHelper.addExpense(name, amount, payerId, currentRoomId, splitCount, new DatabaseHelper.Callback<Long>() {
            @Override
            public void onSuccess(Long result) {
                runOnUiThread(() -> {
                    showStatus("Expense added successfully");
                    binding.editExpenseName.setText("");
                    binding.editExpenseAmount.setText("");
                    binding.editSplitCount.setText("");
                    loadRoomData();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> showStatus("Error adding expense: " + e.getMessage()));
            }
        });
    }

    private void updateExpenseList() {
        StringBuilder text = new StringBuilder();
        for (Expense expense : expenses) {
            String payer = "Unknown";
            for (Roommate roommate : roommates) {
                if (roommate.id == expense.payerId) {
                    payer = roommate.name;
                    break;
                }
            }
            double perPerson = expense.amount / expense.splitCount;
            text.append(expense.name).append(" - $").append(String.format("%.2f", expense.amount))
                .append(" paid by ").append(payer).append(" (Split ").append(expense.splitCount)
                .append(" ways, $").append(String.format("%.2f", perPerson)).append(" each)\n");
        }
        binding.textExpensesList.setText(text.length() > 0 ? text.toString() : "No expenses added yet");
    }

    private void showStatus(String message) {
        binding.textStatus.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

