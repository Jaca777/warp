package pl.warp.engine.physics;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.btPersistentManifold;
import org.joml.Vector3f;
import pl.warp.engine.core.scene.Component;
import pl.warp.engine.core.scene.properties.TransformProperty;
import pl.warp.engine.physics.collider.PointCollider;
import pl.warp.engine.physics.property.ColliderProperty;
import pl.warp.engine.physics.property.GravityAffectedBodyProperty;
import pl.warp.engine.physics.property.PhysicalBodyProperty;

/**
 * Created by Hubertus on 2016-07-19.
 */

public class CollisionHandler {

    private static final float COLLISION_MARGIN = 0.15f;

    private PhysicsWorld world;
    private CollisionStrategy collisionStrategy;

    private Component component1;
    private Component component2;
    private Vector3 contactPos = new Vector3();
    private Vector3 normal = new Vector3();

    public CollisionHandler(PhysicsWorld world, CollisionStrategy collisionStrategy) {
        this.world = world;
        this.collisionStrategy = collisionStrategy;
        result = new ClosestRayResultCallback(new Vector3(), new Vector3());
        tmpTranslation = new Vector3f();
        contactPos = new Vector3();
        rayTestNormal = new Vector3();
    }


    public void updateCollisions() {
        synchronized (world) {
            world.getCollisionWorld().performDiscreteCollisionDetection();
            world.getActiveCollisions().forEach(manifold -> {
                manifold.getContactPoint(0).getPositionWorldOnA(contactPos);
                manifold.getContactPoint(0).getNormalWorldOnB(normal);
                assingValues(manifold);
                //collisionStrategy.calculateCollisionResponse(component1, component2, contactPos);
                //findContactPos(manifold);
                processCollision(manifold);
            });
        }
    }

    private void processCollision(btPersistentManifold manifold) {
        if (isGravityAffected(component1) && !isGravityAffected(component2)) {
            processGravityBodiesIntersection(component1, component2, manifold);
        } else if (isGravityAffected(component2) && !isGravityAffected(component1)) {
            processGravityBodiesIntersection(component2, component1, manifold);
        } else {
            collisionStrategy.calculateCollisionResponse(component1, component2, contactPos, normal);
            findContactPos(manifold);
        }
    }

    private boolean isGravityAffected(Component component) {
        return component.hasEnabledProperty(GravityAffectedBodyProperty.GRAVITY_AFFECTED_BODY_PROPERTY_NAME);
    }

    private void processGravityBodiesIntersection(Component component1, Component component2, btPersistentManifold manifold) {
        GravityAffectedBodyProperty bodyProperty = component1.getProperty(GravityAffectedBodyProperty.GRAVITY_AFFECTED_BODY_PROPERTY_NAME);
        bodyProperty.stand();
    }

    private Vector3f direction1 = new Vector3f();
    private Vector3f direction2 = new Vector3f();
    private Vector3f translation1 = new Vector3f();
    private Vector3f translation2 = new Vector3f();

    private void findContactPos(btPersistentManifold manifold) {
        Component component1;
        Component component2;
        synchronized (world) {
            component1 = world.getComponent(manifold.getBody0().getUserValue());
            component2 = world.getComponent(manifold.getBody1().getUserValue());
        }

        TransformProperty transformProperty1 = component1.getProperty(TransformProperty.TRANSFORM_PROPERTY_NAME);
        PhysicalBodyProperty physicalBodyProperty1 = component1.getProperty(PhysicalBodyProperty.PHYSICAL_BODY_PROPERTY_NAME);
        TransformProperty transformProperty2 = component2.getProperty(TransformProperty.TRANSFORM_PROPERTY_NAME);
        PhysicalBodyProperty physicalBodyProperty2 = component2.getProperty(PhysicalBodyProperty.PHYSICAL_BODY_PROPERTY_NAME);
        ColliderProperty colliderProperty1 = component1.getProperty(ColliderProperty.COLLIDER_PROPERTY_NAME);
        ColliderProperty colliderProperty2 = component2.getProperty(ColliderProperty.COLLIDER_PROPERTY_NAME);
        //Transforms

        float distance = findLongestDistance(manifold);


        //distance is always > 0
        if (distance < -COLLISION_MARGIN) {
            distance += COLLISION_MARGIN;
            if (!isMoving(physicalBodyProperty1) && !isMoving(physicalBodyProperty2)) {
                direction1.set(transformProperty1.getTranslation());
                direction1.sub(contactPos.x, contactPos.y, contactPos.z);
                direction1.normalize();
                direction1.mul(distance / 2).negate();

                direction2.set(transformProperty2.getTranslation());
                direction2.sub(contactPos.x, contactPos.y, contactPos.z);
                direction2.normalize();
                direction1.mul(distance / 2).negate();

                physicalBodyProperty1.getNextTickTranslation().add(physicalBodyProperty1.getNextTickTranslation().add(direction1));
                physicalBodyProperty2.getNextTickTranslation().add(physicalBodyProperty2.getNextTickTranslation().add(direction2));

            }

            if (isMoving(physicalBodyProperty1) && isApproaching(transformProperty1.getTranslation(), transformProperty2.getTranslation(), physicalBodyProperty1.getNextTickTranslation())) {
                moveBack(physicalBodyProperty1, distance);
            }
            if (isMoving(physicalBodyProperty2) && isApproaching(transformProperty2.getTranslation(), transformProperty1.getTranslation(), physicalBodyProperty2.getNextTickTranslation())) {
                moveBack(physicalBodyProperty2, distance);
            }
        }
    }

    private boolean isMoving(PhysicalBodyProperty property) {
        return !(property.getNextTickTranslation().length() == 0 && property.getNextTickRotation().length() == 0);
    }

    private Vector3f direction = new Vector3f();

    private void moveBack(PhysicalBodyProperty physicalBodyProperty, float distance) {


        if (physicalBodyProperty.getNextTickTranslation().length() > -distance) {
            physicalBodyProperty.getNextTickTranslation().normalize(direction);
            direction.mul(distance);
        } else {
            direction.set(physicalBodyProperty.getNextTickTranslation()).negate();
        }
        physicalBodyProperty.getNextTickTranslation().add(direction);
        physicalBodyProperty.applyForce(direction.mul(physicalBodyProperty.getMass()));
    }

    private float findLongestDistance(btPersistentManifold manifold) {
        float maxDistance = 0;
        float currentDistance;
        for (int i = 0; i < manifold.getNumContacts(); i++) {
            currentDistance = manifold.getContactPoint(i).getDistance();
            if (maxDistance > currentDistance) {
                maxDistance = currentDistance;
            }
        }
        return maxDistance;
    }

    private Vector3f tmp = new Vector3f();

    private boolean isApproaching(Vector3f pos1, Vector3f pos2, Vector3f velocity) {
        tmp.set(pos1);
        tmp.sub(pos2);
        float distance1 = (float) Math.sqrt((tmp.x * tmp.x) + (tmp.y * tmp.y) + (tmp.z * tmp.z));
        tmp.set(pos1);
        tmp.add(velocity);
        tmp.sub(pos2);
        float distance2 = (float) Math.sqrt((tmp.x * tmp.x) + (tmp.y * tmp.y) + (tmp.z * tmp.z));
        return distance1 > distance2;
    }

    private void assingValues(btPersistentManifold manifold) {
        synchronized (world) {
            component1 = world.getComponent(manifold.getBody0().getUserValue());
            component2 = world.getComponent(manifold.getBody1().getUserValue());
        }
    }

    private ClosestRayResultCallback result;
    private Vector3f tmpTranslation;
    private Vector3 rayTestNormal;

    public void performRayTests() {
        for (int i = 0; i < world.getRayTestColliders().size(); i++) {
            PointCollider collider;
            synchronized (world) {
                collider = world.getRayTestColliders().get(i);
            }
            result.setCollisionObject(null);
            result.setClosestHitFraction(1f);
            result.setRayFromWorld(collider.getLastPos());
            result.setRayToWorld(collider.getCurrentPos());
            synchronized (world) {
                world.getCollisionWorld().rayTest(collider.getLastPos(), collider.getCurrentPos(), result);
            }
            if (result.hasHit()) {
                result.getHitPointWorld(contactPos);
                TransformProperty property = collider.getOwner().getProperty(TransformProperty.TRANSFORM_PROPERTY_NAME);
                PhysicalBodyProperty physicalBodyProperty = collider.getOwner().getProperty(PhysicalBodyProperty.PHYSICAL_BODY_PROPERTY_NAME);
                tmpTranslation.set(physicalBodyProperty.getVelocity());
                tmpTranslation.normalize();
                tmpTranslation.negate();
                tmpTranslation.mul(physicalBodyProperty.getRadius());
                tmpTranslation.add(contactPos.x, contactPos.y, contactPos.z);
                property.setTranslation(tmpTranslation);
                Component component;
                component = world.getComponent(result.getCollisionObject().getUserValue());
                result.getHitNormalWorld(rayTestNormal);
                collisionStrategy.calculateCollisionResponse(component, collider.getOwner(), contactPos, rayTestNormal);
            }
        }
    }

}
