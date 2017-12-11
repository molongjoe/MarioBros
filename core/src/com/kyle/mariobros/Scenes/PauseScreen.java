package com.kyle.mariobros.Scenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kyle.mariobros.MarioBros;

/**
 * Created by kyle on 11/19/17.
 */

//As written, this functionality does nothing. In the future, a label could be added
    //to this code to add the word 'paused' or bring up menu choices
public class PauseScreen implements Disposable{

    public Stage stage;
    private Viewport viewport;

    public PauseScreen(SpriteBatch sb){
        viewport = new FitViewport(MarioBros.V_WIDTH, MarioBros.V_HEIGHT, new OrthographicCamera());
        stage = new Stage(viewport, sb);
    }

    public void update(float dt) {
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
