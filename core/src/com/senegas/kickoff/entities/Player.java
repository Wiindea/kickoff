package com.senegas.kickoff.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.senegas.kickoff.pitches.Pitch;

/**
 * Player entity class
 * 
 * @author Sébastien Sénégas
 *
 */
public class Player implements InputProcessor {

	/** Player enum direction */
	public enum Direction {
		NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST, NONE
	}; // create an enum outside

	private static final int SPRITE_WIDTH = 16;
	private static final int SPRITE_HEIGHT = 16;

	private Vector3 position;
	private Vector3 velocity;
	private Circle bounds;
	private Direction direction;
	private Vector3 desiredPosition;
	private float speed;
	private int height; // 1m 77
	private Texture texture;
	private TextureRegion[][] frames;
	private int currentFrameAnimationRow;
	private int currentFrameAnimationColumn;
	private float currentFrameTime;
	private float maxFrameTime;
	private int[] runningFrameAnimation = { 0, 3, 2, 1, 1, 2, 3, 4, 7, 6, 5, 5, 6, 7 };
	private int frameCount;
	private int currentFrame;
	private Vector2[] directionCoefficients;

	private ShapeRenderer shapeRenderer;

	/**
	 * Constructor
	 * 
	 * @param x x-axis position
	 * @param y y-axis position
	 */
	public Player(int x, int y, Texture texture) {
		this.position = new Vector3(x, y, 0);
		this.velocity = new Vector3(0, 0, 0);
		this.texture = texture;
		this.frames = TextureRegion.split(texture, SPRITE_WIDTH, SPRITE_HEIGHT);
		this.bounds = new Circle(position.x, position.y, SPRITE_WIDTH / 2);
		this.desiredPosition = new Vector3((int) (Pitch.PITCH_WIDTH_IN_PX / 2 + Pitch.OUTER_TOP_EDGE_X),
				(int) (Pitch.PITCH_HEIGHT_IN_PX / 2 + Pitch.OUTER_TOP_EDGE_Y + 16), 0);
		this.direction = Direction.NONE;
		this.speed = 200f;
		this.height = 177; // 1m 77
		this.currentFrameAnimationRow = 0;
		this.currentFrameAnimationColumn = 0;
		this.currentFrameTime = 0.0f;
		this.maxFrameTime = 5 / speed; // max time between each frame
		this.frameCount = 14;
		this.currentFrame = 0;
		this.directionCoefficients={new Vector2(0,1f),new Vector2(0.707f,0.707f),new Vector2(1f,0),new Vector2(0.707f,0.707f),new Vector2(0,1f),new Vector2(0.707f,0.707f),new Vector2(1f,0),new Vector2(0.707f,0.707f),new Vector2(0,0)};
		this.shapeRenderer = new ShapeRenderer(); // mainly used for debug purpose
	}

	/**
	 * Update the position and animation frame
	 * 
	 * @param deltaTime The time in seconds since the last render.
	 */
	public void update(float deltaTime) {
		moveToDesiredPosition();

		if (direction != Direction.NONE) {
			// update animation
			currentFrameTime += deltaTime;
			currentFrame = (int) (currentFrameTime / maxFrameTime) % frameCount;
			currentFrameAnimationRow = ((runningFrameAnimation[currentFrame] + 8 * direction.ordinal()) / 20);
			currentFrameAnimationColumn = ((runningFrameAnimation[currentFrame] + 8 * direction.ordinal()) % 20);
		}

		position.x += velocity.x * directionCoefficients[direction.ordinal()].x * deltaTime;
		position.y += velocity.y * directionCoefficients[direction.ordinal()].y * deltaTime;
		bounds.setPosition(position.x, position.y);
	}

	/**
	 * Draw the player frame into the batch at his current position
	 * 
	 * @param batch the batch
	 */
	public void draw(Batch batch) {
		// draw the frame
		batch.draw(frames[currentFrameAnimationRow][currentFrameAnimationColumn], position.x - SPRITE_WIDTH / 2,
				position.y - SPRITE_HEIGHT / 2);
	}

	public void showBounds(OrthographicCamera camera) {
		// enable transparency
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		shapeRenderer.setProjectionMatrix(camera.combined);

		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(new Color(0, 0, 0, 0.5f));
		shapeRenderer.circle(bounds.x, bounds.y, bounds.radius);
		shapeRenderer.end();

		Gdx.gl.glDisable(GL20.GL_BLEND);
	}

	/**
	 * The player boundaries used to check collision
	 * 
	 * @return Circle
	 */
	public Circle getBounds() {
		return bounds;
	}

	public void setDestination(Vector3 destination) {
		this.desiredPosition = new Vector3(destination.x, destination.y, 0);
	}

	public boolean inPosition() {
		Vector3 currentDistance = new Vector3(this.position.x, this.position.y, 0);
		currentDistance.sub(this.desiredPosition);

		if (currentDistance.len() < 2.25)
			return true;
		return false;
	}

	/**
	 * Move the player to destination position
	 */
	public void moveToDesiredPosition() {
		Vector3 start = new Vector3(position.x, position.y, 0);
		float distance = start.dst(desiredPosition);

		if (distance <= 2) {
			moving = false;
			velocity.x = 0;
			velocity.y = 0;
		}

		else {
			velocity.set(desiredPosition.x - position.x, desiredPosition.y - position.y, 0);
			velocity.nor(); // Normalizes the value to be used

			float fThreshold = (float) Math.cos(Math.PI / 8);

			if (velocity.x == 0 || velocity.y == 0)
				; // Do nothing

			else if (Math.abs(velocity.x) > fThreshold) {
				// speed * 1 or -1 depending on the sign of velocity.x
				velocity.x = speed * ((int) Math.abs(velocity.x) / velocity.x);
				velocity.y = 0;

			} else if (Math.abs(velocity.y) > fThreshold) {
				velocity.x = 0;
				// speed * 1 or -1 depending on the sign of velocity.y
				velocity.y = speed * ((int) Math.abs(velocity.y) / velocity.y);

			} else {
				// speed * 1 or -1 depending on the sign of velocity.x
				velocity.x = speed * ((int) Math.abs(velocity.x) / velocity.x);
				// speed * 1 or -1 depending on the sign of velocity.x
				velocity.y = speed * ((int) Math.abs(velocity.y) / velocity.y);
				;
			}
		}

		updateDirection();
	}

	/**
	 * Get the player position
	 * 
	 * @return the Vector3 player position
	 */
	public Vector3 getPosition() {
		return position;
	}

	@Override
	public boolean keyDown(int keycode) {
		switch (keycode) {
		case Keys.LEFT:
			velocity.x = -speed;
			break;
		case Keys.RIGHT:
			velocity.x = speed;
			break;
		case Keys.UP:
			velocity.y = speed;
			break;
		case Keys.DOWN:
			velocity.y = -speed;
			break;
		default:
			// Prompt an error or something
			break;
		}

		updateDirection();

		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch (keycode) {
		case Keys.LEFT:
		case Keys.RIGHT:
			velocity.x = 0.0f;
			break;
		case Keys.UP:
		case Keys.DOWN:
			velocity.y = 0.0f;
			break;
		default:
			// Prompt an error or something
			break;
		}

		updateDirection();

		return true;
	}

	/**
	 * Update the player direction according to its velocity
	 */
	private void updateDirection() {
		if (velocity.y > 0 && velocity.x < 0)
			direction = Direction.NORTH_WEST;

		else if (velocity.y > 0 && velocity.x > 0)
			direction = Direction.NORTH_EAST;

		else if (velocity.y > 0) // velocity.x == 0
			direction = Direction.NORTH;

		else if (velocity.y < 0 && velocity.x < 0)
			direction = Direction.SOUTH_WEST;

		else if (velocity.y < 0 && velocity.x > 0)
			direction = Direction.SOUTH_EAST;

		else if (velocity.y < 0) // velocity.x == 0
			direction = Direction.SOUTH;

		else if (velocity.x < 0) // velocity.y == 0
			direction = Direction.WEST;

		else if (velocity.x > 0) // velocity.y == 0
			direction = Direction.EAST;

		else // velocity.x == 0 && velocity.y == 0
			direction = Direction.NONE;

	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

	public int getDirection() {
		return direction.ordinal();
	}

	public Texture getTexture() {
		return texture;
	}

	// Since there is a public getter and setter of height, it's not private anymore
	public int height() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	// Same thing here
	public float speed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public void dispose() {
		shapeRenderer.dispose();
	}
}
