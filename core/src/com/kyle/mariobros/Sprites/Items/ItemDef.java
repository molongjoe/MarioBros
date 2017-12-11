package com.kyle.mariobros.Sprites.Items;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by kyle on 11/25/17.
 */

//A helper class used to refer to items as they're created
public class ItemDef {
    public Vector2 position;
    public Class<?> type;

    public ItemDef(Vector2 position, Class<?> type) {
        this.position = position;
        this.type = type;
    }
}
