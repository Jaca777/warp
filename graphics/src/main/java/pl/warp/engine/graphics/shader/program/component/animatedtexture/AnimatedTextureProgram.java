package pl.warp.engine.graphics.shader.program.component.animatedtexture;

import pl.warp.engine.core.scene.Component;
import pl.warp.engine.graphics.animation.AnimatedTextureProperty;
import pl.warp.engine.graphics.shader.extendedglsl.ExtendedGLSLProgramCompiler;
import pl.warp.engine.graphics.shader.extendedglsl.LocalProgramLoader;
import pl.warp.engine.graphics.shader.program.component.defaultprog.DefaultMeshProgram;

/**
 * @author Jaca777
 *         Created 2017-03-04 at 14
 */
public class AnimatedTextureProgram extends DefaultMeshProgram {
    private static final String VERTEX_SHADER = "component/animatedtexture/vert";
    private static final String FRAGMENT_SHADER = "component/animatedtexture/frag";

    private int unifDirection;
    private int unifDelta;

    public AnimatedTextureProgram() {
        super(VERTEX_SHADER, FRAGMENT_SHADER,
                new ExtendedGLSLProgramCompiler(CONSTANT_FIELD, LocalProgramLoader.DEFAULT_LOCAL_PROGRAM_LOADER));
        this.unifDirection = getUniformLocation("direction");
        this.unifDelta = getUniformLocation("delta");
    }

    @Override
    public void useComponent(Component component) {
        super.useComponent(component);
        AnimatedTextureProperty animatedTextureProperty = component.getProperty(AnimatedTextureProperty.ANIMATED_TEXTURE_PROPERTY_NAME);
        setUniformV2(unifDirection, animatedTextureProperty.getDirection());
        setUniformf(unifDelta, animatedTextureProperty.getDelta());
    }
}