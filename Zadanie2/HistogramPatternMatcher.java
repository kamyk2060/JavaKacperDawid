
import java.util.*;

class HistogramPatternMatcher extends AbstractHistogramPatternMatcher {

    private Map<Integer, Integer> histogramMap = new TreeMap<>();
    private Map<Integer, Integer> histogramWypelniony = new TreeMap<>();

    @Override
    public void data(int value) {
        histogramMap.put(value, histogramMap.getOrDefault(value, 0) + 1);
    }

    @Override
    public Map<Integer, Integer> histogram() {
        return new TreeMap<>(histogramMap); // Zwracamy kopię, aby chronić oryginalne dane
    }

    @Override
    public Set<Integer> match(List<Integer> pattern) {
        Set<Integer> result = new TreeSet<>();

        // Sprawdzenie przypadków brzegowych
        if (pattern == null || pattern.isEmpty() || histogramMap.isEmpty()) {
            return result; // Pusty zbiór dla pustego wzorca lub pustych danych
        }

        histogramWypelniony.clear();
        histogramWypelniony.putAll(histogramMap);

        // Znajdź min i max klucz
        int min = Collections.min(histogramMap.keySet());
        int max = Collections.max(histogramMap.keySet());

        // Wypełnij brakujące klucze wartością 0
        for (int i = min; i <= max; i++) {
            histogramWypelniony.putIfAbsent(i, 0);
        }

        int patternSize = pattern.size();

        // Jeśli wzorzec jest dłuższy niż zakres danych
        if (patternSize > (max - min + 1)) {
            return result;
        }

        for (int i = min; i <= max - patternSize + 1; i++) {
            boolean matches = true;

            for (int j = 0; j < patternSize - 1; j++) {
                int currentKey = i + j;
                int nextKey = i + j + 1;

                // Sprawdź czy klucze istnieją w mapie
                if (!histogramWypelniony.containsKey(currentKey) ||
                        !histogramWypelniony.containsKey(nextKey)) {
                    matches = false;
                    break;
                }

                int obecnaWartosc = histogramWypelniony.get(currentKey);
                int nastepnaWartosc = histogramWypelniony.get(nextKey);

                int patternNumber1 = pattern.get(j + 1);
                int patternNumber2 = pattern.get(j);

                // Oba wzorca są zerowe - akceptujemy tylko gdy obie wartości są zerowe
                if (patternNumber1 == 0 && patternNumber2 == 0) {
                    if (obecnaWartosc != 0 || nastepnaWartosc != 0) {
                        matches = false;
                        break;
                    }
                    continue;
                }

                // Jedna ze wartości wzorca jest zerowa
                if (patternNumber1 == 0 || patternNumber2 == 0) {
                    if (patternNumber1 == 0 && nastepnaWartosc != 0) {
                        matches = false;
                        break;
                    }
                    if (patternNumber2 == 0 && obecnaWartosc != 0) {
                        matches = false;
                        break;
                    }
                    continue;
                }

                // Obie wartości danych są zerowe
                if (obecnaWartosc == 0 && nastepnaWartosc == 0) {
                    // Dla 0:0 uznajemy, że proporcja 1:1 tylko gdy wzorzec ma taką samą wartość
                    if (patternNumber1 == patternNumber2) {
                        continue;
                    } else {
                        matches = false;
                        break;
                    }
                }

                // Jedna z wartości danych jest zerowa, ale wzorzec nie jest zerowy
                if (obecnaWartosc == 0 || nastepnaWartosc == 0) {
                    matches = false;
                    break;
                }

                // Porównujemy proporcje używając mnożenia (unikanie dzielenia przez zero)
                if (nastepnaWartosc * patternNumber2 != obecnaWartosc * patternNumber1) {
                    matches = false;
                    break;
                }
            }

            if (matches) {
                result.add(i);
            }
        }
        return result;
    }

    public static void main(String[] args) {
        // Test 1: Normalne dane
        AbstractHistogramPatternMatcher pp = new HistogramPatternMatcher();
        int[] dane = {5, 3, 4, 2, 5, 5, 4, -1, -2, 2, 3, 4};
        for (int value : dane) {
            pp.data(value);
        }

        // Test 2: Więcej danych
        AbstractHistogramPatternMatcher pp2 = new HistogramPatternMatcher();
        int[] dane2 = {5, 3, 4, 2, 5, 5, 4, -1, -2, 2, 3, 4, 6, 6, 6, 6, 6, 6};
        for (int value : dane2) {
            pp2.data(value);
        }

        // Test 3: Złożone dane
        AbstractHistogramPatternMatcher pp3 = new HistogramPatternMatcher();
        int[] dane3 = {1, 3, 2, 10, 8, 12, 7, 8, 0, 9, 11, 11, 12, 12, 12, 12, 128, 9, 8, 9, 100, 7, 9, 8, 0, 9, 9, 10, 1, -5, -7, 2, -6, -7, -5, 3, -7, -5, -7, -6, -7, -5, -7, 1, -7, -7, 2, 11, 101, 200, 12, 300, 201, 201, 300, 100, 200, 13, 300, 102, 102, 200, 300, 100, 200, 100, 11};
        for (int value : dane3) {
            pp3.data(value);
        }

        // Test 4: Puste dane
        AbstractHistogramPatternMatcher pp4 = new HistogramPatternMatcher();

        // Test 5: Pojedyncza wartość
        AbstractHistogramPatternMatcher pp5 = new HistogramPatternMatcher();
        pp5.data(5);

        System.out.println("Test 1:");
        System.out.println(pp.histogram());
        System.out.println("Match [1,1]: " + pp.match(List.of(1, 1)));
        System.out.println("-=-".repeat(20));

        System.out.println("Test 2:");
        System.out.println(pp2.histogram());
        System.out.println("Match [1,1,2]: " + pp2.match(List.of(1, 1, 2)));
        System.out.println("-=-".repeat(20));

        System.out.println("Test 3:");
        System.out.println(pp3.histogram());
        System.out.println("Match [1,2,3]: " + pp3.match(List.of(1, 2, 3)));
        System.out.println("Match [4,1,2]: " + pp3.match(List.of(4, 1, 2)));
        System.out.println("Match [3,3,2]: " + pp3.match(List.of(3, 3, 2)));
        System.out.println("-=-".repeat(20));

        System.out.println("Test 4 (puste dane):");
        System.out.println(pp4.histogram());
        System.out.println("Match [1,1] na pustych danych: " + pp4.match(List.of(1, 1)));
        System.out.println("Match pustego wzorca: " + pp4.match(List.of()));
        System.out.println("-=-".repeat(20));

        System.out.println("Test 5 (pojedyncza wartość):");
        System.out.println(pp5.histogram());
        System.out.println("Match [1] na pojedynczej wartości: " + pp5.match(List.of(1)));
        System.out.println("Match [1,1] na pojedynczej wartości: " + pp5.match(List.of(1, 1)));
    }
}