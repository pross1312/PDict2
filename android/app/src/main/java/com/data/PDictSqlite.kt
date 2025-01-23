package com.data;

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.OPEN_READWRITE
import android.database.sqlite.SQLiteDatabase.OpenParams
import android.util.Log
import com.viewmodel.Entry
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

    fun list(page: Int = 0, limit: Int = 10, includes_group: List<String> = emptyList(), excludes_group: List<String> = emptyList()): List<String> {
        return listOf()
    }

    fun query(keyword: String): Entry? {
        val query = "SELECT * FROM ${SchemaEntry.TABLE_NAME} WHERE ${SchemaEntry.COLUMN_KEYWORD_NAME} = ?0";
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
            result.id = it.getInt(it.getColumnIndexOrThrow(SchemaEntry._ID));
            result.keyword = it.getString(it.getColumnIndexOrThrow(SchemaEntry.COLUMN_KEYWORD_NAME));
            result.pronounciation = it.getString(it.getColumnIndexOrThrow(SchemaEntry.COLUMN_PRONOUNCIATION_NAME));
            result.last_read = it.getInt(it.getColumnIndexOrThrow(SchemaEntry.COLUMN_LAST_READ_NAME));
        }
        db.query(
            SchemaDefinition.TABLE_NAME,
            null,
            "${SchemaDefinition.COLUMN_ENTRY_ID_NAME} = ?",
            arrayOf(result.id.toString()),
            null,
            null,
            null
        ).use {
            result.definitions = List<String>(it.count) { _ ->
                it.moveToNext()
                return@List it.getString(it.getColumnIndexOrThrow(SchemaDefinition.COLUMN_DEFINITION_NAME))
            }
        }
        db.query(
            SchemaUsage.TABLE_NAME,
            null,
            "${SchemaUsage.COLUMN_ENTRY_ID_NAME} = ?",
            arrayOf(result.id.toString()),
            null,
            null,
            null
        ).use {
            result.usages = List<String>(it.count) { _ ->
                it.moveToNext()
                return@List it.getString(it.getColumnIndexOrThrow(SchemaUsage.COLUMN_USAGE_NAME))
            }
        }
        db.query(
            SchemaGroupEntry.TABLE_NAME,
            null,
            "${SchemaGroupEntry.COLUMN_ENTRY_ID_NAME} = ?",
            arrayOf(result.id.toString()),
            null,
            null,
            null
        ).use {
            result.groups = List<String>(it.count) { _ ->
                it.moveToNext()
                return@List it.getString(it.getColumnIndexOrThrow(SchemaGroupEntry.COLUMN_GROUP_NAME))
            }
        }
        Log.i(TAG, "Query for $keyword, result: $result")
        return result;
    }

    override fun close() {
        _db?.close();
    }

    companion object {
        val TAG = "PDict:PDictSqlite"
        val instance: PDictSqlite = PDictSqlite()
    }
}