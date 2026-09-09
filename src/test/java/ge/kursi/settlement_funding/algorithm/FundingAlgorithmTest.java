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
}