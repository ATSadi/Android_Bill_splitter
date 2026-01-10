package com.example.roomshare.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "roommates",
    foreignKeys = @ForeignKey(
        entity = RoomEntity.class,
        parentColumns = "id",
        childColumns = "room_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index(value = "room_id", name = "idx_roommate_room")}
)
public class Roommate {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public String name;
    
    public String email;
    
    public String phone;
    
    @ColumnInfo(name = "room_id")
    public long roomId;

    public Roommate(long id, String name, String email, String phone, long roomId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.roomId = roomId;
    }

    @Ignore
    public Roommate(String name, String email, String phone, long roomId) {
        this.id = 0;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.roomId = roomId;
    }
}

