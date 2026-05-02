package com.supplychain.main;

import com.supplychain.model.*;
import com.supplychain.db.*;
import com.supplychain.logic.BottleneckAnalyzer;
import com.supplychain.logic.DisruptionSimulator;
import com.supplychain.logic.GraphTraversal;
import com.supplychain.logic.RecoveryPlanner;
import com.supplychain.ui.*;

import java.util.List;

public class MainTest {

    public static void main(String[] args) {

        Graph graph = new Graph();

        NodeDAO nodeDAO = new NodeDAO();
        EdgeDAO edgeDAO = new EdgeDAO();

        List<Node> nodes = nodeDAO.getAllNodes();

        for (Node n : nodes) {
            graph.addNode(n);
        }
        edgeDAO.loadEdges(graph);

        new MainMenuUI(graph);
    }
}