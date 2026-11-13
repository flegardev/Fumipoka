package com.example

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ReleaseStorageStressTest {
    @Test fun twoThousandCheckpointsRetainProjectLinksAcrossReopenAndRollback() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "release-stress-${System.nanoTime()}.db"
        fun open() = Room.databaseBuilder(context, ReturnmarkDatabase::class.java, name).build()
        var db = open()
        try {
            val projectId = db.dao().insertProject(ProjectEntity(name = "Fictional stress fixture"))
            db.withTransaction {
                repeat(2000) { db.dao().insertSavePoint(SavePointEntity(projectId = projectId,
                    title = "Fixture $it", nextAction = "Continue $it")) }
            }
            db.close(); db = open()
            val before = db.dao().getAllSavePointsSnapshot()
            assertEquals(2000, before.size)
            assertTrue(before.all { it.projectId == projectId })
            try {
                db.withTransaction {
                    db.dao().insertSavePoint(SavePointEntity(projectId = projectId, title = "Rolled back"))
                    throw IllegalStateException("deliberate fixture rollback")
                }
                fail("Transaction must propagate the fixture failure")
            } catch (expected: IllegalStateException) {
                assertEquals("deliberate fixture rollback", expected.message)
            }
            assertEquals(before, db.dao().getAllSavePointsSnapshot())
            db.dao().deleteProjectById(projectId)
            assertTrue(db.dao().getAllSavePointsSnapshot().isEmpty())
        } finally { db.close(); context.deleteDatabase(name) }
    }
}
