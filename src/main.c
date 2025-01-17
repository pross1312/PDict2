#include <stdio.h>
#include <raylib.h>
#include <sqlite3.h>
#include <android/asset_manager_jni.h>
#include <android_native_app_glue.h>

#define NOB_IMPLEMENTATION
#include "nob.h"
#include "helper/jni_helper.h"

#define UNREACHABLE() assert(0 && "Unreachable")

typedef struct {
    int col;
    int type;
    const char *col_name;
} ColumnMetaData;

const char *sqlite3_type_str(int type) {
    switch (type) {
        case SQLITE_INTEGER: return "INTEGER";
        case SQLITE3_TEXT: return "TEXT";
        case SQLITE_BLOB: return "BLOB";
        case SQLITE_FLOAT: return "FLOAT";
        default: UNREACHABLE();
    }
    return NULL;
}

// return null on success
const char *check_table_schema(sqlite3_stmt *stmt, const ColumnMetaData *cols_info, int col_count) {
    int count = sqlite3_column_count(stmt);
    if (count != col_count) {
        return nob_temp_sprintf("Error: Expect %d columns but got %d columns.", col_count, count);
    }
    for (int i = 0; i < col_count; i++) {
        ColumnMetaData col = cols_info[i];
        int type = sqlite3_column_type(stmt, i);
        if (type != col.type) {
            return nob_temp_sprintf("Error: Expect column %s to be type %s but got %s.", col.col_name, sqlite3_type_str(col.type), sqlite3_type_str(type));
        }
    }
    return NULL;
}

typedef struct {
    int64_t id;
    const char *keyword;
    const char *pronounciation;
    int64_t last_read;
} Entry;
static const ColumnMetaData entry_cols_info[] = {
    {.col = 0, .type = SQLITE_INTEGER, .col_name = "id"},
    {.col = 1, .type = SQLITE3_TEXT, .col_name = "keyword"},
    {.col = 2, .type = SQLITE3_TEXT, .col_name = "pronounciation"},
    {.col = 3, .type = SQLITE_INTEGER, .col_name = "last_read"},
};
static const size_t entry_col_count = sizeof(entry_cols_info)/sizeof(*entry_cols_info);

const char *entry_find_temp(sqlite3 *db, int64_t id, Entry *entry) {
    sqlite3_stmt *stmt = NULL;
    if (sqlite3_prepare_v2(db, "SELECT * FROM entry WHERE id = ?1", -1, &stmt, NULL) != SQLITE_OK) {
        TraceLog(LOG_INFO, sqlite3_errmsg(db));
        return "Could not prepare stmt statement";
    }

    if (sqlite3_bind_int64(stmt, 1, id) != SQLITE_OK) {
        TraceLog(LOG_INFO, sqlite3_errmsg(db));
        return "Could not bind id to stmt statement";
    }

    if (sqlite3_step(stmt) == SQLITE_DONE) {
        return "Id not found";
    }
    const char *err = NULL;
    if ((err = check_table_schema(stmt, entry_cols_info, entry_col_count))) {
        return err;
    }

    entry->id = sqlite3_column_int64(stmt, 0);
    entry->keyword = nob_temp_sprintf("%s", sqlite3_column_text(stmt, 1));
    entry->pronounciation = nob_temp_sprintf("%s", sqlite3_column_text(stmt, 2));
    entry->last_read = sqlite3_column_int64(stmt, 3);

    if (sqlite3_step(stmt) != SQLITE_DONE) {
        return "Expected 1 entry but got more than 1";
    }

    if (sqlite3_finalize(stmt) != SQLITE_OK) {
        TraceLog(LOG_INFO, sqlite3_errmsg(db));
    }
    return NULL;
}

bool load_asset_into_file(struct android_app *app, const char *asset_name, const char *data_file_path) {
    AAssetManager* asset_manager = app->activity->assetManager;
    assert(asset_manager);
    AAsset *db_asset = AAssetManager_open(asset_manager, asset_name, AASSET_MODE_STREAMING);
    if (db_asset == NULL) {
        TraceLog(LOG_ERROR, "Could not open asset %s", asset_name);
        return false;
    }

    FILE *data_file = fopen(data_file_path, "wb");
    if (!data_file) {
        TraceLog(LOG_ERROR, "Could not open %s", data_file_path);
        AAsset_close(db_asset);
        return false;
    }

    off_t length = AAsset_getLength(db_asset);
    TraceLog(LOG_INFO, "Loading asset %s, length: %zu -> %s", asset_name, length, data_file_path);
    uint8_t buffer[128];
    int read_count;
    do {
        read_count = AAsset_read(db_asset, buffer, 128);
        if (read_count < 0) {
            TraceLog(LOG_ERROR, "Could not read asset %s", asset_name);
            fclose(data_file);
            AAsset_close(db_asset);
        } else if (read_count > 0) {
            length -= read_count;
            assert(fwrite(buffer, 1, read_count, data_file) == (size_t)read_count);
        }
    } while (length > 0);

    AAsset_close(db_asset);
    fclose(data_file);
    return true;
}

bool load_font_from_asset(struct android_app *app, const char *font_asset_name, Font *font, int font_size) {
    AAssetManager* asset_manager = app->activity->assetManager;
    assert(asset_manager);
    AAsset *font_asset = AAssetManager_open(asset_manager, font_asset_name, AASSET_MODE_BUFFER);
    if (font_asset == NULL) {
        TraceLog(LOG_ERROR, "Could not open asset %s", font_asset_name);
        return false;
    }

    const unsigned char *buffer = AAsset_getBuffer(font_asset);
    off_t length = AAsset_getLength(font_asset);
    TraceLog(LOG_INFO, "Loading font asset %s, length: %zu", font_asset_name, length);

    *font = LoadFontFromMemory("otf", buffer, length, font_size, NULL, 0);
    AAsset_close(font_asset);
    return true;
}

int main(int argc, char ** argv) {
    (void)argc, (void)argv;
    struct android_app *app = GetAndroidApp();

    InitWindow(0, 0, "test");
    int width = GetScreenWidth();
    int height = GetScreenHeight();
    const char* ex_dir = AndroidGetExternalFilesDir(app);
    TraceLog(LOG_INFO, "****************************************");
    TraceLog(LOG_INFO, "Width: %d, Height: %d", width, height);
    TraceLog(LOG_INFO, "External dir: %s", ex_dir);

    const char *db_name = "test.db";
    const char *data_file_path = nob_temp_sprintf("%s/%s", ex_dir, db_name);
    if (!nob_file_exists(data_file_path)) {
        if (!load_asset_into_file(app, db_name, data_file_path)) return 1;
    } else {
        TraceLog(LOG_INFO, "Database %s exists", data_file_path);
    }

    int sqlite_err;
    sqlite_err = sqlite3_initialize();
    if (sqlite_err != SQLITE_OK) {
        TraceLog(LOG_INFO, sqlite3_errstr(sqlite_err));
        return 1;
    } else {
        TraceLog(LOG_INFO, "Sqlite initialized");
    }

    sqlite3 *db;
    sqlite_err = sqlite3_open_v2(data_file_path, &db, SQLITE_OPEN_READWRITE, NULL);
    if (sqlite_err != SQLITE_OK) {
        TraceLog(LOG_INFO, sqlite3_errstr(sqlite_err));
        return 1;
    } else {
        TraceLog(LOG_INFO, "Sqlite opened %s", data_file_path);
    }

    Entry entry = {0};
    const char *err = entry_find_temp(db, 752, &entry);
    if (err != NULL) {
        TraceLog(LOG_INFO, err);
        return 1;
    }
    TraceLog(LOG_INFO, "id: %d", entry.id);
    TraceLog(LOG_INFO, "keyword: %s", entry.keyword);
    TraceLog(LOG_INFO, "pronounciation: %s", entry.pronounciation);
    TraceLog(LOG_INFO, "last_read: %d", entry.last_read);
    entry.keyword = "HELLO WORLD";

    Font font;
    const int font_size = 50;
    float spacing = 10;
    if (!load_font_from_asset(app, "spleen-32x64.otf", &font, font_size)) {
        return 1;
    }

    while (!WindowShouldClose()) {
        BeginDrawing();

        ClearBackground(GetColor(0x101010ff));
        Vector2 text_size = MeasureTextEx(font, entry.keyword, font_size, spacing);
        DrawTextEx(font, entry.keyword, (Vector2){
                .x = (width-text_size.x)/2.0f,
                .y = 0,
        }, font_size, spacing, WHITE);

        EndDrawing();
    }

    CloseWindow();
    sqlite3_shutdown();
    return 0;
}
