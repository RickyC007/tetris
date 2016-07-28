package spypunk.tetris.ui.factory;

import java.awt.Image;

import spypunk.tetris.model.ShapeType;

@FunctionalInterface
public interface BlockImageFactory {

    public Image createBlockImage(ShapeType shapeType);
}