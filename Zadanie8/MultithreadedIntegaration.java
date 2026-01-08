// Dobry kod :>
// z literowka
import java.util.function.Function;

class MultithreadedIntegaration implements ParallelIntegaration {

    private Function<Double, Double> funkcja;
    private int liczbaWatkow;
    private double wynik;

    // Osobne zamki dla różnych zasobów współdzielonych
    private final Object lockIndeks = new Object();
    private final Object lockSuma = new Object();

    private Range przedzial;
    private int liczbaPodprzedzialow;
    private double szerokoscProstokata;
    private int nastepnyIndeks;
    private double sumaCzesciowa;

    @Override
    public void setFunction(Function<Double, Double> function) {
        this.funkcja = function;
    }

    @Override
    public void setThreadsNumber(int threads) {
        this.liczbaWatkow = threads;
    }

    @Override
    public void calc(Range range, int subintervals) {
        double xp = range.min();  // początek
        double xk = range.max();  // koniec

        // Walidacja zakresu
        if (xp >= xk) {
            this.wynik = 0.0;
            return;
        }

        this.przedzial = range;
        this.liczbaPodprzedzialow = subintervals;
        this.szerokoscProstokata = (xk - xp) / subintervals;  // dx
        this.nastepnyIndeks = 0;
        this.sumaCzesciowa = 0.0;

        // Tworzenie i uruchamianie wątków roboczych
        Thread[] watki = new Thread[liczbaWatkow];
        for (int i = 0; i < liczbaWatkow; i++) {
            watki[i] = new Thread(new Worker());
            watki[i].start();
        }

        // Czekamy aż wszystkie wątki zakończą pracę
        for (Thread watek : watki) {
            try {
                watek.join();
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }

        this.wynik = sumaCzesciowa * szerokoscProstokata;
    }

    @Override
    public double getResult() {
        return wynik;
    }

    private class Worker implements Runnable {
        @Override
        public void run() {
            while (true) {
                int indeks;

                // Pobieramy następny prostokąt do obliczenia
                synchronized (lockIndeks) {
                    if (nastepnyIndeks >= liczbaPodprzedzialow) {
                        break;
                    }
                    indeks = nastepnyIndeks;
                    nastepnyIndeks++;
                }

                // Obliczamy granice prostokąta
                double left = przedzial.min() + indeks * szerokoscProstokata;
                double right = left + szerokoscProstokata;
                double mid = (left + right) / 2.0;  // środek przedziału

                // Obliczamy wartość funkcji w środku prostokąta
                double wartosc = funkcja.apply(mid);

                // Dodajemy wynik do wspólnej sumy
                synchronized (lockSuma) {
                    sumaCzesciowa += wartosc;
                }
            }
        }
    }
}