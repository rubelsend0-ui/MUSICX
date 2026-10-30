package com.soniqxpro.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatTest {
    @Test fun durationUnderHour() {
        assertEquals("0:00", formatDuration(0))
        assertEquals("1:05", formatDuration(65_000))
        assertEquals("59:59", formatDuration(3_599_000))
    }

    @Test fun durationWithHours() {
        assertEquals("1:02:05", formatDuration(3_725_000))
    }

    @Test fun negativeDurationIsZero() {
        assertEquals("0:00", formatDuration(-5000))
    }

    @Test fun bytes() {
        assertEquals("512 B", formatBytes(512))
        assertEquals("1.5 KB", formatBytes(1536))
        assertEquals("1.0 MB", formatBytes(1024L * 1024))
    }

    @Test fun songCount() {
        assertEquals("1 song", songCountLabel(1))
        assertEquals("0 songs", songCountLabel(0))
        assertEquals("12 songs", songCountLabel(12))
    }
}
