import java.util.*;

// Dobry kod :>
class MazeSolver implements Maze {

    private int liczbaWierszy;
    private int liczbaKolumn;
    public boolean[][] czyZajete;
    private Set<Square> zajetePola;

    // wyniki dla kazdej kolumny
    private List<Integer> najdalejWiersz;
    private List<Integer> iloscPol;
    private Set<Square> polaNiedostepne;

    @Override
    public void rows(int rows) {
        this.liczbaWierszy = rows;
    }

    @Override
    public void cols(int cols) {
        this.liczbaKolumn = cols;
    }

    @Override
    public void occupiedSquare(Set<Square> squares) {
        this.zajetePola = squares;

        // tworzymy tablice z informacja ktore pola sa zajete
        czyZajete = new boolean[liczbaWierszy][liczbaKolumn];

        for (Square pole : zajetePola) {
            int r = pole.row();
            int c = pole.col();
            if (r >= 0 && r < liczbaWierszy && c >= 0 && c < liczbaKolumn) {
                czyZajete[r][c] = true;
            }
        }

        // przygotowujemy listy na wyniki
        najdalejWiersz = new ArrayList<>();
        iloscPol = new ArrayList<>();

        // zbior wszystkich pol ktore kiedykolwiek odwiedzilismy
        Set<Square> odwiedzoneKiedykolwiek = new HashSet<>();

        // przechodzimy po kazdej kolumnie z wiersza 0
        for (int kolumna = 0; kolumna < liczbaKolumn; kolumna++) {
            Square startowePole = new Square(kolumna, 0);

            // sprawdzamy czy pole startowe jest wolne
            if (czyZajete[0][kolumna]) {
                najdalejWiersz.add(0);
                iloscPol.add(0);
                continue;
            }

            // robimy przeszukiwanie wszerz (BFS)
            Set<Square> odwiedzoneTuTaj = new HashSet<>();
            Queue<Square> doSprawdzenia = new LinkedList<>();

            doSprawdzenia.add(startowePole);
            odwiedzoneTuTaj.add(startowePole);

            int maksymalnyWiersz = 0;

            // poki mamy cos do sprawdzenia
            while (!doSprawdzenia.isEmpty()) {
                Square aktualnie = doSprawdzenia.poll();

                // aktualizujemy najdalszy wiersz
                int aktualnyWiersz = aktualnie.row();
                if (aktualnyWiersz > maksymalnyWiersz) {
                    maksymalnyWiersz = aktualnyWiersz;
                }

                // pobieramy sasiadow
                Set<Square> sasiedzi = aktualnie.neighbours();

                // sprawdzamy kazdego sasiada
                for (Square sasiad : sasiedzi) {
                    int r = sasiad.row();
                    int c = sasiad.col();

                    // czy sasiad jest w granicach planszy?
                    if (r < 0 || r >= liczbaWierszy || c < 0 || c >= liczbaKolumn) {
                        continue; // jest poza plansza
                    }

                    // czy to pole jest zajete?
                    if (czyZajete[r][c]) {
                        continue; // jest zajete
                    }

                    // czy juz bylo odwiedzone?
                    if (odwiedzoneTuTaj.contains(sasiad)) {
                        continue; // juz tam bylismy
                    }

                    // dodajemy do odwiedzonych i kolejki
                    odwiedzoneTuTaj.add(sasiad);
                    doSprawdzenia.add(sasiad);
                }
            }

            // zapisujemy wyniki dla tej kolumny
            najdalejWiersz.add(maksymalnyWiersz);
            iloscPol.add(odwiedzoneTuTaj.size());

            // dodajemy wszystkie odwiedzone pola do globalnego zbioru
            for (Square odwiedzone : odwiedzoneTuTaj) {
                odwiedzoneKiedykolwiek.add(odwiedzone);
            }
        }

        // teraz znajdujemy pola niedostepne
        polaNiedostepne = new HashSet<>();

        for (int w = 0; w < liczbaWierszy; w++) {
            for (int k = 0; k < liczbaKolumn; k++) {
                Square pole = new Square(k, w);

                // jesli pole nie jest zajete i nigdy nie zostalo odwiedzone
                boolean jestZajete = czyZajete[w][k];
                boolean byloOdwiedzone = odwiedzoneKiedykolwiek.contains(pole);

                if (!jestZajete && !byloOdwiedzone) {
                    polaNiedostepne.add(pole);
                }
            }
        }
    }

    @Override
    public List<Integer> howFar() {
        return najdalejWiersz;
    }

    @Override
    public List<Integer> area() {
        return iloscPol;
    }

    @Override
    public Set<Square> unreachableSquares() {
        return polaNiedostepne;
    }
}