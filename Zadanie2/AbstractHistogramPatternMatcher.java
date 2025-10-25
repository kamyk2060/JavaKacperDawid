import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractHistogramPatternMatcher {
	/**
	 * Metoda umożliwia użytkownikowi wprowadzanie danych.
	 * 
	 * @param value wprowadzana przez użytkownika liczba
	 */
	abstract public void data(int value);

	/**
	 * Metoda zwraca histogram. Histogram nie może zawierać pozycji zawierających
	 * zero zliczeń. Jeśli wywołanie metody nastąpi przed prowadzeniem danych metoda
	 * zwraca pustą mapę (mapę o rozmiarze 0). Metoda nigdy nie zwraca NULL.
	 * 
	 * @return mapa reprezentująca histogram. Klucz to liczba, wartość wskazywana
	 *         kluczem, to liczba wystąpień danej liczby we wprowadzanych danych.
	 */
	abstract public Map<Integer, Integer> histogram();

	/**
	 * Metoda zwraca zbiór kluczy z histogramu. Do zbioru wprawdzane są te klucze,
	 * od których zaczyna się sekwencja zliczeń, która pasuje do wskazanego wzorca.
	 * Wzorzec należy rozumieć na zasadzie proporcji pomiędzy kolejnymi zliczeniami.
	 * Wzorzec: [2,1,2] pasować będzie np. do sekwencji zliczeń 10:5:10 czy 4:2:4,
	 * jednocześnie wzorzec ten nie pasuje np. do 11:5:10. Brak odpowiedzi metoda
	 * sygnalizuje zwracając zbiór pusty. Metoda nigdy nie zwraca NULL.
	 * 
	 * @param pattern wzorzec kolejnych zliczeń w histogramie
	 * @return zbiór liczby rozpoczynających sekwencję zliczeń
	 */
	abstract public Set<Integer> match(List<Integer> pattern);

}
