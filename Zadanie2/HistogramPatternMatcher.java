import java.util.*;

public class HistogramPatternMatcher extends AbstractHistogramPatternMatcher {

    private int dane[];
    private Map<Integer, Integer> histogramMap = new TreeMap<>();
    private Map<Integer, Integer> histogramWypelniony = new TreeMap<>();


    @Override
    public void data(int value) {
        if (histogramMap.containsKey(value)) {
            histogramMap.put(value, histogramMap.get(value) + 1);
        } else {
            histogramMap.put(value, 1);
        }
    }

    @Override
    public Map<Integer, Integer> histogram() {
        return histogramMap;
    }


    @Override
    public Set<Integer> match(List<Integer> pattern) {
        histogramWypelniony.putAll(histogramMap);
        // Znajdź min i max klucz
        int min = Collections.min(histogramMap.keySet());
        int max = Collections.max(histogramMap.keySet());

        // Wypełnij brakujące klucze wartością 0
        for (int i = min; i <= max; i++) {
            histogramWypelniony.put(i, histogramMap.getOrDefault(i, 0));
        }

        //System.out.println(histogramWypelniony);

        Set<Integer> result = new TreeSet<>();
        int patternSize = pattern.size();

        for (int i = min; i <= max -patternSize + 1; i++) {

            boolean matches = true;

            for (int j = 0; j < patternSize - 1; j++) {
                int obecnaWartosc = histogramWypelniony.get(i + j);
                int nastepnaWartosc = histogramWypelniony.get(i + j + 1);

                int patternNumber1 = pattern.get(j + 1);
                int patternNumber2 = pattern.get(j);

                // obie wartości są 0
                if (obecnaWartosc == 0 && nastepnaWartosc == 0) {
                    // Dla 0:0 uznajemy, że proporcja 1:1
                    if (patternNumber1 == patternNumber2) {
                        continue; // proporcja się zgadza
                    } else {
                        matches = false;
                        break;
                    }
                }

                if (obecnaWartosc == 0) {
                    matches = false;
                    break;
                }

                // Porównujemy proporcje używając mnożenia
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

    static void main(String[] args) {

        AbstractHistogramPatternMatcher pp = new HistogramPatternMatcher();
        int[] dane = {5, 3, 4, 2, 5, 5, 4, -1, -2, 2, 3, 4};
        for (int i = 0; i < dane.length; i++) {
            pp.data(dane[i]);
        }

        AbstractHistogramPatternMatcher pp2 = new HistogramPatternMatcher();
        int[] dane2 = {5, 3, 4, 2, 5, 5, 4, -1, -2, 2, 3, 4,6,6,6,6,6,6};
        for (int i = 0; i < dane2.length; i++) {
            pp2.data(dane2[i]);
        }

        AbstractHistogramPatternMatcher pp3 = new HistogramPatternMatcher();
        int[] dane3 = {1,3,2,10,8, 12,7, 8, 0, 9, 11,11,12,12,12,12,128, 9, 8, 9, 100, 7, 9, 8, 0, 9, 9, 10, 1, -5, -7, 2, -6, -7, -5, 3, -7, -5, -7, -6, -7, -5, -7, 1, -7, -7, 2, 11, 101, 200, 12, 300, 201,201, 300, 100, 200, 13, 300, 102,102, 200, 300, 100, 200, 100, 11};
        for (int i = 0; i < dane3.length; i++) {
            pp3.data(dane3[i]);
        }

        System.out.println(pp.histogram());
        System.out.println(pp.match(List.of(1,1)));
        System.out.println("-=-".repeat(20));
        System.out.println(pp2.histogram());
        System.out.println(pp2.match(List.of(1,1,2)));
        System.out.println("-=-".repeat(20));
        System.out.println(pp3.histogram());
        System.out.println(pp3.match(List.of(1,2,3)));
        System.out.println(pp3.match(List.of(4,1,2)));
        System.out.println(pp3.match(List.of(3,3,2)));

    }

}
