package com.senegas.kickoff.states;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;
import com.senegas.kickoff.pitches.Pitch;
import com.senegas.kickoff.screens.Match;

public enum MatchState implements State<Match> {

	INTRODUCTION() {
		@Override
		public void enter(Match match) {

			match.getHomeTeam().setupIntroduction();
			match.getAwayTeam().setupIntroduction();

			match.cameraHelper.setPosition(
					MathUtils.clamp(match.getBall().getPosition().x,
							match.getCamera().viewportWidth / 2 * match.getCamera().zoom,
							Pitch.WIDTH - match.getCamera().viewportWidth / 2 * match.getCamera().zoom),
					MathUtils.clamp(match.getBall().getPosition().y,
							match.getCamera().viewportHeight / 2 * match.getCamera().zoom,
							Pitch.HEIGHT - match.getCamera().viewportHeight / 2 * match.getCamera().zoom));

			match.cameraHelper
					.setTarget(new Vector2(352, (int) (Pitch.PITCH_HEIGHT_IN_PX / 2 + Pitch.OUTER_TOP_EDGE_Y + 16)));
		}

		@Override
		public void update(final Match match) {
			if (match.getHomeTeam().isReady() && match.getAwayTeam().isReady()) {
				float delay = 7; // seconds
				Timer.schedule(new Timer.Task() {
					@Override
					public void run() {
						match.getFSM().changeState(PREPAREFORKICKOFF);
					}
				}, delay);
			}
		}

		@Override
		public boolean onMessage(Match match, Telegram telegram) {
			return false;
		}
	},

	PREPAREFORKICKOFF() {
		@Override
		public void enter(final Match match) {
			match.cameraHelper.setTarget(Pitch.getCenterSpot());

			match.getHomeTeam().getTactic().setupKickoff(true);
			match.getAwayTeam().getTactic().setupKickoff(false);

			float delay = 5; // seconds
			Timer.schedule(new Timer.Task() {
				@Override
				public void run() {
					match.cameraHelper.setTarget(null);
					match.getFSM().changeState(INPLAY);
				}
			}, delay);
		}

		@Override
		public void update(Match match) {
			if (!match.cameraHelper.hasTarget()) {
				match.cameraHelper.setPosition(
						MathUtils.clamp(match.getBall().getPosition().x,
								match.getCamera().viewportWidth / 2 * match.getCamera().zoom,
								Pitch.WIDTH - match.getCamera().viewportWidth / 2 * match.getCamera().zoom),
						MathUtils.clamp(match.getBall().getPosition().y,
								match.getCamera().viewportHeight / 2 * match.getCamera().zoom,
								Pitch.HEIGHT - match.getCamera().viewportHeight / 2 * match.getCamera().zoom));
			}
		}

		@Override
		public boolean onMessage(Match match, Telegram telegram) {
			return false;
		}
	},

	INPLAY() {

		@Override
		public void update(Match match) {
			match.cameraHelper.setPosition(
					MathUtils.clamp(match.getBall().getPosition().x,
							match.getCamera().viewportWidth / 2 * match.getCamera().zoom,
							Pitch.WIDTH - match.getCamera().viewportWidth / 2 * match.getCamera().zoom),
					MathUtils.clamp(match.getBall().getPosition().y,
							match.getCamera().viewportHeight / 2 * match.getCamera().zoom,
							Pitch.HEIGHT - match.getCamera().viewportHeight / 2 * match.getCamera().zoom));
			match.getHomeTeam().getTactic().update(match.getBall());
			match.getAwayTeam().getTactic().update(match.getBall());
		}

		@Override
		public boolean onMessage(Match match, Telegram telegram) {
			return false;
		}
	}
}
