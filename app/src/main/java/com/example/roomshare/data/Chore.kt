package com.example.roomshare.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chores",
    foreignKeys = [
        ForeignKey(
            entity = Roommate::class,
            parentColumns = ["id"],
            childColumns = ["assigned_to_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = RoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["room_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["room_id"], name = "idx_chore_room"),
        Index(value = ["assigned_to_id"])
    ]
)
data class Chore(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    @ColumnInfo(name = "assigned_to_id")
    val assigned_to_id: Long?,
    @ColumnInfo(name = "room_id")
    val room_id: Long,
    val completed: Int,
    val date: String
) {
    constructor(name: String, assigned_to_id: Long?, room_id: Long, completed: Int, date: String) : 
        this(0, name, assigned_to_id, room_id, completed, date)
}

