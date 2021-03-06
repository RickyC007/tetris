/*
 * Copyright © 2016 spypunk <spypunk@gmail.com>
 *
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package spypunk.tetris.ui.view;

import static spypunk.tetris.ui.constants.TetrisUIConstants.BLOCK_SIZE;
import static spypunk.tetris.ui.constants.TetrisUIConstants.DEFAULT_BORDER_COLOR;
import static spypunk.tetris.ui.constants.TetrisUIConstants.DEFAULT_FONT_COLOR;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Map;

import javax.swing.ImageIcon;

import com.google.common.collect.Maps;

import spypunk.tetris.constants.TetrisConstants;
import spypunk.tetris.model.Block;
import spypunk.tetris.model.ShapeType;
import spypunk.tetris.model.Tetris;
import spypunk.tetris.model.TetrisInstance;
import spypunk.tetris.model.TetrisInstance.State;
import spypunk.tetris.ui.cache.ImageCache;
import spypunk.tetris.ui.font.FontType;
import spypunk.tetris.ui.font.cache.FontCache;
import spypunk.tetris.ui.util.SwingUtils;

public class TetrisInstanceGridView extends AbstractTetrisInstanceView {

    private static final long serialVersionUID = -3487901883637598196L;

    private static final String PAUSE = "PAUSE".intern();

    private static final Color TETRIS_FROZEN_FG_COLOR = new Color(30, 30, 30, 200);

    private static final String GAME_OVER = "GAME OVER".intern();

    private static final String PRESS_SPACE = "PRESS SPACE".intern();

    private final Rectangle gridRectangle;

    private final Rectangle frozenGridRectangle;

    private final ImageCache imageCache;

    private final Font frozenFont;

    private final int blockX;

    private final int blockY;

    private final Map<Point, Rectangle> blockRectanglesCache = Maps.newHashMap();

    public TetrisInstanceGridView(final FontCache fontCache,
            final ImageCache imageCache,
            final Tetris tetris) {
        this.imageCache = imageCache;
        this.tetris = tetris;

        frozenFont = fontCache.getFont(FontType.FROZEN);

        gridRectangle = new Rectangle(0, 0, TetrisConstants.WIDTH * BLOCK_SIZE + 1,
                TetrisConstants.HEIGHT * BLOCK_SIZE + 1);

        frozenGridRectangle = new Rectangle(gridRectangle.x + 1, gridRectangle.y + 1, gridRectangle.width - 1,
                gridRectangle.height - 1);

        blockX = gridRectangle.x + 1;
        blockY = gridRectangle.y + 1;

        image = new BufferedImage(gridRectangle.width + 1, gridRectangle.height + 1,
                BufferedImage.TYPE_INT_ARGB);

        setIcon(new ImageIcon(image));
        setIgnoreRepaint(true);
    }

    @Override
    public void update() {
        SwingUtils.doInGraphics(image, this::renderBlocks);
        repaint();
    }

    private void renderBlocks(final Graphics2D graphics) {
        final TetrisInstance tetrisInstance = tetris.getTetrisInstance();

        graphics.setColor(DEFAULT_BORDER_COLOR);

        graphics.drawRect(gridRectangle.x, gridRectangle.y, gridRectangle.width,
            gridRectangle.height);

        if (tetrisInstance == null) {
            renderTetrisNew(graphics);
            return;
        }

        tetrisInstance.getBlocks().values().stream()
                .forEach(block -> renderBlock(graphics, block));

        tetrisInstance.getCurrentShape().getBlocks().stream().forEach(block -> renderBlock(graphics, block));

        final State state = tetrisInstance.getState();

        if (!State.RUNNING.equals(state)) {
            renderTetrisFrozen(graphics, state);
        }
    }

    private void renderBlock(final Graphics2D graphics, final Block block) {
        final ShapeType shapeType = block.getShape().getShapeType();
        final Image blockImage = imageCache.getBlockImage(shapeType);
        final Point location = block.getLocation();

        final Rectangle rectangle = getBlockRectangle(location);

        SwingUtils.drawImage(graphics, blockImage, rectangle);
    }

    private Rectangle getBlockRectangle(final Point location) {
        Rectangle rectangle;

        if (!blockRectanglesCache.containsKey(location)) {
            final int x1 = blockX + location.x * BLOCK_SIZE;
            final int y1 = blockY + location.y * BLOCK_SIZE;

            rectangle = new Rectangle(x1, y1, BLOCK_SIZE, BLOCK_SIZE);

            blockRectanglesCache.put(location, rectangle);
        } else {
            rectangle = blockRectanglesCache.get(location);
        }

        return rectangle;
    }

    private void renderTetrisNew(final Graphics2D graphics) {
        SwingUtils.renderCenteredText(graphics, PRESS_SPACE, gridRectangle,
            frozenFont, DEFAULT_FONT_COLOR);
    }

    private void renderTetrisFrozen(final Graphics2D graphics, final State state) {
        graphics.setColor(TETRIS_FROZEN_FG_COLOR);
        graphics.fillRect(frozenGridRectangle.x, frozenGridRectangle.y, frozenGridRectangle.width,
            frozenGridRectangle.height);

        SwingUtils.renderCenteredText(graphics, State.GAME_OVER.equals(state) ? GAME_OVER : PAUSE, gridRectangle,
            frozenFont, DEFAULT_FONT_COLOR);
    }
}
