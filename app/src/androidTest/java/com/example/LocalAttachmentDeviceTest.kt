package com.example

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.data.LocalAttachments
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalAttachmentDeviceTest {
    @Test fun restoredFileIsReadableThroughTheAppProvider() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val original = "fumipoka-attachment:124e4567-e89b-12d3-a456-426614174000.pdf"
        val bytes = "%PDF-1.4\nDevice fixture\n".toByteArray()
        val token = LocalAttachments.restoreFromBackup(context, original, bytes)
        try {
            val uri = checkNotNull(LocalAttachments.openUri(context, token))
            assertEquals("${context.packageName}.attachments", uri.authority)
            assertEquals("application/pdf", context.contentResolver.getType(uri))
            val actual = checkNotNull(context.contentResolver.openInputStream(uri)).use { it.readBytes() }
            assertArrayEquals(bytes, actual)
        } finally {
            LocalAttachments.delete(context, token)
        }
    }
}
