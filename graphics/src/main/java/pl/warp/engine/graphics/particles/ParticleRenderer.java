package pl.warp.engine.graphics.particles;

import org.apache.log4j.Logger;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import pl.warp.engine.core.scene.Component;
import pl.warp.engine.graphics.camera.Camera;
import pl.warp.engine.graphics.math.MatrixStack;
import pl.warp.engine.graphics.Renderer;
import pl.warp.engine.graphics.postprocessing.lens.LensFlareRenderer;
import pl.warp.engine.graphics.shader.program.particle.ParticleProgram;
import pl.warp.engine.graphics.texture.Texture2DArray;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

/**
 * @author Jaca777
 *         Created 2016-07-11 at 13
 */
public class ParticleRenderer implements Renderer {

    private static final Logger logger = Logger.getLogger(LensFlareRenderer.class);

    public static final int MAX_PARTICLES_NUMBER = 1000;

    private Camera camera;
    private ParticleProgram program;

    private int positionVBO;
    private int rotationVBO;
    private int textureIndexVBO;
    private int indexBuff;
    private int vao;

    public ParticleRenderer(Camera camera) {
        this.camera = camera;
    }

    @Override
    public void init() {
        logger.info("Initializing particle renderer...");
        this.program = new ParticleProgram();
        initBuffers();
        logger.info("Particle renderer initialized...");
    }

    private void initBuffers() {
        this.positionVBO = GL15.glGenBuffers();
        this.rotationVBO = GL15.glGenBuffers();
        this.textureIndexVBO = GL15.glGenBuffers();
        createIndexBuffer();
        createVAO();
    }

    private void createIndexBuffer() {
        IntBuffer indices = BufferUtils.createIntBuffer(MAX_PARTICLES_NUMBER);
        for (int i = 0; i < MAX_PARTICLES_NUMBER; i++)
            indices.put(i);
        indices.rewind();
        this.indexBuff = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuff);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW);
    }

    private void createVAO() {
        this.vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);

        GL20.glEnableVertexAttribArray(ParticleProgram.POSITION_ATTR);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, positionVBO);
        GL20.glVertexAttribPointer(ParticleProgram.POSITION_ATTR, 3, GL11.GL_FLOAT, false, 0, 0);

        GL20.glEnableVertexAttribArray(ParticleProgram.ROTATION_ATTR);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, rotationVBO);
        GL20.glVertexAttribPointer(ParticleProgram.ROTATION_ATTR, 1, GL11.GL_FLOAT, false, 0, 0);

        GL20.glEnableVertexAttribArray(ParticleProgram.TEXTURE_INDEX_ATTR);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, textureIndexVBO);
        GL20.glVertexAttribPointer(ParticleProgram.TEXTURE_INDEX_ATTR, 1, GL11.GL_FLOAT, false, 0, 0);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuff);
        GL30.glBindVertexArray(0);
    }

    @Override
    public void initRendering(int delta) {
        program.use();
        program.useCamera(camera);
    }

    @Override
    public void render(Component component, MatrixStack stack) {
        if (component.hasEnabledProperty(GraphicsParticleEmitterProperty.PARTICLE_EMITTER_PROPERTY_NAME)) {
            GraphicsParticleEmitterProperty emitterProperty =
                    component.getProperty(GraphicsParticleEmitterProperty.PARTICLE_EMITTER_PROPERTY_NAME);
            ParticleSystem system = emitterProperty.getSystem();
            renderParticles(system.getParticles(), system.getSpriteSheet(), stack);
        }
    }

    private void renderParticles(List<Particle> particles, Texture2DArray spriteSheet, MatrixStack stack) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDepthMask(false);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        program.use();
        program.useMatrixStack(stack);
        program.useSpriteSheet(spriteSheet);
        GL30.glBindVertexArray(vao);
        updateVBOS(particles);
        GL11.glDrawElements(GL11.GL_POINTS, Math.min(particles.size(), MAX_PARTICLES_NUMBER), GL11.GL_UNSIGNED_INT, 0);
    }

    private FloatBuffer positions = BufferUtils.createFloatBuffer(MAX_PARTICLES_NUMBER * 3);
    private FloatBuffer rotations = BufferUtils.createFloatBuffer(MAX_PARTICLES_NUMBER);
    private FloatBuffer textureIndices = BufferUtils.createFloatBuffer(MAX_PARTICLES_NUMBER);

    private void updateVBOS(List<Particle> particles) {
        clearBuffers();
        int particleCounter = 1;
        for (Particle particle : particles) {
            if (particleCounter > MAX_PARTICLES_NUMBER) break;
            putPosition(particle.getPosition());
            putRotation(particle.getRotation());
            putTextureIndex(particle.getTextureIndex());
            particleCounter++;
        }
        rewindBuffers();
        storeDataInVBOs();
    }

    private void clearBuffers() {
        positions.clear();
        rotations.clear();
        textureIndices.clear();
    }


    private void putPosition(Vector3f position) {
        positions.put(position.x).put(position.y).put(position.z);
    }

    private void putRotation(float rotation) {
        rotations.put(rotation);
    }

    private void putTextureIndex(int textureIndex) {
        textureIndices.put(textureIndex);
    }

    private void rewindBuffers() {
        positions.rewind();
        rotations.rewind();
        textureIndices.rewind();
    }

    private void storeDataInVBOs() {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, positionVBO);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, positions, GL15.GL_DYNAMIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, rotationVBO);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, rotations, GL15.GL_DYNAMIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, textureIndexVBO);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, textureIndices, GL15.GL_DYNAMIC_DRAW);
    }


    @Override
    public void destroy() {
        GL15.glDeleteBuffers(new int[]{positionVBO, textureIndexVBO, rotationVBO, indexBuff});
        GL30.glDeleteVertexArrays(vao);
        program.delete();
        logger.info("Particle renderer destroyed.");
    }
}
