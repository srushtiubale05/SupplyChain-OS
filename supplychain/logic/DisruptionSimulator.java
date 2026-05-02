package com.supplychain.logic;

import com.supplychain.model.*;

import java.util.*;

public class DisruptionSimulator {

    private Graph graph;

    public DisruptionSimulator(Graph graph) {
        this.graph = graph;
    }

    public List<Node> simulateFailure(Node failedNode) {

        List<Node> affectedNodes = new ArrayList<>();
        Queue<Node> queue = new LinkedList<>();

        queue.add(failedNode);
        affectedNodes.add(failedNode);

        while(!queue.isEmpty()) {

            Node current = queue.poll();

            for(Edge edge : graph.getNeighbors(current)) {

                // 🚫 STRICT DIRECTION CHECK
                if(!edge.getSource().equals(current)) continue;

                Node next = edge.getDestination();

                if(!affectedNodes.contains(next)) {
                    affectedNodes.add(next);
                    queue.add(next);
                }
            }
        }

        return affectedNodes;
    }
    private Node findAlternative(Node source, Node failed) {

        for(Edge edge : graph.getNeighbors(source)) {

            Node next = edge.getDestination();

            if(!next.equals(failed)) {
                return next;
            }
        }

        return null;
    }

    public Map<Node, String> calculateImpact(Node failedNode) {

        Map<Node, String> impact = new HashMap<>();

        // Step 1: find nodes that were sending to failed node (incoming edges)
        for(Node node : graph.getAllNodes()) {

            for(Edge edge : graph.getNeighbors(node)) {

                if(edge.getDestination().equals(failedNode)) {

                    int lostCapacity = edge.getCapacity();

                    // Step 2: try to find alternative path
                    Node alt = findAlternative(node, failedNode);

                    if(alt != null) {

                        impact.put(alt,
                                "LOAD INCREASE +" + lostCapacity);

                        impact.put(node,
                                "REDIRECTED to " + alt.getName());

                    } else {

                        impact.put(node,
                                "NO ROUTE → LOSS");

                    }
                }
            }
        }

        return impact;
    }
}