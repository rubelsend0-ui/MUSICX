package com.soniqxpro.util

import com.soniqxpro.database.SongEntity
import com.soniqxpro.model.SortOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SongOpsTest {
    private fun song(
        id: String,
        title: String = "T$id",
        artist: String = "Artist",
        album: String = "Album",
        added: Long = 0,
        folder: String = "/music"
    ) = SongEntity(id, title, artist, album, 1000L, "content://x/$id", null, added, folder)

    @Test fun sortByTitleIgnoresCase() {
        val sorted = listOf(song("1", "banana"), song("2", "Apple")).sortedByOrder(SortOrder.TITLE)
        assertEquals(listOf("2", "1"), sorted.map { it.id })
    }

    @Test fun sortByArtistThenTitle() {
        val sorted = listOf(
            song("1", "z", artist = "B"),
            song("2", "a", artist = "B"),
            song("3", "m", artist = "A")
        ).sortedByOrder(SortOrder.ARTIST)
        assertEquals(listOf("3", "2", "1"), sorted.map { it.id })
    }

    @Test fun sortRecentlyAddedNewestFirst() {
        val sorted = listOf(song("1", added = 10), song("2", added = 30), song("3", added = 20))
            .sortedByOrder(SortOrder.RECENTLY_ADDED)
        assertEquals(listOf("2", "3", "1"), sorted.map { it.id })
    }

    @Test fun queryMatchesTitleArtistAlbum() {
        val s = song("1", title = "Moonlight", artist = "Sonata", album = "Night")
        assertTrue(s.matchesQuery("moon"))
        assertTrue(s.matchesQuery("SONA"))
        assertTrue(s.matchesQuery("night"))
        assertFalse(s.matchesQuery("zzz"))
    }

    @Test fun groupByAlbum() {
        val groups = listOf(
            song("1", album = "B"), song("2", album = "A"), song("3", album = "B")
        ).groupInto(GroupType.ALBUM)
        assertEquals(listOf("A", "B"), groups.map { it.title })
        assertEquals(2, groups.first { it.title == "B" }.songs.size)
    }

    @Test fun groupByFolderUsesLastSegmentAsTitle() {
        val groups = listOf(song("1", folder = "/storage/emulated/0/Music/Rock")).groupInto(GroupType.FOLDER)
        assertEquals("Rock", groups.single().title)
    }
}
