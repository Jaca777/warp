package pl.warp.test;

import pl.warp.engine.graphics.camera.CameraProperty;
import pl.warp.engine.graphics.math.projection.PerspectiveMatrix;
import pl.warp.engine.graphics.mesh.RenderableMeshProperty;
import pl.warp.engine.game.scene.GameComponent;
import pl.warp.engine.game.script.GameScriptWithInput;
import pl.warp.engine.game.script.OwnerProperty;

import java.awt.event.MouseEvent;

/**
 * Created by Marcin on 04.03.2017.
 */
public class SecondCameraScript extends GameScriptWithInput{

    @OwnerProperty(name = CameraProperty.CAMERA_PROPERTY_NAME)
    private CameraProperty cameraProperty;

    private RenderableMeshProperty trueGun;

    @OwnerProperty(name = SecondCameraProperty.SECOND_CAMERA_PROPERTY_NAME)
    private SecondCameraProperty secondCameraProperty;

    private PerspectiveMatrix secondCameraPerspectiveMatrix;

    private int currState;


    public SecondCameraScript(GameComponent owner) {
        super(owner);
    }

    @Override
    protected void init() {
        trueGun = this.getOwner().getParent().getProperty(RenderableMeshProperty.MESH_PROPERTY_NAME);
        secondCameraPerspectiveMatrix = (PerspectiveMatrix) cameraProperty.getCamera().getProjectionMatrix();
        currState = 0;
    }

    @Override
    protected void update(int delta) {
        if(super.getInputHandler().wasMouseButtonPressed(MouseEvent.BUTTON2))
            currState = ++currState%3;

        switch (currState) {
            case 0:
                cameraProperty.enable();
                trueGun.enable();
                secondCameraProperty.getFakeGun().disable();
                this.getContext().getGraphics().setMainViewCamera(secondCameraProperty.getMainCameraProperty().getCamera());
                break;
            case 1:
                secondCameraProperty.getTurret().disable();
                trueGun.disable();
                secondCameraProperty.getFakeGun().enable();
                secondCameraPerspectiveMatrix.setFov(70);
                this.getContext().getGraphics().setMainViewCamera(cameraProperty.getCamera());
                break;
            case 2:
                secondCameraPerspectiveMatrix.setFov(30);
                break;
        }
    }
}
