import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TestFunction implements Function {
    private final Set<Range> exclusions;

    public TestFunction() {
        this.exclusions = new HashSet<>();
    }

    public TestFunction(Set<Range> exclusions) {
        this.exclusions = exclusions;
    }

    @Override
    public Set<Range> domainExclusions() {
        return Collections.unmodifiableSet(exclusions);
    }

    @Override
    public double apply(double x) {
        // Sprawdź czy x nie jest w obszarze zabronionym
        for (Range exclusion : exclusions) {
            if (x >= exclusion.min() && x <= exclusion.max()) {
                throw new IllegalArgumentException("Point " + x + " is in excluded range " + exclusion);
            }
        }

        // Przykładowa funkcja: f(x) = x² + 2x (ta sama co w C++)
        return x * x + 2 * x;
    }

    // Funkcja z obszarami zabronionymi
    public static TestFunction createWithExclusions() {
        Set<Range> exclusions = new HashSet<>();
        exclusions.add(new SimpleRange(-2.0, -1.0));  // obszar zabroniony 1
        exclusions.add(new SimpleRange(1.5, 2.5));    // obszar zabroniony 2
        return new TestFunction(exclusions);
    }
}