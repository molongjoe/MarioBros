package com.kyle.mariobros.Sprites.TileObjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.kyle.mariobros.MarioBros;
import com.kyle.mariobros.Scenes.Hud;
import com.kyle.mariobros.Screens.PlayScreen;
import com.kyle.mariobros.Sprites.Items.ItemDef;
import com.kyle.mariobros.Sprites.Items.Mushroom;
import com.kyle.mariobros.Sprites.Mario;

/**
 * Created by kyle on 11/19/17.
 */

//When mario hits a coin block, give him a coin and add to his score.
    // Change the coin block to a regular brick block after
public class Coin extends InteractiveTileObject {
    private static TiledMapTileSet tileSet;
    private final int BLANK_COIN = 28;

    public Coin(PlayScreen screen, MapObject object) {
        super(screen, object);
        tileSet = map.getTileSets().getTileSet("tileset_gutter");
        fixture.setUserData(this);
        setCategoryFilter(MarioBros.COIN_BIT);
    }

    @Override
    public void onHeadHit(Mario mario) {
        //If the coin block is used, do nothing
        if(getCell().getTile().getId() == BLANK_COIN)
            MarioBros.manager.get("audio/sounds/bump.wav", Sound.class).play();
        else {
            //otherwise give some points.
            //if the tile has the mushroom property, spawn a mushroom. Otherwise, give a coin
            Hud.addScore(100);
            if(object.getProperties().containsKey("mushroom")) {
                screen.spawnItem(new ItemDef(new Vector2(body.getPosition().x, body.getPosition().y + 16 / MarioBros.PPM),
                        Mushroom.class));
                MarioBros.manager.get("audio/sounds/powerup_spawn.wav", Sound.class).play();
            }
            else {
                Hud.addCoins(1);
                MarioBros.manager.get("audio/sounds/coin.wav", Sound.class).play();
            }
        }
        getCell().setTile(tileSet.getTile(BLANK_COIN));
    }
}
