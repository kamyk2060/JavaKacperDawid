// Dobry kod :>

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class MultithreadedTaskProcessor implements TaskProcessor {

    private int limitWatkow;

    @Override
    public void threadsLimit(int limit) {
        this.limitWatkow = limit;
    }

    @Override
    public void set(List<Task> tasks, ThreadsFactory factory, ResultConsumer consumer) {

        // tablica na wyniki - indeks to numer zadania
        int[] wyniki = new int[tasks.size()];
        boolean[] czyGotowe = new boolean[tasks.size()];

        // kolejka z numerami zadan do zrobienia
        BlockingQueue<Integer> kolejkaZadan = new LinkedBlockingQueue<>();

        // wrzucamy wszystkie numery zadan do kolejki
        for (int i = 0; i < tasks.size(); i++) {
            kolejkaZadan.add(i);
        }

        // obiekt do synchronizacji
        Object blokada = new Object();

        // licznik ile juz oddano wynikow
        int[] nastepnyDoOddania = new int[1];
        nastepnyDoOddania[0] = 0;

        // tworzymy liste pracownikow
        List<Runnable> pracownicy = new ArrayList<>();

        for (int i = 0; i < limitWatkow; i++) {
            Runnable pracownik = new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        // pobieramy numer zadania z kolejki
                        Integer numerZadania = kolejkaZadan.poll();

                        // jak null to koniec pracy
                        if (numerZadania == null) {
                            break;
                        }

                        // wykonujemy zadanie
                        Task zadanie = tasks.get(numerZadania);
                        int wynik = zadanie.result();

                        // zapisujemy wynik
                        synchronized (blokada) {
                            wyniki[numerZadania] = wynik;
                            czyGotowe[numerZadania] = true;

                            // probujemy oddac wyniki po kolei
                            while (nastepnyDoOddania[0] < tasks.size() && czyGotowe[nastepnyDoOddania[0]]) {
                                int nr = nastepnyDoOddania[0];
                                Task zad = tasks.get(nr);
                                consumer.save(zad.id(), wyniki[nr]);
                                nastepnyDoOddania[0]++;
                            }

                            // budzimy glowny watek jak wszystko zrobione
                            if (nastepnyDoOddania[0] == tasks.size()) {
                                blokada.notifyAll();
                            }
                        }
                    }
                }
            };
            pracownicy.add(pracownik);
        }

        // tworzymy watki przez fabryke
        List<Thread> watki = factory.createThreads(pracownicy);

        // uruchamiamy wszystkie watki
        for (Thread watek : watki) {
            watek.start();
        }

        // czekamy az wszystko sie skonczy
        synchronized (blokada) {
            while (nastepnyDoOddania[0] < tasks.size()) {
                try {
                    blokada.wait();
                } catch (InterruptedException e) {
                    // ignorujemy
                }
            }
        }

        // czekamy na zakonczenie watkow
        for (Thread watek : watki) {
            try {
                watek.join();
            } catch (InterruptedException e) {
                // ignorujemy
            }
        }
    }
}