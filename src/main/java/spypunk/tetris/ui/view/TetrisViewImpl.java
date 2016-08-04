/*
 * Copyright © 2016 spypunk <spypunk@gmail.com>
 *
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package spypunk.tetris.ui.view;

import static spypunk.tetris.ui.constants.TetrisUIConstants.BLOCK_SIZE;
import static spypunk.tetris.ui.constants.TetrisUIConstants.DEFAULT_CONTAINER_COLOR;
import static spypunk.tetris.ui.constants.TetrisUIConstants.DEFAULT_DIMENSION;
import static spypunk.tetris.ui.constants.TetrisUIConstants.DEFAULT_FONT_COLOR;
import static spypunk.tetris.ui.constants.TetrisUIConstants.DEFAULT_FONT_SIZE;
import static spypunk.tetris.ui.constants.TetrisUIConstants.GAME_OVER;
import static spypunk.tetris.ui.constants.TetrisUIConstants.INFO_CONTAINERS_WIDTH;
import static spypunk.tetris.ui.constants.TetrisUIConstants.INFO_CONTAINERS_X;
import static spypunk.tetris.ui.constants.TetrisUIConstants.LEVEL;
import static spypunk.tetris.ui.constants.TetrisUIConstants.LEVEL_CONTAINER_Y;
import static spypunk.tetris.ui.constants.TetrisUIConstants.NEXT_SHAPE;
import static spypunk.tetris.ui.constants.TetrisUIConstants.NEXT_SHAPE_CONTAINER_HEIGHT;
import static spypunk.tetris.ui.constants.TetrisUIConstants.NEXT_SHAPE_CONTAINER_Y;
import static spypunk.tetris.ui.constants.TetrisUIConstants.PAUSE;
import static spypunk.tetris.ui.constants.TetrisUIConstants.ROWS;
import static spypunk.tetris.ui.constants.TetrisUIConstants.ROWS_CONTAINER_Y;
import static spypunk.tetris.ui.constants.TetrisUIConstants.SCORE;
import static spypunk.tetris.ui.constants.TetrisUIConstants.SCORE_CONTAINER_Y;
import static spypunk.tetris.ui.constants.TetrisUIConstants.STATISTICS;
import static spypunk.tetris.ui.constants.TetrisUIConstants.STATISTICS_CONTAINERS_WIDTH;
import static spypunk.tetris.ui.constants.TetrisUIConstants.STATISTICS_CONTAINERS_X;
import static spypunk.tetris.ui.constants.TetrisUIConstants.STATISTICS_CONTAINER_HEIGHT;
import static spypunk.tetris.ui.constants.TetrisUIConstants.TETRIS_CONTAINERS_X;
import static spypunk.tetris.ui.constants.TetrisUIConstants.TETRIS_CONTAINER_HEIGHT;
import static spypunk.tetris.ui.constants.TetrisUIConstants.TETRIS_CONTAINER_WIDTH;
import static spypunk.tetris.ui.constants.TetrisUIConstants.TETRIS_FROZEN_FG_COLOR;
import static spypunk.tetris.ui.constants.TetrisUIConstants.TETRIS_FROZEN_FONT_SIZE;
import static spypunk.tetris.ui.constants.TetrisUIConstants.TITLE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import spypunk.tetris.model.Block;
import spypunk.tetris.model.Shape;
import spypunk.tetris.model.ShapeType;
import spypunk.tetris.model.Tetris;
import spypunk.tetris.model.Tetris.State;
import spypunk.tetris.ui.controller.TetrisController;
import spypunk.tetris.ui.factory.FontFactory;
import spypunk.tetris.ui.factory.ImageFactory;
import spypunk.tetris.ui.model.Container;
import spypunk.tetris.ui.util.SwingUtils;

@Singleton
public class TetrisViewImpl implements TetrisView {

    private final class TetrisWindowListener extends WindowAdapter {

        @Override
        public void windowClosed(WindowEvent e) {
            tetrisController.onWindowClosed();
        }
    }

    private final class TetrisKeyAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            tetrisController.onKeyPressed(e.getKeyCode());
        }

        @Override
        public void keyReleased(KeyEvent e) {
            tetrisController.onKeyReleased(e.getKeyCode());
        }
    }

    private final FontFactory fontFactory;

    private Font defaultFont;

    private Font frozenTetrisFont;

    private JFrame frame;

    private JLabel label;

    private BufferedImage image;

    private Container statisticsContainer;

    private Container tetrisContainer;

    private Container nextShapeContainer;

    private Container levelContainer;

    private Container scoreContainer;

    private Container rowsContainer;

    @Inject
    private TetrisController tetrisController;

    @Inject
    private ImageFactory imageFactory;

    @Inject
    public TetrisViewImpl(FontFactory fontFactory) {
        this.fontFactory = fontFactory;

        initializeFonts();
        initializeContainers();
        initializeFrame();
    }

    @Override
    public void setVisible(boolean visible) {
        SwingUtils.doInAWTThread(() -> frame.setVisible(visible), true);
    }

    @Override
    public void update(Tetris tetris) {
        SwingUtils.doInAWTThread(() -> doUpdate(tetris), true);
    }

    private void initializeFonts() {
        defaultFont = fontFactory.createDefaultFont(DEFAULT_FONT_SIZE);
        frozenTetrisFont = fontFactory.createDefaultFont(TETRIS_FROZEN_FONT_SIZE);
    }

    private void initializeContainers() {
        tetrisContainer = initializeContainer(TETRIS_CONTAINERS_X, BLOCK_SIZE,
            TETRIS_CONTAINER_WIDTH,
            TETRIS_CONTAINER_HEIGHT);
        nextShapeContainer = initializeContainer(INFO_CONTAINERS_X, NEXT_SHAPE_CONTAINER_Y, INFO_CONTAINERS_WIDTH,
            NEXT_SHAPE_CONTAINER_HEIGHT,
            NEXT_SHAPE);
        levelContainer = initializeContainer(INFO_CONTAINERS_X, LEVEL_CONTAINER_Y, INFO_CONTAINERS_WIDTH, BLOCK_SIZE,
            LEVEL);
        scoreContainer = initializeContainer(INFO_CONTAINERS_X, SCORE_CONTAINER_Y, INFO_CONTAINERS_WIDTH, BLOCK_SIZE,
            SCORE);
        rowsContainer = initializeContainer(INFO_CONTAINERS_X, ROWS_CONTAINER_Y, INFO_CONTAINERS_WIDTH, BLOCK_SIZE,
            ROWS);
        statisticsContainer = initializeContainer(STATISTICS_CONTAINERS_X, 2 * BLOCK_SIZE, STATISTICS_CONTAINERS_WIDTH,
            STATISTICS_CONTAINER_HEIGHT,
            STATISTICS);
    }

    private void initializeFrame() {
        frame = new JFrame(TITLE);

        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setFocusable(false);
        frame.getContentPane().setLayout(new BorderLayout(0, 0));
        frame.setResizable(false);
        frame.addWindowListener(new TetrisWindowListener());

        image = new BufferedImage(DEFAULT_DIMENSION.width, DEFAULT_DIMENSION.height, BufferedImage.TYPE_INT_ARGB);

        label = new JLabel(new ImageIcon(image));
        label.setFocusable(true);
        label.addKeyListener(new TetrisKeyAdapter());

        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.pack();

        frame.setLocationRelativeTo(null);
    }

    private void doUpdate(Tetris tetris) {
        final Graphics2D graphics = initializeGraphics();

        renderBlocks(tetris, graphics);
        renderLevel(tetris, graphics);
        renderScore(tetris, graphics);
        renderRows(tetris, graphics);
        renderNextShape(tetris, graphics);
        renderStatistics(tetris, graphics);

        graphics.dispose();

        label.repaint();
    }

    private Graphics2D initializeGraphics() {
        final Graphics2D graphics = image.createGraphics();

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, DEFAULT_DIMENSION.width, DEFAULT_DIMENSION.height);
        return graphics;
    }

    private void renderBlocks(Tetris tetris, Graphics2D graphics) {
        renderContainer(graphics, tetrisContainer);

        final Rectangle rectangle = tetrisContainer.getRectangle();

        tetris.getBlocks().values().stream().filter(Optional::isPresent)
                .map(Optional::get)
                .filter(block -> block.getLocation().y >= 2)
                .forEach(block -> renderBlock(graphics, block, rectangle.x + 1,
                    rectangle.y - 2 * BLOCK_SIZE + 1, BLOCK_SIZE));

        final State state = tetris.getState();

        if (!State.RUNNING.equals(state)) {
            renderTetrisFrozen(graphics, tetrisContainer, state);
        }
    }

    private void renderLevel(Tetris tetris, Graphics2D graphics) {
        renderInfo(graphics, levelContainer, String.valueOf(tetris.getLevel()));
    }

    private void renderScore(Tetris tetris, Graphics2D graphics) {
        renderInfo(graphics, scoreContainer, String.valueOf(tetris.getScore()));
    }

    private void renderRows(Tetris tetris, Graphics2D graphics) {
        renderInfo(graphics, rowsContainer, String.valueOf(tetris.getCompletedRows()));
    }

    private void renderNextShape(Tetris tetris, Graphics2D graphics) {
        final Shape nextShape = tetris.getNextShape();

        renderContainer(graphics, nextShapeContainer);

        final Image shapeImage = imageFactory.createShapeImage(nextShape.getShapeType());

        final int imageWidth = shapeImage.getWidth(null);
        final int imageHeight = shapeImage.getHeight(null);

        final Rectangle containerRectangle = nextShapeContainer.getRectangle();
        final int width = containerRectangle.width;
        final int height = containerRectangle.height;

        final int x1 = containerRectangle.x + (width - imageWidth) / 2;
        final int y1 = containerRectangle.y + (height - imageHeight) / 2;
        final int x2 = x1 + imageWidth;
        final int y2 = y1 + imageHeight;

        graphics.drawImage(shapeImage, x1, y1, x2, y2, 0, 0, imageWidth, imageHeight,
            null);
    }

    private void renderStatistics(Tetris tetris, Graphics2D graphics) {
        renderContainer(graphics, statisticsContainer);

        // TODO render statistics
    }

    private void renderBlock(Graphics2D graphics, Block block, int dx, int dy, int blockSize) {
        final ShapeType shapeType = block.getShape().getShapeType();

        final Image blockImage = imageFactory.createBlockImage(shapeType);
        final int dx1 = dx + block.getLocation().x * blockSize;
        final int dx2 = dx1 + blockSize;
        final int dy1 = dy + block.getLocation().y * blockSize;
        final int dy2 = dy1 + blockSize;

        graphics.drawImage(blockImage, dx1, dy1, dx2, dy2, 0, 0, blockImage.getWidth(null), blockImage.getHeight(null),
            null);
    }

    private void renderInfo(Graphics2D graphics, Container container, String info) {
        renderContainer(graphics, container);
        renderTextCentered(graphics, info, container.getRectangle(), defaultFont);
    }

    private void renderContainer(Graphics2D graphics, Container container) {
        graphics.setColor(DEFAULT_CONTAINER_COLOR);

        final Rectangle rectangle = container.getRectangle();

        graphics.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);

        final String title = container.getTitle();

        if (title != null) {
            renderTextCentered(graphics, title,
                new Rectangle(rectangle.x, rectangle.y - BLOCK_SIZE, rectangle.width, BLOCK_SIZE), defaultFont);
        }
    }

    private void renderTextCentered(Graphics2D graphics, String text, Rectangle rectangle, Font font) {
        graphics.setColor(DEFAULT_FONT_COLOR);
        graphics.setFont(font);

        final Point location = SwingUtils.getCenteredTextLocation(graphics, text, rectangle);

        graphics.drawString(text, location.x, location.y);
    }

    private void renderTetrisFrozen(Graphics2D graphics, Container container, State state) {
        final Rectangle rectangle = container.getRectangle();

        graphics.setColor(TETRIS_FROZEN_FG_COLOR);
        graphics.fillRect(rectangle.x + 1, rectangle.y + 1, rectangle.width - 1, rectangle.height - 1);

        renderTextCentered(graphics, State.GAME_OVER.equals(state) ? GAME_OVER : PAUSE, rectangle,
            frozenTetrisFont);
    }

    private Container initializeContainer(int x, int y, int width, int height, String title) {
        return Container.Builder.instance()
                .setRectangle(new Rectangle(x, y, width, height))
                .setTitle(title)
                .build();
    }

    private Container initializeContainer(int x, int y, int width, int height) {
        return initializeContainer(x, y, width, height, null);
    }
}
