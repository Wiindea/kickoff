package com.senegas.kickoff.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Joystick {
	private float maxDistance;
	private float maxDistanceSqrt;

	private float centreX;
	private float centreY;

	private float currentX;
	private float currentY;

	public int fingerId;

	public static final float DEADZONE = 0.1f;

	Vector2 tmp;
	Vector2 centreVector;

	public Rectangle area;

	public Joystick() {
		maxDistance = 2000.0f; // in pixels non sqrt
		maxDistanceSqrt = (float) Math.sqrt((double) maxDistance);
		centreX = -1;
		centreY = -1;
		fingerId = -1;
		tmp = new Vector2();
		centreVector = new Vector2();
	}

	public void setMaxDistance(float maxDistanceInPixels) {
		maxDistance = (float) Math.pow((double) maxDistanceInPixels, 2);
		maxDistanceSqrt = maxDistanceInPixels;
	}

	public void update() {
		if (fingerId == -1)
			return;

		if (!Gdx.input.isTouched(fingerId)) {
			fingerId = -1;

			centreX = -1;
			centreY = -1;

			return;
		}

		currentX = Gdx.input.getX(fingerId);
		currentY = Gdx.input.getY(fingerId);

		// Reverse height, so we get graphics height instead of touch height (touch 0,0
		// = top left; graphics 0,0 = bottom left)
		currentY = Gdx.graphics.getHeight() - currentY;

		// On first touch, set the centre
		if (centreX == -1 && centreY == -1) {
			centreX = currentX;
			centreY = currentY;
		}

		float x = centreX - currentX;
		float y = centreY - currentY;

		float dis = (x * x) + (y * y);

		if (dis > maxDistance) {
			dis = (float) Math.sqrt((double) dis);

			float dif = dis - maxDistanceSqrt;

			centreVector.x = centreX;
			centreVector.y = centreY;

			centreVector.sub(currentX, currentY);
			centreVector.nor(); // Direction
			centreVector.scl(dif);

			centreX -= centreVector.x;
			centreY -= centreVector.y;

			if (!area.contains(centreX, centreY)) {
				centreX += centreVector.x;
				centreY += centreVector.y;
			}
		}

	}

	public Vector2 value() {
		if (fingerId == -1) {
			tmp.x = 0;
			tmp.y = 0;
		} else {
			tmp.x = centreX - currentX;
			tmp.y = centreY - currentY;

			tmp.y *= -1;
			tmp.x *= -1;

			float length = tmp.len();
			float percentage = length / maxDistanceSqrt;

			if (percentage < DeadZone) {
				tmp.x = 0;
				tmp.y = 0;
			} else {
				tmp.nor();
				tmp.scl(percentage);
			}

		}

		return tmp;
	}

	public void draw(ShapeRenderer shapeRenderer) {
		if (fingerId == -1)
			return;

		shapeRenderer.begin(ShapeType.Filled);

		shapeRenderer.circle(centreX, centreY, (float) Math.sqrt((double) maxDistance));
		shapeRenderer.circle(currentX, currentY, 10);

		shapeRenderer.end();
	}

}
