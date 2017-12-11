package com.kyle.mariobros.Sprites.Items;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.kyle.mariobros.MarioBros;
import com.kyle.mariobros.Scenes.Hud;
import com.kyle.mariobros.Screens.PlayScreen;
import com.kyle.mariobros.Sprites.Mario;

/**
 * Created by kyle on 11/25/17.
 */

//The mushroom is an item that spawns from certain coin blocks. Grows Mario if he's small
public class Mushroom extends Item {
    public Mushroom(PlayScreen screen, float x, float y) {
        //define the mushrooms coordinates and give it a velocity
        super(screen, x, y);
        setRegion(screen.getAtlas().findRegion("mushroom"), 0, 0, 16, 16);
        velocity = new Vector2(0.7f, -1);
    }

    @Override
    public void defineItem() {
        //define the mushroom in the physics world
        BodyDef bdef = new BodyDef();
        bdef.position.set(getX(), getY());
        bdef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioBros.PPM);
        fdef.filter.categoryBits = MarioBros.ITEM_BIT;
        fdef.filter.maskBits = MarioBros.MARIO_BIT |
                MarioBros.OBJECT_BIT |
                MarioBros.GROUND_BIT |
                MarioBros.COIN_BIT |
                MarioBros.BRICK_BIT;

        fdef.shape = shape;
        body.createFixture(fdef).setUserData(this);
    }

    @Override
    public void use(Mario mario) {
        //when collided with Mario, destroy the mushroom and grow mario if he's small
        destroy();
        Hud.addScore(100);
        if (!mario.isBig())
            mario.grow();
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        if (!destroyed) {
            //move the mushroom around
            setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2);
            body.setLinearVelocity(velocity);
            velocity.y = body.getLinearVelocity().y;
            body.setLinearVelocity(velocity);
        }
    }
}
