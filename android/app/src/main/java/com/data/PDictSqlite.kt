package com.data;

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.OPEN_READWRITE
import android.database.sqlite.SQLiteDatabase.OpenParams
import android.util.Log
import java.io.Closeable
import java.io.File
import java.io.InputStream
import kotlin.io.path.Path

class PDictSqlite(private val dir: String): Closeable {
    var _db: SQLiteDatabase? = null;
    val db get() = _db!!;

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
                throw Exception("Database does not exist at $path");
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
        } catch (ex: Exception) {
            Log.i(TAG, ex.toString())
            return false;
        }
        return true;
    }

    override fun close() {
        _db?.close();
    }

    companion object {
        val TAG = "PDict:PDictSqlite"
    }
}