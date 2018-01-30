package com.agateau.tinywheels.enginelab;

import com.agateau.tinywheels.sound.SynthEngineSound;
import com.agateau.ui.Menu;
import com.agateau.ui.SelectorMenuItem;
import com.agateau.ui.SliderMenuItem;
import com.agateau.ui.StageScreen;
import com.agateau.ui.anchor.Anchor;
import com.agateau.ui.anchor.AnchorGroup;
import com.agateau.ui.anchor.SizeRule;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Main screen for EngineLab
 */
class EngineLabScreen extends StageScreen {
    private Skin mSkin;
    private SynthEngineSound mEngineSound;
    private SynthEngineSound.Settings mSettings = new SynthEngineSound.Settings();

    private SliderMenuItem mSpeedItem;
    private SliderMenuItem mMinFrequencyItem;
    private SliderMenuItem mMaxFrequencyItem;
    private SelectorMenuItem<SynthEngineSound.WaveForm> mWaveFormItem;
    private SliderMenuItem mGainItem;
    private SliderMenuItem mModFrequencyItem;
    private SliderMenuItem mModStrengthItem;
    private SliderMenuItem mEchoStrengthItem;
    private SliderMenuItem mEchoDelayItem;
    private SliderMenuItem mLpfBetaItem;

    public EngineLabScreen() {
        super(new ScreenViewport());
        setupEngineLab();
        loadSkin();
        setupUi();
    }

    @Override
    public void hide() {
        mEngineSound.dispose();
    }

    private void loadSkin() {
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("ui/uiskin.atlas"));
        mSkin = new Skin(atlas);
        loadFonts();
        mSkin.load(Gdx.files.internal("ui/uiskin.json"));
    }

    private void loadFonts() {
        FreeTypeFontGenerator.FreeTypeFontParameter parameter;
        mSkin.add("default-font", loadFont("fonts/Xolonium-Regular.ttf", 28));
        mSkin.add("title-font", loadFont("fonts/Aero.ttf", 32));

        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 12;
        parameter.borderWidth = 0.5f;
        mSkin.add("small-font", loadFont("fonts/Xolonium-Regular.ttf", parameter));

        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 28;
        parameter.borderWidth = 0.5f;
        mSkin.add("hud-font", loadFont("fonts/Xolonium-Regular.ttf", parameter));

        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 56;
        parameter.characters = "1234567890thsrdneméè";
        parameter.borderWidth = 0.5f;
        mSkin.add("hud-rank-font", loadFont("fonts/Xolonium-Regular.ttf", parameter));
    }

    private BitmapFont loadFont(String name, int size) {
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        return loadFont(name, parameter);
    }

    private BitmapFont loadFont(String name, FreeTypeFontGenerator.FreeTypeFontParameter parameter) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(name));
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();
        return font;
    }

    private void setupUi() {
        AnchorGroup root = new AnchorGroup();
        getStage().addActor(root);
        root.setFillParent(true);

        Menu menu = new Menu(mSkin);
        menu.setLabelColumnWidth(300);
        menu.setDefaultItemWidth(600);

        mSpeedItem = new SliderMenuItem(menu);
        mSpeedItem.setRange(0, 1, 0.01f);
        mSpeedItem.setIntValue(0);
        menu.addItemWithLabel("Speed", mSpeedItem);

        mWaveFormItem = new SelectorMenuItem<SynthEngineSound.WaveForm>(menu);
        mWaveFormItem.addEntry("Sin", SynthEngineSound.WaveForm.SIN);
        mWaveFormItem.addEntry("Sawtooth", SynthEngineSound.WaveForm.SAWTOOTH);
        mWaveFormItem.addEntry("Pop", SynthEngineSound.WaveForm.POP);
        mWaveFormItem.addEntry("Square", SynthEngineSound.WaveForm.SQUARE);
        mWaveFormItem.setData(mSettings.waveForm);
        menu.addItemWithLabel("Wave form", mWaveFormItem);

        mMinFrequencyItem = new SliderMenuItem(menu);
        mMinFrequencyItem.setRange(10, 800);
        mMinFrequencyItem.setIntValue(mSettings.minFrequency);
        menu.addItemWithLabel("Min frequency", mMinFrequencyItem);

        mMaxFrequencyItem = new SliderMenuItem(menu);
        mMaxFrequencyItem.setRange(10, 800);
        mMaxFrequencyItem.setIntValue(mSettings.maxFrequency);
        menu.addItemWithLabel("Max frequency", mMaxFrequencyItem);

        mGainItem = new SliderMenuItem(menu);
        mGainItem.setRange(1, 2, 0.01f);
        mGainItem.setFloatValue(mSettings.gain);
        menu.addItemWithLabel("Gain", mGainItem);

        mLpfBetaItem = new SliderMenuItem(menu);
        mLpfBetaItem.setRange(0.001f, 1f, 0.001f);
        mLpfBetaItem.setFloatValue(mSettings.lpfBeta);
        menu.addItemWithLabel("LPF", mLpfBetaItem);

        menu.addTitleLabel("Mod");
        mModStrengthItem = new SliderMenuItem(menu);
        mModStrengthItem.setRange(0, 0.5f, 0.1f);
        mModStrengthItem.setFloatValue(mSettings.modulationStrength);
        menu.addItemWithLabel("Strength", mModStrengthItem);

        mModFrequencyItem = new SliderMenuItem(menu);
        mModFrequencyItem.setRange(10, 2000, 10);
        mModFrequencyItem.setIntValue(mSettings.modulationFrequency);
        menu.addItemWithLabel("Frequency", mModFrequencyItem);

        menu.addTitleLabel("Echo");
        mEchoStrengthItem = new SliderMenuItem(menu);
        mEchoStrengthItem.setRange(0, 1f, 0.1f);
        mEchoStrengthItem.setFloatValue(mSettings.echoStrength);
        menu.addItemWithLabel("Strength", mEchoStrengthItem);

        mEchoDelayItem = new SliderMenuItem(menu);
        mEchoDelayItem.setRange(0.001f, 1f, 0.001f);
        mEchoDelayItem.setFloatValue(mSettings.echoDelay);
        menu.addItemWithLabel("Delay", mEchoDelayItem);

        root.addPositionRule(menu, Anchor.TOP_CENTER, root, Anchor.TOP_CENTER);

        SoundView view = new SoundView();
        view.setSound(mEngineSound);
        root.addPositionRule(view, Anchor.BOTTOM_LEFT, root, Anchor.BOTTOM_LEFT);
        root.addSizeRule(view, root, 1, SizeRule.IGNORE);
    }

    private void setupEngineLab() {
        mEngineSound = new SynthEngineSound();
    }

    @Override
    public void render(float dt) {
        super.render(dt);
        mSettings.minFrequency = mMinFrequencyItem.getIntValue();
        mSettings.maxFrequency = mMaxFrequencyItem.getIntValue();
        mSettings.waveForm = mWaveFormItem.getData();
        mSettings.gain = mGainItem.getFloatValue();
        mSettings.modulationFrequency = mModFrequencyItem.getIntValue();
        mSettings.modulationStrength = mModStrengthItem.getFloatValue();
        mSettings.echoStrength = mEchoStrengthItem.getFloatValue();
        mSettings.echoDelay = mEchoDelayItem.getFloatValue();
        mSettings.lpfBeta = mLpfBetaItem.getFloatValue();
        mEngineSound.setSettings(mSettings);
        mEngineSound.play(mSpeedItem.getFloatValue());
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public boolean isBackKeyPressed() {
        return false;
    }
}