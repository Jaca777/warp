package net.warpgame.engine.graphics.window;

import net.warpgame.engine.core.context.service.Service;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;

import java.io.PrintStream;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * @author Jaca777
 *         Created 2016-06-25 at 21
 */

@Service
public class WindowManager {

    private long windowHandle;
    private Runnable closeCallback;

    public void makeWindow(Display display) {
        setErrorPrintStream(System.err);
        initGLFW();
        configureHints(display);
        createHandle(display);
        setupCloseCallbackCallback();
        makeOGLContext();
        if(display.isVisible()) {
            centerWindow(display);
            showWindow();
            enableVSync();
            glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        } else glfwHideWindow(windowHandle);
    }

    public static final int CURSOR_DISABLED = GLFW_CURSOR_DISABLED;
    public static final int CURSOR_HIDDEN = GLFW_CURSOR_HIDDEN;
    public static final int CURSOR_NORMAL = GLFW_CURSOR_NORMAL;

    public void setCursorDisabled() {
        glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    public void setCursorHidden() {
        glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
    }

    public void setCursorNormal() {
        glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }

    public int getCursorMode() {
        return glfwGetInputMode(windowHandle, GLFW_CURSOR);
    }

    private void initGLFW() {
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");
    }

    private GLFWErrorCallback setErrorPrintStream(PrintStream printStream) {
        GLFWErrorCallback.createPrint(printStream).set();
        return GLFWErrorCallback.createPrint(printStream).set();
    }


    private void showWindow() {
        glfwShowWindow(this.windowHandle);
    }

    private void createHandle(Display display) {
        long monitor = display.isFullscreen() ? glfwGetPrimaryMonitor() : NULL;
        this.windowHandle = glfwCreateWindow(display.getWidth(), display.getHeight(), "Warp Engine Demo", monitor, NULL);
        if (this.windowHandle == NULL)
            throw new RuntimeException("Failed to create the GLFW window");
    }

    private void setupCloseCallbackCallback() {
        glfwSetWindowCloseCallback(windowHandle, (window) -> {
            glfwSetWindowShouldClose(window, true);
            closeWindow();
            if(closeCallback != null) closeCallback.run();
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
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_VISIBLE, display.isVisible() ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, display.isResizable() ? GLFW_TRUE : GLFW_FALSE);
    }

    private void centerWindow(Display display) {
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(
                this.windowHandle,
                (vidmode.width() - display.getWidth()) / 2,
                (vidmode.height() - display.getHeight()) / 2
        );
    }

    public void closeWindow() {
        try {
            glfwFreeCallbacks(windowHandle);
            glfwDestroyWindow(windowHandle);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            glfwTerminate();
        }
    }

    public void resize(int w, int h) {
        glfwSetWindowSize(windowHandle, w, h);
    }

    public void updateWindow() {
        glfwSwapBuffers(windowHandle);
        glfwPollEvents();
    }

    public long getWindowHandle() {
        return windowHandle;
    }

    public void setCloseCallback(Runnable closeCallback) {
        this.closeCallback = closeCallback;
    }
}
