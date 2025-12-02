import java.util.Set;

/**
 * Pole na planszy do gry. Pole opisuje para liczb całkowitych: kolumna i
 * wiersz.
 * 
 * @param col kolumna
 * @param row wiersz
 */

public record Square(int col, int row) {

	/**
	 * Zbiór wszystkich potencjalnych sąsiadów. Metoda nie zna rozmiarów planszy.
	 * Zawsze zwraca zbiór czterech możliwych sąsiadów.
	 * 
	 * @return zbiór potencjalnych sąsiadów danego pola.
	 */
	public Set<Square> neighbours() {
		return Set.of(new Square(col + 1, row), new Square(col - 1, row), new Square(col, row + 1),
				new Square(col, row - 1));
	}
}
