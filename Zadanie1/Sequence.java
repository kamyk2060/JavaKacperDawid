import java.util.*;

class Sequence extends AbstractSequence {

    private int delta;
    private int[] encodedData;
    private int[] decoded;

    @Override
    public void sequence(int[] data, int deltaSegmentSize) {
        encodedData = data.clone();
        delta = deltaSegmentSize;
        decoded = null;
    }

    @Override
    public int[] decode() {
        if (decoded != null) {
            return decoded.clone();
        }

        decoded = new int[encodedData.length];
        if (encodedData.length == 0) {
            return decoded.clone();
        }

        for (int i = 0; i < encodedData.length; i++) {
            if (i % (delta + 1) == 0) {
                // To jest wartość bazowa - przepisz bez zmian
                decoded[i] = encodedData[i];
            } else {
                // To jest delta - oblicz jako suma wartości bazowej i wszystkich delt od ostatniej wartości bazowej
                int indexStart = i - (i % (delta + 1));
                int sum = decoded[indexStart];

                // Dodaj wszystkie delty od wartości bazowej do bieżącego indeksu
                for (int j = indexStart + 1; j <= i; j++) {
                    sum += encodedData[j];
                }
                decoded[i] = sum;
            }
        }

        return decoded.clone();
    }

    @Override
    public int[] encode(int deltaSegmentSize) {
        // Najpierw upewnij się, że mamy odkodowane dane
        if (decoded == null) {
            decode();
        }

        int[] result = new int[decoded.length];
        if (decoded.length == 0) {
            return result;
        }

        for (int i = 0; i < decoded.length; i++) {
            if (i % (deltaSegmentSize + 1) == 0) {
                // Wartość bazowa - przepisz bez zmian
                result[i] = decoded[i];
            } else {
                // Delta - różnica między obecną a poprzednią wartością
                result[i] = decoded[i] - decoded[i - 1];
            }
        }

        return result;
    }

    @Override
    public boolean equals(int[] data, int deltaSegmentSize) {
        if (decoded == null) {
            decode();
        }

        // Sprawdź czy tablica data po odkodowaniu z podanym deltaSegmentSize
        // jest równa naszej odkodowanej tablicy

        int[] otherDecoded = new int[data.length];
        if (data.length == 0) {
            return decoded.length == 0;
        }

        // Odkoduj przekazaną tablicę data z podanym deltaSegmentSize
        for (int i = 0; i < data.length; i++) {
            if (i % (deltaSegmentSize + 1) == 0) {
                otherDecoded[i] = data[i];
            } else {
                int baseIndex = i - (i % (deltaSegmentSize + 1));
                int sum = otherDecoded[baseIndex];
                for (int j = baseIndex + 1; j <= i; j++) {
                    sum += data[j];
                }
                otherDecoded[i] = sum;
            }
        }

        // Porównaj rozmiary
        if (otherDecoded.length != decoded.length) {
            return false;
        }

        // Porównaj elementy
        for (int i = 0; i < otherDecoded.length; i++) {
            if (otherDecoded[i] != decoded[i]) {
                return false;
            }
        }

        return true;
    }

    public static void main(String[] args) {
        // Test 1: delta = 3
        System.out.println("Test 1: delta = 3");
        int seg = 3;
        Sequence sequence = new Sequence();
        sequence.sequence(new int[]{10, 5, -3, 2, 20, 2, 8, 1, 31, -1, 70, 20}, seg);

        int[] arr1 = sequence.decode();
        System.out.println("Decode result: " + Arrays.toString(arr1));
        int[] arr2 = sequence.encode(seg);
        System.out.println("Encoded result: " + Arrays.toString(arr2));

        boolean result1 = sequence.equals(new int[]{10, 5, -3, 2, 20, 2, 8, 1, 31, -1, 70, 20}, seg);
        System.out.println("Test 1 equals: " + result1);

        System.out.println("-".repeat(50));

        // Test 2: delta = 4 (ten sam ciąg danych, różne kodowanie)
        System.out.println("Test 2: delta = 4 (ten sam ciąg)");
        int seg2 = 4;
        Sequence sequence2 = new Sequence();
        sequence2.sequence(new int[]{10, 5, -3, 2, 6, 22, 8, 1, 0, -1, 100, 20}, seg2);

        int[] arr3 = sequence2.decode();
        System.out.println("Decode result: " + Arrays.toString(arr3));

        // Sprawdź czy oba kodowania reprezentują ten sam ciąg
        boolean result3 = sequence2.equals(new int[]{10, 5, -3, 2, 20, 2, 8, 1, 31, -1, 70, 20}, 3);
        System.out.println("Test 2 equals (porównanie z kodowaniem delta=3): " + result3);

        System.out.println("-".repeat(50));

        // Test 3: krótki ciąg
        System.out.println("Test 3: krótki ciąg");
        int seg3 = 2;
        Sequence sequence3 = new Sequence();
        sequence3.sequence(new int[]{3, 2, 5}, seg3);

        int[] arr5 = sequence3.decode();
        System.out.println("Decode result: " + Arrays.toString(arr5));
        int[] arr6 = sequence3.encode(seg3);
        System.out.println("Encoded result: " + Arrays.toString(arr6));

        boolean result4 = sequence3.equals(new int[]{3, 2, 5}, seg3);
        System.out.println("Test 3 equals: " + result4);
    }
}