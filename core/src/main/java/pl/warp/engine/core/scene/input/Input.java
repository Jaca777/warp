package pl.warp.engine.core.scene.input;

import org.joml.Vector2f;

/**
 * @author Jaca777
 *         Created 2017-01-22 at 11
 */
public interface Input {
    void update();

    Vector2f getCursorPosition();

    Vector2f getCursorPositionDelta();

    boolean isKeyDown(int key);

    boolean isMouseButtonDown(int button);

    void destroy();
}