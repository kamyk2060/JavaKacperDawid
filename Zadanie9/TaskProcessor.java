import java.util.List;

/**
 * Interfejs procesora zadań
 */
public interface TaskProcessor {
	/**
	 * Interfejs pojedynczego zadania
	 */
	public interface Task {
		/**
		 * Identyfikator zadania. Unikalna liczba całkowita identyfikująca zadanie.
		 * 
		 * @return identyfikator zadania
		 */
		public int id();

		/**
		 * Wynik zadania.
		 * 
		 * @return liczba będąca wynikiem zadania
		 */
		public int result();
	}

	/**
	 * Interfejs producenta wątków
	 */
	public interface ThreadsFactory {
		/**
		 * Metoda otrzymuje listę obiektów-pracowników zgodnych z interfejsem Runnable.
		 * Dla każdego z obiektów tworzony jest osobny wątek. Wątki zwracane są w stanie
		 * NEW.
		 * 
		 * @param workers lista obiektów-pracowników
		 * @return lista wątków
		 */
		public List<Thread> createThreads(List<Runnable> workers);
	}

	/**
	 * Interfejs pozwalający na przekazanie wyniku zadań.
	 */
	public interface ResultConsumer {
		/**
		 * Metoda otrzymuje wynik (result) zrealizowanego zadania. Metoda musi być
		 * wykonywana w kolejności odpowiadającej kolejności zadań na liście przekazanej
		 * poprzez addTasks.
		 * 
		 * @param taskID identyfikator zadania
		 * @param result wynik zadania o identyfikatorze taskID.
		 */
		public void save(int taskID, int result);
	}

	/**
	 * Metoda dostarcza zadania do wykonania, fabrykę wątków, której należy użyć w
	 * celu wytworzenia wątków, którymi zadania będą wykonywane oraz obiekt, do
	 * którego wyniki zadań należy dostarczać. Metoda kończy pracę po przekazaniu do
	 * consumer-a wyniku ostaniego zadania.
	 * 
	 * @param tasks    list zadań
	 * @param factory  fabryka wątków
	 * @param consumer obiekt, do którego należy przekazywać wyniki zadań.
	 */
	public void set(List<Task> tasks, ThreadsFactory factory, ResultConsumer consumer);

	/**
	 * Limit wątków, które mają być używane do wykonania zadań. Zadania należy
	 * wykonywać za pomocą wątków utworzonych poprzez ThreadsFactory.
	 * 
	 * @param limit liczba wątków do wykorzystania
	 */
	public void threadsLimit(int limit);

}
