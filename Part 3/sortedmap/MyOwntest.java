package cmsc420.sortedmap;

import java.util.ArrayList; 
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import cmsc420.sortedmap.Treap.TreapNode;
 
public class MyOwntest {
	
	public static void main (String [] args){
		Treap<Integer,Integer> t = new Treap<Integer, Integer>();
		TreeMap<Integer, Integer> r = new TreeMap<Integer, Integer>();
		ArrayList<TreapNode<Integer, Integer>> a= new ArrayList<TreapNode<Integer, Integer>>();
		
		
		for(int i = 0; i < 10; i++) {
			r.put(i, i);
			t.put(i, i);
		}
		System.out.println(r.entrySet().equals(t.entrySet()));
		
//		//System.out.println("Before:");
//				//t.printTree(t);
//		
//		//System.out.println("Root: " + t.getRoot().getKey());
//		//System.out.println("After:");
//		
//		System.out.println(t.equals(r));
//	
//		Entry<Integer, Integer> i  = t.getEntry(5);
//		
//		t.remove(1);
//		t.remove(0);
//		t.remove(2);
//		System.out.println(t.equals(r));
//		r.remove(0);
//		System.out.println(t.equals(r));
//		t.remove(5);
//		//t.entrySet().remove(i);
//		//r.entrySet().remove(i);
//		System.out.println(t.equals(r));
//		System.out.println(t.entrySet().equals(r.entrySet()));
//		
//		Entry<Integer, Integer> j  = t.getEntry(4);
//		
//		
//		System.out.println(t.subMap(1, 6).equals(r.subMap(1, 6)));
//		System.out.println(t.subMap(1, 5).entrySet().equals(r.subMap(1, 5).entrySet()));
		
		
//		System.out.println("Root: " + t.getRoot().getKey());
//		t.printTree(t);
//		
//		
//		TreapNode<Integer, Integer> i  = t.getEntry(0);	
//		a.add(i);
//		i  = t.getEntry(7);	
//		a.add(i);
//		i  = t.getEntry(5);
//		a.add(i);
//		i  = t.getEntry(4);	
//		a.add(i);
//		i  = t.getEntry(6);
//		a.add(i);
		
//		t.remove(7);
//		t.remove(5);
//		t.remove(4);
//		t.remove(6);
//		t.remove(0);
		
//		t.entrySet().removeAll(a);
//		
//		System.out.println("Root: " + t.getRoot().getKey());
//		t.printTree(t);
//		
//		Iterator i = t.entrySet().iterator();
//		while(i.hasNext()){
//			i.remove();
//			i.next();
//			
//		}
		
		
		
		Treap<Integer, Integer> map1 = new Treap<Integer,Integer>();
		Set entrySet1 = map1.entrySet();
			    
		Treap<Integer, Integer> map2 = new Treap<Integer,Integer>();
		Set entrySet2 = map2.entrySet();
			    
		for(int j = 0; j < 5; j++) {
			map1.put(j, j);
			map2.put(j, j);
		}
		
		System.out.println("Root: " + map2.getRoot().getKey());
		map2.printTree(map2);

		Iterator iterator = entrySet1.iterator();
		//while (iterator.hasNext()) {
			TreapNode<Integer, Integer> e = (TreapNode<Integer, Integer>) iterator.next();
			entrySet2.remove(e);
		//}
			e = (TreapNode<Integer, Integer>) iterator.next();
			e = (TreapNode<Integer, Integer>) iterator.next();
			e = (TreapNode<Integer, Integer>) iterator.next();
			entrySet2.remove(e);
			    
		System.out.println(map1);
		System.out.println(map2);
		System.out.println("Root: " + map2.getRoot().getKey());
		map2.printTree(map2);
		
//		TreeMap<Integer, Integer> map = new TreeMap<Integer,Integer>();
//		Set entrySet = map.entrySet();
//		map.put(1, 1);
//		//TreapNode<Integer,Integer> e = new TreapNode<Integer, Integer>(1, 1, null);
//		entrySet.remove(e);
//		System.out.println(map);
		
		
		
	}

}
