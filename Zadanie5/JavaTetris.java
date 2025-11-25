import java.util.*;

public class JavaTetris implements Tetris {
    private int wysokosc;
    private int szerokosc;
    private boolean[][] plansza;
    private List<Integer> stan;

    public JavaTetris() {
        stan = new ArrayList<>();
    }

    @Override
    public void rows(int rows) {
        wysokosc = rows;
        inicjujPlansze();
    }

    @Override
    public void cols(int cols) {
        szerokosc = cols;
        inicjujPlansze();
    }

    private void inicjujPlansze() {
        if (wysokosc > 0 && szerokosc > 0) {
            plansza = new boolean[wysokosc + 1][szerokosc];
            stan = new ArrayList<>();
            for (int i = 0; i < szerokosc; i++) {
                stan.add(0);
            }
        }
    }

    @Override
    public void drop(Block block) {
        int kolumna = block.base().col();
        int wiersz = znajdzMiejsce(block, kolumna);
        if (wiersz != -1) {
            polozKlocek(block, kolumna, wiersz);
        }
    }

    @Override
    public void optimalDrop(Block block) {
        int najlepszaKolumna = -1;
        int najlepszyWiersz = -1;
        int najlepszyWynik = Integer.MAX_VALUE;

        for (int kolumna = 0; kolumna < szerokosc; kolumna++) {
            if (!czyKlocekMiesciSie(block, kolumna)) continue;

            int wiersz = znajdzMiejsce(block, kolumna);
            if (wiersz == -1) continue;

            // Symulujemy co się stanie
            boolean[][] kopia = zrobKopiePlanszy();

            // Oznaczamy które kratki należą do tego klocka
            boolean[][] kratkiKlocka = new boolean[wysokosc + 1][szerokosc];
            polozKlocekNaPlanszy(kopia, block, kolumna, wiersz, kratkiKlocka);
            usunPelneWiersze(kopia, kratkiKlocka);

            // Szukamy najwyższej kratki która należy do klocka
            int najwyzszaKratkaKlocka = znajdzNajwyzszaKratkeKlocka(kratkiKlocka);

            if (najwyzszaKratkaKlocka < najlepszyWynik ||
                    (najwyzszaKratkaKlocka == najlepszyWynik && kolumna < najlepszaKolumna)) {
                najlepszyWynik = najwyzszaKratkaKlocka;
                najlepszaKolumna = kolumna;
                najlepszyWiersz = wiersz;
            }
        }

        if (najlepszaKolumna != -1) {
            polozKlocek(block, najlepszaKolumna, najlepszyWiersz);
        }
    }

    private boolean czyKlocekMiesciSie(Block klocek, int kolumnaBazowa) {
        Position baza = klocek.base();

        // Sprawdzamy kratkę bazową
        if (kolumnaBazowa < 0 || kolumnaBazowa >= szerokosc) return false;

        // Sprawdzamy wszystkie kratki klocka
        for (Vector v : klocek.squares()) {
            int kolumna = kolumnaBazowa + v.dCol();
            if (kolumna < 0 || kolumna >= szerokosc) return false;
        }
        return true;
    }

    private int znajdzMiejsce(Block klocek, int kolumnaBazowa) {
        // Klocek spada od góry do dołu
        for (int wierszBazowy = wysokosc; wierszBazowy >= 1; wierszBazowy--) {
            if (czyMoznaPolozyc(klocek, kolumnaBazowa, wierszBazowy)) {
                // Sprawdzamy czy klocek dotyka dna lub innego klocka
                if (czyDotykaDnaLubKlocka(klocek, kolumnaBazowa, wierszBazowy)) {
                    return wierszBazowy;
                }
            }
        }
        return -1;
    }

    private boolean czyMoznaPolozyc(Block klocek, int kolumnaBazowa, int wierszBazowy) {
        // Sprawdzamy kratkę bazową
        if (!czyWolne(kolumnaBazowa, wierszBazowy)) return false;

        // Sprawdzamy wszystkie kratki klocka
        for (Vector v : klocek.squares()) {
            int kolumna = kolumnaBazowa + v.dCol();
            int wiersz = wierszBazowy + v.dRow();
            if (!czyWolne(kolumna, wiersz)) return false;
        }

        return true;
    }

    private boolean czyDotykaDnaLubKlocka(Block klocek, int kolumnaBazowa, int wierszBazowy) {
        // Sprawdzamy kratkę bazową
        if (wierszBazowy == 1) return true;
        if (czyZajete(kolumnaBazowa, wierszBazowy - 1)) return true;

        // Sprawdzamy wszystkie kratki klocka
        for (Vector v : klocek.squares()) {
            int kolumna = kolumnaBazowa + v.dCol();
            int wiersz = wierszBazowy + v.dRow();
            if (wiersz == 1) return true;
            if (czyZajete(kolumna, wiersz - 1)) return true;
        }

        return false;
    }

    private boolean czyWolne(int kolumna, int wiersz) {
        if (kolumna < 0 || kolumna >= szerokosc || wiersz < 1 || wiersz > wysokosc)
            return false;
        return !plansza[wiersz][kolumna];
    }

    private boolean czyZajete(int kolumna, int wiersz) {
        if (kolumna < 0 || kolumna >= szerokosc || wiersz < 1 || wiersz > wysokosc)
            return false;
        return plansza[wiersz][kolumna];
    }

    private void polozKlocek(Block klocek, int kolumnaBazowa, int wierszBazowy) {
        polozKlocekNaPlanszy(plansza, klocek, kolumnaBazowa, wierszBazowy, null);
        usunPelneWiersze(plansza, null);
        aktualizujStan();
    }

    private void polozKlocekNaPlanszy(boolean[][] p, Block klocek, int kolumnaBazowa, int wierszBazowy, boolean[][] kratkiKlocka) {
        // Kładziemy kratkę bazową
        p[wierszBazowy][kolumnaBazowa] = true;
        if (kratkiKlocka != null) kratkiKlocka[wierszBazowy][kolumnaBazowa] = true;

        // Kładziemy pozostałe kratki
        for (Vector v : klocek.squares()) {
            int kolumna = kolumnaBazowa + v.dCol();
            int wiersz = wierszBazowy + v.dRow();
            if (kolumna >= 0 && kolumna < szerokosc && wiersz >= 1 && wiersz <= wysokosc) {
                p[wiersz][kolumna] = true;
                if (kratkiKlocka != null) kratkiKlocka[wiersz][kolumna] = true;
            }
        }
    }

    private void usunPelneWiersze(boolean[][] p, boolean[][] kratkiKlocka) {
        for (int wiersz = 1; wiersz <= wysokosc; wiersz++) {
            boolean pelny = true;

            // Sprawdzamy czy wiersz jest pełny
            for (int kolumna = 0; kolumna < szerokosc; kolumna++) {
                if (!p[wiersz][kolumna]) {
                    pelny = false;
                    break;
                }
            }

            if (pelny) {
                // Przesuwamy wszystko w dół (planszę i oznaczenia klocka)
                for (int w = wiersz; w < wysokosc; w++) {
                    for (int k = 0; k < szerokosc; k++) {
                        p[w][k] = p[w + 1][k];
                        if (kratkiKlocka != null) {
                            kratkiKlocka[w][k] = kratkiKlocka[w + 1][k];
                        }
                    }
                }

                // Czyścimy górny wiersz
                for (int k = 0; k < szerokosc; k++) {
                    p[wysokosc][k] = false;
                    if (kratkiKlocka != null) {
                        kratkiKlocka[wysokosc][k] = false;
                    }
                }

                // Sprawdzamy ten sam wiersz ponownie
                wiersz--;
            }
        }
    }

    private int znajdzNajwyzszaKratkeKlocka(boolean[][] kratkiKlocka) {
        for (int wiersz = wysokosc; wiersz >= 1; wiersz--) {
            for (int kolumna = 0; kolumna < szerokosc; kolumna++) {
                if (kratkiKlocka[wiersz][kolumna]) {
                    return wiersz;
                }
            }
        }
        return 0;
    }

    private boolean[][] zrobKopiePlanszy() {
        boolean[][] kopia = new boolean[wysokosc + 1][szerokosc];
        for (int wiersz = 1; wiersz <= wysokosc; wiersz++) {
            for (int kolumna = 0; kolumna < szerokosc; kolumna++) {
                kopia[wiersz][kolumna] = plansza[wiersz][kolumna];
            }
        }
        return kopia;
    }

    private void aktualizujStan() {
        for (int kolumna = 0; kolumna < szerokosc; kolumna++) {
            int najwyzsza = 0;
            for (int wiersz = wysokosc; wiersz >= 1; wiersz--) {
                if (plansza[wiersz][kolumna]) {
                    najwyzsza = wiersz;
                    break;
                }
            }
            stan.set(kolumna, najwyzsza);
        }
    }

    @Override
    public List<Integer> state() {
        return new ArrayList<>(stan);
    }
}