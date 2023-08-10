/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author milic
 */
public class Graph {
    
    private Set<Node> nodes = new HashSet<>();
    
    public void add(Node node){
        nodes.add(node);
    }

    public Set<Node> getNodes() {
        return nodes;
    }

    public void setNodes(Set<Node> nodes) {
        this.nodes = nodes;
    }
    
}
