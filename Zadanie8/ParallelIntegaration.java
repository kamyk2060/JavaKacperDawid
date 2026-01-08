import java.util.function.Function;

public interface ParallelIntegaration {
	/**
	 * Metoda ustawia funkcję, której całkę trzeba policzyć.
	 * 
	 * @param function funkcja do scałkowania
	 */
	public void setFunction(Function<Double, Double> function);

	/**
	 * Metoda ustawia liczbę wątków jaką wolno użyć do równoległego liczenia całki.
	 * 
	 * @param threads liczba wątków
	 */
	public void setThreadsNumber(int threads);

	/**
	 * Metoda zleca wykonanie rachunku. Całka wyznaczana jest metodą prostokątów.
	 * Liczbę podprzedziałów przekazuje ta metoda. Liczba podprzedziałów będzie
	 * większa od liczby wątków.
	 * 
	 * @param range        przedział, w jakim całkę należy policzyć
	 * @param subintervals liczba przedziałów
	 */
	public void calc(Range range, int subintervals);

	/**
	 * Metoda zwraca wynik rachunku.
	 * 
	 * @return wynik rachunku
	 */
	public double getResult();
}
