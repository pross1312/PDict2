#include <stdio.h>
#include <raylib.h>

int main(int argc, char ** argv) {
    (void)argc, (void)argv;

    InitWindow(600, 600, "test");
    int width = GetScreenHeight();
    int height = GetScreenWidth();
    const char *text = "Hello world";
    const int font_size = 45;
    TraceLog(LOG_INFO, "Width: %d, Height: %d", width, height);
    while (!WindowShouldClose()) {
        BeginDrawing();

        ClearBackground(GetColor(0x101010ff));
        int text_width = MeasureText(text, font_size);
        DrawText(text, (width-text_width)/2.0f, (height-font_size)/2.0f, font_size, WHITE);

        EndDrawing();
    }
    CloseWindow();
    return 0;
}
