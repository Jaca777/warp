package pl.warp.engine.graphics.window;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import pl.warp.engine.graphics.RenderingTask;

import java.io.PrintStream;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * @author Jaca777
 *         Created 2016-06-25 at 21
 */
public class GLFWWindowManager implements WindowManager {

    private long windowHandle;

    public void makeWindow(Display display) {
        setErrorCallback(System.err);
        initGLFW();
        configureHints(display);
        createHandle(display);
        enableCloseKeyCallback();
        centerWindow(display);
        makeOGLContext();
        enableVSync();
        showWindow();
    }

    private void initGLFW() {
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");
    }

    private GLFWErrorCallback setErrorCallback(PrintStream errorCallback) {
        return GLFWErrorCallback.createPrint(errorCallback).set();
    }

    private void showWindow() {
        glfwShowWindow(this.windowHandle);
    }

    private void createHandle(Display display) {
        this.windowHandle = glfwCreateWindow(display.getWidth(), display.getWidth(), "Hello World!", NULL, NULL);
        if (this.windowHandle == NULL)
            throw new RuntimeException("Failed to create the GLFW window");
    }

    private void enableCloseKeyCallback() {
        glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
        });
    }

    private void makeOGLContext() {
        glfwMakeContextCurrent(this.windowHandle);
    }

    private void enableVSync() {
        glfwSwapInterval(1);
    }

    private void configureHints(Display display) {
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, display.isVisible() ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, display.isResizable() ? GLFW_TRUE : GLFW_FALSE);
    }

    private void centerWindow(Display display) {
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

        glfwSetWindowPos(
                this.windowHandle,
                (vidmode.width() - display.getWidth()) / 2,
                (vidmode.height() - display.getWidth()) / 2
        );
    }

    @Override
    public void closeWindow() {
        try {
            glfwFreeCallbacks(windowHandle);
            glfwDestroyWindow(windowHandle);
        } finally {
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    }

    @Override
    public void resize(int w, int h) {
        //TODO
        throw new UnsupportedOperationException();
    }
}