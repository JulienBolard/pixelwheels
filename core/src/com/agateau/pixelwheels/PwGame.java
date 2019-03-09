/*
 * Copyright 2017 Aurélien Gâteau <mail@agateau.com>
 *
 * This file is part of Pixel Wheels.
 *
 * Tiny Wheels is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.agateau.pixelwheels;

import com.agateau.pixelwheels.debug.Debug;
import com.agateau.pixelwheels.gamesetup.ChampionshipMaestro;
import com.agateau.pixelwheels.gamesetup.Maestro;
import com.agateau.pixelwheels.gamesetup.PlayerCount;
import com.agateau.pixelwheels.gamesetup.QuickRaceMaestro;
import com.agateau.pixelwheels.stats.JsonGameStatsIO;
import com.agateau.pixelwheels.stats.GameStats;
import com.agateau.pixelwheels.screens.MainMenuScreen;
import com.agateau.pixelwheels.screens.PwStageScreen;
import com.agateau.pixelwheels.sound.AudioManager;
import com.agateau.pixelwheels.sound.DefaultAudioManager;
import com.agateau.pixelwheels.vehicledef.VehicleDefId;
import com.agateau.ui.ScreenStack;
import com.agateau.utils.FileUtils;
import com.agateau.utils.Introspector;
import com.agateau.utils.PlatformUtils;
import com.agateau.utils.ScreenshotCreator;
import com.agateau.utils.log.NLog;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.physics.box2d.Box2D;

/**
 * The game
 */
public class PwGame extends Game implements GameConfig.ChangeListener {
    private Assets mAssets;
    private final ScreenStack mScreenStack = new ScreenStack(this);
    private Maestro mMaestro;
    private GameConfig mGameConfig;
    private AudioManager mAudioManager = new DefaultAudioManager();

    private Introspector mGamePlayIntrospector;
    private Introspector mDebugIntrospector;
    private GameStats mGameStats;

    public Assets getAssets() {
        return mAssets;
    }

    public AudioManager getAudioManager() {
        return mAudioManager;
    }

    static class Ids {
        private static final String[] VEHICLE_IDS = { "red", "police", "pickup", "roadster", "antonin", "santa", "2cv", "harvester", "rocket" };

        public static void createIds() {
            VehicleDefId.registry.clear();
            for (String id : VEHICLE_IDS) {
                new VehicleDefId(id);
            }
        }
    }

    @Override
    public void create() {
        mGamePlayIntrospector = new Introspector(GamePlay.instance, new GamePlay(),
                FileUtils.getUserWritableFile("gameplay.xml"));
        mDebugIntrospector = new Introspector(Debug.instance, new Debug(),
                FileUtils.getUserWritableFile("debug.xml"));

        mGamePlayIntrospector.load();
        mDebugIntrospector.load();

        Ids.createIds();
        mAssets = new Assets();
        setupConfig();
        setupTrackStats();
        Box2D.init();
        hideMouseCursor();
        setupDisplay();
        showMainMenu();
    }

    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            String path = ScreenshotCreator.saveScreenshot();
            NLog.i("Screenshot saved in %s", path);
        }
        super.render();
    }

    public void reloadAssets() {
        mAssets = new Assets();
    }

    private void setupConfig() {
        mGameConfig = new GameConfig();
        mGameConfig.addListener(this);
        onGameConfigChanged();
    }

    private void setupTrackStats() {
        JsonGameStatsIO io = new JsonGameStatsIO(FileUtils.getUserWritableFile("gamestats.json"));
        mGameStats = new GameStats(io);
    }

    public void showMainMenu() {
        mScreenStack.clear();
        mScreenStack.push(new MainMenuScreen(this));
    }

    public void showQuickRace(PlayerCount playerCount) {
        mMaestro = new QuickRaceMaestro(this, playerCount);
        mMaestro.start();
    }

    public void showChampionship(PlayerCount playerCount) {
        mMaestro = new ChampionshipMaestro(this, playerCount);
        mMaestro.start();
    }

    public void replaceScreen(Screen screen) {
        mScreenStack.replace(screen);
    }

    public GameConfig getConfig() {
        return mGameConfig;
    }

    public GameStats getGameStats() {
        return mGameStats;
    }

    public Introspector getGamePlayIntrospector() {
        return mGamePlayIntrospector;
    }

    public Introspector getDebugIntrospector() {
        return mDebugIntrospector;
    }

    public ScreenStack getScreenStack() {
        return mScreenStack;
    }

    public void pushScreen(Screen screen) {
        mScreenStack.push(screen);
    }

    public void popScreen() {
        mScreenStack.pop();
    }

    private void hideMouseCursor() {
        Pixmap pixmap = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();
        Cursor cursor = Gdx.graphics.newCursor(pixmap, 0, 0);
        if (cursor != null) {
            Gdx.graphics.setCursor(cursor);
        }
    }

    private void setupDisplay() {
        setFullscreen(mGameConfig.fullscreen);
    }

    public void setFullscreen(boolean fullscreen) {
        if (!PlatformUtils.isDesktop()) {
            return;
        }
        if (fullscreen) {
            Graphics.DisplayMode mode = Gdx.graphics.getDisplayMode();
            Gdx.graphics.setFullscreenMode(mode);
        } else {
            Gdx.graphics.setWindowedMode(PwStageScreen.WIDTH, PwStageScreen.HEIGHT);
        }
    }

    @Override
    public void onGameConfigChanged() {
        mAudioManager.setMuted(!mGameConfig.audio);
    }
}
