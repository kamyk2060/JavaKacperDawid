/**
 * Interfejs generatora tabel w SQLite
 */
public interface SQLiteHelper {
	/**
	 * Metoda generuje ciąg znaków pozwalający na utworzenie tabeli w bazie danych
	 * SQLite.
	 * 
	 * @param object    obiekt, którego należy wygenerować tabelę
	 * @param tableName nazwa tabeli
	 * @return ciąg znaków generujący tabelę o podanej nazwie
	 */
	public String toSQL(Object object, String tableName);
}
