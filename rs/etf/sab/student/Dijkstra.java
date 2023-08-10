/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 *
 * @author milic
 */
public class Dijkstra {

    public static Graph shortestPath(Graph graph, Node source) {

        source.setDistance(0);

        Set<Node> visitedNodes = new HashSet<>();
        Queue<Node> unvisitedNodes = new PriorityQueue<>(Collections.singleton(source));

        unvisitedNodes.add(source);

        while (!unvisitedNodes.isEmpty()) {
            
            Node current = unvisitedNodes.poll();

            for (Entry<Node, Integer> pair : current.getAdjacentNodes().entrySet()) {
                Node adjacentNode = pair.getKey();
                Integer distanceNode = pair.getValue();
                if (!visitedNodes.contains(adjacentNode)) {
                    minimumDistance(adjacentNode, distanceNode, current);
                    unvisitedNodes.add(adjacentNode);
                }
            }
            visitedNodes.add(current);
        }

        return graph;
    }

    public static void minimumDistance(Node node, Integer distance, Node source) {

        Integer sourceDistance = source.getDistance();
        Integer temp = sourceDistance + distance;
        if (temp < node.getDistance()) {
            node.setDistance(temp);
            LinkedList<Node> shortestPath = new LinkedList<>(source.getShortestPath());
            shortestPath.add(source);
            node.setShortestPath(shortestPath);
        }

    }


}
