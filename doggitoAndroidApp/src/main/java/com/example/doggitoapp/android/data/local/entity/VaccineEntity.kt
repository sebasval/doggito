package com.example.doggitoapp.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.doggitoapp.android.domain.model.VaccineRecord

@Entity(tableName = "vaccine_records")
data class VaccineEntity(
    @PrimaryKey val id: String,
    val petId: String,
    val vaccineName: String,
    val dateAdministered: Long,
    val nextDueDate: Long? = null,
    val vetName: String? = null,
    val notes: String? = null,
    val synced: Boolean = false
) {
    fun toDomain() = VaccineRecord(
        id = id,
        petId = petId,
        vaccineName = vaccineName,
        dateAdministered = dateAdministered,
        nextDueDate = nextDueDate,
        vetName = vetName,
        notes = notes,
        synced = synced
    )

    companion object {
        fun fromDomain(record: VaccineRecord) = VaccineEntity(
            id = record.id,
            petId = record.petId,
            vaccineName = record.vaccineName,
            dateAdministered = record.dateAdministered,
            nextDueDate = record.nextDueDate,
            vetName = record.vetName,
            notes = record.notes,
            synced = record.synced
        )
    }
}
