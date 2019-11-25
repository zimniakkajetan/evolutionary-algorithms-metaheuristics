package sample;

public class MoveBetweenClass {
    private int pointID;
    private int startClassID;
    private int targetClassID;
    private int arcs;
    private double sumOfDistances;
    private double penalties;

    public MoveBetweenClass(int pointID, int startClassID, int targetClassID) {
        this.pointID = pointID;
        this.startClassID = startClassID;
        this.targetClassID = targetClassID;
    }

    public int getPointID() {
        return pointID;
    }

    public int getStartClassID() {
        return startClassID;
    }

    public int getTargetClassID() {
        return targetClassID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MoveBetweenClass move = (MoveBetweenClass) o;
        return move.getTargetClassID() == this.targetClassID
                && move.getStartClassID() == this.startClassID
                && move.getPointID() == this.pointID;
    }

    @Override
    public int hashCode() {
        return this.targetClassID * 1_000_000 + this.startClassID * 10_000 + this.pointID;
    }

    public double getPenalties() {
        return penalties;
    }

    public void updateData(int howManyArcChanged, double change) {
        this.arcs += howManyArcChanged;
        this.sumOfDistances += change;
        this.calculatePenalties();
    }

    private void calculatePenalties() {
        this.penalties = this.sumOfDistances / (this.arcs > 0 ? this.arcs : 1);
    }

    public void setArcs(int arcs) {
        this.arcs = arcs;
        this.calculatePenalties();
    }

    public void setSumOfDistances(double sumOfDistances) {
        this.sumOfDistances = sumOfDistances;
        this.calculatePenalties();
    }
}
