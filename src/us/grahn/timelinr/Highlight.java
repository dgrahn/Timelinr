package us.grahn.timelinr;

import java.awt.Color;

class Highlight {

    private int col = -1;
    private String type = null;
    private Color color = null;

    public Highlight() {
    }

    public Color getColor() {
        return color;
    }

    public int getCol() {
        return col;
    }

    public String getType() {
        return type;
    }

    public void setColor(final String color) {

        try {
            this.color = Color.decode(color);
        } catch (final NumberFormatException nfe) {
            try {
                this.color = (Color) Color.class.getField(color).get(null);
            } catch (final Exception ce) {
                this.color = Color.BLACK;
            }
        }
    }

    public void setType(final String type) {
        this.type = type;
    }

    public void setCol(final int col) {
        this.col = col;
    }

    @Override
    public String toString() {
        return String.format("%d %s %s", col, type, color.toString());
    }

}