package com.example.roomshare.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RoommateDao {
    @Query("SELECT * FROM roommates WHERE room_id = :roomId ORDER BY name")
    List<Roommate> getRoommatesByRoom(long roomId);

    @Query("SELECT * FROM roommates WHERE id = :id")
    Roommate getRoommateById(long id);

    @Query("SELECT * FROM roommates WHERE name = :name")
    List<Roommate> getRoommateByName(String name);

    @Insert
    long insertRoommate(Roommate roommate);

    @Delete
    void deleteRoommate(Roommate roommate);
}

