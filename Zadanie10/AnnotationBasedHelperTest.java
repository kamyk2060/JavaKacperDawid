import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AnnotationBasedHelperTest {
    private AnnotationBasedHelper helper;

    @BeforeEach
    void setUp() {
        helper = new AnnotationBasedHelper();
    }

    public class Pracownik {
        @SQL public int indeks;
        @SQL public String imie;
        @SQL public String nazwisko;
        @SQL public float premia;
        @SQL public Set<String> obowiazki;
        public List<String> hobby;
    }

    @Test
    void testToSQL() {
        Pracownik pracownik = new Pracownik();
        String tableName = "pracownicy";

        String expectedSQL = "CREATE TABLE pracownicy (indeks INTEGER, imie TEXT, nazwisko TEXT, premia REAL);";
        String actualSQL = helper.toSQL(pracownik, tableName);

        System.out.println(actualSQL);
        assertEquals(expectedSQL, actualSQL);
    }

    // 1. Test sprawdzający ignorowanie pól prywatnych i bez adnotacji
    public class MixFields {
        @SQL public int id;            // Powinno być
        public String ignoreMe;        // Brak @SQL
        @SQL private String secret;    // Niepubliczne
        @SQL public boolean active;    // Nieobsługiwany typ
    }

    @Test
    void testFiltering() {
        MixFields obj = new MixFields();
        String sql = helper.toSQL(obj, "mix");
        // Oczekujemy tylko pola id

        System.out.println(sql);
        assertEquals("CREATE TABLE mix (id INTEGER);", sql);
    }

    // 2. Test typów opakowujących (Long, Double)
    public class WrapperTypes {
        @SQL public Long bigId;
        @SQL public Double price;
    }

    @Test
    void testWrappers() {
        WrapperTypes obj = new WrapperTypes();
        String sql = helper.toSQL(obj, "products");

        System.out.println(sql);
        assertTrue(sql.contains("bigId INTEGER"));
        assertTrue(sql.contains("price REAL"));

    }
}