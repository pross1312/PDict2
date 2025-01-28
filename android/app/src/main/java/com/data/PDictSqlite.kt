package com.data;

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.OPEN_READWRITE
import android.database.sqlite.SQLiteDatabase.OpenParams
import android.database.Cursor
import android.content.ContentValues
import android.util.Log
import com.data.PDictContract.SchemaEntry
import com.data.PDictContract.SchemaUsage
import com.data.PDictContract.SchemaDefinition
import com.data.PDictContract.SchemaGroupEntry
import java.io.Closeable
import java.io.File
import java.io.InputStream
import kotlin.io.path.Path

class PDictSqlite private constructor() : Closeable {
    var _db: SQLiteDatabase? = null;
    val db get() = _db!!;
    var dir: String = ""
    val isOpen: Boolean get() = _db?.isOpen ?: false

    init {
        Log.i(TAG, PDictContract.CREATE_QUERY)
    }

    fun newDatabase(name: String): PDictSqlite {
        Log.i(TAG, "Create new database $name");
        val path = Path(dir, name).toString();
        val file = File(path);
        if (!file.exists()) {
            file.createNewFile();
            _db?.close();
            _db = SQLiteDatabase.openDatabase(file, OpenParams.Builder().setOpenFlags(OPEN_READWRITE).build());
            db.execSQL(PDictContract.CREATE_QUERY);
            Log.i(TAG, PDictContract.CREATE_QUERY);
            return this;
        } else {
            throw Exception("Database exist at $path");
        }
    }

    fun importDatabase(input: InputStream, name: String): Boolean {
        Log.i(TAG, "Import database $name");
        val path = Path(dir, name).toString();
        val file = File(path);
        if (!file.exists()) {
            file.createNewFile();
            val buffer = ByteArray(256);
            file.outputStream().use { out ->
                var readCount = 0;
                try {
                    do {
                        readCount = input.read(buffer);
                        if (readCount != -1) {
                            out.write(buffer.sliceArray(0..<readCount))
                        }
                    } while (readCount != -1);
                } catch (ex: Exception) {
                    Log.i(TAG, ex.toString());
                }
            }
            // file.inputStream().readAllBytes().also {
            //     Log.i(TAG, it.toString(Charsets.UTF_8));
            // }
            return tryOpen(file);
        } else {
            throw Exception("Database exist at $path");
        }
    }

    fun openDatabase(name: String, createIfNotExist: Boolean): Boolean {
        Log.i(TAG, "Switch to database $name");
        val path = Path(dir, name).toString();
        val file = File(path);
        return if (!file.exists()) {
            if (!createIfNotExist) {
                false;
            } else try {
                newDatabase(name);
                true;
            } catch (ex: Exception) {
                Log.i(TAG, ex.toString());
                false;
            }
        } else {
            tryOpen(file);
        }
    }

    fun tryOpen(file: File): Boolean {
        try {
            _db?.close();
            _db = SQLiteDatabase.openDatabase(file, OpenParams.Builder().setOpenFlags(OPEN_READWRITE).build());
            Log.i(TAG, "Database opened");
        } catch (ex: Exception) {
            Log.i(TAG, ex.toString())
            return false;
        }
        return true;
    }

    // fun list(page: Int = 0, limit: Int = 10, includes_group: List<String> = emptyList(), excludes_group: List<String> = emptyList()): List<String> {
    //     return listOf()
    // }

    private fun parseEntryTableRow(entry: Entry, cursor: Cursor) {
        entry.id = cursor.getLong(cursor.getColumnIndexOrThrow(SchemaEntry._ID));
        entry.keyword = cursor.getString(cursor.getColumnIndexOrThrow(SchemaEntry.COLUMN_KEYWORD_NAME));
        entry.pronounciation = cursor.getString(cursor.getColumnIndexOrThrow(SchemaEntry.COLUMN_PRONOUNCIATION_NAME));
        entry.last_read = cursor.getLong(cursor.getColumnIndexOrThrow(SchemaEntry.COLUMN_LAST_READ_NAME));
    }

    private fun fetchGroups(entry: Entry, id: Long) {
        db.query(
            SchemaGroupEntry.TABLE_NAME,
            null,
            "${SchemaGroupEntry.COLUMN_ENTRY_ID_NAME} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        ).use {
            entry.groups = List<String>(it.count) { _ ->
                it.moveToNext()
                return@List it.getString(it.getColumnIndexOrThrow(SchemaGroupEntry.COLUMN_GROUP_NAME))
            }
        }
    }

    private fun fetchDefinitions(entry: Entry, id: Long) {
        db.query(
            SchemaDefinition.TABLE_NAME,
            null,
            "${SchemaDefinition.COLUMN_ENTRY_ID_NAME} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        ).use {
            entry.definitions = List<String>(it.count) { _ ->
                it.moveToNext()
                return@List it.getString(it.getColumnIndexOrThrow(SchemaDefinition.COLUMN_DEFINITION_NAME))
            }
        }
    }

    private fun fetchUsages(entry: Entry, id: Long) {
        db.query(
            SchemaUsage.TABLE_NAME,
            null,
            "${SchemaUsage.COLUMN_ENTRY_ID_NAME} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        ).use {
            entry.usages = List<String>(it.count) { _ ->
                it.moveToNext()
                return@List it.getString(it.getColumnIndexOrThrow(SchemaUsage.COLUMN_USAGE_NAME))
            }
        }
    }

    private fun updateLastRead(id: Long) {
        val rowsUpdated = db.update(
            SchemaEntry.TABLE_NAME,
            ContentValues().apply {
                put(SchemaEntry.COLUMN_LAST_READ_NAME, (System.currentTimeMillis() / 1000).toString())
            },
            "${SchemaEntry._ID} = ?",
            arrayOf(id.toString())
        )
        Log.i(TAG, "Updated $rowsUpdated rows, set new last_read for entry $id")
    }

    fun query(keyword: String): Entry? {
        var result = Entry()
        db.query(
            SchemaEntry.TABLE_NAME,
            null,
            "${SchemaEntry.COLUMN_KEYWORD_NAME} = ?",
            arrayOf(keyword),
            null,
            null,
            null
        ).use {
            if (it.count > 1) throw Exception("Find 2 entry with the same keyword")
            if (it.count == 0) return null
            it.moveToFirst()
            parseEntryTableRow(result, it)
        }
        fetchGroups(result, result.id)
        fetchDefinitions(result, result.id)
        fetchUsages(result, result.id)
        Log.i(TAG, "Query for $keyword, result: $result")
        return result;
    }

    fun nextword(): Entry? {
        val result = Entry()
        db.query(
            SchemaEntry.TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            "${SchemaEntry.COLUMN_LAST_READ_NAME} ASC",
            "1"
        ).use {
            if (it.count > 1) throw Exception("Not possible since limit is 1")
            if (it.count == 0) return null
            it.moveToFirst()
            parseEntryTableRow(result, it)
        }
        fetchGroups(result, result.id)
        fetchDefinitions(result, result.id)
        fetchUsages(result, result.id)
        updateLastRead(result.id)

        Log.i(TAG, "Next learn word $result")
        return result
    }

    // select distinct g1.group_name, g2.id from entry_group g1 left join entry_group g2 on g1.group_name = g2.group_name and g2.entry_id = '751';
    fun findGroups(id: Long): List<EditGroupEntry> {
        return db.rawQuery("""
            SELECT distinct g1.${SchemaGroupEntry.COLUMN_GROUP_NAME} as ${SchemaGroupEntry.COLUMN_GROUP_NAME},
                            g2.${SchemaGroupEntry._ID} AS ${SchemaGroupEntry._ID}
            FROM entry_group g1
            LEFT JOIN entry_group g2 ON g1.${SchemaGroupEntry.COLUMN_GROUP_NAME} = g2.${SchemaGroupEntry.COLUMN_GROUP_NAME}
                                    AND g2.${SchemaGroupEntry.COLUMN_ENTRY_ID_NAME} = ?
            ORDER BY g2.${SchemaGroupEntry._ID} DESC, g1.${SchemaGroupEntry.COLUMN_GROUP_NAME} ASC
            """,
            arrayOf(id.toString())
        ).use {
            List<EditGroupEntry>(it.count) { _ ->
                it.moveToNext()
                return@List EditGroupEntry(it.getString(it.getColumnIndexOrThrow(SchemaGroupEntry.COLUMN_GROUP_NAME)),
                                           !it.isNull(it.getColumnIndexOrThrow(SchemaGroupEntry._ID)))
            }
        }
    }

    fun addGroup(id: Long, group: String): Boolean {
        val newId = db.insert(
            SchemaGroupEntry.TABLE_NAME,
            null,
            ContentValues().apply {
                put(SchemaGroupEntry.COLUMN_ENTRY_ID_NAME, id.toString())
                put(SchemaGroupEntry.COLUMN_GROUP_NAME, group.lowercase())
            },
        )
        Log.i(TAG, "Add new group $group to $id")
        return newId != -1L
    }

    fun removeGroup(id: Long, group: String): Int {
        val rowsDeleted = SchemaGroupEntry.let {
            db.delete(
                it.TABLE_NAME,
                "${it.COLUMN_ENTRY_ID_NAME} = ? AND ${it.COLUMN_GROUP_NAME} = ?",
                arrayOf(id.toString(), group.lowercase())
            )
        }
        Log.i(TAG, "Delete $rowsDeleted which has $id $group")
        return rowsDeleted
    }

    override fun close() {
        _db?.close();
    }

    companion object {
        val TAG = "PDict:PDictSqlite"
        val instance: PDictSqlite = PDictSqlite()
    }
}
