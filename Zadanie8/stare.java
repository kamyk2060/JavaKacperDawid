import java.util.function.Function;

class STARE implements ParallelIntegaration {

    private Function<Double, Double> funkcja;
    private int liczbaWatkow;
    private double wynik;

    // Osobne zamki dla różnych zasobów współdzielonych
    private final Object lockIndeks = new Object();
    private final Object lockSuma = new Object();

    private Range przedzial;
    private int liczbaPodprzedzialow;
    private double szerokoscProstokata;
    private int nastepnyIndeks;  // Który prostokąt obliczyć następny
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
        this.przedzial = range;
        this.liczbaPodprzedzialow = subintervals;
        this.szerokoscProstokata = (range.max() - range.min()) / subintervals;
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
                Thread.currentThread().interrupt();
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

                // Obliczamy wartość funkcji w środku prostokąta
                double x = przedzial.min() + (indeks + 0.5) * szerokoscProstokata;
                double wartosc = funkcja.apply(x);

                // Dodajemy wynik do wspólnej sumy
                synchronized (lockSuma) {
                    sumaCzesciowa += wartosc;
                }
            }
        }
    }
}