package com.supplychain.logic;

import com.supplychain.model.*;
import java.util.*;

public class RecoveryPlanner {

    public RouteOptimizer optimizer;

    public RecoveryPlanner(Graph graph) {
        this.optimizer = new RouteOptimizer(graph);
    }

    // Return ALL paths instead of printing
    public Map<String, PathResult> getRecoveryPlans(Node source, Node destination) {

        Map<String, PathResult> plans = new LinkedHashMap<>();

        plans.put("Fastest", optimizer.getBestPath(source, destination, "time"));
        plans.put("Cheapest", optimizer.getBestPath(source, destination, "cost"));
        plans.put("Reliable", optimizer.getBestPath(source, destination, "capacity"));

        return plans;
    }

    // ALTERNATE ROUTES (simple version)
    public List<PathResult> getAlternateRoutes(Node source, Node destination) {

        List<PathResult> routes = new ArrayList<>();

        routes.add(optimizer.getBestPath(source, destination, "time"));
        routes.add(optimizer.getBestPath(source, destination, "cost"));
        routes.add(optimizer.getBestPath(source, destination, "capacity"));

        return routes;
    }
}