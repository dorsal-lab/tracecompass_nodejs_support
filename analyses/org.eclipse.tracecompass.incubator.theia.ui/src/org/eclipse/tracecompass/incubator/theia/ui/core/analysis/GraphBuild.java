package org.eclipse.tracecompass.incubator.theia.ui.core.analysis;
import java.util.*;
public class GraphBuild<T> {
	
		 
	    // We use Hashmap to store the edges in the graph
	    private Map<T, List<T> > map = new HashMap<>();
	 
	    // Adding new vertex to th graph
	    public void addVertex(T s)
	    {
	        map.put(s, new LinkedList<T>());
	    }
	 
	    // Adding an edge
	    // between source to destination
	    public void addEdge(T source,
	                        T destination,
	                        boolean bidirectional)
	    {
	 
	        if (!map.containsKey(source))
	            addVertex(source);
	 
	        if (!map.containsKey(destination))
	            addVertex(destination);
	 
	        map.get(source).add(destination);
	        if (bidirectional == true) {
	            map.get(destination).add(source);
	        }
	    }
	 
	    // Returns  the count of vertices
	    public int getVertexCount()
	    {
	        return map.keySet().size();
	    }
	 
	    // Returns the count of edges
	    public int getEdgesCount(boolean bidirection)
	    {
	        int count = 0;
	        for (T v : map.keySet()) {
	            count += map.get(v).size();
	        }
	        if (bidirection == true) {
	            count = count / 2;
	        }
	        return count;
	    }
	 
	    // Check wether or not
	    // a vertex is present.
	    public boolean hasVertex(T s)
	    {
	        if (map.containsKey(s)) {
	           return true;
	        }
	        else {
	            return false;
	        }
	    }
	 
	    // Check wether or not an edge is present.
	    public boolean hasEdge(T s, T d)
	    {
	        if (map.get(s).contains(d)) {
	            return true;
	        }
	        else {
	            return false;
	        }
	    }
	 
	    // Prints the adjancency list of each vertex.
	    @Override
	    public String toString()
	    {
	        StringBuilder builder = new StringBuilder();
	 
	        for (T v : map.keySet()) {
	            builder.append(v.toString() + ": ");
	            for (T w : map.get(v)) {
	                builder.append(w.toString() + " ");
	            }
	            builder.append("\n");
	        }
	 
	        return (builder.toString());
	    }
	    
	    
	    
	    public int Test( T t)
	    {
	 
	        for (T v : map.keySet()) {
	            
	        	if(map.get(v).contains(t)) {
	        		
	        		
	        		return (int) v;
	        	}
	        	
	        	
	        	
	        }
	 
	        return -1;
	    }
	    
	    
	    public int First()
	    {
	         int root=-1;
             for (T v : map.keySet()) {
	            
	        	
	        	root = (int)v;
	        	break;
	        	
	        	
	        }
             
             return root;
	    }
	    
	   

}
