package com.example.roomshare.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "expenses",
    foreignKeys = {
        @ForeignKey(
            entity = Roommate.class,
            parentColumns = "id",
            childColumns = "payer_id",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = RoomEntity.class,
            parentColumns = "id",
            childColumns = "room_id",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index(value = "room_id", name = "idx_expense_room"),
        @Index(value = "payer_id")
    }
)
public class Expense {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public String name;
    
    public double amount;
    
    @ColumnInfo(name = "payer_id")
    public long payerId;
    
    @ColumnInfo(name = "room_id")
    public long roomId;
    
    @ColumnInfo(name = "split_count")
    public int splitCount;
    
    public String date;

    public Expense(long id, String name, double amount, long payerId, long roomId, int splitCount, String date) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.payerId = payerId;
        this.roomId = roomId;
        this.splitCount = splitCount;
        this.date = date;
    }

    @Ignore
    public Expense(String name, double amount, long payerId, long roomId, int splitCount, String date) {
        this.id = 0;
        this.name = name;
        this.amount = amount;
        this.payerId = payerId;
        this.roomId = roomId;
        this.splitCount = splitCount;
        this.date = date;
    }
}

