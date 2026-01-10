package com.example.roomshare.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "bills",
    foreignKeys = @ForeignKey(
        entity = RoomEntity.class,
        parentColumns = "id",
        childColumns = "room_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index(value = "room_id")}
)
public class Bill {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public String name;
    
    public double amount;
    
    @ColumnInfo(name = "room_id")
    public long roomId;

    public Bill(long id, String name, double amount, long roomId) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.roomId = roomId;
    }

    @Ignore
    public Bill(String name, double amount, long roomId) {
        this.id = 0;
        this.name = name;
        this.amount = amount;
        this.roomId = roomId;
    }
}

