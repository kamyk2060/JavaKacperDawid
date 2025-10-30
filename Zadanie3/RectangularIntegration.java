class RectangularIntegration implements NumericalIntegration {

    private Function NaszaFunkcja;

    @Override
    public void setFunction(Function f) {
        NaszaFunkcja = f;
    }

    @Override
    public double integrate(Range range, int subintervals) {

        if (subintervals <= 0) {
            return 0.0;
        }

        double xp = range.min();  // początek
        double xk = range.max();  // koniec
        double dx = (xk - xp) / subintervals;  // szerokość
        double sum = 0.0;

        if (xp >= xk) {
            return 0.0;
        }

        for (int i = 1; i <= subintervals; i++) {
            double left = xp + i * dx;
            double right = left + dx;
            double mid = (left + right) / 2.0;  // ŚRODEK

            // zabronionym
            if (!CzyZabroniony(mid)) {
                sum += NaszaFunkcja.apply(mid);
            }
        }

        return sum * dx;
    }

    private boolean CzyZabroniony(double x) {
        for (Range element : NaszaFunkcja.domainExclusions()) {
            if (x >= element.min() && x <= element.max()) {
                return true;
            }
        }
        return false;
    }

}
