package com.senegas.kickoff;

import com.badlogic.gdx.Game;
import com.senegas.kickoff.screens.MainMenu;

public class KickOff extends Game {

	public static final String TITLE = "Open Kick Off";
	public static final String VERSION = "0.3.5";

	@Override
	public void create() {
		setScreen(new MainMenu());
	}
}
