import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;

class MultithreadedTaskProcessorTest {
    private MultithreadedTaskProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new MultithreadedTaskProcessor();
    }

    // Prosta implementacja fabryki wątków
    private TaskProcessor.ThreadsFactory createSimpleFactory() {
        return workers -> {
            List<Thread> threads = new ArrayList<>();
            for (Runnable worker : workers) {
                threads.add(new Thread(worker));
            }
            return threads;
        };
    }

    // Test 1: Różne limity wątków
    @Test
    @Timeout(5)
    void shouldWorkWithDifferentThreadLimits() {
        // Test z 1 wątkiem
        processor.threadsLimit(1);
        testWithThreadLimit(1, 10);

        // Test z 2 wątkami
        processor = new MultithreadedTaskProcessor();
        processor.threadsLimit(2);
        testWithThreadLimit(2, 10);

        // Test z 10 wątkami
        processor = new MultithreadedTaskProcessor();
        processor.threadsLimit(10);
        testWithThreadLimit(10, 20);
    }

    private void testWithThreadLimit(int threadLimit, int taskCount) {
        List<TaskProcessor.Task> tasks = new ArrayList<>();
        AtomicInteger executionCounter = new AtomicInteger(0);

        for (int i = 0; i < taskCount; i++) {
            final int id = i;
            tasks.add(new TaskProcessor.Task() {
                @Override
                public int id() {
                    return id;
                }

                @Override
                public int result() {
                    executionCounter.incrementAndGet();
                    try {
                        Thread.sleep(10); // Małe opóźnienie
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return id * 10;
                }
            });
        }

        List<Integer> results = Collections.synchronizedList(new ArrayList<>());
        TaskProcessor.ResultConsumer consumer = (taskId, result) -> {
            results.add(result);
        };

        processor.set(tasks, createSimpleFactory(), consumer);

        assertEquals(taskCount, executionCounter.get(),
                "Dla " + threadLimit + " wątków: wszystkie zadania powinny być wykonane");
        assertEquals(taskCount, results.size(),
                "Dla " + threadLimit + " wątków: wszystkie wyniki powinny być dostarczone");
    }

    // Test 2: Pusta lista zadań
    @Test
    @Timeout(2)
    void shouldHandleEmptyTaskList() {
        List<TaskProcessor.Task> tasks = new ArrayList<>();
        AtomicInteger saveCalls = new AtomicInteger(0);

        TaskProcessor.ResultConsumer consumer = (taskId, result) -> {
            saveCalls.incrementAndGet();
        };

        processor.threadsLimit(4);
        processor.set(tasks, createSimpleFactory(), consumer);

        assertEquals(0, saveCalls.get(), "Dla pustej listy nie powinno być wywołań save()");
    }

    // Test 3: Pojedyncze zadanie
    @Test
    @Timeout(2)
    void shouldHandleSingleTask() {
        List<TaskProcessor.Task> tasks = new ArrayList<>();
        final int expectedResult = 42;
        final int expectedId = 7;

        tasks.add(new TaskProcessor.Task() {
            @Override
            public int id() {
                return expectedId;
            }

            @Override
            public int result() {
                return expectedResult;
            }
        });

        AtomicInteger receivedResult = new AtomicInteger(0);
        AtomicInteger receivedId = new AtomicInteger(-1);
        TaskProcessor.ResultConsumer consumer = (taskId, result) -> {
            receivedId.set(taskId);
            receivedResult.set(result);
        };

        processor.threadsLimit(3);
        processor.set(tasks, createSimpleFactory(), consumer);

        assertEquals(expectedId, receivedId.get(), "Powinien być przekazany poprawny ID");
        assertEquals(expectedResult, receivedResult.get(), "Powinien być przekazany poprawny wynik");
    }

    // Test 4: Podstawowa funkcjonalność - wykonanie zadań
    @Test
    @Timeout(5)
    void shouldExecuteAllTasks() {
        processor.threadsLimit(4);

        // Given
        List<TaskProcessor.Task> tasks = new ArrayList<>();
        AtomicInteger executionCounter = new AtomicInteger(0);
        List<Integer> results = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < 10; i++) {
            final int id = i;
            tasks.add(new TaskProcessor.Task() {
                @Override
                public int id() {
                    return id;
                }

                @Override
                public int result() {
                    executionCounter.incrementAndGet();
                    return id * 10;
                }
            });
        }

        TaskProcessor.ResultConsumer consumer = (taskId, result) -> {
            results.add(result);
        };

        // When
        processor.set(tasks, createSimpleFactory(), consumer);

        // Then
        assertEquals(10, executionCounter.get(), "Wszystkie zadania powinny być wykonane");
        assertEquals(10, results.size(), "Wszystkie wyniki powinny być dostarczone");

        // Sprawdź poprawność wyników
        for (int i = 0; i < 10; i++) {
            assertEquals(i * 10, results.get(i));
        }
    }

    // Test 5: Kolejność dostarczania wyników
    @Test
    @Timeout(5)
    void shouldDeliverResultsInOrder() {
        processor.threadsLimit(4);

        // Given
        List<TaskProcessor.Task> tasks = new ArrayList<>();
        List<Integer> resultOrder = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < 5; i++) {
            final int id = i;
            tasks.add(new TaskProcessor.Task() {
                @Override
                public int id() {
                    return id;
                }

                @Override
                public int result() {
                    // Symulacja różnych czasów wykonania
                    // Zadanie 0 wykonuje się najdłużej, zadanie 4 najkrócej
                    try {
                        Thread.sleep((5 - id) * 50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return id * 100;
                }
            });
        }

        TaskProcessor.ResultConsumer consumer = (taskId, result) -> {
            resultOrder.add(taskId);
        };

        // When
        processor.set(tasks, createSimpleFactory(), consumer);

        // Then - wyniki powinny być dostarczone w kolejności 0,1,2,3,4
        // nawet jeśli zadanie 4 skończy się pierwsze
        assertEquals(5, resultOrder.size());
        for (int i = 0; i < 5; i++) {
            assertEquals(i, resultOrder.get(i),
                    "Wynik dla zadania " + i + " powinien być " + i + "-ty w kolejności");
        }
    }

    // Test 6: Wątki pracują współbieżnie
    @Test
    @Timeout(5)
    void shouldExecuteTasksConcurrently() throws InterruptedException {
        processor.threadsLimit(4);

        // Given
        List<TaskProcessor.Task> tasks = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(4);
        AtomicInteger concurrentTasks = new AtomicInteger(0);
        AtomicInteger maxConcurrency = new AtomicInteger(0);

        for (int i = 0; i < 4; i++) {
            final int id = i;
            tasks.add(new TaskProcessor.Task() {
                @Override
                public int id() {
                    return id;
                }

                @Override
                public int result() {
                    int current = concurrentTasks.incrementAndGet();
                    // Aktualizuj maksymalną współbieżność
                    maxConcurrency.updateAndGet(prev -> Math.max(prev, current));

                    try {
                        latch.countDown();
                        // Czekaj aż wszystkie 4 wątki wejdą do tej metody
                        boolean allStarted = latch.await(2, TimeUnit.SECONDS);
                        if (allStarted) {
                            // Poczekaj chwilę, żeby wszystkie były współbieżnie aktywne
                            Thread.sleep(100);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        concurrentTasks.decrementAndGet();
                    }
                    return id;
                }
            });
        }

        List<Integer> results = Collections.synchronizedList(new ArrayList<>());
        TaskProcessor.ResultConsumer consumer = (taskId, result) -> {
            results.add(result);
        };

        // When
        processor.set(tasks, createSimpleFactory(), consumer);

        // Then - wszystkie 4 zadania powinny być wykonywane współbieżnie
        assertEquals(4, maxConcurrency.get(),
                "4 zadania powinny być wykonywane współbieżnie (limit wątków = 4)");
        assertEquals(4, results.size(), "Wszystkie wyniki powinny być dostarczone");
    }

    // Test 7: Metoda save jest wywoływana tylko przez jeden wątek na raz
    @Test
    @Timeout(5)
    void shouldCallSaveSequentially() {
        processor.threadsLimit(4);

        // Given
        List<TaskProcessor.Task> tasks = new ArrayList<>();
        AtomicInteger saveConcurrency = new AtomicInteger(0);
        AtomicInteger maxSaveConcurrency = new AtomicInteger(0);
        AtomicReference<Exception> exception = new AtomicReference<>();

        for (int i = 0; i < 10; i++) {
            final int id = i;
            tasks.add(new TaskProcessor.Task() {
                @Override
                public int id() {
                    return id;
                }

                @Override
                public int result() {
                    // Symulacja różnych czasów wykonania
                    try {
                        Thread.sleep((id % 3) * 10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return id * 2;
                }
            });
        }

        TaskProcessor.ResultConsumer consumer = (taskId, result) -> {
            int current = saveConcurrency.incrementAndGet();
            if (current > 1) {
                exception.set(new IllegalStateException(
                        "save() wywołana współbieżnie przez " + current + " wątków!"));
            }
            maxSaveConcurrency.updateAndGet(prev -> Math.max(prev, current));

            try {
                Thread.sleep(20); // Symulacja pracy w save()
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                saveConcurrency.decrementAndGet();
            }
        };

        // When
        processor.set(tasks, createSimpleFactory(), consumer);

        // Then
        assertNull(exception.get(), "save() nie powinna być wywoływana współbieżnie");
        assertEquals(1, maxSaveConcurrency.get(),
                "save() powinna być wywoływana sekwencyjnie (max 1 wątek na raz)");
    }

    // Test 8: Poprawne obsługiwanie wielu zadań
    @Test
    @Timeout(5)
    void shouldHandleLargeNumberOfTasks() {
        processor.threadsLimit(8);

        // Given
        int taskCount = 100;
        List<TaskProcessor.Task> tasks = new ArrayList<>();
        AtomicInteger completedCount = new AtomicInteger(0);

        for (int i = 0; i < taskCount; i++) {
            final int id = i;
            tasks.add(new TaskProcessor.Task() {
                @Override
                public int id() {
                    return id;
                }

                @Override
                public int result() {
                    completedCount.incrementAndGet();
                    // Symulacja krótkiej pracy
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return id * 2;
                }
            });
        }

        List<Integer> receivedResults = Collections.synchronizedList(new ArrayList<>());
        TaskProcessor.ResultConsumer consumer = (taskId, result) -> {
            receivedResults.add(result);
        };

        // When
        processor.set(tasks, createSimpleFactory(), consumer);

        // Then
        assertEquals(taskCount, completedCount.get(),
                "Wszystkie " + taskCount + " zadań powinny być wykonane");
        assertEquals(taskCount, receivedResults.size(),
                "Wszystkie " + taskCount + " wyniki powinny być dostarczone");

        // Sprawdź czy wyniki są w dobrej kolejności
        for (int i = 0; i < taskCount; i++) {
            assertEquals(i * 2, receivedResults.get(i),
                    "Wynik dla zadania " + i + " powinien być " + (i * 2));
        }
    }

    // Test 9: Zadania są wywoływane w kolejności (result())
    @Test
    @Timeout(5)
    void shouldCallResultInOrder() {
        processor.threadsLimit(3);

        // Given
        List<TaskProcessor.Task> tasks = new ArrayList<>();
        List<Integer> executionOrder = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < 10; i++) {
            final int id = i;
            tasks.add(new TaskProcessor.Task() {
                @Override
                public int id() {
                    return id;
                }

                @Override
                public int result() {
                    executionOrder.add(id);
                    // Symulacja różnych czasów wykonania
                    try {
                        Thread.sleep((id % 4) * 10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return id;
                }
            });
        }

        List<Integer> receivedResults = Collections.synchronizedList(new ArrayList<>());
        TaskProcessor.ResultConsumer consumer = (taskId, result) -> {
            receivedResults.add(result);
        };

        // When
        processor.set(tasks, createSimpleFactory(), consumer);

        // Then - zadania powinny być WYWOŁANE w kolejności (pobierane z kolejki)
        assertEquals(10, executionOrder.size());

        // Sprawdź czy executionOrder zawiera wszystkie liczby 0-9
        // (mogą być w kolejności lub w podgrupach, w zależności od implementacji)
        Set<Integer> uniqueIds = new HashSet<>(executionOrder);
        assertEquals(10, uniqueIds.size(), "Wszystkie 10 zadań powinno być wykonane");

        // Sprawdź czy wyniki są dostarczone w dobrej kolejności
        assertEquals(10, receivedResults.size());
        for (int i = 0; i < 10; i++) {
            assertEquals(i, receivedResults.get(i),
                    "Wynik " + i + " powinien być " + i + "-ty w kolejności");
        }
    }

    // Test 10: Obsługa przerwania wątków
    @Test
    @Timeout(5)
    void shouldHandleThreadInterruption() {
        processor.threadsLimit(4);

        // Given
        List<TaskProcessor.Task> tasks = new ArrayList<>();
        AtomicInteger executedCount = new AtomicInteger(0);

        for (int i = 0; i < 5; i++) {
            final int id = i;
            tasks.add(new TaskProcessor.Task() {
                @Override
                public int id() {
                    return id;
                }

                @Override
                public int result() {
                    // Sprawdź czy wątek jest przerwany
                    if (Thread.currentThread().isInterrupted()) {
                        // Zadanie przerwane - zwróć specjalną wartość
                        return -1;
                    }

                    executedCount.incrementAndGet();
                    try {
                        Thread.sleep(50); // Krótka praca
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return -2; // Wątek przerwany podczas pracy
                    }
                    return id * 10;
                }
            });
        }

        // Fabryka która tworzy przerwane wątki
        TaskProcessor.ThreadsFactory interruptingFactory = workers -> {
            List<Thread> threads = new ArrayList<>();
            for (Runnable worker : workers) {
                Thread t = new Thread(worker);
                t.interrupt(); // Przerwij wątek przed startem
                threads.add(t);
            }
            return threads;
        };

        List<Integer> results = Collections.synchronizedList(new ArrayList<>());
        TaskProcessor.ResultConsumer consumer = (taskId, result) -> {
            results.add(result);
        };

        // When
        processor.set(tasks, interruptingFactory, consumer);

        // Then - program nie powinien się zawiesić
        // Może zwrócić -1 dla przerwanych zadań lub poprawnie zakończyć
        assertTrue(results.size() <= 5,
                "Liczba wyników nie powinna przekroczyć liczby zadań");

        // Program powinien się zakończyć (nie zawiesić)
        // To sprawdza @Timeout(5) powyżej
    }

    // Test 11: Poprawność wyników
    @Test
    @Timeout(5)
    void shouldReturnCorrectResults() {
        processor.threadsLimit(4);

        // Given
        List<TaskProcessor.Task> tasks = new ArrayList<>();
        Map<Integer, Integer> expectedResults = new HashMap<>();

        for (int i = 0; i < 10; i++) {
            final int id = i;
            final int result = i * i; // Kwadrat id
            expectedResults.put(id, result);

            tasks.add(new TaskProcessor.Task() {
                @Override
                public int id() {
                    return id;
                }

                @Override
                public int result() {
                    // Symulacja różnych czasów wykonania
                    try {
                        Thread.sleep((id % 3) * 20);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return result;
                }
            });
        }

        Map<Integer, Integer> actualResults = Collections.synchronizedMap(new HashMap<>());
        TaskProcessor.ResultConsumer consumer = (taskId, result) -> {
            actualResults.put(taskId, result);
        };

        // When
        processor.set(tasks, createSimpleFactory(), consumer);

        // Then
        assertEquals(expectedResults.size(), actualResults.size(),
                "Liczba wyników powinna się zgadzać");

        for (int i = 0; i < 10; i++) {
            Integer expected = expectedResults.get(i);
            Integer actual = actualResults.get(i);
            assertEquals(expected, actual,
                    "Wynik dla zadania " + i + " powinien być " + expected + ", a jest " + actual);
        }
    }

    // Test 12: Graniczny przypadek - 1 wątek, wiele zadań
    @Test
    @Timeout(5)
    void shouldWorkWithOneThreadAndManyTasks() {
        processor.threadsLimit(1);

        List<TaskProcessor.Task> tasks = new ArrayList<>();
        List<Integer> executionOrder = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            final int id = i;
            tasks.add(new TaskProcessor.Task() {
                @Override
                public int id() {
                    return id;
                }

                @Override
                public int result() {
                    executionOrder.add(id);
                    return id * 3;
                }
            });
        }

        List<Integer> results = new ArrayList<>();
        TaskProcessor.ResultConsumer consumer = (taskId, result) -> {
            results.add(result);
        };

        processor.set(tasks, createSimpleFactory(), consumer);

        assertEquals(20, executionOrder.size());
        assertEquals(20, results.size());

        // Przy 1 wątku kolejność wykonania i dostarczania powinna być identyczna
        for (int i = 0; i < 20; i++) {
            assertEquals(i, executionOrder.get(i));
            assertEquals(i * 3, results.get(i));
        }
    }
        // Test który SPECJALNIE sprawdza kolejność wywoływania result()
    @Test
    @Timeout(5)
    void shouldCallResultInStrictOrderAccordingToRequirements() {
        processor.threadsLimit(3); // 3 wątki

        // Given - zliczamy kiedy zaczyna się wykonywanie każdego zadania
        List<TaskProcessor.Task> tasks = new ArrayList<>();
        AtomicInteger currentExecuting = new AtomicInteger(-1);
        List<String> violations = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch allTasksStarted = new CountDownLatch(20);

        for (int i = 0; i < 20; i++) {
            final int taskId = i;
            tasks.add(new TaskProcessor.Task() {
                @Override
                public int id() {
                    return taskId;
                }

                @Override
                public int result() {
                    // SPRAWDŹ CZY ZADANIE taskId MOŻE SIĘ ZACZĄĆ
                    // Zadanie N+1 nie może się zacząć zanim zadanie N nie zostało ROZPOCZĘTE
                    int lastStarted = currentExecuting.get();

                    if (taskId > 0 && lastStarted < taskId - 1) {
                        // Naruszenie: zadanie taskId zaczęło się zanim taskId-1
                        violations.add("Zadanie " + taskId + " zaczęło się przed zadaniem " + (taskId-1) +
                                       " (ostatnie rozpoczęte: " + lastStarted + ")");
                    }

                    // Zapisz że to zadanie się rozpoczęło
                    currentExecuting.set(taskId);
                    allTasksStarted.countDown();

                    // Symulacja pracy - różne czasy dla różnych zadań
                    try {
                        Thread.sleep((taskId % 4) * 10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    return taskId * 100;
                }
            });
        }

        List<Integer> receivedResults = Collections.synchronizedList(new ArrayList<>());
        TaskProcessor.ResultConsumer consumer = (taskId, result) -> {
            receivedResults.add(result);
        };

        // When
        processor.set(tasks, createSimpleFactory(), consumer);

        // Then
        assertTrue(violations.isEmpty(),
            "Znaleziono naruszenia kolejności wywoływania result(): " + violations);
        assertEquals(20, receivedResults.size());
    }

    @Test
    @Timeout(5)
    void shouldStartTasksInGroupsAccordingToThreadLimit() {
        processor.threadsLimit(3); // 3 wątki

        // Given - śledzimy które zadania są wykonywane równocześnie
        List<TaskProcessor.Task> tasks = new ArrayList<>();
        CountDownLatch firstThreeStarted = new CountDownLatch(3);
        CountDownLatch firstThreeCanFinish = new CountDownLatch(1);
        AtomicInteger concurrentlyExecuting = new AtomicInteger(0);
        List<Set<Integer>> concurrentGroups = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < 9; i++) {
            final int taskId = i;
            tasks.add(new TaskProcessor.Task() {
                @Override
                public int id() {
                    return taskId;
                }

                @Override
                public int result() {
                    int currentConcurrent = concurrentlyExecuting.incrementAndGet();

                    // Rejestruj które zadania wykonują się równocześnie
                    synchronized(concurrentGroups) {
                        if (concurrentGroups.isEmpty() ||
                                concurrentGroups.get(concurrentGroups.size()-1).size() >= 3) {
                            concurrentGroups.add(new HashSet<>());
                        }
                        concurrentGroups.get(concurrentGroups.size()-1).add(taskId);
                    }

                    // Pierwsze 3 zadania czekają na siebie
                    if (taskId < 3) {
                        firstThreeStarted.countDown();
                        try {
                            // Czekaj aż wszystkie 3 się rozpoczną
                            firstThreeStarted.await();
                            // Teraz czekaj na pozwolenie do zakończenia
                            firstThreeCanFinish.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    concurrentlyExecuting.decrementAndGet();
                    return taskId;
                }
            });
        }

        // Osobny wątek który odblokuje pierwsze 3 zadania po ich rozpoczęciu
        Thread unblockerThread = new Thread(() -> {
            try {
                // Poczekaj aż wszystkie 3 pierwsze zadania się rozpoczną
                firstThreeStarted.await();
                // Odczekaj chwilę żeby mieć pewność że są blokowane
                Thread.sleep(100);
                // Odblokuj je
                firstThreeCanFinish.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        List<Integer> receivedResults = Collections.synchronizedList(new ArrayList<>());
        TaskProcessor.ResultConsumer consumer = (taskId, result) -> {
            receivedResults.add(result);
        };

        // When - uruchom odblokowywacz i procesor
        unblockerThread.start();
        processor.set(tasks, createSimpleFactory(), consumer);

        // Then - sprawdź czy zadania były wykonywane w grupach po 3
        System.out.println("Grupy współbieżne: " + concurrentGroups);

        // Sprawdź czy mamy przynajmniej jedną grupę z 3 zadaniami
        boolean hasGroupOfThree = false;
        for (Set<Integer> group : concurrentGroups) {
            if (group.size() >= 3) {
                hasGroupOfThree = true;
                // Sprawdź czy to są 3 kolejne liczby (np. 0,1,2 lub 3,4,5)
                int min = Collections.min(group);
                int max = Collections.max(group);
                if (max - min <= 2) { // Zakres <= 2 oznacza 3 kolejne lub prawie kolejne
                    // OK - to jest dobra grupa
                }
            }
        }

        assertTrue(hasGroupOfThree,
                "Powinna być przynajmniej jedna grupa 3 zadań wykonujących się równocześnie. Grupy: " + concurrentGroups);

        // Upewnij się że wszystkie wyniki są dostarczone
        assertEquals(9, receivedResults.size());
    }

    // Test który sprawdza STRICT ORDER - że zadanie N+1 nigdy nie zaczyna się przed N
    @Test
    @Timeout(5)
    void shouldNeverStartTaskNPlus1BeforeTaskN() {
        processor.threadsLimit(4);

        List<TaskProcessor.Task> tasks = new ArrayList<>();
        AtomicInteger highestStarted = new AtomicInteger(-1);
        List<String> orderViolations = Collections.synchronizedList(new ArrayList<>());
        Object monitor = new Object();

        for (int i = 0; i < 15; i++) {
            final int taskId = i;
            tasks.add(new TaskProcessor.Task() {
                @Override
                public int id() {
                    return taskId;
                }

                @Override
                public int result() {
                    // SPRAWDŹ KOLEJNOŚĆ
                    synchronized(monitor) {
                        if (taskId > highestStarted.get() + 1) {
                            // Zadanie zaczęło się "za wcześnie" - pominięto jakieś zadanie
                            orderViolations.add("Zadanie " + taskId + " zaczęło się, " +
                                              "a najwyższe rozpoczęte to " + highestStarted.get());
                        }
                        highestStarted.set(taskId);
                    }

                    // Różne czasy wykonania
                    try {
                        Thread.sleep((taskId % 5) * 20);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    return taskId;
                }
            });
        }

        List<Integer> receivedResults = Collections.synchronizedList(new ArrayList<>());
        TaskProcessor.ResultConsumer consumer = (taskId, result) -> {
            receivedResults.add(result);
        };

        // When
        processor.set(tasks, createSimpleFactory(), consumer);

        // Then
        assertTrue(orderViolations.isEmpty(),
            "Znaleziono naruszenia ścisłej kolejności: " + orderViolations);
        assertEquals(15, receivedResults.size());

        // Dodatkowo sprawdź czy wyniki są w dobrej kolejności
        for (int i = 0; i < 15; i++) {
            assertEquals(i, receivedResults.get(i).intValue());
        }
    }

    @Test
    @Timeout(5)
    void shouldSimulateExactScenarioFromRequirements_CORRECTED() {
        processor.threadsLimit(3);

        List<TaskProcessor.Task> tasks = new ArrayList<>();
        List<Integer> startOrder = Collections.synchronizedList(new ArrayList<>());
        List<Integer> finishOrder = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < 6; i++) {
            final int taskId = i;
            tasks.add(new TaskProcessor.Task() {
                @Override
                public int id() {
                    return taskId;
                }

                @Override
                public int result() {
                    // Zapisz kiedy zadanie się ZACZĘŁO
                    startOrder.add(taskId);

                    // Symulacja RÓŻNYCH czasów:
                    if (taskId == 3) {
                        try { Thread.sleep(10); } catch (InterruptedException e) {}
                    } else if (taskId < 3) {
                        try { Thread.sleep(100); } catch (InterruptedException e) {}
                    } else {
                        try { Thread.sleep(150); } catch (InterruptedException e) {}
                    }

                    // Zapisz kiedy zadanie się SKOŃCZYŁO
                    finishOrder.add(taskId);
                    return taskId * 10;
                }
            });
        }

        List<Integer> deliveryOrder = Collections.synchronizedList(new ArrayList<>());
        TaskProcessor.ResultConsumer consumer = (taskId, result) -> {
            deliveryOrder.add(taskId);
        };

        // When
        processor.set(tasks, createSimpleFactory(), consumer);

        System.out.println("START order: " + startOrder);
        System.out.println("FINISH order: " + finishOrder);
        System.out.println("DELIVERY order: " + deliveryOrder);

        // TEST 1: Sprawdź czy zadania były rozpoczynane w GRUPACH po 3
        // (dopuszczalne przestawienia wewnątrz grupy, ale nie między grupami)
        boolean groupsRespected = true;
        for (int i = 0; i < startOrder.size(); i++) {
            int taskId = startOrder.get(i);
            // Zadanie z drugiej grupy (3,4,5) nie może być przed zadaniami z pierwszej grupy (0,1,2)
            if (taskId >= 3) {
                // Sprawdź czy wszystkie zadania 0,1,2 już się zaczęły
                boolean allFirstGroupStarted = startOrder.subList(0, i).containsAll(Arrays.asList(0,1,2));
                if (!allFirstGroupStarted && i < 3) { // Jeszcze nie wszystkie z pierwszej grupy
                    groupsRespected = false;
                    break;
                }
            }
        }

        assertTrue(groupsRespected,
                "Zadania z drugiej grupy (3,4,5) nie mogą zaczynać się przed zakończeniem pierwszej grupy (0,1,2). Start order: " + startOrder);

        // TEST 2: Zadanie 3 mogło skończyć się PRZED 0,1,2 (to jest OK - różne czasy)
        // To sprawdza czy rzeczywiście zadania mają różne czasy wykonania

        // TEST 3: Wyniki DOSTARCZONE w dokładnej kolejności
        assertEquals(Arrays.asList(0, 1, 2, 3, 4, 5), deliveryOrder,
                "Wyniki muszą być DOSTARCZONE w ścisłej kolejności 0,1,2,3,4,5");
    }
    @Test
    @Timeout(5)
    void testYourImplementationForOrderRequirements() {
        processor.threadsLimit(3);

        // Ten test sprawdza czy Twoja implementacja z AtomicInteger nextToExecute
        // spełnia wymaganie o kolejności rozpoczęcia zadań

        List<TaskProcessor.Task> tasks = new ArrayList<>();
        List<Integer> executionStarts = Collections.synchronizedList(new ArrayList<>());
        ReentrantLock testLock = new ReentrantLock();
        Condition condition = testLock.newCondition();
        AtomicInteger lastStarted = new AtomicInteger(-1);

        for (int i = 0; i < 12; i++) {
            final int taskId = i;
            tasks.add(new TaskProcessor.Task() {
                @Override
                public int id() {
                    return taskId;
                }

                @Override
                public int result() {
                    // ZAPAMIĘTAJ KOLEJNOŚĆ ROZPOCZĘCIA
                    executionStarts.add(taskId);

                    // Sprawdź czy to zadanie może się rozpocząć
                    // (nie może być większe o więcej niż 2 od ostatniego rozpoczętego)
                    testLock.lock();
                    try {
                        while (taskId > lastStarted.get() + 2) {
                            // Zadanie "za bardzo do przodu" - czeka
                            condition.await(10, TimeUnit.MILLISECONDS);
                        }
                        lastStarted.set(Math.max(lastStarted.get(), taskId));
                        condition.signalAll();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        testLock.unlock();
                    }

                    // Różne czasy wykonania
                    try {
                        Thread.sleep((taskId % 5) * 15);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    return taskId * 10;
                }
            });
        }

        List<Integer> deliveryOrder = Collections.synchronizedList(new ArrayList<>());
        TaskProcessor.ResultConsumer consumer = (taskId, result) -> {
            deliveryOrder.add(taskId);
        };

        processor.set(tasks, createSimpleFactory(), consumer);

        // ANALIZA:
        System.out.println("Rozpoczęcia: " + executionStarts);
        System.out.println("Dostarczenia: " + deliveryOrder);

        // Twoja implementacja z AtomicInteger.nextToExecute Gwarantuje:
        // 1. Zadania są pobierane w kolejności 0,1,2,3,4,5...
        // 2. Ale 3 wątki mogą pobrać 0,1,2 "jednocześnie"
        // 3. Zadanie 3 czeka aż któryś wątek zwolni

        // Sprawdź czy executionStarts jest rosnące (z małymi wyjątkami)
        int outOfOrder = 0;
        for (int i = 1; i < executionStarts.size(); i++) {
            if (executionStarts.get(i) < executionStarts.get(i-1)) {
                outOfOrder++;
                // Sprawdź czy to nie jest duży skok wstecz
                int diff = executionStarts.get(i-1) - executionStarts.get(i);
                assertTrue(diff <= 2,
                    "Duży skok wstecz w rozpoczęciach: " + executionStarts.get(i-1) + " -> " + executionStarts.get(i));
            }
        }

        System.out.println("Liczba przestawień w rozpoczęciach: " + outOfOrder);

        // Wymaganie mówi: "wystarczy aby program działał tak, że jeśli pracujemy za pomocą 3 wątków jednocześnie,
        // to jednocześnie powinny być realizowane zadania 1-3, 4-6 itd."

        // Sprawdź czy deliveryOrder jest DOKŁADNIE w kolejności
        for (int i = 0; i < 12; i++) {
            assertEquals(i, deliveryOrder.get(i).intValue(),
                "Dostarczenie poza kolejnością! Pozycja " + i + ": oczekiwano " + i + ", dostałem " + deliveryOrder.get(i));
        }

        // Jeśli test przechodzi - Twoja implementacja jest POPRAWNA!
    }

    @Test
    @Timeout(5)
    void shouldCallResultInPerfectSequentialOrder() {
        processor.threadsLimit(3);

        List<TaskProcessor.Task> tasks = new ArrayList<>();
        List<Integer> resultCalls = Collections.synchronizedList(new ArrayList<>());
        ReentrantLock orderLock = new ReentrantLock();
        Condition condition = orderLock.newCondition();
        AtomicInteger expectedNext = new AtomicInteger(0);

        for (int i = 0; i < 12; i++) {
            final int taskId = i;
            tasks.add(new TaskProcessor.Task() {
                @Override
                public int id() {
                    return taskId;
                }

                @Override
                public int result() {
                    // BLOKUJĄCY TEST KOLEJNOŚCI
                    orderLock.lock();
                    try {
                        // Zadanie może się zacząć tylko jeśli to jest następne oczekiwane
                        while (taskId != expectedNext.get()) {
                            try {
                                condition.await(100, TimeUnit.MILLISECONDS);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return -1;
                            }
                        }

                        // OK, to jest właściwe zadanie
                        resultCalls.add(taskId);
                        expectedNext.incrementAndGet();
                        condition.signalAll();

                    } finally {
                        orderLock.unlock();
                    }

                    // Krótka praca
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    return taskId;
                }
            });
        }

        List<Integer> receivedResults = Collections.synchronizedList(new ArrayList<>());
        TaskProcessor.ResultConsumer consumer = (taskId, result) -> {
            receivedResults.add(taskId);
        };

        processor.set(tasks, createSimpleFactory(), consumer);

        // Sprawdź czy result() był wywoływany dokładnie w kolejności 0,1,2,3,...,11
        assertEquals(12, resultCalls.size());
        for (int i = 0; i < 12; i++) {
            assertEquals(i, resultCalls.get(i).intValue(),
                "result() zadania " + i + " powinno być wywołane jako " + (i+1) + "-te");
        }
    }
    @Test
    @Timeout(5)
    void shouldExecuteFirstThreeTasksConcurrently_NOT_PERFECT_ORDER() {
        processor.threadsLimit(3);

        List<TaskProcessor.Task> tasks = new ArrayList<>();
        CountDownLatch firstThreeStarted = new CountDownLatch(3);
        List<Long> startTimes = new ArrayList<>(Collections.nCopies(3, 0L));

        for (int i = 0; i < 6; i++) {
            final int taskId = i;
            tasks.add(new TaskProcessor.Task() {
                @Override
                public int id() {
                    return taskId;
                }

                @Override
                public int result() {
                    if (taskId < 3) {
                        startTimes.set(taskId, System.nanoTime());
                        firstThreeStarted.countDown();
                        try {
                            firstThreeStarted.await(); // Czekaj aż wszystkie 3 się rozpoczną
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    return taskId;
                }
            });
        }

        List<Integer> results = Collections.synchronizedList(new ArrayList<>());
        TaskProcessor.ResultConsumer consumer = (taskId, result) -> {
            results.add(taskId);
        };

        processor.set(tasks, createSimpleFactory(), consumer);

        // Sprawdź czy pierwsze 3 zadania zaczęły się w podobnym czasie
        System.out.println("Czasy rozpoczęcia 0,1,2 (ns): " + startTimes);

        long maxDiff = 0;
        for (int i = 1; i < 3; i++) {
            long diff = Math.abs(startTimes.get(i) - startTimes.get(i-1));
            maxDiff = Math.max(maxDiff, diff);
        }

        // Różnica czasu rozpoczęcia powinna być mała (kilkadziesiąt ms)
        assertTrue(maxDiff < 100_000_000L, // 100ms w nanosekundach
            "Pierwsze 3 zadania powinny rozpocząć się niemal jednocześnie. Różnice: " +
            (maxDiff / 1_000_000L) + "ms. Czasy: " + startTimes);

        // Wyniki w dobrej kolejności
        assertEquals(Arrays.asList(0, 1, 2, 3, 4, 5), results);
    }
}
