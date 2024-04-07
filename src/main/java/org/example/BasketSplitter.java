package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasketSplitter {

    private static Map<String, List<String>> availableDeliveriesMap = new HashMap<>();

    private static final Map<String, Integer> productToIndexMap = new HashMap<>();

    private static final Map<String, Integer> deliveriesToIndexMap = new HashMap<>();

    private static final Map<String, List<String>> result = new HashMap<>();

    private static Map<Integer, String> indexToDeliveryMap = new HashMap<>();

    private static List<String> items = new ArrayList<>();

    private static NetworkFlowSolverBase flowSolver;

    private static final int INF = 1000007;

    private static int source, sink, counterOfProducts, counterOfDeliveries;

    private static int numberOfDeliveries;

    long minPartition = INF;

    long maxBiggestPart = -INF;

    static void setUpGraph() {
        source = items.size() + 11;
        sink = items.size() + 12;
        flowSolver = new MinCostMaxFlowWithBellmanFord(items.size() + 13, source, sink);

        counterOfProducts = 0;
        counterOfDeliveries = items.size();
        for (String item : items) {
            productToIndexMap.put(item, counterOfProducts);
            flowSolver.addEdge(source, counterOfProducts, 1, 0);

            for (String delivery : availableDeliveriesMap.get(item)) {
                if (!deliveriesToIndexMap.containsKey(delivery)) {
                    deliveriesToIndexMap.put(delivery, counterOfDeliveries);
                    indexToDeliveryMap.put(counterOfDeliveries, delivery);
                    counterOfDeliveries++;
                }
                flowSolver.addEdge(counterOfProducts, deliveriesToIndexMap.get(delivery), 1, 0);
            }
            counterOfProducts++;
        }

        for (String delivery : deliveriesToIndexMap.keySet()) {
            flowSolver.addEdge(deliveriesToIndexMap.get(delivery), sink, INF, 10);
        }

        numberOfDeliveries = counterOfDeliveries - counterOfProducts;
    }

    public BasketSplitter(String absolutePathToConfigFile) {
        try {
            String json = new String(Files.readAllBytes(Paths.get(absolutePathToConfigFile)));
            availableDeliveriesMap =
                    new ObjectMapper().readValue(json, HashMap.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, List<String>> split(List<String> input) {

        items = input;

        setUpGraph();

        for (String delivery : deliveriesToIndexMap.keySet()) {

            int deliveryIndex = deliveriesToIndexMap.get(delivery);

            flowSolver.updateEdge(deliveryIndex, sink, INF, 1);

            for (int i = 0; i < (1 << numberOfDeliveries); i++) {

                for (int j = 0; j < numberOfDeliveries; j++) {

                    if ((counterOfProducts + j + 1 != deliveryIndex) && ((1 << j) & i) == 1) {
                        flowSolver.updateEdge(counterOfProducts + j + 1, sink, 0, 20);
                    }
                }

                flowSolver.solve();

                if (flowSolver.maxFlow == items.size()) {
                    if (flowSolver.minCost < minPartition) {

                        saveNewBestResult(deliveryIndex);
                    } else if (flowSolver.minCost == minPartition && flowSolver.graph[deliveryIndex].getLast().flow > maxBiggestPart) {

                        saveNewBestResult(deliveryIndex);
                    }
                }

                for (int j = 0; j < numberOfDeliveries; j++) {
                    if ((counterOfProducts + j + 1 != deliveryIndex) && ((1 << j) & i) == 1) {
                        flowSolver.updateEdge(counterOfProducts + j + 1, sink, INF, 20);
                    }
                }

            }

            flowSolver.updateEdge(deliveryIndex, sink, INF, 20);
        }

        return result;
    }

    private void saveNewBestResult(int deliveryIndex) {
        result.clear();

        for (String item : items) {
            result.put(item, new ArrayList<>());
            int itemIndex = productToIndexMap.get(item);
            for (NetworkFlowSolverBase.Edge edge : flowSolver.graph[itemIndex]) {
                if (edge.flow > 0) {
                    String delivery = indexToDeliveryMap.get(edge.to);
                    result.get(item).add(delivery);
                }
            }
        }
        minPartition = flowSolver.getMinCost();
        maxBiggestPart = flowSolver.graph[deliveryIndex].getLast().flow;
    }

    public static void main(String[] args) {

        /*
        I came up with a solution which uses MinCostMaxFlow technique, which will work for small input
         (I believe this problem is not on this).
         Unfortunately, I ran out of time to debug it.
         */

        smallTest();
    }

    static void smallTest() {
        BasketSplitter bs = new BasketSplitter("C:\\config\\config.json");

        List<String> a = new ArrayList<>();
        a.add("Sauce - Salsa");
        a.add("Juice - Ocean Spray Cranberry");
        a.add("Pork Salted Bellies");

        System.out.println(bs.split(a));
    }

}

