public class EdgeCaseTester {
    public static void main(String[] args) {
        System.out.println("=== TESTY PRZYPADKÓW BRZEGOWYCH ===");

        // Test: Funkcja bez ustawionej funkcji
        System.out.println("\n--- TEST: Brak ustawionej funkcji ---");
        try {
            NumericalIntegration integrator = new RectangularIntegration();
            Range range = new SimpleRange(0, 1);
            double result = integrator.integrate(range, 100);
            System.out.println("Niespodziewanie się udało: " + result);
        } catch (Exception e) {
            System.out.println("Oczekiwany błąd: " + e.getMessage());
        }

        // Test: Zero podprzedziałów
        System.out.println("\n--- TEST: Zero podprzedziałów ---");
        try {
            NumericalIntegration integrator = new RectangularIntegration();
            Function function = new TestFunction();
            integrator.setFunction(function);
            Range range = new SimpleRange(0, 1);
            double result = integrator.integrate(range, 0);
            System.out.println("Niespodziewanie się udało: " + result);
        } catch (Exception e) {
            System.out.println("Oczekiwany błąd: " + e.getMessage());
        }

        // Test: Punkt w obszarze zabronionym
        System.out.println("\n--- TEST: Obszar zabroniony ---");
        try {
            NumericalIntegration integrator = new RectangularIntegration();
            Function function = TestFunction.createWithExclusions();
            integrator.setFunction(function);
            Range range = new SimpleRange(-2.5, -0.5); // obejmuje obszar zabroniony [-2,-1]
            double result = integrator.integrate(range, 100);
            System.out.println("Wynik (powinien pominąć obszar zabroniony): " + result);
        } catch (Exception e) {
            System.out.println("Błąd: " + e.getMessage());
        }
    }
}