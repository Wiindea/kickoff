package com.senegas.kickoff.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.senegas.kickoff.entities.Ball;
import com.senegas.kickoff.entities.Player;
import com.senegas.kickoff.entities.Player.Direction;
import com.senegas.kickoff.entities.Team;
import com.senegas.kickoff.pitches.FootballDimensions;
import com.senegas.kickoff.pitches.Pitch;
import com.senegas.kickoff.pitches.PitchFactory;
import com.senegas.kickoff.pitches.Scanner;
import com.senegas.kickoff.states.MatchState;
import com.senegas.kickoff.utils.CameraHelper;
import com.senegas.kickoff.utils.PitchUtils;

/**
 * Match
 *
 * @author Sébastien Sénégas
 */
public class Match implements Screen {
	private OrthogonalTiledMapRenderer renderer;
	private OrthographicCamera camera;
	private BitmapFont font;
	private SpriteBatch batch;

	private Pitch pitch;
	private Scanner scanner;
	private Ball ball;
	private Team home;
	private Team away;

	public CameraHelper cameraHelper;
	public ShapeRenderer shapeRenderer;

	private StateMachine<Match, MatchState> matchFsm;
	public Sound crowd;
	public Sound whistle;

	private static final boolean DEBUG = true;

	public Match() {
		pitch = PitchFactory.getInstance().make(Pitch.Type.PLAYERMANAGER);
		renderer = new OrthogonalTiledMapRenderer(pitch.getTiledMap());

		camera = new OrthographicCamera();
		cameraHelper = new CameraHelper();
		cameraHelper.setZoom(.45f);

		Vector2 centerSpot = Pitch.getCenterSpot();
		ball = new Ball(centerSpot.x, centerSpot.y, 160);
		home = new Team(this, "TeamA", Direction.NORTH);
		away = new Team(this, "TeamB", Direction.SOUTH);

		scanner = new Scanner(this);

		crowd = Gdx.audio.newSound(Gdx.files.internal("sounds/crowd.ogg"));
		whistle = Gdx.audio.newSound(Gdx.files.internal("sounds/whistle.ogg"));

		font = new BitmapFont();
		batch = new SpriteBatch();
		matchFsm = new DefaultStateMachine<>(this);
		shapeRenderer = new ShapeRenderer();
	}

	@Override
	public void show() {
		matchFsm.changeState(MatchState.INTRODUCTION);
	}

	@Override
	public void render(float deltaTime) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		handleInput();

		matchFsm.update();

		updateEntities(deltaTime);

		cameraHelper.update(deltaTime);

		checkCollisions();
		cameraHelper.applyTo(camera);

		camera.update();
		renderer.setView(camera);
		renderer.render();

		renderer.getBatch().begin();
		home.draw(renderer.getBatch());
		away.draw(renderer.getBatch());
		ball.draw(renderer.getBatch());
		renderer.getBatch().end();

		scanner.draw();

		if (DEBUG) {
			displayDebugInfo();
		}
	}

	private void displayDebugInfo() {
		this.home.showDebug();
		this.ball.showPosition(this.camera);

		Player player = this.home.getPlayers().get(0);
		Vector2 ballLocation = PitchUtils.globalToPitch(ball.getPosition().x, ball.getPosition().y);

		batch.begin();
		font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, 20);
		font.draw(batch, "Player: " + (int) player.getPosition().x + ", " + (int) player.getPosition().y, 10, 40);

		font.draw(batch,
				"Ball: " + (int) ballLocation.x + ", " + (int) ballLocation.y + ", " + (int) ball.getPosition().z, 10,
				60);
		font.draw(batch, this.home.getTactic().getName(), 10, 80);
		font.draw(batch, this.matchFsm.getCurrentState().toString(), 10, 100);
		batch.end();
	}

	private void updateEntities(float deltaTime) {
		home.update(deltaTime);
		away.update(deltaTime);
		ball.update(deltaTime);
	}

	public StateMachine getFSM() {
		return this.matchFsm;
	}

	/**
	 * Check collisions between players and the ball
	 */
	private void checkCollisions() {
		for (Player player : home.getPlayers()) {
			// !Reimp move constant elsewhere
			if (player.getBounds().contains(ball.getPosition().x, ball.getPosition().y)
					&& ball.getPosition().z < player.height() / FootballDimensions.CM_PER_PIXEL) {
				ball.applyForce(player.speed() * 1.125f + 30.0f, player.getDirection());
			}
		}
	}

	@Override
	public void resize(int width, int height) {
		camera.viewportHeight = height;
		camera.viewportWidth = width;
	}

	public Team getHomeTeam() {
		return home;
	}

	public Team getAwayTeam() {
		return away;
	}

	public Ball getBall() {
		return ball;
	}

	public Pitch pitch() {
		return pitch;
	}

	public OrthographicCamera getCamera() {
		return camera;
	}

	@Override
	public void dispose() {
		renderer.dispose();
		pitch.dispose();
		home.dispose();
		away.dispose();
		ball.dispose();
		ball.getTexture().dispose();
		shapeRenderer.dispose();
		crowd.dispose();
		whistle.dispose();
	}

	private void handleInput() {
		if (Gdx.input.isKeyPressed(Input.Keys.O)) {
			cameraHelper.setZoom(cameraHelper.getZoom() + 0.02f);
		}
		if (Gdx.input.isKeyPressed(Input.Keys.I)) {
			cameraHelper.setZoom(cameraHelper.getZoom() - 0.02f);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
			ball.applyForce(400, 6);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
			ball.applyForce(400, 2);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
			ball.applyForce(400, 0);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
			ball.applyForce(400, 4);
		}
		// handle scanner zoom
		if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
			scanner.toggleZoom();
		}
	}
}
