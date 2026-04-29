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

    public RoutingService(
        GraphRoutingUtil graphRoutingUtil,
        DistanceUtil distanceUtil
    ) {
        this.graphRoutingUtil = graphRoutingUtil;
        this.distanceUtil = distanceUtil;
    }

    public List<CoordinateDto> calculateShortestPath(
        CoordinateDto start,
        CoordinateDto end
    ) {
        List<CoordinateDto> graphNodes = new ArrayList<>();
        graphNodes.add(start);
        graphNodes.add(new CoordinateDto(28.6139, 77.2090));
        graphNodes.add(new CoordinateDto(28.5672, 77.2100));
        graphNodes.add(new CoordinateDto(28.7041, 77.1025));
        graphNodes.add(new CoordinateDto(28.5355, 77.3910));
        graphNodes.add(end);

        return graphRoutingUtil.dijkstraShortestPath(
            graphNodes,
            0,
            graphNodes.size() - 1,
            distanceUtil
        );
    }

    public int estimateETA(double totalDistanceKm) {
        double etaHours = totalDistanceKm / 35.0;
        return Math.max(2, (int) Math.ceil(etaHours * 60));
    }
}
