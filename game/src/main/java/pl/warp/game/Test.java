package pl.warp.game;

import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL11;
import pl.warp.engine.core.*;
import pl.warp.engine.core.scene.Component;
import pl.warp.engine.core.scene.Scene;
import pl.warp.engine.core.scene.SimpleComponent;
import pl.warp.engine.graphics.RenderingSettings;
import pl.warp.engine.graphics.RenderingTask;
import pl.warp.engine.graphics.SceneRenderer;
import pl.warp.engine.graphics.camera.Camera;
import pl.warp.engine.graphics.camera.QuaternionCamera;
import pl.warp.engine.graphics.material.Material;
import pl.warp.engine.graphics.math.projection.PerspectiveMatrix;
import pl.warp.engine.graphics.mesh.Mesh;
import pl.warp.engine.graphics.pipeline.Pipeline;
import pl.warp.engine.graphics.pipeline.builder.PipelineBuilder;
import pl.warp.engine.graphics.pipeline.OnScreenRenderer;
import pl.warp.engine.graphics.property.MaterialProperty;
import pl.warp.engine.graphics.property.MeshProperty;
import pl.warp.engine.graphics.resource.mesh.ObjLoader;
import pl.warp.engine.graphics.resource.texture.ImageDecoder;
import pl.warp.engine.graphics.resource.texture.PNGDecoder;
import pl.warp.engine.graphics.shader.ComponentRendererProgram;
import pl.warp.engine.graphics.texture.Texture2D;
import pl.warp.engine.graphics.window.Display;
import pl.warp.engine.graphics.window.GLFWWindowManager;

/**
 * @author Jaca777
 *         Created 2016-06-27 at 14
 */
public class Test {

    private static final int WIDTH = 512, HEIGHT = 512;
    private static final Logger logger = Logger.getLogger(Test.class);

    public static void main(String... args) {
        EngineContext context = new EngineContext();
        Component root = new SimpleComponent(context);
        Camera camera = new QuaternionCamera(root, new PerspectiveMatrix(60, 0.01f, 100f, WIDTH, HEIGHT));
        Scene scene = new Scene(root);
        EngineThread thread = new SyncEngineThread(new SyncTimer(50), new RapidExecutionStrategy());
        thread.scheduleOnce(() -> {
            Component goat = new SimpleComponent(root);
            Mesh mesh = ObjLoader.read(Test.class.getResourceAsStream("goat.obj")).toVAOMesh(ComponentRendererProgram.ATTRIBUTES);
            new MeshProperty(goat, mesh);
            ImageDecoder.DecodedImage decodedTexture = ImageDecoder.decodePNG(Test.class.getResourceAsStream("goat.png"), PNGDecoder.Format.RGBA);
            Texture2D goatTexture = new Texture2D(decodedTexture.getW(), decodedTexture.getH(), GL11.GL_RGBA, GL11.GL_RGBA, true, decodedTexture.getData());
            new MaterialProperty(goat, new Material(goatTexture));
        });
        RenderingSettings settings = new RenderingSettings(WIDTH, HEIGHT);
        Pipeline pipeline = PipelineBuilder.from(new SceneRenderer(scene, camera, settings)).to(new OnScreenRenderer());
        thread.scheduleTask(new RenderingTask(context, new Display(WIDTH, HEIGHT), new GLFWWindowManager(thread::interrupt), pipeline));
        thread.start();
    }
}
