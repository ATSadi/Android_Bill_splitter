package com.example.roomshare.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RoomDao {
    @Query("SELECT * FROM rooms ORDER BY name")
    List<RoomEntity> getAllRooms();

    @Query("SELECT * FROM rooms WHERE id = :id")
    RoomEntity getRoomById(long id);

    @Query("SELECT * FROM rooms WHERE name = :name LIMIT 1")
    RoomEntity getRoomByName(String name);

    @Insert
    long insertRoom(RoomEntity room);

    @Delete
    void deleteRoom(RoomEntity room);
}

