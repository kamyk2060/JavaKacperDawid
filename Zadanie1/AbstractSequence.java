/**
 * Klasa abstrakcyjna prezentująca metody obsługi ciągu liczb.
 */
public abstract class AbstractSequence {
	/**
	 * Ustawienie danych, dla których wykonywane będą kolejne operacje. Ponowne
	 * wykonanie metody powoduje zmianę obsługiwanego zestawu danych. Dane zapisane
	 * są w postaci (kolejne pozycje tablicy):
	 * 
	 * <pre>
	 * dana delta1 delta2 ... deltaN dana delta1 delta2 ... deltaN itd.
	 * </pre>
	 * 
	 * W powyższy sposób zakodowany jest pewien ciąg liczb. Idea kodowania polega na
	 * tym, że oryginalny ciąg liczb dzielony jest na fragmenty (podciągi) o
	 * rozmiarze N+1. Pierwsza liczba w podciągu podawana jest wprost. Kolejne N
	 * liczb to różnice pomiedzy kolejnymi liczbami ciągu. I tak:
	 * 
	 * <pre>
	 * indeks   tablica wejściowa    odkodowany ciąg
	 * 0           data[0]             data[0]
	 * 1           data[1]             data[0]+data[1]
	 * 2           data[2]             data[0]+data[1]+data[2]				
	 * .           .
	 * .           .
	 * N           data[N]             data[0]+data[1]+data[2]+...+data[N]
	 * N+1         data[N+1]           data[N+1]
	 * N+2         data[N+2]           data[N+1]+data[N+2]
	 * N+2         data[N+3]           data[N+1]+data[N+2]+data[N+3]
	 * itd.
	 * </pre>
	 * 
	 * Nie ma gwarancji, że wszystkie podciągi są używane w całości. Ostatni może
	 * zawierać mniej niż N delt. Czyli, rozmiar tablicy data nie musi być całkowitą
	 * wielokrotnością (deltaSegmentSize+1).
	 * 
	 * @param data             tablica zawierająca dane
	 * @param deltaSegmentSize rozmiar segmentu zawierającego delty
	 *                         (deltaSegmentSize = N). Podciąg ma efektywnie długość
	 *                         deltaSegmentSize+1
	 * 
	 */
	abstract public void sequence(int[] data, int deltaSegmentSize);

	/**
	 * Metoda zwraca odkodowany ciąg.
	 * 
	 * @return odkodowany ciąg
	 */
	abstract public int[] decode();

	/**
	 * Metoda koduje otrzymany metodą sequence ciąg dostosowując wynik do podanej
	 * długości segmentu zawierającego delta.
	 * 
	 * @param deltaSegmentSize
	 * @return ciąg zakodowany z podanym rozmiarem segmentu zawierającego delty.
	 */
	abstract public int[] encode(int deltaSegmentSize);

	/**
	 * Metoda sprawdza, czy podany ciąg liczb jest równoważny temu, który wcześniej
	 * otrzymano za pomocą sequence. Metoda uwzględnia zawartość ciągu liczb jak i
	 * jego długość.
	 * 
	 * @param data             dane nowego ciągu liczb
	 * @param deltaSegmentSize rozmiar segmentu delt
	 * @return prawda - odkodowane ciągi liczb są równie, false - odkodowane ciągi
	 *         liczb do siebie nie pasują.
	 */
	abstract public boolean equals(int[] data, int deltaSegmentSize);
}
