package com.kyle.mariobros.Sprites.Enemies;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.kyle.mariobros.MarioBros;
import com.kyle.mariobros.Scenes.Hud;
import com.kyle.mariobros.Screens.PlayScreen;
import com.kyle.mariobros.Sprites.Mario;

/**
 * Created by kyle on 11/21/17.
 */

//draw goombas to the screen, and give them functionality
public class Goomba extends Enemy {
    //set up goomba
    private float stateTime;
    private Animation<TextureRegion> walkAnimation;
    private Array<TextureRegion> frames;
    private boolean setToDestroy;
    private boolean destroyed;
    private float angle;

    public Goomba(PlayScreen screen, float x, float y) {
        //draw goomba animation and set them to their default variables
        super(screen, x, y);
        frames = new Array<TextureRegion>();
        for(int i = 0; i < 2; i++)
            frames.add(new TextureRegion(screen.getAtlas().findRegion("goomba"), i*16, 0, 16, 16));
        walkAnimation = new Animation<TextureRegion>(0.4f, frames);
        stateTime = 0;
        setBounds(getX(), getY(), 16 / MarioBros.PPM, 16 / MarioBros.PPM);
        setToDestroy = false;
        destroyed = false;
        angle = 0;
    }

    public void update(float dt) {
        stateTime += dt;
        //with each iteration, check if a goomba's dead. If it is, show squished frame. If not, define it's movement
        if(setToDestroy && !destroyed) {
            world.destroyBody(b2Body);
            destroyed = true;
            setRegion(new TextureRegion(screen.getAtlas().findRegion("goomba"), 32, 0, 16, 16));
            stateTime = 0;
        }
        else if (!destroyed) {
            b2Body.setLinearVelocity(velocity);
            setPosition(b2Body.getPosition().x - getWidth() / 2, b2Body.getPosition().y - getHeight() / 2);
            setRegion(walkAnimation.getKeyFrame(stateTime, true));
        }
    }

    @Override
    protected void defineEnemy() {
        //Give the Goomba a physics body, and define what it can collide with
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
        fdef.restitution = 0.5f;
        fdef.filter.categoryBits = MarioBros.ENEMY_HEAD_BIT;
        b2Body.createFixture(fdef).setUserData(this);
    }

    //draw the goomba
    public void draw(Batch batch) {
        if (!destroyed || stateTime < 1)
            super.draw(batch);
    }

    //if a goomba collides with a turtle shell that's moving, kill the goomba
    public void onEnemyHit(Enemy enemy) {
        if (enemy instanceof Turtle && ((Turtle) enemy).currentState == Turtle.State.MOVING_SHELL)
            setToDestroy = true;
        else
            reverseVelocity(true, false);
    }

    //if head collided with mario, kill goomba
    @Override
    public void hitOnHead(Mario mario) {
        setToDestroy = true;
        Hud.addScore(100);
        MarioBros.manager.get("audio/sounds/stomp.wav", Sound.class).play();
    }

    //set the goomba so that it collides with nothing and can fall off the map
    @Override
    public void killed() {
        Filter filter = new Filter();
        filter.maskBits = MarioBros.NOTHING_BIT;

        for(Fixture fixture : b2Body.getFixtureList())
            fixture.setFilterData(filter);
        b2Body.applyLinearImpulse(new Vector2(0,10f), b2Body.getWorldCenter(), true);
    }
}
