/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author milic
 */
public class Node implements Comparable<Node> {
    
    private String cityName;
    private int cityId;
    
    private LinkedList<Node> shortestPath = new LinkedList<>();
    
    private Integer distance = Integer.MAX_VALUE;
    
    private Map<Node, Integer> adjacentNodes = new HashMap<>();
    
    public void addAdjacentNode(Node adjacent, int distance){
        adjacentNodes.put(adjacent, distance);
    }
    
    @Override
    public int compareTo(Node node){
        return Integer.compare(this.distance, node.getDistance());
    }
    
    public Node( int cityId){
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCityId() {
        return this.cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public List<Node> getShortestPath() {
        return shortestPath;
    }

    public void setShortestPath(LinkedList<Node> shortestPath) {
        this.shortestPath = shortestPath;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public Map<Node, Integer> getAdjacentNodes() {
        return adjacentNodes;
    }

    public void setAdjacentNodes(Map<Node, Integer> adjacentNodes) {
        this.adjacentNodes = adjacentNodes;
    }
    
    
    
}
