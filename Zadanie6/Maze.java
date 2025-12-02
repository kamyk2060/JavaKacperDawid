import java.util.List;
import java.util.Set;

/**
 * Interfejs labiryntu.
 */
public interface Maze {

	/**
	 * Ustawia wysokość planszy w polach.
	 * 
	 * @param rows wysokość planszy
	 */

	void rows(int rows);

	/**
	 * Ustawia szerokość planszy w polach.
	 * 
	 * @param cols szerokość planszy
	 */
	void cols(int cols);

	/**
	 * Metoda przekazuje zbiór zajętch pól planszy.
	 * 
	 * @param squares zbiór zajętych pól
	 */
	void occupiedSquare(Set<Square> squares);

	/**
	 * Zwracana lista zawiera największy wiersz, do którego udało się dotrzeć
	 * wypełniając puste pola planszy zaczynając od kolejnych pól wiersza zerowego.
	 * 
	 * @return lista najwyższych wierszy osiągniętych w trakcie wypełniania planszy.
	 */
	List<Integer> howFar();

	/**
	 * Zwracana lista zawiera pole powierzchni wypełnionego obszaru, gdy wypełnianie
	 * rozpoczęto od kolejnych pól wiersza zerowego. Pole powierzchni wyrażone jest
	 * w liczbie zajętych pól.
	 * 
	 * @return lista pól powierzchni osiągniętych w trakcie wypełniania planszy.
	 */
	List<Integer> area();

	/**
	 * Metoda zwraca zbiór tych pól, do których nigdy nie udało się dotrzeć w
	 * procesie wypełniania planszy.
	 * 
	 * @return zbiór pól, do których nie można dotrzeć rozpoczynając wypełnianie
	 *         planszy od kolejnych pól wiersza zero.
	 * 
	 */
	Set<Square> unreachableSquares();

}
