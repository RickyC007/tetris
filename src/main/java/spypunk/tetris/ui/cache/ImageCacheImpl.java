/*
 * Copyright © 2016 spypunk <spypunk@gmail.com>
 *
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package spypunk.tetris.ui.cache;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import spypunk.tetris.exception.TetrisException;
import spypunk.tetris.model.ShapeType;

@Singleton
public class ImageCacheImpl implements ImageCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageCacheImpl.class);

    private static final String BLOCKS_FOLDER = "/img/blocks/";

    private static final String SHAPES_FOLDER = "/img/shapes/";

    private final Map<ShapeType, Image> blockImages = createBlockImages();

    private final Map<ShapeType, Image> shapeImages = createShapeImages();

    @Override
    public Image getBlockImage(ShapeType shapeType) {
        return blockImages.get(shapeType);
    }

    @Override
    public Image getShapeImage(ShapeType shapeType) {
        return shapeImages.get(shapeType);
    }

    private static Image createImage(String imageFolder, ShapeType shapeType) {
        final String resourceName = String.format("%s%s.png", imageFolder, shapeType.name());

        try (InputStream inputStream = ImageCacheImpl.class.getResourceAsStream(resourceName)) {
            return ImageIO.read(inputStream);
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new TetrisException(e);
        }
    }

    private static Map<ShapeType, Image> createShapeImages() {
        return Lists.newArrayList(ShapeType.values()).stream().collect(Collectors.toMap(Function.identity(),
            shapeType -> createImage(SHAPES_FOLDER, shapeType)));
    }

    private static Map<ShapeType, Image> createBlockImages() {
        return Lists.newArrayList(ShapeType.values()).stream().collect(Collectors.toMap(Function.identity(),
            shapeType -> createImage(BLOCKS_FOLDER, shapeType)));
    }
}
