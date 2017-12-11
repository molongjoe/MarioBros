package com.kyle.mariobros.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kyle.mariobros.MarioBros;
import com.kyle.mariobros.Scenes.Hud;
import com.kyle.mariobros.Scenes.PauseScreen;
import com.kyle.mariobros.Sprites.Enemies.Enemy;
import com.kyle.mariobros.Sprites.Items.Item;
import com.kyle.mariobros.Sprites.Items.ItemDef;
import com.kyle.mariobros.Sprites.Items.Mushroom;
import com.kyle.mariobros.Sprites.Mario;
import com.kyle.mariobros.Tools.B2WorldCreator;
import com.kyle.mariobros.Tools.WorldContactListener;


import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by kyle on 11/16/17.
 */

public class PlayScreen implements Screen {

    //Reference to the Game, used to set Screens
    private MarioBros game;
    private TextureAtlas atlas;

    //basic playscreen variables
    private OrthographicCamera gamecam;
    private Viewport gamePort;
    private Hud hud;
    private PauseScreen pause;

    //Tiled map variables
    private TmxMapLoader mapLoader;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;

    //Box2d variables
    private World world;
    private Box2DDebugRenderer b2dr;
    private B2WorldCreator creator;

    //sprites
    private Mario player;

    //music
    private Music music;

    //items
    private Array<Item> items;
    private LinkedBlockingQueue<ItemDef> itemsToSpawn;

    //pause State shifting variables
    private boolean setToPause;
    private boolean setToResume;

    public PlayScreen(MarioBros game) {
        //helps to locate sprites
        atlas = new TextureAtlas("Mario_and_Enemies_Final.pack");

        this.game = game;
        //create cam used to follow mario through cam world
        gamecam = new OrthographicCamera();

        //create a FitViewport to maintain virtual aspect ratio despite screen size
        gamePort = new FitViewport(MarioBros.V_WIDTH / MarioBros.PPM, MarioBros.V_HEIGHT / MarioBros.PPM, gamecam);

        //create the game HUD for scores/timers/level info
        hud = new Hud(game.batch);

        pause = new PauseScreen(game.batch);

        //Load the map and setup the map renderer
        mapLoader = new TmxMapLoader();
        map = mapLoader.load("first level.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1 / MarioBros.PPM);

        //initially set the gamcam to be centered correctly at the start of of map
        gamecam.position.set((gamePort.getWorldWidth() / 2), (gamePort.getWorldHeight() / 2), 0);

        //create the Box2D world, setting no gravity in X, -10 gravity in Y, and allow bodies to sleep
        world = new World(new Vector2(0, -10), true);

        //allows for debug lines of the box2d world.
        b2dr = new Box2DDebugRenderer();

        creator = new B2WorldCreator(this);

        //create mario in the game world
        player = new Mario(this);

        world.setContactListener(new WorldContactListener());

        music = MarioBros.manager.get("audio/music/mario_music.ogg", Music.class);
        music.setLooping(true);
        music.setVolume(0.3f);
        music.play();

        items = new Array<Item>();
        itemsToSpawn = new LinkedBlockingQueue<ItemDef>();

        setToPause = false;
        setToResume = false;
    }

    public void spawnItem(ItemDef idef) {
        itemsToSpawn.add(idef);
    }

    public void handleSpawningItems() {
        //this acts as a queue, adding items as they enter
        if(!itemsToSpawn.isEmpty()) {
            ItemDef idef = itemsToSpawn.poll();
            if(idef.type == Mushroom.class) {
                items.add(new Mushroom(this, idef.position.x, idef.position.y));
            }
        }

    }

    public TextureAtlas getAtlas() {
        return atlas;
    }

    @Override
    public void show() {

    }

    public void handleInput(float dt) {
        //control mario using immediate impulses

        //if Mario isn't dead, these inputs are valid
        if(player.currentState != Mario.State.DEAD) {
            if(setToResume || !setToPause) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.UP))
                    player.b2Body.applyLinearImpulse(new Vector2(0, 4f), player.b2Body.getWorldCenter(), true);
                if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && player.b2Body.getLinearVelocity().x <= 2)
                    player.b2Body.applyLinearImpulse(new Vector2(0.1f, 0), player.b2Body.getWorldCenter(), true);
                if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && player.b2Body.getLinearVelocity().x >= -2)
                    player.b2Body.applyLinearImpulse(new Vector2(-0.1f, 0), player.b2Body.getWorldCenter(), true);
                if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                    player.fire();
                }
            }

            //pause and unpause functionality
            if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
                if (!setToPause)
                    pause();

                else if (!setToResume)
                    resume();
            }
        }
    }

    public void update(float dt) {
        //handle user input first
        handleInput(dt);

        if (setToResume || !setToPause) {
            handleSpawningItems();

            //takes 1 step in the physics simulation(60 times per second)
            world.step(1 / 60f, 6, 2);

            player.update(dt);
            for (Enemy enemy : creator.getEnemies()) {
                enemy.update(dt);
                if (enemy.getX() < player.getX() + 224 / MarioBros.PPM)
                    enemy.b2Body.setActive(true);
            }

            for (Item item : items)
                item.update(dt);

            hud.update(dt);

            //attach the gamecam to our players.x coordinate
            if (player.currentState != Mario.State.DEAD) {
                gamecam.position.x = player.b2Body.getPosition().x;
            }

            //update the gamecam with correct coordinates after changes
            gamecam.update();

            //tell the renderer to draw only what the camera can see in the game world.
            renderer.setView(gamecam);
        }

    }

    @Override
    public void render(float delta) {
        //separate the update logic from render
        update(delta);

        //Clear the game screen with Black
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //render the game map
        renderer.render();

        //render the Box2DDebugLines
        b2dr.render(world, gamecam.combined);

        game.batch.setProjectionMatrix(gamecam.combined);
        game.batch.begin();
        player.draw(game.batch);
        for(Enemy enemy : creator.getEnemies())
            enemy.draw(game.batch);
        for(Item item : items)
            item.draw(game.batch);
        game.batch.end();

        //Set the batch to now draw what the Hud camera sees.
        game.batch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.draw();

        if (gameOver()) {
            game.setScreen(new GameOverScreen(game));
            dispose();
        }

        if (setToPause) {
            pause.stage.draw();
        }

        if (setToResume) {
            pause.stage.dispose();
        }
    }

    public boolean gameOver() {
        if(player.currentState == Mario.State.DEAD && player.getStateTimer() > 3) {
            return true;
        }
        return false;
    }

    @Override
    public void resize(int width, int height) {
        //update the game viewport
        gamePort.update(width, height);
    }

    public TiledMap getMap() {
        return map;
    }

    public World getWorld() {
        return world;
    }

    @Override
    public void pause() {
        setToPause = true;
        setToResume = false;
        music.pause();
    }

    @Override
    public void resume() {
        setToResume = true;
        setToPause = false;
        music.play();
    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        //dispose of all the opened resources
        map.dispose();
        renderer.dispose();
        world.dispose();
        b2dr.dispose();
        hud.dispose();

    }

    public Hud getHud() {
        return hud;
    }
}
