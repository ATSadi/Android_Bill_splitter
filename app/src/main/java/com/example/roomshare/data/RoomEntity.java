package com.example.roomshare.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "rooms",
    indices = {@Index(value = "name", unique = true)}
)
public class RoomEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public String name;

    public RoomEntity(long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Ignore
    public RoomEntity(String name) {
        this.id = 0;
        this.name = name;
    }
}

