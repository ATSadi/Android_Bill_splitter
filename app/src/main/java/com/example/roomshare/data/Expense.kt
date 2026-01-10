package com.example.roomshare.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Roommate::class,
            parentColumns = ["id"],
            childColumns = ["payer_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["room_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["room_id"], name = "idx_expense_room"),
        Index(value = ["payer_id"])
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val amount: Double,
    @ColumnInfo(name = "payer_id")
    val payer_id: Long,
    @ColumnInfo(name = "room_id")
    val room_id: Long,
    @ColumnInfo(name = "split_count")
    val split_count: Int,
    val date: String
) {
    constructor(name: String, amount: Double, payer_id: Long, room_id: Long, split_count: Int, date: String) : 
        this(0, name, amount, payer_id, room_id, split_count, date)
}

