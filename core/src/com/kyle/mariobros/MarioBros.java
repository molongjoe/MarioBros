package com.kyle.mariobros;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.kyle.mariobros.Screens.PlayScreen;

/**
 * Created by kyle on 11/13/17.
 */


public class MarioBros extends Game {
	/*
	Because Box2D scales things out very far initially,
	Virtual Machine Width and Height are used constrain the game to a smaller
	play area. PPM(Pixels per Meter) is the scaling factor used to make the conversion
	to this area
	 */
	public static final int V_WIDTH = 400;
	public static final int V_HEIGHT = 208;
	public static final float PPM = 100;

	/*
	Declare a series of bits that will be used for Box2D's collision system.
	Bitwise operations can be performed using them as arguments to determine what
	fixtures collided. For instance, when Mario collides with a Brick, an OR operation
	will yield a unique number (6 in this case), and appropriate code can follow
	 */
	public static final short NOTHING_BIT = 0;
	public static final short GROUND_BIT = 1;
	public static final short MARIO_BIT = 2;
	public static final short BRICK_BIT = 4;
	public static final short COIN_BIT = 8;
	public static final short DESTROYED_BIT = 16;
    public static final short OBJECT_BIT = 32;
    public static final short ENEMY_BIT = 64;
    public static final short ENEMY_HEAD_BIT = 128;
    public static final short ITEM_BIT = 256;
    public static final short MARIO_HEAD_BIT = 512;
	public static final short FIREBALL_BIT = 1024;

	//Universal SpriteBatch. All sprites contained, and passed around by this one instance
	public SpriteBatch batch;

	//Universal AssetManager. All assets contained, and passed around by this one instance
	public static AssetManager manager;

	/*
	Create SpriteBatch and AssetManager. Load manager with sounds and music. Set Beginning
	screen to PlayScreen
	 */
	@Override
	public void create () {
		batch = new SpriteBatch();
		manager = new AssetManager();
		manager.load("audio/music/mario_music.ogg", Music.class);
		manager.load("audio/sounds/coin.wav", Sound.class);
		manager.load("audio/sounds/bump.wav", Sound.class);
		manager.load("audio/sounds/breakblock.wav", Sound.class);
		manager.load("audio/sounds/powerup_spawn.wav", Sound.class);
        manager.load("audio/sounds/powerup.wav", Sound.class);
		manager.load("audio/sounds/powerdown.wav", Sound.class);
		manager.load("audio/sounds/stomp.wav", Sound.class);
		manager.load("audio/sounds/mariodie.wav", Sound.class);
        manager.finishLoading();
		setScreen(new PlayScreen(this));
	}

	//dispose of resources
	@Override
	public void dispose() {
		super.dispose();
		manager.dispose();
		batch.dispose();
	}

	//render resources
	@Override
	public void render () {
		super.render();
		/*
		if(manager.update())
			waits to see if all assets are loaded, and returns true if they are. Can use this
			to wait and see if things are loaded, and put code here that requires
			those assets. manager.finishLoading() above waits til all assets are loaded
			and then continues. This option is just if there is something more specific
			thing to do with the assets once they're loaded.
		 */
	}
}
