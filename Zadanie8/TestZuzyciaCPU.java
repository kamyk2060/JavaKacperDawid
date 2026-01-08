import java.io.IOException;

public class TestZuzyciaCPU {
    public static void main(String[] args) throws InterruptedException, IOException {
        MultithreadedIntegaration kalkulator = new MultithreadedIntegaration();

        // Funkcja, która zużywa dużo CPU
        kalkulator.setFunction(x -> {
            double wynik = 0;
            // Symulacja ciężkich obliczeń - pętla zużywająca CPU
            for (int i = 0; i < 100000000; i++) {
                wynik += Math.sin(x * i) * Math.cos(x / (i + 1));
            }
            return wynik;
        });

        Range przedzial = new SimpleRange(0, 10);

        System.out.println("Rozpoczynam test zużycia CPU...");
        System.out.println("Uruchamiam 4 wątki na 10 sekund");
        System.out.println("Sprawdź w menedżerze zadań!");
        System.out.println("Naciśnij Enter, aby kontynuować...");
        System.in.read();

        kalkulator.setThreadsNumber(4);

        // Mierzymy czas
        long start = System.currentTimeMillis();
        kalkulator.calc(przedzial, 1000); // 1000 podprzedziałów
        long koniec = System.currentTimeMillis();

        System.out.println("Czas obliczeń: " + (koniec - start) + " ms");
        System.out.println("Wynik: " + kalkulator.getResult());
    }
}