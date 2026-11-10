package com.graphlab.search;

import com.graphlab.model.*;
import java.util.*;

/** Fictional location graph. Edge weights represent hypothetical travel minutes. */
public final class SearchNetwork {
    private SearchNetwork() {}
    public record Site(String id,String name,double x,double y) {}
    public static final List<Site> SITES=List.of(
        new Site("CAMP","Campus",95,120),new Site("STN","Transit",270,82),
        new Site("PARK","Park",460,115),new Site("CAFE","Café",185,255),
        new Site("CLINIC","Clinic",390,280),new Site("HQ","Search HQ",80,320));
    public static Graph build() {
        Graph g=new Graph(); for(var s:SITES)g.addVertex(s.id());
        g.addEdge("CAMP","STN",5);g.addEdge("CAMP","CAFE",4);g.addEdge("CAMP","HQ",7);
        g.addEdge("STN","PARK",6);g.addEdge("STN","CAFE",3);g.addEdge("STN","CLINIC",8);
        g.addEdge("CAFE","CLINIC",5);g.addEdge("CAFE","HQ",4);g.addEdge("CLINIC","PARK",4);
        g.addEdge("PARK","HQ",12);
        return g;
    }
    public record Path(List<Vertex> nodes,double total) { public Path {nodes=List.copyOf(nodes);} }
    public static Path dijkstra(Graph graph, String start, String end, Set<Edge> blocked) {
        Vertex source=graph.requireVertex(start),target=graph.requireVertex(end);
        if(source==null||target==null)throw new IllegalArgumentException("Unknown site");
        Map<Vertex,Double> distances=new HashMap<>();Map<Vertex,Vertex> previous=new HashMap<>();
        record Entry(Vertex vertex,double cost){}
        PriorityQueue<Entry> pq=new PriorityQueue<>(Comparator.comparingDouble(Entry::cost));
        distances.put(source,0.0);pq.add(new Entry(source,0));
        while(!pq.isEmpty()){
            Entry current=pq.poll();if(current.cost()>distances.getOrDefault(current.vertex(),Double.POSITIVE_INFINITY))continue;
            if(current.vertex().equals(target))break;
            for(Edge edge:graph.edges()){
                if(blocked.contains(edge))continue;
                Vertex next=null;
                if(edge.from().equals(current.vertex()))next=edge.to();
                else if(edge.to().equals(current.vertex()))next=edge.from();
                if(next==null)continue;
                double newCost=current.cost()+edge.weight();
                if(newCost<distances.getOrDefault(next,Double.POSITIVE_INFINITY)){
                    distances.put(next,newCost);previous.put(next,current.vertex());pq.add(new Entry(next,newCost));
                }
            }
        }
        if(!distances.containsKey(target))return new Path(List.of(),Double.POSITIVE_INFINITY);
        LinkedList<Vertex> ordered=new LinkedList<>();
        for(Vertex v=target;v!=null;v=previous.get(v))ordered.addFirst(v);
        return new Path(ordered,distances.get(target));
    }
    public static List<Vertex> bfs(Graph graph,String start,Set<Edge> blocked){
        Vertex root=graph.requireVertex(start);if(root==null)throw new IllegalArgumentException("Unknown site");
        List<Vertex> visited=new ArrayList<>();Queue<Vertex> q=new ArrayDeque<>();Set<Vertex> seen=new HashSet<>();
        q.add(root);seen.add(root);
        while(!q.isEmpty()){
            Vertex v=q.remove();visited.add(v);
            for(Edge e:graph.edges()){
                if(blocked.contains(e))continue;
                Vertex other=e.from().equals(v)?e.to():e.to().equals(v)?e.from():null;
                if(other!=null&&seen.add(other))q.add(other);
            }
        }
        return visited;
    }
}
