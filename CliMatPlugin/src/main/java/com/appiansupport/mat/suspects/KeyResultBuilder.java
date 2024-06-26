package com.appiansupport.mat.suspects;

import com.appiansupport.mat.utils.PrintUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class KeyResultBuilder {
    Map<String, Map<String, Integer>> keyResultTitleToCountByValue;
    static final int DEFAULT_THRESHOLD_TO_PRINT = 2;

    public KeyResultBuilder() {
        keyResultTitleToCountByValue = new HashMap<>(8);
    }

    public Map<String, Map<String, Integer>> addKeyResults(Map<String, Set<String>> keyResults) {
        for (Map.Entry<String, Set<String>> keyResult : keyResults.entrySet()) {
            String keyResultType = keyResult.getKey();
            Map<String, Integer> keyResultValueToCount = keyResultTitleToCountByValue.getOrDefault(keyResultType, new HashMap<>());
            for (String keyResultValue : keyResult.getValue()) {
                //Increment the count of this key result
                keyResultValueToCount.merge(keyResultValue, 1, Integer::sum);
                keyResultTitleToCountByValue.put(keyResultType, keyResultValueToCount);
            }
        }
        return new HashMap<>(keyResultTitleToCountByValue);
    }

    public String printKeyResults() {
        return printKeyResults(DEFAULT_THRESHOLD_TO_PRINT);
    }

    public String printKeyResults(int minCountToPrint) {
        return printKeyResults(minCountToPrint, null);
    }

    public String printKeyResults(String boxHeader) {
        return printKeyResults(DEFAULT_THRESHOLD_TO_PRINT, boxHeader);
    }

    public String printKeyResults(int minCountToPrint, String boxHeader) {
        if (keyResultTitleToCountByValue.isEmpty()) {
            return "";
        }
        Map<String, Integer> flatKeyToCount = flattenKeyResults();
        Comparator<Map.Entry<String, Integer>> compareKeyResultsByCountThenKey = Collections.reverseOrder(Map.Entry.<String, Integer>comparingByValue()).thenComparing(Map.Entry.comparingByKey());
        Stream<Map.Entry<String, Integer>> topKeyResultsOrdered = flatKeyToCount.entrySet().stream().filter(e -> e.getValue() >= minCountToPrint).sorted(compareKeyResultsByCountThenKey);
        //topKeyResultsOrdered.forEach(e -> keyResultOutput.appendln("%s | %d", e.getKey(), e.getValue()));
        List<String> keyResultNamesOrdered = new ArrayList<>(flatKeyToCount.size());
        List<String> keyResultCountsOrdered = new ArrayList<>(flatKeyToCount.size());
        topKeyResultsOrdered.forEach(e -> {
            keyResultNamesOrdered.add(e.getKey());
            keyResultCountsOrdered.add(e.getValue().toString());
        });
        List<List<String>> keyResultNestedLists = Arrays.asList(keyResultNamesOrdered, keyResultCountsOrdered);
        return PrintUtils.printTableFromLists(keyResultNestedLists, new String[] { "Value", "Count" }, boxHeader);
    }

    private Map<String, Integer> flattenKeyResults() {
        final String FLAT_KEY_DELIM = ": ";
        Map<String, Integer> resultTitleAndValueToCount = new HashMap<>();
        for (Map.Entry<String, Map<String, Integer>> keyResultSection : keyResultTitleToCountByValue.entrySet()) {
            String keyResultType = keyResultSection.getKey();
            for (Map.Entry<String, Integer> keyValueToCount : keyResultSection.getValue().entrySet()) {
                String keyResultValue = keyValueToCount.getKey();
                int resultCount = keyValueToCount.getValue();
                String flatKey = keyResultType + FLAT_KEY_DELIM + keyResultValue;
                //In unexpected case where two different key result types+values concatenate to the same flatKey, treat them as the same and sum
                resultTitleAndValueToCount.merge(flatKey, resultCount, Integer::sum);
            }
        }
        return resultTitleAndValueToCount;
    }

    public Map<String, Map<String, Integer>> getKeyResults() {
        return new HashMap<>(keyResultTitleToCountByValue);
    }
}

