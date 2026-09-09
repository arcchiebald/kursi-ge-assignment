package ge.kursi.settlement_funding.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FundingAlgorithm {

    // We are using 0-1 Knapsack to solve this problem.
    
    // weights: the weights of the items
    // values: the values of the items
    // capacity: the capacity of the knapsack
    public List<Integer> solve(long[] weights, long[] values, long capacity) {

        // Input validaion
        if (weights == null || values == null || weights.length != values.length || capacity < 0) {
            throw new IllegalArgumentException("Weights and values must be non-null, have equal length, and capacity must be non-negative.");
        }

        final int N = weights.length;
        final int tableCapacity = Math.toIntExact(capacity);

        long[][] table = new long[N + 1][tableCapacity + 1];

        // Building the table
        for (int i = 1; i < N + 1; i++) {
            
            long currentValue = values[i - 1];
            long currentWeight = weights[i - 1];

            for (int j = 1; j <= tableCapacity; j++) {
                
                // In every situation, we can not go less then previous choice, 
                // so if we dont add current value, it stays the same
                table[i][j] = table[i - 1][j];

                /**
                 * But if it is possible to add current element without overrunning 
                 * the maximum capacity and it increases the cumulative value, we should do it.
                 */
                if (currentWeight <= j) {
                    int remainingCapacity = Math.toIntExact(j - currentWeight);
                    long candidateValue = table[i - 1][remainingCapacity] + currentValue;

                    if (candidateValue > table[i][j]) {
                        table[i][j] = candidateValue;
                    }
                }
            }

        }


        // Now we need to backtrack the table in order to find selected items.
        List<Integer> selectedItemsIdx = new ArrayList<>();
        int remainingCapacity = tableCapacity;
        for (int i = N; i > 0; i--) {
            // If value did not change, ignore it
            if (table[i][remainingCapacity] == table[i - 1][remainingCapacity]) {
                continue;
            }

            // Else, value is improved, so (i - 1)-th item was selected
            // We add it to the selectedItemIndexes and reduce capacity
            selectedItemsIdx.add(i - 1);
            remainingCapacity -= weights[i - 1];

        }
        
        // We reverse the list because we went backwards through the table.
        Collections.reverse(selectedItemsIdx);

        return selectedItemsIdx;
    }

}
