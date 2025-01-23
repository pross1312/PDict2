package com.data;

object PDictContract {
    object SchemaEntry {
        const val _ID = "id";
        const val TABLE_NAME = "entry"
        const val COLUMN_KEYWORD_NAME = "keyword"
        const val COLUMN_PRONOUNCIATION_NAME = "pronounciation"
        const val COLUMN_LAST_READ_NAME = "last_read"
        const val CREATE_QUERY =
            "CREATE TABLE ${this.TABLE_NAME} (" +
                "${this._ID} INTEGER PRIMARY KEY," +
                "${this.COLUMN_KEYWORD_NAME} TEXT UNIQUE NOT NULL," +
                "${this.COLUMN_PRONOUNCIATION_NAME} TEXT NOT NULL," +
                "${this.COLUMN_LAST_READ_NAME} INTEGER NOT NULL" +
            ");";
    }
    object SchemaGroupEntry {
        const val _ID = "id";
        const val TABLE_NAME = "entry_group"
        const val COLUMN_ENTRY_ID_NAME = "entry_id"
        const val COLUMN_GROUP_NAME = "group_name"
        const val CREATE_QUERY =
            "CREATE TABLE ${this.TABLE_NAME} (" +
                "${this._ID} INTEGER PRIMARY KEY," +
                "${this.COLUMN_ENTRY_ID_NAME} INTEGER NOT NULL REFERENCES ${SchemaEntry.TABLE_NAME}(${SchemaEntry._ID})," +
                "${this.COLUMN_GROUP_NAME} TEXT NOT NULL" +
            ");";
    }
    object SchemaDefinition {
        const val _ID = "id";
        const val TABLE_NAME = "definition";
        const val COLUMN_ENTRY_ID_NAME = "entry_id";
        const val COLUMN_DEFINITION_NAME = "definition";
        const val CREATE_QUERY =
            "CREATE TABLE ${this.TABLE_NAME} (" +
                "${this._ID} INTEGER PRIMARY KEY," +
                "${this.COLUMN_ENTRY_ID_NAME} INTEGER NOT NULL REFERENCES ${SchemaEntry.TABLE_NAME}(${SchemaEntry._ID})," +
                "${this.COLUMN_DEFINITION_NAME} TEXT NOT NULL" +
            ");";
    }
    object SchemaUsage {
        const val _ID = "id";
        const val TABLE_NAME = "usage"
        const val COLUMN_ENTRY_ID_NAME = "entry_id"
        const val COLUMN_USAGE_NAME = "usage"
        const val CREATE_QUERY =
            "CREATE TABLE ${this.TABLE_NAME} (" +
                "${this._ID} INTEGER PRIMARY KEY," +
                "${this.COLUMN_ENTRY_ID_NAME} INTEGER NOT NULL REFERENCES ${SchemaEntry.TABLE_NAME}(${SchemaEntry._ID})," +
                "${this.COLUMN_USAGE_NAME} TEXT NOT NULL" +
            ");";
    }

    const val CREATE_QUERY =
        "${SchemaEntry.CREATE_QUERY}\n${SchemaGroupEntry.CREATE_QUERY}\n${SchemaDefinition.CREATE_QUERY}\n${SchemaUsage.CREATE_QUERY}"
}