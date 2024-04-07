package org.example;

import static java.lang.Math.min;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MinCostMaxFlowWithBellmanFord extends NetworkFlowSolverBase {

    public MinCostMaxFlowWithBellmanFord(int n, int s, int t) {
        super(n, s, t);
    }

    @Override
    public void solve() {

        List<Edge> path;
        while ((path = getAugmentingPath()).size() != 0) {

            long bottleNeck = Long.MAX_VALUE;
            for (Edge edge : path) bottleNeck = min(bottleNeck, edge.remainingCapacity());

            for (Edge edge : path) {
                edge.augment(bottleNeck);
                minCost += bottleNeck * edge.originalCost;
            }
            maxFlow += bottleNeck;
        }

    }

    private List<Edge> getAugmentingPath() {
        long[] dist = new long[n];
        Arrays.fill(dist, INF);
        dist[s] = 0;

        Edge[] prev = new Edge[n];

        for (int i = 0; i < n - 1; i++) {
            for (int from = 0; from < n; from++) {
                for (Edge edge : graph[from]) {
                    if (edge.remainingCapacity() > 0 && dist[from] + edge.cost < dist[edge.to]) {
                        dist[edge.to] = dist[from] + edge.cost;
                        prev[edge.to] = edge;
                    }
                }
            }
        }

        LinkedList<Edge> path = new LinkedList<>();
        for (Edge edge = prev[t]; edge != null; edge = prev[edge.from]) path.addFirst(edge);
        return path;
    }

}