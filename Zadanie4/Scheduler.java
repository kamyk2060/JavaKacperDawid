import java.util.List;
import java.util.Set;

/**
 * Narzędzie do wyszukiwania rozkładu programów telewizyjnych
 */
public interface Scheduler {
	/**
	 * Metoda pozwala wprowadzić informację o programie telewizyjnym i czasie jego
	 * trwania. Program zajmuje pewien przedział czasowy. Programy dla jednego
	 * nadawcy nie nakładają się na siebie.
	 * 
	 * @param program informacja o pojedynczym programie telewizyjnym
	 */
	public void addSlot(Slot program);

	/**
	 * Narzędzie optymalizacji. Przegląda zarejestrowane programy telewizyjne i
	 * proponuje możliwe scenariusze ich oglądania. Każdy scenariusz (lista slotów)
	 * pozwala na oglądnięcie programów o podanych nazwach tak, że nie nakładają się
	 * one na siebie. Ponieważ może istnieć więcej niż jedno rozwiązanie, metoda
	 * zwraca zbiór poprawnych scenariuszy. W przypadku braku możliwości rozwiązania
	 * problemu, metoda zwraca pusty zbiór.
	 * 
	 * @param programs zbiór programów, które chcemy oglądnąć w dowolnej kolejności
	 * @return zbiór scenariuszy pozwalających na oglądnięcie wszystkich programów w
	 *         całości.
	 */
	public Set<List<Slot>> match(Set<String> programs);
}
