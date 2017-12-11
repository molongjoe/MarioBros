package com.kyle.mariobros.Sprites.Enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.kyle.mariobros.MarioBros;
import com.kyle.mariobros.Scenes.Hud;
import com.kyle.mariobros.Screens.PlayScreen;
import com.kyle.mariobros.Sprites.Mario;

import javax.xml.soap.Text;

/**
 * Created by kyle on 11/23/17.
 */

//draw turtles to the screen and give them functionality
public class Turtle extends Enemy {

    //turtle shells can be kicked left and right
    public static final int KICK_LEFT_SPEED = -2;
    public static final int KICK_RIGHT_SPEED = 2;

    //Turtles can be in different states, with different functionality
    public enum State {WALKING, STANDING_SHELL, MOVING_SHELL, DEAD}
    public State currentState;
    public State previousState;

    private float stateTime;
    private Animation<TextureRegion> walkAnimation;
    private Array<TextureRegion> frames;
    private TextureRegion shell;
    private boolean setToDestroy;
    private float deadRotationDegrees;
    private boolean destroyed;


    //Define turtle sprite and animations
    public Turtle(PlayScreen screen, float x, float y) {
        super(screen, x, y);
        frames = new Array<TextureRegion>();
        frames.add(new TextureRegion(screen.getAtlas().findRegion("turtle"),0,0,16,24));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("turtle"),16,0,16,24));
        shell = new TextureRegion(screen.getAtlas().findRegion("turtle"),64,0,16,24);
        walkAnimation = new Animation<TextureRegion>(0.2f, frames);
        currentState = previousState = State.WALKING;
        deadRotationDegrees = 0;

        setBounds(getX(), getY(), 16 / MarioBros.PPM, 24 / MarioBros.PPM);
    }

    @Override
    protected void defineEnemy() {
        //define turtle physics body and set what the turtle can collide with
        BodyDef bdef = new BodyDef();
        bdef.position.set(getX(), getY());
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2Body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();

        PolygonShape shape = new PolygonShape();
        Vector2[] bodyVector = new Vector2[4];
        bodyVector[0] = new Vector2(-6,0).scl(1 / MarioBros.PPM);
        bodyVector[1] = new Vector2(6,0).scl(1 / MarioBros.PPM);
        bodyVector[2] = new Vector2(6,-6).scl(1 / MarioBros.PPM);
        bodyVector[3] = new Vector2(-6,-6).scl(1 / MarioBros.PPM);

        shape.set(bodyVector);

        fdef.filter.categoryBits = MarioBros.ENEMY_BIT;
        fdef.filter.maskBits = MarioBros.GROUND_BIT |
                MarioBros.COIN_BIT |
                MarioBros.BRICK_BIT |
                MarioBros.ENEMY_BIT |
                MarioBros.OBJECT_BIT |
                MarioBros.MARIO_BIT |
                MarioBros.FIREBALL_BIT;

        fdef.shape = shape;
        b2Body.createFixture(fdef).setUserData(this);

        PolygonShape head = new PolygonShape();
        Vector2[] vertice = new Vector2[4];
        vertice[0] = new Vector2(-5,8).scl(1 / MarioBros.PPM);
        vertice[1] = new Vector2(5,8).scl(1 / MarioBros.PPM);
        vertice[2] = new Vector2(-3,3).scl(1 / MarioBros.PPM);
        vertice[3] = new Vector2(3,3).scl(1 / MarioBros.PPM);
        head.set(vertice);

        fdef.shape = head;
        fdef.restitution = 1.5f;
        fdef.filter.categoryBits = MarioBros.ENEMY_HEAD_BIT;
        b2Body.createFixture(fdef).setUserData(this);
    }

    //if a turtle collides with a moving shell and the turtle isn't also in that state, kill the turtle.
    //otherwise, the turtle lives
    public void onEnemyHit(Enemy enemy) {
        if(enemy instanceof Turtle) {
            if(((Turtle) enemy).currentState == State.MOVING_SHELL && currentState != State.MOVING_SHELL) {
                killed();
            }
            else if(currentState == State.MOVING_SHELL && currentState != State.MOVING_SHELL)
                return;
            else
                reverseVelocity(true, false);
        }
        else if(currentState != State.MOVING_SHELL)
            reverseVelocity(true, false);
    }

    public TextureRegion getFrame(float dt) {
        //define what sprite should be shown based on which state the turtle is in
        TextureRegion region;

        switch(currentState) {
            case STANDING_SHELL:
            case MOVING_SHELL:
                region = shell;
                break;
            case WALKING:
            default:
                region = walkAnimation.getKeyFrame(stateTime, true);
                break;
        }

        if(velocity.x > 0 && region.isFlipX() == false)
            region.flip(true, false);
        if(velocity.x < 0 && region.isFlipX() == true)
            region.flip(true, false);

        stateTime = currentState == previousState ? stateTime + dt : 0;
        previousState = currentState;
        return region;
    }

    @Override
    public void update(float dt) {
        //if turtle hasn't been kicked for 5 seconds, get back up
        setRegion(getFrame(dt));
        if (currentState == State.STANDING_SHELL && stateTime > 5) {
            currentState = State.WALKING;
            velocity.x = 1;
        }

        //move turtle around, spin turtle out if it dies
        setPosition(b2Body.getPosition().x - getWidth() / 2, b2Body.getPosition().y - 8 / MarioBros.PPM);
        if(currentState == State.DEAD) {
            deadRotationDegrees += 3;
            rotate(deadRotationDegrees);
            if(stateTime > 5 && !destroyed) {
                world.destroyBody(b2Body);
                destroyed = true;
            }
        }
        else
            b2Body.setLinearVelocity(velocity);
    }

    //if hit on the head, change states depending on what the current state is
    @Override
    public void hitOnHead(Mario mario) {
        if(currentState != State.STANDING_SHELL) {
            currentState = State.STANDING_SHELL;
            velocity.x = 0;
        } else {
            kick(mario.getX() <= this.getX() ? KICK_RIGHT_SPEED : KICK_LEFT_SPEED);
        }
    }

    //draw turtle
    public void draw(Batch batch) {
        if(!destroyed)
            super.draw(batch);
    }

    public void kick(int speed) {
        velocity.x = speed;
        currentState = State.MOVING_SHELL;
    }

    public State getCurrentState() {
        return currentState;
    }

    //when killed, give mario points, and set the turtle to collide with nothing so it can fall off the map
    @Override
    public void killed() {
        Hud.addScore(100);
        currentState = State.DEAD;
        Filter filter = new Filter();
        filter.maskBits = MarioBros.NOTHING_BIT;

        for(Fixture fixture : b2Body.getFixtureList())
            fixture.setFilterData(filter);
        b2Body.applyLinearImpulse(new Vector2(0,10f), b2Body.getWorldCenter(), true);
    }
}
