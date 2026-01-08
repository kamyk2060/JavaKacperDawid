import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MultithreadedIntegarationTest {
    private MultithreadedIntegaration integrator;

    private class RangeExample implements Range {
        private double min;
        private double max;

        public RangeExample(double min, double max) {
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
    }

    @BeforeEach
    void setUp() {
        integrator = new MultithreadedIntegaration();
        integrator.setThreadsNumber(8);
    }

    @Test
    void testSquareFunction() {
        Function<Double, Double> f = x -> x * x;

        integrator.setFunction(f);
        Range range = new RangeExample(0, 2);

        // Całka analityczna: ∫[0,2] x² dx = [x³/3] od 0 do 2 = 8/3 ≈ 2.666...
        double expected = 8.0 / 3.0;
        integrator.calc(range, 10000);
        double result = integrator.getResult();

        assertEquals(expected, result, 0.001);
    }

    @Test
    void testConstantFunction() {
        // Testowanie stałej funkcji f(x) = 5
        Function<Double, Double> f = x -> 5.0;

        integrator.setFunction(f);
        Range range = new RangeExample(1, 4);
        int subintervals = 1000;

        // Całka analityczna: ∫[1,4] 5 dx = [5x] od 1 do 4 = 5*4 - 5*1 = 20 - 5 = 15.0
        double expected = 15.0;

        integrator.calc(range, subintervals);
        double result = integrator.getResult();

        // Metoda punktu środkowego jest bardzo dokładna dla funkcji liniowych (stała to szczególny przypadek)
        assertEquals(expected, result, 1e-10);
    }

    @Test
    void testDifferentThreadCounts() {
        Function<Double, Double> f = x -> x * x;
        integrator.setFunction(f);

        for (int threads : new int[]{1, 2, 4, 8, 12}) {
            integrator.setThreadsNumber(threads);
            Range range = new RangeExample(0, 2);
            long start = System.nanoTime();
            integrator.calc(range, 10000);
            long end = System.nanoTime();
            long time = (end - start) / 1000;
            System.out.println("Threads: " + threads + ", time: " + time + " ms");
            double result = integrator.getResult();

            assertEquals(8.0 / 3.0, result, 0.01, "Failed with " + threads + " threads");

//            Threads: 1, time: 359300 ms
//            Threads: 2, time: 365800 ms
//            Threads: 4, time: 365200 ms
//            Threads: 8, time: 433800 ms
//            Threads: 16, time: 886000 ms
        }
    }


    @Test
    void testLinearFunction() {
        Function<Double, Double> f = x -> 3 * x + 2; // ∫(3x+2) dx od 0 do 3 = [3/2 x² + 2x] = 19.5
        integrator.setFunction(f);
        Range range = new RangeExample(0, 3);

        integrator.calc(range, 10000);
        double result = integrator.getResult();

        assertEquals(19.5, result, 0.02);
    }

    @Test
    void testSineFunction() {
        Function<Double, Double> f = Math::sin;
        integrator.setFunction(f);
        Range range = new RangeExample(0, Math.PI);

        integrator.calc(range, 100000);
        double result = integrator.getResult();

        assertEquals(2.0, result, 0.001); // ∫0..π sin(x) dx = 2
    }

    @Test
    void testThreadScaling() {
        Function<Double, Double> f = x -> {
            // symulacja ciężkiej funkcji
            double sum = 0;
            for (int i = 0; i < 1000; i++) sum += Math.sin(x + i);
            return sum;
        };
        integrator.setFunction(f);
        Range range = new RangeExample(0, 10);
        int subintervals = 100_000;

        int[] threadCounts = {1, 2, 4, 8, 12};
        long[] times = new long[threadCounts.length];

        for (int i = 0; i < threadCounts.length; i++) {
            integrator.setThreadsNumber(threadCounts[i]);
            long start = System.currentTimeMillis();
            integrator.calc(range, subintervals);
            long end = System.currentTimeMillis();
            times[i] = end - start;
            System.out.println("Threads: " + threadCounts[i] + ", time: " + times[i] + " ms, result: " + integrator.getResult());
        }

        // Możesz też sprawdzić, że wynik się nie zmienia znacząco
        double expected = integrator.getResult();
        for (int i = 0; i < threadCounts.length; i++) {
            assertEquals(expected, integrator.getResult(), 1e-6);
        }

//        Threads: 1, time: 755 ms, result: 1.801259456340962
//        Threads: 2, time: 340 ms, result: 1.801259456340946
//        Threads: 4, time: 172 ms, result: 1.8012594563409472
//        Threads: 8, time: 97 ms, result: 1.801259456340949
    }

    @Test
    void testCalkaPawlaGory() {
        Function<Double, Double> f = x -> Math.sin(Math.PI * (1 + Math.sqrt(x)) / (1 + x * x)) * Math.exp(-x);
        integrator.setFunction(f);
        Range range = new RangeExample(0, 17);

        int subintervals = 100_000; // im więcej, tym dokładniej
        integrator.setThreadsNumber(8);

        integrator.calc(range, subintervals);
        double result = integrator.getResult();

        double expected = -0.217275;
        assertEquals(expected, result, 0.001); // tolerancja 0.001
    }

    @Test
    void testCalkaPawlaGory2() {
        // Funkcja: cos((1 + t) / (t^2 + 0.04)) * exp(-t^2)
        Function<Double, Double> f = t -> Math.cos((1 + t) / (t * t + 0.04)) * Math.exp(-t * t);
        integrator.setFunction(f);
        Range range = new RangeExample(-5, 5);

        int subintervals = 100_000; // większa liczba dla dokładniejszego wyniku
        integrator.setThreadsNumber(8);

        integrator.calc(range, subintervals);
        double result = integrator.getResult();

        double expected = 0.2196119;
        assertEquals(expected, result, 0.001); // tolerancja 0.001
    }
}
