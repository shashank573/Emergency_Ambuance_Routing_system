package com.emergency.routing.service;

import com.emergency.routing.dto.CoordinateDto;
import com.emergency.routing.util.DistanceUtil;
import com.emergency.routing.util.GraphRoutingUtil;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RoutingService {
    private final GraphRoutingUtil graphRoutingUtil;
    private final DistanceUtil distanceUtil;

    public RoutingService(GraphRoutingUtil graphRoutingUtil, DistanceUtil distanceUtil) {
        this.graphRoutingUtil = graphRoutingUtil;
        this.distanceUtil = distanceUtil;
    }

    public List<CoordinateDto> calculateShortestPath(CoordinateDto start, CoordinateDto end) {
        // A small city graph with fixed hubs in Delhi to demonstrate Dijkstra.
        List<CoordinateDto> graphNodes = new ArrayList<>();
        graphNodes.add(start); // index 0
        graphNodes.add(new CoordinateDto(28.6139, 77.2090)); // CP
        graphNodes.add(new CoordinateDto(28.5672, 77.2100)); // AIIMS area
        graphNodes.add(new CoordinateDto(28.7041, 77.1025)); // North Delhi
        graphNodes.add(new CoordinateDto(28.5355, 77.3910)); // East/Noida connector
        graphNodes.add(end); // last index

        return graphRoutingUtil.dijkstraShortestPath(graphNodes, 0, graphNodes.size() - 1, distanceUtil);
    }

    public int estimateETA(double totalDistanceKm) {
        // Assume average response speed of 35 km/h in city traffic.
        double etaHours = totalDistanceKm / 35.0;
        return Math.max(2, (int) Math.ceil(etaHours * 60));
    }
}
