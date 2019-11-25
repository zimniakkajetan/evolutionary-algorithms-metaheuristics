package sample;

public class PointCoordinates {
    /**
     * Point's ID
     */
    private int ID;
    /**
     * Point's X location
     */
    private int x;
    /**
     * Point's Y location
     */
    private int y;

    /**
     * @param ID ID
     * @param x  X location
     * @param y  Y location
     */
    public PointCoordinates(int ID, int x, int y) {
        this.ID = ID;
        this.x = x;
        this.y = y;
    }

    /**
     * @return X location
     */
    public int getX() {
        return x;
    }

    /**
     * @return Y location
     */
    public int getY() {
        return y;
    }

    /**
     * @return Point ID
     */
    public int getID() {
        return ID;
    }
}
