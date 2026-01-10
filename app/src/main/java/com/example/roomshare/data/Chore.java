package com.example.roomshare.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "chores",
    foreignKeys = {
        @ForeignKey(
            entity = Roommate.class,
            parentColumns = "id",
            childColumns = "assigned_to_id",
            onDelete = ForeignKey.SET_NULL
        ),
        @ForeignKey(
            entity = RoomEntity.class,
            parentColumns = "id",
            childColumns = "room_id",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index(value = "room_id", name = "idx_chore_room"),
        @Index(value = "assigned_to_id")
    }
)
public class Chore {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public String name;
    
    @ColumnInfo(name = "assigned_to_id")
    public Long assignedToId;  // Long allows null
    
    @ColumnInfo(name = "room_id")
    public long roomId;
    
    public int completed;
    
    public String date;

    public Chore(long id, String name, Long assignedToId, long roomId, int completed, String date) {
        this.id = id;
        this.name = name;
        this.assignedToId = assignedToId;
        this.roomId = roomId;
        this.completed = completed;
        this.date = date;
    }

    @Ignore
    public Chore(String name, Long assignedToId, long roomId, int completed, String date) {
        this.id = 0;
        this.name = name;
        this.assignedToId = assignedToId;
        this.roomId = roomId;
        this.completed = completed;
        this.date = date;
    }
}

