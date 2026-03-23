package com.emergency.routing.util;

import com.emergency.routing.dto.CoordinateDto;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import org.springframework.stereotype.Component;

@Component
public class GraphRoutingUtil {
    public List<CoordinateDto> dijkstraShortestPath(List<CoordinateDto> nodes, int start, int target, DistanceUtil distanceUtil) {
        int n = nodes.size();
        Map<Integer, List<Edge>> graph = buildCompleteGraph(nodes, distanceUtil);
        double[] dist = new double[n];
        int[] parent = new int[n];
        boolean[] visited = new boolean[n];

        for (int i = 0; i < n; i++) {
            dist[i] = Double.MAX_VALUE;
            parent[i] = -1;
        }
        dist[start] = 0;

        PriorityQueue<NodeDist> pq = new PriorityQueue<>((a, b) -> Double.compare(a.dist, b.dist));
        pq.offer(new NodeDist(start, 0));

        while (!pq.isEmpty()) {
            NodeDist current = pq.poll();
            if (visited[current.node]) {
                continue;
            }
            visited[current.node] = true;
            if (current.node == target) {
                break;
            }

            for (Edge edge : graph.getOrDefault(current.node, List.of())) {
                if (dist[current.node] + edge.weight < dist[edge.to]) {
                    dist[edge.to] = dist[current.node] + edge.weight;
                    parent[edge.to] = current.node;
                    pq.offer(new NodeDist(edge.to, dist[edge.to]));
                }
            }
        }

        List<CoordinateDto> path = new ArrayList<>();
        int crawl = target;
        while (crawl != -1) {
            path.add(0, nodes.get(crawl));
            crawl = parent[crawl];
        }
        return path;
    }

    private Map<Integer, List<Edge>> buildCompleteGraph(List<CoordinateDto> nodes, DistanceUtil distanceUtil) {
        Map<Integer, List<Edge>> graph = new HashMap<>();
        for (int i = 0; i < nodes.size(); i++) {
            graph.putIfAbsent(i, new ArrayList<>());
            for (int j = 0; j < nodes.size(); j++) {
                if (i == j) {
                    continue;
                }
                double weight = distanceUtil.haversineKm(
                        nodes.get(i).getLatitude(), nodes.get(i).getLongitude(),
                        nodes.get(j).getLatitude(), nodes.get(j).getLongitude());
                graph.get(i).add(new Edge(j, weight));
            }
        }
        return graph;
    }

    private record Edge(int to, double weight) { }
    private record NodeDist(int node, double dist) { }
}
