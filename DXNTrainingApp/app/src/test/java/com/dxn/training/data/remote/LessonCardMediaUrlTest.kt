package com.dxn.training.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LessonCardMediaUrlTest {

    @Test
    fun mediaUrlIsMappedFromResponseToDomain() {
        val response = LessonCardResponse(
            id = "c1",
            title = "درس",
            body = "نص الدرس",
            mediaUrl = "https://example.org/content-images/c1.jpg"
        )

        assertEquals("https://example.org/content-images/c1.jpg", response.toDomain().mediaUrl)
    }

    @Test
    fun mediaUrlDefaultsToNullWhenAbsent() {
        val response = LessonCardResponse(id = "c2", title = "درس", body = "نص الدرس")

        assertNull(response.mediaUrl)
        assertNull(response.toDomain().mediaUrl)
    }
}
