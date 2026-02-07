package com.example.doggitoapp.android.domain.model

data class VaccineRecord(
    val id: String,
    val petId: String,
    val vaccineName: String,
    val dateAdministered: Long,
    val nextDueDate: Long? = null,
    val vetName: String? = null,
    val notes: String? = null,
    val synced: Boolean = false
)
