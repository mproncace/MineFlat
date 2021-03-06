/*
 * Voxem
 * Copyright (c) 2014-2015, Maxim Roncacé <caseif@caseif.net>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.caseif.voxem;

import static org.lwjgl.opengl.GL11.GL_ALPHA_TEST;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_ARRAY;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_GREATER;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_COORD_ARRAY;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_VERSION;
import static org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL11.glAlphaFunc;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnableClientState;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glGetString;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

import net.caseif.voxem.entity.Entity;
import net.caseif.voxem.entity.living.player.Player;
import net.caseif.voxem.gui.GuiElement;
import net.caseif.voxem.gui.GuiFactory;
import net.caseif.voxem.gui.TextElement;
import net.caseif.voxem.util.BufferUtil;
import net.caseif.voxem.util.ImageUtil;
import net.caseif.voxem.util.NumUtil;
import net.caseif.voxem.util.Timing;
import net.caseif.voxem.util.VboUtil;
import net.caseif.voxem.vector.Vector2i;
import net.caseif.voxem.vector.Vector3f;
import net.caseif.voxem.world.Block;
import net.caseif.voxem.world.Chunk;
import net.caseif.voxem.world.Location;
import net.caseif.voxem.world.TickManager;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Random;

import javax.imageio.ImageIO;

//TODO: organize and possibly divide this class
public class GraphicsHandler implements Runnable {

    /**
     * The minimum OpenGL version required to run the game
     */
    public static final double MINIMUM_GL_VERSION = 1.5;

    /**
     * The system's OpenGL version
     */
    public static double glVersion;

    // fps-related stuff
    public static float renderDelta = 0f;
    public static long lastRenderTime = Timing.getTime();
    public static int fps = 0;
    public static long lastFpsUpdate = 0;
    public static final long fpsUpdateTime = (long) (0.25 * Timing.TIME_RESOLUTION);

    //public static int texSize = 16;

    /**
     * The number of horizontal pixels visual elements will be shifted before being rendered
     * (- is left; + is right)
     */
    public static int xOffset = 0;

    /**
     * The number of vertical pixels visual elements will be shifted before being rendered
     * (- is up; + is down)
     */
    public static int yOffset = 150;

    /**
     * The number of chunks adjacent to the player's that should be generated/loaded
     */
    public static int renderDistance = 6;

    private static final float charWHRatio = 3f / 4f;

    // space between characters when height is 32px
    private static final float interCharSpace = 0.1f;

    // offset of character shadows (duh)
    private static final float shadowOffset = 1;

    public static final HashMap<Character, Float> specialChars = new HashMap<>();

    public static boolean TEXTURES_READY = false;

    public static int[][] borderLines = new int[Block.length / 2][6];
    public static float borderColor = 0f;
    public static final int BORDER_COLOR_CHANGE_SPEED = 750;
    public static final int BORDER_LINE_MIN_SPEED = 400;
    public static final int BORDER_LINE_MAX_SPEED = 550;
    public static final int BORDER_LINE_SIZE_DIVIDER = 8;
    public static boolean borderColorIncreasing = true;

    public static String timeOfDay = "DAY";

    private static Vector3f filterColor = new Vector3f(1f, 0f, 0f);
    private static final float FILTER_OPACITY = 0.3f;
    private static final float FILTER_STEP = 0.05f;

    public void run() {

        initDisplay();
        configureGLContext();
        GuiFactory.construct();

        //initializeFont();

        try {
            initializeChars();
        } catch (Exception ex) {
            System.err.println("Exception occurred while preparing texture for characters");
            ex.printStackTrace();
            System.exit(1);
        }
        Entity.initialize();

        VboUtil.initialize();
        VboUtil.prepareBindArray();

        initWorldBorders();

        while (!Main.closed && !Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            updateDeltas();
            float skyBrightness = getSkyBrightness();

            glClearColor(
                    0.3f * Math.max(Player.light * skyBrightness, TickManager.MIN_SKY_BRIGHTNESS),
                    0.3f * Math.max(Player.light * skyBrightness, TickManager.MIN_SKY_BRIGHTNESS),
                    0.8f * Math.max(Player.light * skyBrightness, TickManager.MIN_SKY_BRIGHTNESS),
                    1f);

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            InputManager.pollInput();

            if (VboUtil.rebindArray)
                VboUtil.bindArray();
            VboUtil.render();

            renderSelectedBlock();
            drawWorldBorders();

            if (Main.player != null) {
                Main.player.center();
                for (Entity e : Main.player.getLevel().getEntities()) {
                    if (!e.isRemoved()) {
                        e.draw();
                    }
                }
            }

            updateDebugUI();

            // draw GUIs
            for (GuiElement gui : GuiFactory.guis.values())
                gui.draw();

            if (Main.dank >= 0) {
                drawMemeScreen();
            }

			/*if(Console.enabled)
                Console.draw();*/

            //Display.sync(60); // already have vsync enabled
            Display.update();
        }
        cleanup();
    }

    public static void initDisplay() {
        try {
            for (DisplayMode mode : Display.getAvailableDisplayModes()) {
                if (mode.getWidth() == Display.getDesktopDisplayMode().getWidth() &&
                        mode.getHeight() == Display.getDesktopDisplayMode()
                                .getHeight() && mode.isFullscreenCapable()) {
                    Display.setDisplayMode(mode);
                    Display.setLocation(Display.getX() - 3, Display.getY());
                    break;
                }
            }
            Display.setVSyncEnabled(true);
            Display.setTitle("Voxem");
            Display.setResizable(false);
            ByteBuffer[] icons;
            String iconPath = "/textures/block/grass.png";
            if (System.getProperty("os.name").startsWith("Windows")) {
                icons = new ByteBuffer[2];
                BufferedImage icon1 = ImageUtil.scaleImage(
                        ImageIO.read(Main.class.getResourceAsStream(iconPath)),
                        Block.length, Block.length);
                BufferedImage icon2 = ImageUtil.scaleImage(ImageIO.read(
                        Main.class.getResourceAsStream(iconPath)), 32, 32);
                icons[0] = BufferUtil.asByteBuffer(icon1);
                icons[1] = BufferUtil.asByteBuffer(icon2);
            } else if (System.getProperty("os.name").startsWith("Mac")) {
                icons = new ByteBuffer[1];
                BufferedImage icon = ImageUtil.scaleImage(ImageIO.read(
                                Main.class.getResourceAsStream(iconPath)),
                        Main.world.getChunkHeight(),
                        Main.world.getChunkHeight());
                icons[0] = BufferUtil.asByteBuffer(icon);
            } else {
                icons = new ByteBuffer[1];
                BufferedImage icon = ImageUtil.scaleImage(ImageIO.read(
                        Main.class.getResourceAsStream(iconPath)), 32, 32);
                icons[0] = BufferUtil.asByteBuffer(icon);
            }
            Display.setIcon(icons);
            Display.create();
            glVersion = Double.parseDouble(glGetString(GL_VERSION).substring(0, 3));
            if (glVersion < MINIMUM_GL_VERSION) {
                System.err.println("Minimum required OpenGL version is " +
                        MINIMUM_GL_VERSION + "; " +
                        "current version is " + glVersion);
                Display.destroy();
                System.exit(0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void configureGLContext() {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, Display.getWidth(), Display.getHeight(), 0, 1, -1);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glEnable(GL_TEXTURE_2D);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glClearColor(0.3f, 0.3f, 0.8f, 1f);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glEnable(GL_ALPHA_TEST);
        glAlphaFunc(GL_GREATER, 0.1f);

        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);

        for (Material m : Material.values())
            Texture.addTexture(m);
        TEXTURES_READY = true;
    }

    public static void initWorldBorders() {
        Random r = new Random();
        for (int i = 0; i < Block.length / 2; i++) {
            borderLines[i] = new int[]{
                    // first line
                    r.nextInt(Display.getHeight()), r.nextInt(Display.getHeight() / BORDER_LINE_SIZE_DIVIDER) +
                    Display.getHeight() / BORDER_LINE_SIZE_DIVIDER,
                    r.nextInt(BORDER_LINE_MAX_SPEED - BORDER_LINE_MIN_SPEED) + BORDER_LINE_MIN_SPEED,
                    // second line
                    r.nextInt(Display.getHeight()), r.nextInt(Display.getHeight() / BORDER_LINE_SIZE_DIVIDER) +
                    Display.getHeight() / BORDER_LINE_SIZE_DIVIDER,
                    r.nextInt(BORDER_LINE_MAX_SPEED - BORDER_LINE_MIN_SPEED) + BORDER_LINE_MIN_SPEED};
        }
    }

    public static void initializeChars() {
        InputStream is = NumUtil.class.getClassLoader().getResourceAsStream(
                "textures/chars.png");
        //InputStream newIs = ImageUtil.asInputStream(ImageUtil.scaleImage(
        //		ImageIO.read(is), MineFlat.world.getChunkLength(),
        //		MineFlat.world.getChunkLength())); // in case I decide to resize it later on

        Main.charTexture = ImageUtil.createTextureFromStream(is);

        specialChars.put('!', 0f);
        specialChars.put('?', 1f);
        specialChars.put('.', 2f);
        specialChars.put(',', 3f);
        specialChars.put(':', 4f);
        specialChars.put('-', 5f);
        specialChars.put('+', 6f);
        specialChars.put('(', 7f);
        specialChars.put(')', 8f);
    }

    public static void updateDeltas() {
        if (Timing.getTime() - lastFpsUpdate >= fpsUpdateTime) {
            fps = (int) Math.floor(Timing.TIME_RESOLUTION / renderDelta);
            Timing.displayDelta = (int) Timing.delta;
            lastFpsUpdate = Timing.getTime();
        }

        renderDelta = Timing.getTime() - lastRenderTime;
        lastRenderTime = Timing.getTime();
    }

    public static float getSkyBrightness() {
        float timeBrightness = 1f;
        if (TickManager.getTicks() >= TickManager.DAWN_END && TickManager.getTicks() <= TickManager.DUSK_BEGIN) {
            timeBrightness = 1f;
            timeOfDay = "DAY";
        } else if (TickManager.getTicks() > TickManager.DUSK_END && TickManager.getTicks() < TickManager.DAWN_BEGIN) {
            timeBrightness = 0;
            timeOfDay = "NIGHT";
        } else {
            if (TickManager.getTicks() > TickManager.DUSK_BEGIN && TickManager.getTicks() < TickManager.DUSK_END) {
                timeBrightness = (TickManager.DUSK_END - TickManager.getTicks()) /
                        (float) (TickManager.DUSK_END - TickManager.DUSK_BEGIN);
                timeOfDay = "DUSK";
            } else if (TickManager.getTicks() / 12000 == 0 && TickManager.getTicks() <= TickManager.DAWN_END) {
                timeBrightness = 1 - (TickManager.DAWN_END - TickManager.getTicks()) /
                        (float) Math.abs(TickManager.DAWN_END - TickManager.DAWN_BEGIN + 24000);
                timeOfDay = "DAWN";
            } else if (TickManager.getTicks() / 12000 == 1 && TickManager.getTicks() >= TickManager.DAWN_BEGIN) {
                timeBrightness = 1 - (TickManager.DAWN_END - TickManager.getTicks() + 24000) /
                        (float) Math.abs(TickManager.DAWN_END - TickManager.DAWN_BEGIN + 24000);
                timeOfDay = "DAWN";
            }
        }
        return timeBrightness;
    }

    public static void renderSelectedBlock() {
        synchronized (Main.lock) {
            if (Block.selected != null) {
                Block.selectedX = Block.selected.getPixelX() + xOffset;
                Block.selectedY = Block.selected.getPixelY() + yOffset;
                Block.isSelected = true;
            } else
                Block.isSelected = false;
            if (Block.isSelected && Block.selected != null) {
                if (Block.selected.getBlock().getLightLevel() < 0.5f)
                    glColor3f(1f, 1f, 1f);
                else
                    glColor3f(0f, 0f, 0f);
                glColor3f(0.5f, 0.5f, 0.5f);
                glBegin(GL_LINES);
                glVertex2f(Block.selectedX, Block.selectedY);
                glVertex2f(Block.selectedX + Block.length, Block.selectedY);
                glVertex2f(Block.selectedX + Block.length, Block.selectedY);
                glVertex2f(Block.selectedX + Block.length, Block.selectedY + Block.length);
                glVertex2f(Block.selectedX + Block.length, Block.selectedY + Block.length);
                glVertex2f(Block.selectedX, Block.selectedY + Block.length);
                glVertex2f(Block.selectedX, Block.selectedY + Block.length);
                glVertex2f(Block.selectedX, Block.selectedY);
                if (Block.selected != null && Block.selected.getY() < 0 ||
                        (Block.selected.getY() < Block.selected.getLevel().getWorld().getChunkHeight() &&
                                Block.isAir(Block.selected.getLevel().getBlock(Block.selected.getX(),
                                        Block.selected.getY() - 1)))) {
                    glVertex2f(Block.selectedX, Block.selectedY);
                    glVertex2f(Block.selectedX, Block.selectedY - Block.length / Block.horAngle);
                    glVertex2f(Block.selectedX, Block.selectedY - Block.length / Block.horAngle);
                    glVertex2f(Block.selectedX + Block.length, Block.selectedY - Block.length / Block.horAngle);
                    glVertex2f(Block.selectedX + Block.length, Block.selectedY - Block.length / Block.horAngle);
                    glVertex2f(Block.selectedX + Block.length, Block.selectedY);
                }
                glEnd();
            }
        }
    }

    public static void drawWorldBorders() {
        // draw world borders
        if (Main.world != null) {
            int minChunk = -Main.world.getChunkCount() / 2;
            int maxChunk = Main.world.getChunkCount() / 2;
            if (borderColor >= 1f)
                borderColorIncreasing = false;
            else if (borderColor <= 0f)
                borderColorIncreasing = true;
            if (borderColorIncreasing)
                borderColor += Timing.displayDelta / (Timing.TIME_RESOLUTION / 1000L) /
                        (float) BORDER_COLOR_CHANGE_SPEED;
            else
                borderColor -= Timing.displayDelta / (Timing.TIME_RESOLUTION / 1000L) /
                        (float) BORDER_COLOR_CHANGE_SPEED;
            glColor3f(0f, borderColor * 0.3f + 0.7f, 1f);
            for (int j = 0; j <= 1; j++) {
                if ((j == 0 && Main.player.getLevel().getChunk(minChunk) != null) ||
                        (j == 1 && Main.player.getLevel().getChunk(maxChunk) != null)) {
                    synchronized (j == 0 ?
                            Main.player.getLevel().getChunk(minChunk) :
                            Main.player.getLevel().getChunk(maxChunk)) {
                        int startPixel = new Location(Main.player.getLevel(),
                                Chunk.getWorldXFromChunkIndex(
                                        Main.player.getLevel().getChunk(j == 0 ? minChunk : maxChunk).getIndex(),
                                        j == 0 ? 0 : Main.player.getLevel().getWorld().getChunkLength() - 1), 0
                        ).add(j == 0 ? -1 : 1, 0).getPixelX() + xOffset;
                        glBegin(GL_LINES);
                        {
                            for (int i = 0; i < Block.length; i += 2) {
                                int[] lineInfo = borderLines[i / 2];
                                // line 1
                                int y1 = lineInfo[0];
                                int length1 = lineInfo[1];
                                int speed1 = lineInfo[2];
                                glVertex2f(startPixel + i, y1);
                                glVertex2f(startPixel + i, y1 + length1);
                                if (y1 + length1 <= 0) {
                                    Random r = new Random();
                                    borderLines[i / 2][0] = Display.getHeight() + length1;
                                    borderLines[i / 2][1] =
                                            r.nextInt(Display.getHeight() / BORDER_LINE_SIZE_DIVIDER +
                                                    Display.getHeight() / BORDER_LINE_SIZE_DIVIDER);
                                    borderLines[i / 2][2] =
                                            r.nextInt(BORDER_LINE_MAX_SPEED - BORDER_LINE_MIN_SPEED) +
                                                    BORDER_LINE_MIN_SPEED;
                                } else
                                    borderLines[i / 2][0] =
                                            (int) (y1 - Timing.displayDelta / Timing.TIME_RESOLUTION * speed1);

                                // line 2
                                int y2 = lineInfo[3];
                                int length2 = lineInfo[4];
                                int speed2 = lineInfo[5];
                                glVertex2f(startPixel + i, y2);
                                glVertex2f(startPixel + i, y2 + length2);
                                if (y2 + length2 <= 0) {
                                    Random r = new Random();
                                    borderLines[i / 2][3] = Display.getHeight() + length2;
                                    borderLines[i / 2][4] =
                                            r.nextInt(Display.getHeight() / BORDER_LINE_SIZE_DIVIDER +
                                                    Display.getHeight() / BORDER_LINE_SIZE_DIVIDER);
                                    borderLines[i / 2][5] =
                                            r.nextInt(BORDER_LINE_MAX_SPEED - BORDER_LINE_MIN_SPEED) +
                                                    BORDER_LINE_MIN_SPEED;
                                } else
                                    borderLines[i / 2][3] =
                                            (int) (y2 - Timing.displayDelta / Timing.TIME_RESOLUTION * speed2);
                            }
                        }
                        glEnd();
                    }
                }
            }
        }
    }

    public static void drawMemeScreen() {
        glColor4f(filterColor.getX(), filterColor.getY(), filterColor.getZ(), FILTER_OPACITY);
        glBegin(GL_QUADS);
        glVertex2f(0f, 0f);
        glVertex2f(Display.getWidth(), 0f);
        glVertex2f(Display.getWidth(), Display.getHeight());
        glVertex2f(0f, Display.getHeight());
        glEnd();

        // update the filter color
        switch (Main.dank) {
            case 0:
                // add green (transition to orange)
                filterColor.setY(filterColor.getY() + FILTER_STEP);
                if (filterColor.getY() >= 1f) {
                    Main.dank++;
                }
                break;
            case 1:
                // subtract red (transition to green)
                filterColor.setX(filterColor.getX() - FILTER_STEP);
                if (filterColor.getX() <= 0f) {
                    Main.dank++;
                }
                break;
            case 2:
                // add blue (transition to aqua)
                filterColor.setZ(filterColor.getZ() + FILTER_STEP);
                if (filterColor.getZ() >= 1f) {
                    Main.dank++;
                }
                break;
            case 3:
                // subtract green (transition to blue)
                filterColor.setY(filterColor.getY() - FILTER_STEP);
                if (filterColor.getY() <= 0f) {
                    Main.dank++;
                }
                break;
            case 4:
                // add red (transition to purple)
                filterColor.setX(filterColor.getX() + FILTER_STEP);
                if (filterColor.getX() >= 1f) {
                    Main.dank++;
                }
                break;
            case 5:
                // subtract blue (transition to red)
                filterColor.setZ(filterColor.getZ() - FILTER_STEP);
                if (filterColor.getZ() <= 0f) {
                    Main.dank = 0;
                }
                break;
        }
    }

    //TODO: idk why this is in the graphics thread, I might move it at some point
    public static void updateDebugUI() {
        if (GuiFactory.guis.get("debug").isActive()) {
            GuiElement debugPanel = GuiFactory.guis.get("debug");
            ((TextElement) debugPanel.getChild("fps"))
                    .setText("fps: " + fps);
            ((TextElement) debugPanel.getChild("delta"))
                    .setText("delta (ms): " + String.format("%.3f", Timing.displayDelta / 1000000));
            ((TextElement) debugPanel.getChild("playerX"))
                    .setText("x: " + (Main.player == null ? "???" : String.format("%.3f", Main.player.getX())));
            ((TextElement) debugPanel.getChild("playerY"))
                    .setText("y: " + (Main.player == null ? "???" : String.format("%.3f", Main.player.getY())));
            ((TextElement) debugPanel.getChild("playerChunk"))
                    .setText("chunk: " + (Main.player == null ? "???" : Main.player.getLocation().getChunk()));
            ((TextElement) debugPanel.getChild("playerG"))
                    .setText("g: " + (Main.player == null ? "???" : Main.player.isOnGround()));
            ((TextElement) debugPanel.getChild("playerLight"))
                    .setText("light level: " +
                            (Main.player == null ? "???" : String.format("%.3f", Player.light * Block.maxLight)));
            ((TextElement) debugPanel.getChild("ticks"))
                    .setText("ticks: " + TickManager.getTicks() + " (" + timeOfDay.toLowerCase() + ")");
            int mb = 1024 * 1024;
            Runtime runtime = Runtime.getRuntime();
            ((TextElement) debugPanel.getChild("memory")).setText(runtime.totalMemory() / mb +
                    "mb allocated memory: " + (runtime.totalMemory() - runtime.freeMemory()) / mb + "mb used, " +
                    runtime.freeMemory() / mb + "mb free");
            int maxSize = 0;
            for (GuiElement ge : debugPanel.getChildren()) {
                if (ge instanceof TextElement) {
                    int size = getStringLength(((TextElement) ge).getText(), 16);
                    if (size > maxSize)
                        maxSize = size;
                }
            }
            debugPanel.setSize(new Vector2i(maxSize + 20, debugPanel.getSize().getY()));
        }
    }

    public static void cleanup() {
        SoundManager.soundSystem.cleanup();
        Main.closed = true;
    }

    public static void drawString(String str, float x, float y, float height, boolean shadow) {
        float wm = 42f + 2f / 3f;
        float hm = 4f;
        float width = height * charWHRatio;
        glPushMatrix();
        glBindTexture(GL_TEXTURE_2D, Main.charTexture);
        for (int i = 0; i <= (shadow ? 1 : 0); i++) {
            if (i == 0 && shadow) {
                glColor3f(0f, 0f, 0f);
                x -= shadowOffset * Math.max(1, height / 16);
                y -= shadowOffset * Math.max(1, height / 16);
            } else if (i == 1) {
                glColor3f(1f, 1f, 1f);
                x += shadowOffset * Math.max(1, height / 16);
                y += shadowOffset * Math.max(1, height / 16);
            } else
                glColor3f(1f, 1f, 1f);
            glBegin(GL_QUADS);
            {
                float pos = 0f;
                for (char c : str.toCharArray()) {
                    float tx, ty;
                    if (Character.isLetter(c)) {
                        if (Character.isUpperCase(c)) {
                            tx = c - 'A';
                            ty = 0;
                        } else {
                            tx = c - 'a';
                            ty = 0; //TODO: temporary until I remember to add lowercase characters to the image
                        }
                    } else if (NumUtil.isInt(Character.toString(c))) {
                        tx = Float.parseFloat((Character.toString(c)));
                        ty = 2f;
                    } else if (c == ' ') {
                        pos += 1;
                        continue;
                    } else {
                        ty = 3f;
                        if (specialChars.containsKey(c))
                            tx = specialChars.get(c);
                        else
                            tx = 25f;
                    }
                    glTexCoord2f(tx / wm, (ty / hm));
                    glVertex2f(x + pos * width, y);
                    glTexCoord2f((tx + 1f) / wm, (ty / hm));
                    glVertex2f(x + pos * width + width, y);
                    glTexCoord2f((tx + 1f) / wm, (ty + 1f) / hm);
                    glVertex2f(x + pos * width + width, y + height);
                    glTexCoord2f(tx / wm, (ty + 1f) / hm);
                    glVertex2f(x + pos * width, y + height);
                    if (c != '.' && c != ',' && c != ':')
                        pos += 1f + (height / 32f) * interCharSpace;
                    else
                        pos += (1f / 6f) + interCharSpace;
                }
            }
        }
        glEnd();
        glBindTexture(GL_TEXTURE_2D, 0);
        glPopMatrix();
    }

    public static int getStringLength(String str, float height) {
        float width = height * charWHRatio;
        float pos = 0f;
        for (char c : str.toCharArray()) {
            if (c == ' ') {
                pos += 1;
                continue;
            }
            if (c != '.' && c != ',' && c != ':')
                pos += 1f + (height / 32f) * interCharSpace;
            else
                pos += (1f / 6f) + interCharSpace;
        }
        return (int) Math.ceil(pos * width);
    }

}
