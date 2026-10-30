package com.soniqxpro.util

import com.soniqxpro.database.SongEntity
import com.soniqxpro.model.SortOrder

fun List<SongEntity>.sortedByOrder(order: SortOrder): List<SongEntity> = when (order) {
    SortOrder.TITLE -> sortedBy { it.title.lowercase() }
    SortOrder.ARTIST -> sortedWith(compareBy({ it.artist.lowercase() }, { it.title.lowercase() }))
    SortOrder.RECENTLY_ADDED -> sortedByDescending { it.dateAdded }
}

fun SongEntity.matchesQuery(query: String): Boolean =
    title.contains(query, ignoreCase = true) ||
        artist.contains(query, ignoreCase = true) ||
        album.contains(query, ignoreCase = true)

enum class GroupType { ALBUM, ARTIST, FOLDER }

data class SongGroup(
    val id: String,
    val title: String,
    val subtitle: String,
    val artworkUri: String?,
    val songs: List<SongEntity>
)

private fun keyOf(song: SongEntity, type: GroupType): String = when (type) {
    GroupType.ALBUM -> song.album
    GroupType.ARTIST -> song.artist
    GroupType.FOLDER -> song.folder
}

fun List<SongEntity>.groupInto(type: GroupType): List<SongGroup> =
    groupBy { keyOf(it, type) }
        .map { (key, list) ->
            SongGroup(
                id = "${type.name}:$key",
                title = if (type == GroupType.FOLDER) key.substringAfterLast('/').ifBlank { key } else key,
                subtitle = when (type) {
                    GroupType.ALBUM -> "${list.first().artist} · ${songCountLabel(list.size)}"
                    GroupType.ARTIST -> songCountLabel(list.size)
                    GroupType.FOLDER -> key
                },
                artworkUri = list.firstNotNullOfOrNull { it.artworkUri },
                songs = list
            )
        }
        .sortedBy { it.title.lowercase() }
