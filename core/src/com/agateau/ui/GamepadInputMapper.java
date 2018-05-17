/*
 * Copyright 2018 Aurélien Gâteau <mail@agateau.com>
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
package com.agateau.ui;

import com.agateau.utils.log.NLog;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;

/**
 * An implementation of InputMapper for gamepads
 */
public class GamepadInputMapper extends ControllerAdapter implements InputMapper {
    private static final int MAX_GAMEPAD_COUNT = 4;

    private static final String TRIGGER_BUTTON_PREF = "trigger";
    private static final String BACK_BUTTON_PREF = "back";

    private enum AxisValue {
        LESS,
        ZERO,
        MORE
    }

    private boolean mActive;

    private final HashMap<VirtualKey, Boolean> mPressedKeys = new HashMap<VirtualKey, Boolean>();

    private int mTriggerButtonCode = 1;
    private int mBackButtonCode = 2;
    private int mHorizontalAxis = 0;
    private int mVerticalAxis = 1;

    private static final GamepadInputMapper[] sInstances = new GamepadInputMapper[MAX_GAMEPAD_COUNT];

    public static GamepadInputMapper[] getInstances() {
        if (sInstances[0] == null) {
            createInstances();
        }
        return sInstances;
    }

    public static GamepadInputMapper getInstance(int idx) {
        return getInstances()[idx];
    }

    private static void createInstances() {
        for (int idx = 0; idx < sInstances.length; ++idx) {
            sInstances[idx] = new GamepadInputMapper(idx);
        }
    }

    private GamepadInputMapper(int idx) {
        Array<Controller> controllers = Controllers.getControllers();
        if (idx < controllers.size) {
            controllers.get(idx).addListener(this);
            mActive = true;
        } else {
            mActive = false;
        }
    }

    public boolean isActive() {
        return mActive;
    }

    @Override
    public boolean isKeyPressed(VirtualKey key) {
        Boolean pressed = mPressedKeys.get(key);
        return pressed != null ? pressed : false;
    }

    @Override
    public boolean isKeyJustPressed(VirtualKey vkey) {
        return isKeyPressed(vkey);
    }

    @Override
    public void loadConfig(Preferences preferences, String prefix) {
        mTriggerButtonCode = preferences.getInteger(prefix + TRIGGER_BUTTON_PREF, 1);
        mBackButtonCode = preferences.getInteger(prefix + BACK_BUTTON_PREF, 2);
    }

    @Override
    public void saveConfig(Preferences preferences, String prefix) {
        preferences.putInteger(prefix + TRIGGER_BUTTON_PREF, mTriggerButtonCode);
        preferences.putInteger(prefix + BACK_BUTTON_PREF, mBackButtonCode);
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        NLog.i("buttonCode=%d", buttonCode);
        setPressed(buttonCode, true);
        return false;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        NLog.i("buttonCode=%d", buttonCode);
        setPressed(buttonCode, false);
        return false;
    }

    @Override
    public boolean povMoved(Controller controller, int povCode, PovDirection value) {
        NLog.i("povCode=%d value=%s", povCode, value);
        boolean up = false;
        boolean down = false;
        boolean left = false;
        boolean right = false;
        switch (value) {
        case center:
            break;
        case north:
            up = true;
            break;
        case south:
            down = true;
            break;
        case east:
            right = true;
            break;
        case west:
            left = true;
            break;
        case northEast:
            up = true;
            right = true;
            break;
        case southEast:
            down = true;
            right = true;
            break;
        case northWest:
            up = true;
            left = true;
            break;
        case southWest:
            down = true;
            left = true;
            break;
        }
        mPressedKeys.put(VirtualKey.UP, up);
        mPressedKeys.put(VirtualKey.DOWN, down);
        mPressedKeys.put(VirtualKey.LEFT, left);
        mPressedKeys.put(VirtualKey.RIGHT, right);
        return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float fvalue) {
        NLog.i("axisCode=%d value=%f", axisCode, fvalue);
        AxisValue value = normalizeAxisValue(fvalue);
        if (axisCode == mHorizontalAxis) {
            mPressedKeys.put(VirtualKey.LEFT, value == AxisValue.LESS);
            mPressedKeys.put(VirtualKey.RIGHT, value == AxisValue.MORE);
        } else if (axisCode == mVerticalAxis) {
            mPressedKeys.put(VirtualKey.UP, value == AxisValue.LESS);
            mPressedKeys.put(VirtualKey.DOWN, value == AxisValue.MORE);
        }
        return false;
    }

    private static AxisValue normalizeAxisValue(float value) {
        if (value < -0.5f) {
            return AxisValue.LESS;
        } else if (value > 0.5f) {
            return AxisValue.MORE;
        } else {
            return AxisValue.ZERO;
        }
    }

    private void setPressed(int buttonCode, boolean pressed) {
        if (buttonCode == mTriggerButtonCode) {
            mPressedKeys.put(VirtualKey.TRIGGER, pressed);
        } else if (buttonCode == mBackButtonCode) {
            mPressedKeys.put(VirtualKey.BACK, pressed);
        }
    }
}