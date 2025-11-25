import java.util.*;

class Sequence extends AbstractSequence {

    private int delta;
    private int[] ZdekodowaneDane;
    private int[] Zakodowane;

    @Override
    public void sequence(int[] data, int deltaSegmentSize) {
        ZdekodowaneDane = data.clone();
        delta = deltaSegmentSize;
        Zakodowane = null;
    }

    @Override
    public int[] decode() {
        if (Zakodowane != null) {
            return Zakodowane.clone();
        }

        Zakodowane = new int[ZdekodowaneDane.length];
        if (ZdekodowaneDane.length == 0) {
            return Zakodowane.clone();
        }

        for (int i = 0; i < ZdekodowaneDane.length; i++) {
            if (i % (delta + 1) == 0) {
            
                Zakodowane[i] = ZdekodowaneDane[i];
            } else {
                
                int indexStart = i - (i % (delta + 1));
                int sum = Zakodowane[indexStart];

                for (int j = indexStart + 1; j <= i; j++) {
                    sum += ZdekodowaneDane[j];
                }
                Zakodowane[i] = sum;
            }
        }

        return Zakodowane.clone();
    }

    @Override
    public int[] encode(int deltaSegmentSize) {
        if (Zakodowane == null) {
            decode();
        }

        int[] result = new int[Zakodowane.length];
        if (Zakodowane.length == 0) {
            return result;
        }

        for (int i = 0; i < Zakodowane.length; i++) {
            if (i % (deltaSegmentSize + 1) == 0) {
                
                result[i] = Zakodowane[i];
            } else {
                
                result[i] = Zakodowane[i] - Zakodowane[i - 1];
            }
        }

        return result;
    }

    @Override
    public boolean equals(int[] data, int deltaSegmentSize) {
        if (Zakodowane == null) {
            decode();
        }

        int[] NoweZakodowane = new int[data.length];
        if (data.length == 0) {
            return Zakodowane.length == 0;
        }

        for (int i = 0; i < data.length; i++) {
            if (i % (deltaSegmentSize + 1) == 0) {
                NoweZakodowane[i] = data[i];
            } else {
                int PoczatekIndex = i - (i % (deltaSegmentSize + 1));
                int sum = NoweZakodowane[PoczatekIndex];
                for (int j = PoczatekIndex + 1; j <= i; j++) {
                    sum += data[j];
                }
                NoweZakodowane[i] = sum;
            }
        }

        // Porównaj rozmiary
        if (NoweZakodowane.length != Zakodowane.length) {
            return false;
        }

        // Porównaj elementy
        for (int i = 0; i < NoweZakodowane.length; i++) {
            if (NoweZakodowane[i] != Zakodowane[i]) {
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