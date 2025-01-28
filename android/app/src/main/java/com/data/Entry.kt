package com.data

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class Entry(
    var id: Long = 0,
    var keyword: String = "",
    var pronounciation: String = "",
    var definitions: List<String> = emptyList(),
    var usages: List<String> = emptyList(),
    var groups: List<String> = emptyList(),
    var last_read: Long = 0,
) {
    override fun toString(): String = """{
    id = $id,
    keyword = $keyword,
    pronounciation = $pronounciation,
    definitions = [${definitions.joinToString(" | ")}],
    usages = [${usages.joinToString(" | ")}],
    groups = [${groups.joinToString(" | ")}],
    last_read = ${DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault()).format(Instant.ofEpochSecond(last_read))},
}"""
};
