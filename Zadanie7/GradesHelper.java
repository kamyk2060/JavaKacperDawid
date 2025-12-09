import java.util.Map;

/**
 * Interfejs pomocnika oceniania
 */
public interface GradesHelper {

	/**
	 * Wyjątek zgłaszany, gdy przedziały dla dwóch ocen na siebie zachodzą.
	 */
	public class RangeConflictException extends Exception {
		private static final long serialVersionUID = -8152212610934429384L;
	}

	/**
	 * Wyjątek zgłaszany, gdy w pliku z zasadami oceny ta sama ocena występuje
	 * więcej niż jeden raz i jednocześnie dane przedziałów są różne.
	 * 
	 */
	public class MarkConflictException extends Exception {
		private static final long serialVersionUID = 6771406201150967367L;

		private final String mark;

		/**
		 * Konstruktor ustawiający pole mark.
		 * 
		 * @param mark ocena, dla której wykryto konflikt
		 */
		public MarkConflictException(String mark) {
			this.mark = mark;
		}

		public String getMark() {
			return mark;
		}
	}

	/**
	 * Ocena studenta o podanych danych nie była możliwa.
	 */
	public class AssessmentImpossible extends Exception {
		private static final long serialVersionUID = 1174503033731861293L;
		private final String firstName;
		private final String lastName;

		/**
		 * Konstruktor ustawiający dane studenta, którego nie udało się ocenić.
		 * 
		 * @param firstName imię studenta
		 * @param lastName  nazwisko studenta
		 */
		public AssessmentImpossible(String firstName, String lastName) {
			this.firstName = firstName;
			this.lastName = lastName;
		}

		public String getFirstName() {
			return firstName;
		}

		public String getLastName() {
			return lastName;
		}
	}

	/**
	 * Metoda powoduje wczytanie z pliku listy studentów. Plik zawiera 3 kolumny
	 * rozdzielone średnikiem. Format wiersza jest następujący:
	 * 
	 * <pre>
	 * IDStudenta,ImięStudenta,NazwiskoStudenta;
	 * </pre>
	 * 
	 * Koniec linii oznaczony jest średnikiem. Wiersze niezgodne z powyższym
	 * formatem należy odrzucić (zignorować). IDStudenta jest liczbą całkowitą.
	 * ImięStudenta i NazwiskoStudenta to ciągi znaków.
	 * 
	 * @param file nazwa pliku do wczytania
	 */
	void loadStudents(String file);

	/**
	 * Metoda wczytuje zasady oceniania. Plik zawiera trzy kolumny rozdzielone
	 * średnikami. Pierwsza kolumna to ciąg znaków, dwie pozostałe to liczby.
	 * Separatorem dziesietym jest kropka. Koniec linii oznaczony jest średnikiem.
	 * Wiersze niezawierające danych należy ignorować. Format wiersza jest
	 * następujący:
	 * 
	 * <pre>
	 * ocena;min;max;
	 * </pre>
	 * 
	 * Idea: ocena obowiązuje w przedziale od min punktów włącznie do max punktów
	 * włącznie.
	 * 
	 * @param file nazwa pliku z danymi
	 * 
	 * @throws RangeConflictException wyjątek zgłaszany w przypadku kolizji
	 *                                przedziałow
	 * @throws MarkConflictException  kolizja - ta sama ocena pojawia się z innym
	 *                                przedziałem min/max.
	 */
	void loadScoring(String file) throws RangeConflictException, MarkConflictException;

	/**
	 * Metoda ocenia pracę studenta. Dane dla jednego studenta to jeden wiersz. Dane
	 * rozdzielają średniki. Separator dziesietny to kropka. Linie niezawierające
	 * danych należy ignorować.
	 * 
	 * Format wiersza jest następujący:
	 * 
	 * <pre>
	 * ImieStudenta;NazwiskoStudenta;nota1;nota2;....;notaN;
	 * </pre>
	 * 
	 * Metoda wczytuje dane ze wskazanego pliku. Na podstawie wcześniej zdobytych
	 * informacji ustala IDStudenta. W przypadku braku możliwości wyznaczenia
	 * IDStudenta należy zgłosić wyjątek. <br>
	 * Następnie, ze wszystkich not studenta należy wyliczyć średnią. Liczba
	 * zdobytych not może być różna dla różnych studentów. Średnią należy dopasować
	 * do zasad oceniania. W przypadku, gdy średnia nie pasuje do żadnego przedziału
	 * min/max należy zgłosić wyjątek. <br>
	 * Wynikiem metody jest mapa zawierająca oceny (nie średnie!) studentów
	 * identyfikowanych za pomocą identyfikatora liczbowego.
	 * 
	 * @param data plik z danymi (dane studenta i otrzymane noty)
	 * @return mapa, której kluczem jest IDStudenta. Wartość to ocena wyznaczona na
	 *         postawie średniej i zasad oceniania.
	 * @throws AssessmentImpossible wyjątek zgłaszany w przypadku braku możliwości
	 *                              oceny studenta.
	 */
	Map<Integer, String> generateGrades(String data) throws AssessmentImpossible;
}
