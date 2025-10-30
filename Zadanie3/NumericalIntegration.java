/**
 * Interfejs całkowania numerycznego funkcji jednej zmiennej.
 */
public interface NumericalIntegration {
	/**
	 * Ustawienie funkcji do scałkowania numerycznego
	 * 
	 * @param f funkcja
	 */
	void setFunction(Function f);

	/**
	 * Obliczenia całki w zadanym zakresie liczb (range). Zakres należy podzielić na
	 * wskazaną liczbę przedziałow (subintervals).
	 * 
	 * @param range        zakres liczb dla jakich wyznaczana jest całka
	 * @param subintervals liczba podprzedziałów
	 * @return wynik całkowania numerycznego
	 */
	double integrate(Range range, int subintervals);
}
