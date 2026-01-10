package com.example.roomshare.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ChoreDao {
    @Query("SELECT * FROM chores WHERE room_id = :roomId ORDER BY date DESC, id DESC")
    List<Chore> getChoresByRoom(long roomId);

    @Query("SELECT * FROM chores WHERE id = :id AND room_id = :roomId")
    Chore getChoreById(long id, long roomId);

    @Insert
    long insertChore(Chore chore);

    @Update
    void updateChore(Chore chore);
}

