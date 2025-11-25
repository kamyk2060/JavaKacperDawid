/**
 * Pojedynczy przedział czasowy, w którym stacja telewizyjna emituje program.
 * Slot zaczyna się o godzinie atH minut atM i trwa duration minut.
 */
public record Slot( String station, String program, int atH, int atM, int duration ) {

}
