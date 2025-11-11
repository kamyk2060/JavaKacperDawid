import java.util.*;

class TVScheduler implements Scheduler {

    private List<Slot> wszystkieProgramy = new ArrayList<>();

    @Override
    public void addSlot(Slot program) {
        if (program == null) return;
        wszystkieProgramy.add(program);
    }

    @Override
    public Set<List<Slot>> match(Set<String> programs) {
        Set<List<Slot>> dobreKombinacje = new HashSet<>();

        if (programs == null || programs.isEmpty()) {
            return dobreKombinacje;
        }

        // Filtruj tylko interesujące programy
        List<Slot> interesujaceProgramy = new ArrayList<>();
        for (Slot slot : wszystkieProgramy) {
            if (programs.contains(slot.program())) {
                interesujaceProgramy.add(slot);
            }
        }

        // Jeśli nie ma wystarczającej liczby programów
        if (interesujaceProgramy.size() < programs.size()) {
            return dobreKombinacje;
        }

        // Szukamy kombinacji używając backtrackingu
        szukajKombinacji(
                new ArrayList<>(),
                interesujaceProgramy,
                programs.size(),
                dobreKombinacje,
                new HashSet<>(),
                0
        );

        return dobreKombinacje;
    }

    private void szukajKombinacji(List<Slot> wybraneProgramy,
                                  List<Slot> dostepneProgramy,
                                  int ilePotrzeba,
                                  Set<List<Slot>> dobreKombinacje,
                                  Set<String> uzyteNazwy,
                                  int startIndex) {

        // Jeśli mamy komplet
        if (wybraneProgramy.size() == ilePotrzeba) {
            if (czyCzasSieNieNaklada(wybraneProgramy)) {
                // Tworzymy posortowaną kopię według czasu rozpoczęcia aby uniknąć duplikatów
                List<Slot> posortowanaKombinacja = new ArrayList<>(wybraneProgramy);
                posortowanaKombinacja.sort(Comparator.comparingInt(s -> s.atH() * 60 + s.atM()));
                dobreKombinacje.add(posortowanaKombinacja);
            }
            return;
        }

        for (int i = startIndex; i < dostepneProgramy.size(); i++) {
            Slot program = dostepneProgramy.get(i);

            // czy ten program już nie został użyty w kombinacji
            if (uzyteNazwy.contains(program.program())) {
                continue;
            }

            // czy ten program nie koliduje z już wybranymi
            boolean koliduje = false;
            for (Slot wybrany : wybraneProgramy) {
                if (czyDwaProgramySieNakladaja(wybrany, program)) {
                    koliduje = true;
                    break;
                }
            }

            if (koliduje) {
                continue;
            }

            wybraneProgramy.add(program);
            uzyteNazwy.add(program.program());

            szukajKombinacji(wybraneProgramy, dostepneProgramy, ilePotrzeba, dobreKombinacje, uzyteNazwy, i + 1);

            wybraneProgramy.remove(wybraneProgramy.size() - 1);
            uzyteNazwy.remove(program.program());
        }
    }

    private boolean czyCzasSieNieNaklada(List<Slot> programy) {
        // Sortuj programy według czasu rozpoczęcia
        List<Slot> posortowane = new ArrayList<>(programy);
        posortowane.sort(Comparator.comparingInt(s -> s.atH() * 60 + s.atM()));

        for (int i = 0; i < posortowane.size() - 1; i++) {
            Slot aktualny = posortowane.get(i);
            Slot nastepny = posortowane.get(i + 1);

            if (czyDwaProgramySieNakladaja(aktualny, nastepny)) {
                return false;
            }
        }
        return true;
    }

    private boolean czyDwaProgramySieNakladaja(Slot program1, Slot program2) {
        int start1 = program1.atH() * 60 + program1.atM();
        int koniec1 = start1 + program1.duration();
        int start2 = program2.atH() * 60 + program2.atM();
        int koniec2 = start2 + program2.duration();

        return (start1 <= koniec2 && start2 <= koniec1);
    }
}