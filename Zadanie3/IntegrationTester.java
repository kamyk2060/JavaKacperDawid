public class IntegrationTester {
    public static void main(String[] args) {
        System.out.println("=== TEST CAŁKOWANIA NUMERYCZNEGO ===");

        // Test 1: Funkcja bez obszarów zabronionych
        System.out.println("\n--- TEST 1: Bez obszarów zabronionych ---");
        testBasicFunction();

        // Test 2: Funkcja z obszarami zabronionymi
        System.out.println("\n--- TEST 2: Z obszarami zabronionymi ---");
        testFunctionWithExclusions();

        // Test 3: Różna liczba podprzedziałów
        System.out.println("\n--- TEST 3: Wpływ liczby podprzedziałów ---");
        testDifferentSubintervals();

        // Test 4: Niepoprawne dane wejściowe
        System.out.println("\n--- TEST 4: Niepoprawne dane wejściowe ---");
        testInvalidInputs();
    }

    private static void testBasicFunction() {
        try {
            NumericalIntegration integrator = new RectangularIntegration();
            Function function = new TestFunction(); // bez obszarów zabronionych
            Range range = new SimpleRange(0, 3);

            integrator.setFunction(function);
            double result = integrator.integrate(range, 1000);

            System.out.println("Całka od 0 do 3 z f(x) = x² + 2x");
            System.out.println("Wynik numeryczny: " + result);
            System.out.println("Wynik dokładny: 18.0");
            System.out.println("Błąd: " + Math.abs(result - 18.0));

        } catch (Exception e) {
            System.out.println("Błąd: " + e.getMessage());
        }
    }

    private static void testFunctionWithExclusions() {
        try {
            NumericalIntegration integrator = new RectangularIntegration();
            Function function = TestFunction.createWithExclusions(); // z obszarami zabronionymi
            Range range = new SimpleRange(-3, 3);

            integrator.setFunction(function);
            double result = integrator.integrate(range, 1000);

            System.out.println("Całka od -3 do 3 z f(x) = x² + 2x");
            System.out.println("Obszary zabronione: [-2.0, -1.0], [1.5, 2.5]");
            System.out.println("Wynik numeryczny: " + result);
            System.out.println("Uwaga: Wynik pomija obszary zabronione!");

        } catch (Exception e) {
            System.out.println("Błąd: " + e.getMessage());
        }
    }

    private static void testDifferentSubintervals() {
        NumericalIntegration integrator = new RectangularIntegration();
        Function function = new TestFunction();
        Range range = new SimpleRange(0, 2);

        integrator.setFunction(function);

        int[] intervals = {10, 100, 1000, 10000};

        System.out.println("Całka od 0 do 2 z f(x) = x² + 2x");
        System.out.println("Wynik dokładny: 6.666...");
        System.out.println("\nLiczba podprzedziałów | Wynik numeryczny | Błąd");
        System.out.println("---------------------|------------------|------");

        for (int n : intervals) {
            double result = integrator.integrate(range, n);
            double exact = 20.0/3.0; // ≈6.6667
            double error = Math.abs(result - exact);
            System.out.printf("%21d | %16.6f | %.6f\n", n, result, error);
        }
    }

    private static void testInvalidInputs() {
        NumericalIntegration integrator = new RectangularIntegration();
        Function function = new TestFunction();
        Range range = new SimpleRange(0, 2);

        integrator.setFunction(function);

        System.out.println("Testowanie niepoprawnych danych wejściowych:");
        System.out.println("subintervals = 0: " + integrator.integrate(range, 0));
        System.out.println("subintervals = -5: " + integrator.integrate(range, -5));
        System.out.println("subintervals = 100: " + integrator.integrate(range, 100));
    }
}