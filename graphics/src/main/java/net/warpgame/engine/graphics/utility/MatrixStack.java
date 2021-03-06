package net.warpgame.engine.graphics.utility;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.nio.FloatBuffer;

/**
 * @author Jaca777
 *         Created 2016-06-28 at 11
 */
public class MatrixStack {
    private static final int MATRIX = 0;
    private static final int R_MATRIX = 1;

    private static final int DEFAULT_DEPTH = 32;

    private int size;
    private Matrix4f[] composeMatrixStack;
    private Matrix3f[] rotationMatrixStack;
    private int top = 0;
    private Matrix3f rotation;

    public MatrixStack(int size) {
        this.size = size;
        this.composeMatrixStack = new Matrix4f[size];
        this.rotationMatrixStack = new Matrix3f[size];
        fill();
    }

    public MatrixStack() {
        this(DEFAULT_DEPTH);
    }

    private void fill() {
        for (int i = 0; i < this.size; i++) {
            composeMatrixStack[i] = new Matrix4f();
            rotationMatrixStack[i] = new Matrix3f();
        }
    }

    /**
     * Pushes a new matrix onto stack.
     */
    public void push() {
        composeMatrixStack[++top].set(composeMatrixStack[top - 1]);
        rotationMatrixStack[top].set(rotationMatrixStack[top - 1]);
    }

    /**
     * Pops the value from the stack.
     */
    public void pop() {
        top--;
    }

    private Vector3f tempVector = new Vector3f();

    /**
     * Translates the topMatrix matrix of the stack.
     *
     * @param x
     * @param y
     * @param z
     */
    public void translate(float x, float y, float z) {
        tempVector.set(x, y, z);
        composeMatrixStack[top].translate(tempVector);
    }

    /**
     * Translates the topMatrix matrix of the stack.
     *
     * @param vector
     */
    public void translate(Vector3f vector) {
        translate(vector.x, vector.y, vector.z);
    }

    /**
     * Rotates the topMatrix matrix of the stack.
     *
     * @param rad Rotation angle in radians.
     * @param x
     * @param y
     * @param z
     */
    public void rotate(float rad, float x, float y, float z) {
        tempVector.set(x, y, z);
        composeMatrixStack[top].rotate(rad, tempVector);
        rotationMatrixStack[top].rotate(rad, tempVector);
    }

    public void rotate(Quaternionf quaternion) {
        composeMatrixStack[top].rotate(quaternion);
        rotationMatrixStack[top].rotate(quaternion);
    }

    /**
     * Scales the topMatrix matrix of the stack.
     *
     * @param x X scale.
     * @param y Y scale.
     * @param z Z scale.
     */
    public void scale(float x, float y, float z) {
        tempVector.set(x, y, z);
        composeMatrixStack[top].scale(tempVector);
    }

    public void scale(Vector3f scale) {
        composeMatrixStack[top].scale(scale);
    }

    /**
     * Multiplies the top stack by @matrix.
     * IT DOESN'T AFFECT THE ROTATION MATRIX.
     *
     * @param matrix
     */
    public void mul(Matrix4f matrix) {
        composeMatrixStack[top].mul(matrix, composeMatrixStack[top]);
    }

    public void setTop(Matrix4f topMatrix) {
        topMatrix().set(topMatrix);
    }

    public void setTopRotation(Matrix3f rotationMatrix) {
        topRotationMatrix().set(rotationMatrix);
    }

    /**
     * @return A direct FloatBuffer containing the topMatrix matrix.
     */
    public FloatBuffer topBuff() {
        return BufferTools.toDirectBuffer(topMatrix());
    }

    /**
     * @return The topMatrix matrix.
     */
    public Matrix4f topMatrix() {
        return composeMatrixStack[top];
    }

    public void storeTopBuffer(FloatBuffer dest) {
        composeMatrixStack[top].get(dest);
    }

    /**
     * @return A direct FloatBuffer containing a rotation matrix of the topMatrix matrix.
     */
    public FloatBuffer topRotationBuff() {
        return BufferTools.toDirectBuffer(topRotationMatrix());
    }

    /**
     * @return A rotation matrix of the topMatrix matrix.
     */
    public Matrix3f topRotationMatrix() {
        return rotationMatrixStack[top];
    }

    public void storeRotationBuffer(FloatBuffer dest) {
        rotationMatrixStack[top].get(dest);
    }

    /**
     * Immutable MatrixStack containing only one, identity matrix.
     */
    public static final MatrixStack IDENTITY_STACK = new MatrixStack(1) {
        @Override
        public void push() {
            throw new UnsupportedOperationException("MatrixStack.IDENTITY_STACK.push()");
        }

        @Override
        public void pop() {
            throw new UnsupportedOperationException("MatrixStack.IDENTITY_STACK.pop()");
        }

        @Override
        public void translate(float x, float y, float z) {
            throw new UnsupportedOperationException("MatrixStack.IDENTITY_STACK.translate(x,y,z)");
        }

        @Override
        public void translate(Vector3f vector) {
            throw new UnsupportedOperationException("MatrixStack.IDENTITY_STACK.translate(vector)");
        }

        @Override
        public void scale(float x, float y, float z) {
            throw new UnsupportedOperationException("MatrixStack.IDENTITY_STACK.scale(x,y,z)");
        }

        @Override
        public void rotate(float rad, float x, float y, float z) {
            throw new UnsupportedOperationException("MatrixStack.IDENTITY_STACK.rotate(x,y,z)");
        }

        @Override
        public void mul(Matrix4f matrix) {
            throw new UnsupportedOperationException("MatrixStack.IDENTITY_STACK.rotate(matrix)");
        }

        @Override
        public void setTop(Matrix4f topMatrix) {
            throw new UnsupportedOperationException("MatrixStack.IDENTITY_STACK.setTop(topMatrix)");
        }

        @Override
        public void setTopRotation(Matrix3f rotationMatrix) {
            throw new UnsupportedOperationException("MatrixStack.IDENTITY_STACK.setTopRotation(rotationMatrix)");
        }
    };

}
