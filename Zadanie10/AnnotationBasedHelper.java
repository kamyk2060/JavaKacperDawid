import java.lang.reflect.Field;

public class AnnotationBasedHelper implements SQLiteHelper {

    @Override
    public String toSQL(Object obiekt, String nazwaTabeli) {

        // pobieramy klase obiektu
        Class<?> klasa = obiekt.getClass();

        // pobieramy wszystkie publiczne pola
        Field[] pola = klasa.getFields();

        // tu bedziemy zbierac kolumny
        String kolumny = "";
        int licznikKolumn = 0;

        // przechodzimy przez kazde pole
        for (int i = 0; i < pola.length; i++) {

            Field pole = pola[i];

            // sprawdzamy czy pole ma adnotacje @SQL
            boolean maAdnotacje = pole.isAnnotationPresent(SQL.class);

            if (maAdnotacje == false) {
                // nie ma adnotacji, pomijamy
                continue;
            }

            // pobieramy typ pola
            Class<?> typPola = pole.getType();

            // zamieniamy typ Java na typ SQLite
            String typSQLite = zamienTypNaSQLite(typPola);

            // jesli typ nie jest obslugiwany to pomijamy
            if (typSQLite == null) {
                continue;
            }

            // pobieramy nazwe pola
            String nazwaPola = pole.getName();

            // dodajemy przecinek jesli to nie pierwsza kolumna
            if (licznikKolumn > 0) {
                kolumny = kolumny + ", ";
            }

            // dodajemy kolumne
            kolumny = kolumny + nazwaPola + " " + typSQLite;
            licznikKolumn++;
        }

        // budujemy cale polecenie SQL
        String sql = "CREATE TABLE " + nazwaTabeli + " (" + kolumny + ");";

        return sql;
    }

    // metoda zamienia typ Java na typ SQLite
    private String zamienTypNaSQLite(Class<?> typ) {

        // typy calkowite -> INTEGER
        if (typ == int.class) {
            return "INTEGER";
        }
        if (typ == Integer.class) {
            return "INTEGER";
        }
        if (typ == long.class) {
            return "INTEGER";
        }
        if (typ == Long.class) {
            return "INTEGER";
        }

        // typy zmiennoprzecinkowe -> REAL
        if (typ == float.class) {
            return "REAL";
        }
        if (typ == Float.class) {
            return "REAL";
        }
        if (typ == double.class) {
            return "REAL";
        }
        if (typ == Double.class) {
            return "REAL";
        }

        // String -> TEXT
        if (typ == String.class) {
            return "TEXT";
        }

        // nieznany typ
        return null;
    }
}