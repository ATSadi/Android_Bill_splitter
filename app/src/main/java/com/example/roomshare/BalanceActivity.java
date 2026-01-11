package com.example.roomshare;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.roomshare.data.RoomEntity;
import com.example.roomshare.databinding.ActivityBalanceBinding;

import java.util.ArrayList;
import java.util.List;

public class BalanceActivity extends AppCompatActivity {
    private ActivityBalanceBinding binding;
    private DatabaseHelper dbHelper;
    private List<RoomEntity> rooms = new ArrayList<>();
    private ArrayAdapter<String> roomAdapter;
    private Long currentRoomId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBalanceBinding.inflate(getLayoutInflater());
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

        binding.btnCalculateBalance.setOnClickListener(v -> calculateBalance());
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

    private void calculateBalance() {
        if (currentRoomId == null) {
            showStatus("Please select a room first");
            return;
        }

        dbHelper.calculateBalance(currentRoomId, new DatabaseHelper.Callback<DatabaseHelper.BalanceResult>() {
            @Override
            public void onSuccess(DatabaseHelper.BalanceResult result) {
                runOnUiThread(() -> {
                    displayBalance(result);
                    showStatus("Balance calculated successfully");
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> showStatus("Error calculating balance: " + e.getMessage()));
            }
        });
    }

    private void displayBalance(DatabaseHelper.BalanceResult balanceResult) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== BALANCE SUMMARY ===\n\n");

        for (DatabaseHelper.RoommateBalance balance : balanceResult.balances) {
            sb.append(balance.roommate.name).append(":\n");
            sb.append("  Paid: $").append(String.format("%.2f", balance.paid)).append("\n");
            sb.append("  Owed: $").append(String.format("%.2f", balance.owed)).append("\n");

            if (balance.net > 0) {
                sb.append("  Net Balance: +$").append(String.format("%.2f", balance.net))
                    .append(" (Others owe ").append(balance.roommate.name).append(")\n");
            } else if (balance.net < 0) {
                sb.append("  Net Balance: -$").append(String.format("%.2f", -balance.net))
                    .append(" (").append(balance.roommate.name).append(" owes others)\n");
            } else {
                sb.append("  Net Balance: $0.00 (Balanced)\n");
            }
            sb.append("\n");
        }

        sb.append("=== SETTLEMENT SUMMARY ===\n\n");

        List<DatabaseHelper.RoommateBalance> receivers = new ArrayList<>();
        List<DatabaseHelper.RoommateBalance> payers = new ArrayList<>();

        for (DatabaseHelper.RoommateBalance balance : balanceResult.balances) {
            if (balance.net > 0) receivers.add(balance);
            else if (balance.net < 0) payers.add(balance);
        }

        if (!receivers.isEmpty()) {
            sb.append("Should receive money:\n");
            for (DatabaseHelper.RoommateBalance balance : receivers) {
                sb.append("  ").append(balance.roommate.name).append(": $")
                    .append(String.format("%.2f", balance.net)).append("\n");
            }
            sb.append("\n");
        }

        if (!payers.isEmpty()) {
            sb.append("Owes money:\n");
            for (DatabaseHelper.RoommateBalance balance : payers) {
                sb.append("  ").append(balance.roommate.name).append(": $")
                    .append(String.format("%.2f", -balance.net)).append("\n");
            }
        }

        if (receivers.isEmpty() && payers.isEmpty()) {
            sb.append("All balances are settled!\n");
        }

        binding.textBalanceDisplay.setText(sb.toString());
    }

    private void showStatus(String message) {
        binding.textStatus.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

