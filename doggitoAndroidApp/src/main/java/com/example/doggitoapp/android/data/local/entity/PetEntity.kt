package com.example.doggitoapp.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.doggitoapp.android.domain.model.Pet

@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val breed: String,
    val birthDate: Long,
    val weight: Float,
    val photoUri: String? = null,
    val synced: Boolean = false
) {
    fun toDomain() = Pet(
        id = id,
        userId = userId,
        name = name,
        breed = breed,
        birthDate = birthDate,
        weight = weight,
        photoUri = photoUri,
        synced = synced
    )

    companion object {
        fun fromDomain(pet: Pet) = PetEntity(
            id = pet.id,
            userId = pet.userId,
            name = pet.name,
            breed = pet.breed,
            birthDate = pet.birthDate,
            weight = pet.weight,
            photoUri = pet.photoUri,
            synced = pet.synced
        )
    }
}
