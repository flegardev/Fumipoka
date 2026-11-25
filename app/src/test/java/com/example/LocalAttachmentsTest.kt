package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.BackupValidationResult
import com.example.data.BackupVaultManager
import com.example.data.LocalAttachments
import com.example.data.ReferenceItemEntity
import com.example.data.ReferenceType
import com.example.data.UserPreferencesState
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LocalAttachmentsTest {
    private val token = "fumipoka-attachment:124e4567-e89b-12d3-a456-426614174000.pdf"
    private val bytes = "%PDF-1.4\nSynthetic fixture\n".toByteArray()

    @Test fun importChecksBothStreamLimitAndSignature() {
        assertTrue(LocalAttachments.matchesMime("application/pdf", bytes))
        assertFalse(LocalAttachments.matchesMime("image/png", bytes))
        assertFalse(LocalAttachments.matchesMime("application/octet-stream", bytes))
        val output = ByteArrayOutputStream()
        LocalAttachments.copyBounded(ByteArrayInputStream(bytes), output, bytes.size.toLong())
        assertArrayEquals(bytes, output.toByteArray())
        assertThrows(IllegalArgumentException::class.java) {
            LocalAttachments.copyBounded(ByteArrayInputStream(bytes), ByteArrayOutputStream(), 4L)
        }
    }

    @Test fun portableBackupRestoresAttachmentUnderNewIdentity() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val reference = ReferenceItemEntity(id = 7, title = "Test PDF", type = ReferenceType.DOCUMENT.name, contentOrUri = token)
        fun export(attachments: Map<String, ByteArray>) = BackupVaultManager.exportFullBackupJson(
            emptyList(), emptyList(), emptyList(), listOf(reference), emptyList(),
            emptyList(), UserPreferencesState(), attachments
        )
        assertTrue(BackupVaultManager.validateBackupJson(export(emptyMap())) is BackupValidationResult.Invalid)
        val backup = BackupVaultManager.validateBackupJson(export(mapOf(token to bytes))) as BackupValidationResult.Valid
        val restored = LocalAttachments.restoreFromBackup(context, token, backup.payload.attachments.getValue(token))
        try {
            assertNotEquals(token, restored)
            assertArrayEquals(bytes, LocalAttachments.readForBackup(context, restored))
            val provider = checkNotNull(context.packageManager.resolveContentProvider(
                "${context.packageName}.attachments", android.content.pm.PackageManager.GET_META_DATA
            ))
            assertFalse(provider.exported)
            assertTrue(provider.grantUriPermissions)
            assertNull(LocalAttachments.openUri(context, "fumipoka-attachment:../../secret.pdf"))
        } finally {
            LocalAttachments.delete(context, restored)
        }
    }
}
