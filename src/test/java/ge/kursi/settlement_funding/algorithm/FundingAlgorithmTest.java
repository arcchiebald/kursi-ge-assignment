package ge.kursi.settlement_funding.algorithm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

class FundingAlgorithmTest {

    private final FundingAlgorithm algorithm = new FundingAlgorithm();

    @Test
    void selectsCombinationWithMaximumValue() {
        long[] weights = {7, 9, 4, 6};
        long[] values = {150, 210, 90, 130};
        long capacity = 20;

        List<Integer> result = algorithm.solve(weights, values, capacity);

        assertEquals(List.of(0,1,2), result);
    }

    @Test
    void returnsZeroWhenNothingFits() {
        long[] weights = {7, 9};
        long[] values = {150, 210};
        long capacity = 3;

        List<Integer> result = algorithm.solve(weights, values, capacity);

        assertEquals(List.of(), result);
    }

    @Test
    void returnsZeroForZeroCapacity() {
        long[] weights = {7, 9};
        long[] values = {150, 210};

        List<Integer> result = algorithm.solve(weights, values, 0);

        assertEquals(List.of(), result);
    }

    @Test
    void selectsBestSingleItem() {
        long[] weights = {5, 10};
        long[] values = {100, 150};
        long capacity = 5;

        List<Integer> result = algorithm.solve(weights, values, capacity);

        assertEquals(List.of(0), result);
    }

    @Test
    void nullArrayInput() {
        long[] weights = null;
        long[] values = {150, 210};
        long capacity = 5;

        try {
            algorithm.solve(weights, values, capacity);
        } catch (IllegalArgumentException e) {
            assertEquals("Weights and values must be non-null, have equal length, and capacity must be non-negative.", e.getMessage());
        }
    }

    @Test
    void unequalArrayLengthInput() {
        long[] weights = {5, 10};
        long[] values = {100};
        long capacity = 5;

        try {
            algorithm.solve(weights, values, capacity);
        } catch (IllegalArgumentException e) {
            assertEquals("Weights and values must be non-null, have equal length, and capacity must be non-negative.", e.getMessage());
        }
    }

    @Test
    void negativeCapacityInput() {
        long[] weights = {5, 10};
        long[] values = {100, 150};
        long capacity = -1;

        try {
            algorithm.solve(weights, values, capacity);
        } catch (IllegalArgumentException e) {
            assertEquals("Weights and values must be non-null, have equal length, and capacity must be non-negative.", e.getMessage());
        }
    }

    @Test 
    void emptyInput() {
        long[] weights = {};
        long[] values = {};
        long capacity = 5;

        List<Integer> result = algorithm.solve(weights, values, capacity);

        assertEquals(List.of(), result);
    }

    @Test 
    void equallyValuedItems() {
        long[] weights = {5, 5, 5};
        long[] values = {100, 100, 100};
        long capacity = 10;

        List<Integer> result = algorithm.solve(weights, values, capacity);

        assertEquals(List.of(0,1), result);
    }

    @Test
    void largeInput() {
        long[] weights = new long[1000];
        long[] values = new long[1000];
        for (int i = 0; i < 1000; i++) {
            weights[i] = i + 1;
            values[i] = (i + 1) * 10;
        }
        long capacity = 528;

        List<Integer> result = algorithm.solve(weights, values, capacity);

        assertEquals(32, result.size());
    }
}