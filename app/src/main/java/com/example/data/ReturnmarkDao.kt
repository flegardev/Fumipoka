package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReturnmarkDao {

    // --- Projects ---
    @Query("SELECT * FROM projects ORDER BY isPinned DESC, lastOpenedAt DESC")
    fun observeAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects ORDER BY isPinned DESC, lastOpenedAt DESC")
    suspend fun getAllProjectsSnapshot(): List<ProjectEntity>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Long): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProjectById(projectId: Long)

    @Query("DELETE FROM projects WHERE isSampleData = 1")
    suspend fun deleteSampleProjects(): Int

    // --- Save Points ---
    @Query("SELECT * FROM save_points ORDER BY isPinned DESC, timestamp DESC")
    fun observeAllSavePoints(): Flow<List<SavePointEntity>>

    @Query("SELECT * FROM save_points ORDER BY timestamp DESC")
    suspend fun getAllSavePointsSnapshot(): List<SavePointEntity>

    @Query("SELECT * FROM save_points WHERE projectId = :projectId ORDER BY isPinned DESC, timestamp DESC")
    fun observeSavePointsForProject(projectId: Long): Flow<List<SavePointEntity>>

    @Query("SELECT * FROM save_points WHERE id = :id LIMIT 1")
    suspend fun getSavePointById(id: Long): SavePointEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavePoint(savePoint: SavePointEntity): Long

    @Update
    suspend fun updateSavePoint(savePoint: SavePointEntity)

    @Query("DELETE FROM save_points WHERE id = :id")
    suspend fun deleteSavePointById(id: Long)

    // --- Missions ---
    @Query("SELECT * FROM missions ORDER BY manualOrder ASC, createdAt DESC")
    fun observeAllMissions(): Flow<List<MissionEntity>>

    @Query("SELECT * FROM missions ORDER BY manualOrder ASC, createdAt DESC")
    suspend fun getAllMissionsSnapshot(): List<MissionEntity>

    @Query("SELECT * FROM missions WHERE id = :id LIMIT 1")
    suspend fun getMissionById(id: Long): MissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: MissionEntity): Long

    @Update
    suspend fun updateMission(mission: MissionEntity)

    @Query("DELETE FROM missions WHERE id = :id")
    suspend fun deleteMissionById(id: Long)

    // --- References ---
    @Query("SELECT * FROM reference_items ORDER BY isFavorite DESC, lastOpenedAt DESC")
    fun observeAllReferences(): Flow<List<ReferenceItemEntity>>

    @Query("SELECT * FROM reference_items ORDER BY createdAt DESC")
    suspend fun getAllReferencesSnapshot(): List<ReferenceItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReference(reference: ReferenceItemEntity): Long

    @Update
    suspend fun updateReference(reference: ReferenceItemEntity)

    @Query("DELETE FROM reference_items WHERE id = :id")
    suspend fun deleteReferenceById(id: Long)

    // --- Focus Sessions ---
    @Query("SELECT * FROM focus_sessions ORDER BY startedAt DESC")
    fun observeAllFocusSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions ORDER BY startedAt DESC")
    suspend fun getAllFocusSessionsSnapshot(): List<FocusSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSessionEntity): Long

    @Update
    suspend fun updateFocusSession(session: FocusSessionEntity)

    // --- Rescue Sessions ---
    @Query("SELECT * FROM rescue_sessions ORDER BY timestamp DESC")
    fun observeAllRescueSessions(): Flow<List<RescueSessionEntity>>

    @Query("SELECT * FROM rescue_sessions ORDER BY timestamp DESC")
    suspend fun getAllRescueSessionsSnapshot(): List<RescueSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRescueSession(session: RescueSessionEntity): Long

    // --- Atomic Transactions ---
    /**
     * Appends a new immutable SavePointEntity without overwriting earlier save points,
     * and atomically updates the parent ProjectEntity's current working context.
     */
    @Transaction
    suspend fun createCheckpointAndSyncProject(savePoint: SavePointEntity): Long {
        val checkpointId = insertSavePoint(savePoint)
        val project = getProjectById(savePoint.projectId)
        if (project != null) {
            updateProject(
                project.copy(
                    currentContextSummary = savePoint.happeningNow.ifBlank { savePoint.title },
                    lastCompletedAction = savePoint.completedSummary.ifBlank { project.lastCompletedAction },
                    nextManualStep = savePoint.nextAction.ifBlank { project.nextManualStep },
                    openBlockers = savePoint.blockers,
                    updatedAt = savePoint.timestamp,
                    lastOpenedAt = savePoint.timestamp
                )
            )
        }
        return checkpointId
    }

    /**
     * Restores an earlier SavePoint working context into the Project's active resume fields
     * while preserving all existing Save Points in the timeline.
     */
    @Transaction
    suspend fun restoreCheckpointContext(savePointId: Long): Boolean {
        val target = getSavePointById(savePointId) ?: return false
        val project = getProjectById(target.projectId) ?: return false
        val now = System.currentTimeMillis()
        updateProject(
            project.copy(
                currentContextSummary = target.happeningNow.ifBlank { target.title },
                lastCompletedAction = target.completedSummary,
                nextManualStep = target.nextAction,
                openBlockers = target.blockers,
                updatedAt = now,
                lastOpenedAt = now
            )
        )
        return true
    }

    // --- Full Wipe & Replace for Validated Restore ---
    @Query("DELETE FROM rescue_sessions")
    suspend fun clearAllRescueSessions()

    @Query("DELETE FROM focus_sessions")
    suspend fun clearAllFocusSessions()

    @Query("DELETE FROM reference_items")
    suspend fun clearAllReferences()

    @Query("DELETE FROM missions")
    suspend fun clearAllMissions()

    @Query("DELETE FROM save_points")
    suspend fun clearAllSavePoints()

    @Query("DELETE FROM projects")
    suspend fun clearAllProjects()

    @Transaction
    suspend fun replaceAllDataTransactionally(
        projects: List<ProjectEntity>,
        savePoints: List<SavePointEntity>,
        missions: List<MissionEntity>,
        references: List<ReferenceItemEntity>,
        focusSessions: List<FocusSessionEntity>,
        rescueSessions: List<RescueSessionEntity>
    ) {
        clearAllRescueSessions()
        clearAllFocusSessions()
        clearAllReferences()
        clearAllMissions()
        clearAllSavePoints()
        clearAllProjects()

        projects.forEach { insertProject(it) }
        savePoints.forEach { insertSavePoint(it) }
        missions.forEach { insertMission(it) }
        references.forEach { insertReference(it) }
        focusSessions.forEach { insertFocusSession(it) }
        rescueSessions.forEach { insertRescueSession(it) }
    }
}
