import java.io.*;
import java.util.*;

// Dobry kod java :>
class JavaGradesHelper implements GradesHelper {

    // Lista studentów: imię i nazwisko -> ID
    private HashMap<String, Integer> listaStudentow = new HashMap<>();

    // Zasady oceniania
    private ArrayList<ZasadaOceny> listaZasad = new ArrayList<>();

    @Override
    public void loadStudents(String nazwaPliku) {
        // Czyścimy starą listę
        listaStudentow.clear();

        try {
            // Otwieramy plik
            File plik = new File(nazwaPliku);
            Scanner scanner = new Scanner(plik);

            // Czytamy linijka po linijce
            while (scanner.hasNextLine()) {
                String linia = scanner.nextLine().trim();

                // Pomijamy puste linie
                if (linia.isEmpty()) {
                    continue;
                }

                // Dzielimy linię na części
                String[] czesci = linia.split(";");

                // Potrzebujemy 3 części: ID, imię, nazwisko
                if (czesci.length >= 3) {
                    String idTekst = czesci[0].trim();
                    String imie = czesci[1].trim();
                    String nazwisko = czesci[2].trim();

                    // Sprawdzamy czy dane są poprawne
                    if (!imie.isEmpty() && !nazwisko.isEmpty()) {
                        try {
                            int id = Integer.parseInt(idTekst);

                            // Tworzymy klucz: imię + nazwisko (małymi literami)
                            String klucz = imie.toLowerCase() + " " + nazwisko.toLowerCase();

                            // Dodajemy do listy
                            listaStudentow.put(klucz, id);
                        } catch (NumberFormatException e) {
                            // ID nie jest liczbą - pomijamy
                        }
                    }
                }
            }

            scanner.close();

        } catch (FileNotFoundException e) {
            // Plik nie istnieje
        }
    }

    @Override
    public void loadScoring(String nazwaPliku) throws RangeConflictException, MarkConflictException {
        // Czyścimy stare zasady
        listaZasad.clear();

        // Mapa do sprawdzania powtarzających się ocen
        HashMap<String, ZasadaOceny> sprawdzaczOcen = new HashMap<>();

        try {
            File plik = new File(nazwaPliku);
            Scanner scanner = new Scanner(plik);

            while (scanner.hasNextLine()) {
                String linia = scanner.nextLine().trim();

                if (linia.isEmpty()) {
                    continue;
                }

                String[] czesci = linia.split(";");

                if (czesci.length >= 3) {
                    String ocena = czesci[0].trim();
                    String minTekst = czesci[1].trim();
                    String maxTekst = czesci[2].trim();

                    if (ocena.isEmpty()) {
                        continue;
                    }

                    try {
                        double min = Double.parseDouble(minTekst);
                        double max = Double.parseDouble(maxTekst);

                        // Tworzymy nową zasadę
                        ZasadaOceny nowaZasada = new ZasadaOceny(ocena, min, max);

                        // SPRAWDZENIE 1: Czy ta ocena już istnieje?
                        if (sprawdzaczOcen.containsKey(ocena)) {
                            ZasadaOceny istniejaca = sprawdzaczOcen.get(ocena);

                            // Sprawdzamy czy przedziały są takie same
                            if (istniejaca.min != min || istniejaca.max != max) {
                                // Inne przedziały dla tej samej oceny - BŁĄD
                                throw new MarkConflictException(ocena);
                            }
                            // Takie same przedziały - pomijamy duplikat
                            continue;
                        }

                        // SPRAWDZENIE 2: Czy przedziały się nakładają?
                        for (ZasadaOceny zasada : listaZasad) {
                            if (czyPrzedzialySieNakladaja(zasada, nowaZasada)) {
                                throw new RangeConflictException();
                            }
                        }

                        // Wszystko OK - dodajemy
                        listaZasad.add(nowaZasada);
                        sprawdzaczOcen.put(ocena, nowaZasada);

                    } catch (NumberFormatException e) {
                        // Niepoprawne liczby - pomijamy
                    }
                }
            }

            scanner.close();

            // Sortujemy zasady od najmniejszej oceny
            Collections.sort(listaZasad, new PorownywaczZasad());

        } catch (FileNotFoundException e) {
            // Plik nie istnieje
        }
    }

    @Override
    public Map<Integer, String> generateGrades(String nazwaPliku) throws AssessmentImpossible {
        HashMap<Integer, String> wynik = new HashMap<>();

        // Sprawdzamy czy mamy dane
        if (listaStudentow.isEmpty() || listaZasad.isEmpty()) {
            return wynik;
        }

        try {
            File plik = new File(nazwaPliku);
            Scanner scanner = new Scanner(plik);

            while (scanner.hasNextLine()) {
                String linia = scanner.nextLine().trim();

                if (linia.isEmpty()) {
                    continue;
                }

                String[] czesci = linia.split(";");

                if (czesci.length < 3) {
                    continue;
                }

                String imie = czesci[0].trim();
                String nazwisko = czesci[1].trim();

                // Szukamy studenta
                String klucz = imie.toLowerCase() + " " + nazwisko.toLowerCase();
                Integer id = listaStudentow.get(klucz);

                if (id == null) {
                    // Nie znaleziono studenta
                    throw new AssessmentImpossible(imie, nazwisko);
                }

                // Obliczamy średnią
                double suma = 0;
                int liczbaOcen = 0;

                for (int i = 2; i < czesci.length; i++) {
                    String ocenaTekst = czesci[i].trim();

                    if (!ocenaTekst.isEmpty()) {
                        try {
                            double ocena = Double.parseDouble(ocenaTekst);
                            suma += ocena;
                            liczbaOcen++;
                        } catch (NumberFormatException e) {
                            // Pomijamy niepoprawną ocenę
                        }
                    }
                }

                if (liczbaOcen == 0) {
                    // Brak ocen
                    throw new AssessmentImpossible(imie, nazwisko);
                }

                double srednia = suma / liczbaOcen;

                // Znajdujemy ocenę końcową
                String ocenaKoncowa = null;

                for (ZasadaOceny zasada : listaZasad) {
                    if (srednia >= zasada.min && srednia <= zasada.max) {
                        ocenaKoncowa = zasada.ocena;
                        break;
                    }
                }

                if (ocenaKoncowa == null) {
                    // Nie znaleziono pasującej oceny
                    throw new AssessmentImpossible(imie, nazwisko);
                }

                // Zapamiętujemy wynik
                wynik.put(id, ocenaKoncowa);
            }

            scanner.close();

        } catch (FileNotFoundException e) {
            // Plik nie istnieje
        }

        return wynik;
    }

    // Pomocnicza metoda do sprawdzania nakładania się przedziałów
    private boolean czyPrzedzialySieNakladaja(ZasadaOceny z1, ZasadaOceny z2) {
        // Przedziały NIE nakładają się jeśli:
        // 1. Jeden kończy się przed drugim
        // 2. Jeden zaczyna się po drugim

        if (z1.max < z2.min) {
            return false;
        }

        if (z1.min > z2.max) {
            return false;
        }

        // W pozostałych przypadkach nakładają się
        return true;
    }

    // Klasa pomocnicza do przechowywania zasad oceniania
    private class ZasadaOceny {
        String ocena;
        double min;
        double max;

        ZasadaOceny(String ocena, double min, double max) {
            this.ocena = ocena;
            this.min = min;
            this.max = max;
        }
    }

    // Klasa pomocnicza do sortowania zasad
    private class PorownywaczZasad implements Comparator<ZasadaOceny> {
        @Override
        public int compare(ZasadaOceny z1, ZasadaOceny z2) {
            if (z1.min < z2.min) {
                return -1;
            } else if (z1.min > z2.min) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}