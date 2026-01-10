package com.example.roomshare.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "bills",
    foreignKeys = [ForeignKey(
        entity = RoomEntity::class,
        parentColumns = ["id"],
        childColumns = ["room_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["room_id"])]
)
data class Bill(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val amount: Double,
    @ColumnInfo(name = "room_id")
    val room_id: Long
) {
    constructor(name: String, amount: Double, room_id: Long) : 
        this(0, name, amount, room_id)
}

