package com.danielJuarez;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main implements ApplicationListener {
    Texture backgroundTexture;
    Texture gameOverBackgroundTexture;

    Texture bucketTexture;
    Texture dropTexture;
    Sound dropSound;
    Music music;
    SpriteBatch spriteBatch;
    FitViewport viewport;
    Sprite bucketSprite;
    Vector2 touchPos;
    Array<Sprite> dropSprites;
    float dropTimer;
    Rectangle bucketRectangle;
    Rectangle dropRectangle;

    // Puntuacio
    int score;
    BitmapFont font;
    GlyphLayout layout;

    //Game over
    boolean gameOver;
    Sound gameOverSound;

    //Restart
    Texture restartTexture;
    Sprite restartButton;


    @Override
    public void create() {
        backgroundTexture = new Texture("background_2.jpg");
        gameOverBackgroundTexture = new Texture("gameOver.jpg");
        bucketTexture = new Texture("bucket.png");
        dropTexture = new Texture("drop.png");
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(32, 20);
        bucketSprite = new Sprite(bucketTexture);
        bucketSprite.setSize(4, 4);
        touchPos = new Vector2();
        dropSprites = new Array<>();
        bucketRectangle = new Rectangle();
        dropRectangle = new Rectangle();
        music.setLooping(true);
        music.setVolume(.5f);
        music.play();

        // Puntuacio
        score = 0;

        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(0.1f);
        layout = new GlyphLayout();

        //Game Over
        gameOverSound = Gdx.audio.newSound(Gdx.files.internal("gameOver.mp3"));
        gameOver = false;

        //Restart
        restartTexture = new Texture("restart.png");
        restartButton = new Sprite(restartTexture);
        restartButton.setSize(8, 4);
        restartButton.setPosition((viewport.getWorldWidth() - restartButton.getWidth()) / 2, viewport.getWorldHeight() / 3);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        input();
        logic();
        draw();
    }

    private void input() {
        float speed = 4f;
        float delta = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            bucketSprite.translateX(speed * delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            bucketSprite.translateX(-speed * delta);
        }

        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos);
            bucketSprite.setCenterX(touchPos.x);
        }

        if (gameOver && Gdx.input.isTouched()) {
            Vector2 touch = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touch);

            if (restartButton.getBoundingRectangle().contains(touch)) {
                restartGame();
            }
        }
    }

    private void logic() {
        if (gameOver) return;

        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        float bucketWidth = bucketSprite.getWidth();
        float bucketHeight = bucketSprite.getHeight();

        bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, worldWidth - bucketWidth));

        float delta = Gdx.graphics.getDeltaTime();
        bucketRectangle.set(bucketSprite.getX(), bucketSprite.getY(), bucketWidth, bucketHeight);

        for (int i = dropSprites.size - 1; i >= 0; i--) {
            Sprite dropSprite = dropSprites.get(i);
            float dropWidth = dropSprite.getWidth();
            float dropHeight = dropSprite.getHeight();

            dropSprite.translateY(-6f * delta);
            dropRectangle.set(dropSprite.getX(), dropSprite.getY(), dropWidth, dropHeight);

            if (dropSprite.getY() < -dropHeight) {
                dropSprites.removeIndex(i);
                gameOver = true;
                gameOverSound.play();
                music.stop();
                break;
            }
            else if (bucketRectangle.overlaps(dropRectangle)) {
                dropSprites.removeIndex(i);
                dropSound.play();
                score++;
            }
        }

        dropTimer += delta;
        if (dropTimer > 1f) {
            dropTimer = 0;
            createDroplet();
        }
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        if (gameOver) {
            spriteBatch.draw(gameOverBackgroundTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());


            font.getData().setScale(0.1f);

            // GAME OVER
            String gameOverText = "GAME OVER";
            layout.setText(font, gameOverText);
            font.draw(spriteBatch, layout,
                (viewport.getWorldWidth() - layout.width) / 2,
                viewport.getWorldHeight() * 0.8f);

            // SCORE
            String scoreText = "SCORE " + score;
            layout.setText(font, scoreText);
            font.draw(spriteBatch, layout,
                (viewport.getWorldWidth() - layout.width) / 2,
                viewport.getWorldHeight() * 0.7f);

            restartButton.draw(spriteBatch);
        }else {

            float worldWidth = viewport.getWorldWidth();
            float worldHeight = viewport.getWorldHeight();

            spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
            bucketSprite.draw(spriteBatch);

            for (Sprite dropSprite : dropSprites) {
                dropSprite.draw(spriteBatch);
            }

            layout.setText(font, "Score - " + score);
            font.draw(spriteBatch, layout, 0.5f, worldHeight - 0.5f);

        }

        spriteBatch.end();
    }

    private void createDroplet() {
        float dropWidth = 3;
        float dropHeight = 3;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        Sprite dropSprite = new Sprite(dropTexture);
        dropSprite.setSize(dropWidth, dropHeight);
        dropSprite.setX(MathUtils.random(0f, worldWidth - dropWidth));
        dropSprite.setY(worldHeight);
        dropSprites.add(dropSprite);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }
    private void restartGame() {
        gameOver = false;
        score = 0;
        dropSprites.clear();
        bucketSprite.setPosition(viewport.getWorldWidth() / 2 - bucketSprite.getWidth() / 2, 0);
        music.play();
    }


    @Override
    public void dispose() {

    }
}
