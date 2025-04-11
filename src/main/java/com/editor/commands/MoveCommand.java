// package com.editor.commands;

// import com.editor.shapes.Shape;

// // MoveCommand.java
// public class MoveCommand implements Command {
// private final Shape shape;
// private final int dx, dy;
// private final int prevX, prevY;

// public MoveCommand(Shape shape, int dx, int dy) {
// this.shape = shape;
// this.dx = dx;
// this.dy = dy;
// this.prevX = shape.getX();
// this.prevY = shape.getY();
// }

// @Override
// public void execute() {
// shape.move(dx, dy);
// }

// @Override
// public void undo() {
// shape.move(prevX - shape.getX(), prevY - shape.getY());
// }
// }
