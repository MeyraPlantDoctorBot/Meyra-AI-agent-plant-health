package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MeyraDao {
    // Farmer Profile
    @Query("SELECT * FROM farmer_profiles WHERE id = :id LIMIT 1")
    fun getFarmerProfile(id: String = "default_farmer"): Flow<FarmerProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFarmerProfile(profile: FarmerProfileEntity)

    // Crop Health Records
    @Query("SELECT * FROM crop_health_records ORDER BY dateLogged DESC")
    fun getAllCropHealthRecords(): Flow<List<CropHealthRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCropHealthRecord(record: CropHealthRecordEntity): Long

    @Query("UPDATE crop_health_records SET isResolved = :isResolved WHERE id = :id")
    suspend fun updateRecordStatus(id: Long, isResolved: Boolean)

    @Query("DELETE FROM crop_health_records WHERE id = :id")
    suspend fun deleteCropHealthRecord(id: Long)

    // Post Harvest Records
    @Query("SELECT * FROM post_harvest_records ORDER BY dateRecorded DESC")
    fun getAllPostHarvestRecords(): Flow<List<PostHarvestRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPostHarvestRecord(record: PostHarvestRecordEntity): Long

    // Treatment History
    @Query("SELECT * FROM treatment_history ORDER BY appliedDate DESC")
    fun getAllTreatmentHistory(): Flow<List<TreatmentHistoryEntity>>

    @Query("SELECT * FROM treatment_history WHERE cropRecordId = :cropRecordId ORDER BY appliedDate DESC")
    fun getTreatmentsForCropRecord(cropRecordId: Long): Flow<List<TreatmentHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTreatmentHistory(treatment: TreatmentHistoryEntity): Long
}
