#include <stdio.h>

#define CNFG_IMPLEMENTATION
#include "android_native_app_glue.h"
#include "rawdraw_sf.h"
struct android_app * gapp;

void HandleKey(int keycode, int bDown) {
    (void)keycode, (void)bDown;
}
void HandleButton(int x, int y, int button, int bDown) {
    (void)x, (void)y, (void)button, (void)bDown;
}
void HandleMotion(int x, int y, int mask) {
    (void)x, (void)y, (void)mask;
}
int HandleDestroy() { return 0; }
void HandleResume() { }
void HandleSuspend() { }

int main(int argc, char ** argv) {
    (void)argc, (void)argv;
    CNFGSetupFullscreen("", 0);

    short width, height;
    CNFGGetDimensions(&width, &height);

    const char *hello_world = "Hello world";
    const short textsize = 15;

	while(CNFGHandleInput()) {

		CNFGColor(0x101010ff);
		CNFGClearFrame();

        {
            int w, h;
            CNFGGetTextExtents(hello_world, &w, &h, textsize);
            CNFGColor(0xffffffff);
            CNFGPenX = (width-w)/2.0f; CNFGPenY = (height-h)/2.0f;
            CNFGSetLineWidth(3);
            CNFGDrawText("Hello world", 15);
        }
		CNFGFlushRender();
		CNFGSwapBuffers();		
	}

    return 0;
}
