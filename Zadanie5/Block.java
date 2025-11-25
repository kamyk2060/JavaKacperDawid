import java.util.Set;

/**
 * Opis pojedynczego klocka
 */
public interface Block {
	/**
	 * Kratka odniesienia. Metoda nigdy nie zwraca null. Kratka odniesienia zawsze
	 * istnieje.
	 * 
	 * @return pozycja kratki odniesienia
	 */
	Position base();

	/**
	 * Dodatkowe kratki wchodzące w skład klocka. Liczba dodatkowych kratek może być
	 * równa zero - w takim przypadku metoda zwraca zbiór pusty, a kształt składa
	 * się wyłącznie z jednej kratki, która jest jednocześnie kratką odniesienia.
	 * 
	 * @return zbiór dodatkowych kratek
	 */
	Set<Vector> squares();
}
