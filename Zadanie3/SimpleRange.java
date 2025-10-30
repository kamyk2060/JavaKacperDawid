public class SimpleRange implements Range {
    private final double min;
    private final double max;

    public SimpleRange(double min, double max) {
        if (min >= max) {
            throw new IllegalArgumentException("min must be less than max");
        }
        this.min = min;
        this.max = max;
    }

    @Override
    public double min() {
        return min;
    }

    @Override
    public double max() {
        return max;
    }

    @Override
    public String toString() {
        return "[" + min + ", " + max + "]";
    }
}