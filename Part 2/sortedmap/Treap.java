package cmsc420.sortedmap;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * The treap is a combination of the binary search tree and the max-heap propertty.
 * It is similar to the treemap class, and for this purpose, most of the code will be 
 * used from the treemap source code  whose authors are @author Josh Bloch and @author Doug Lea. 
 * Changes will be made to the source code to implement the treap not using the red-black 
 * structure, and to also meet the project requirements.
 * 
 * @author Enock Gansou
 */
import java.util.Random;

public class Treap<K, V> extends AbstractMap<K, V> implements SortedMap<K, V> {
	
	static Random rand = new Random();
	
	/**
	 * The comparator used to maintain order in this treap, or
	 * null if it uses the natural ordering of its keys.
	 * 
	 * @serial
	 * */
	private final Comparator<? super K> comparator;
	
	private transient Entry<K,V> root;
	
	/**
	 * The number of entries in the treap
	 * */
	private transient int size = 0;
	
	/**
	 * The number of structural modifications to the treap.
	 * */
	private transient int modCount = 0;
	
	
	/**
	 * Test two values for equality.  Differs from o1.equals(o2) only in
		that it copes with {@code null} o1 properly.
	 */
	static final boolean valEquals(Object o1, Object o2) {
		return (o1==null ? o2==null : o1.equals(o2));
	}
	
	/**
	 * Return SimpleImmutableEntry for entry, or null if null
	 */
	static <K,V> Map.Entry<K,V> exportEntry(Treap.Entry<K,V> e) {
		return (e == null) ? null : new AbstractMap.SimpleImmutableEntry<>(e);
	}
	
	/**
	 * Return key for entry, or null if null
	 */
	static <K,V> K keyOrNull(Treap.Entry<K,V> e) {
		return (e == null) ? null : e.key;
	}
	
	/**
	 * Compares two keys using the correct comparison method for this Treap.
	 */
	final int compare(Object k1, Object k2) {
		return comparator==null ? ((Comparable<? super K>)k1).compareTo((K)k2)
				: comparator.compare((K)k1, (K)k2);
		}
	 
	static final class Entry<K,V> implements Map.Entry<K,V> {
		K key;
		V value;
		int priority = rand.nextInt();
		Entry<K,V> left = null;
		Entry<K,V> right = null;
		Entry<K,V> parent;
		
		/**
		  * Make a new cell with given key, value, and parent, and with
		  * null child links.
		  */
		public Entry(K key, V value, Entry<K,V> parent) {
			this.key = key;
			this.value = value;
			this.parent = parent;
		}
		/**
		  * Returns the key.
		  * 
		  * @return the key
		  */
		public K getKey() {
			return key;
		}
		
		/**
		  * Returns the priority.
		  * 
		  * @return the priority
		  */
		public int getPriority() {
			return priority;
		}
		/**
		  * Returns the value associated with the key.
		  *
		  * @return the value associated with the key
		  */
		public V getValue() {
			return value;
		}
		/**
		  * Replaces the value currently associated with the key with the given
		  * value.
		  *
		  * @return the value associated with the key before this method was
		  * called
		  *         */
		public V setValue(V value) {
			V oldValue = this.value;
			this.value = value;
			return oldValue;
		} 
		
		public boolean equals(Object o) {
			if (!(o instanceof Map.Entry))
				return false;
			Map.Entry<?,?> e = (Map.Entry<?,?>)o;
			return valEquals(key,e.getKey()) && valEquals(value,e.getValue());
		}
		
		public int hashCode() {
			int keyHash = (key==null ? 0 : key.hashCode());
			int valueHash = (value==null ? 0 : value.hashCode());
			return keyHash ^ valueHash;
		}
		
		public String toString() {
			return key + "=" + value;
		}
		
	}
	   
	/**
	 * Constructs a new, empty tree map, using the natural ordering of its
	 * keys.  
	 */ 
	public Treap(){
		comparator = null;
	}
	
	/**
	 * Constructs a new, empty tree map, ordered according to the given
	 * comparator.
	 */
	public Treap(Comparator<? super K> comp){
		this.comparator = comp;
	}
	
	/**
	 * Removes all of the mappings from this map.
	 * The map will be empty after this call returns.
	 * */
	@Override
	public void clear() {
		 modCount++;
		 size = 0;
		 root = null;	
	}
	
	/**
	 * Returns the root of the Treap
	 * @return the root of the treap
	 */
	public Map.Entry<K, V> getRoot(){
		return root;
	}
	
	 /**
	  * Associates the specified value with the specified key in this map.
	  *  If the map previously contained a mapping for the key, the old
	  *  value is replaced.
	  *  
	  *  @param key key with which the specified value is to be associated
	  *  @param value value to be associated with the specified key
	  *  
	  *  @return the previous value associated with key, or
	  *  null if there was no mapping for key.
	  *  (A null return can also indicate that the map
	  *  previously associated null with key.)
	  */
	@Override
	public V put(K key, V value) {
		if (key == null) throw new NullPointerException();
		Entry<K,V> curr = root;
		if (curr == null) {
			compare(key, key);  // type (and possibly null) check
			root = new Entry<>(key, value, null);
			size = 1;
			modCount++;
			return null;
		}
		int cmp;
		
		Entry<K,V> parent;
		do {
			parent = curr;
			cmp = compare(key, curr.key);
			if(cmp < 0){
				curr = curr.left;
			}
			else if(cmp > 0){
				curr = curr.right;
			}
			else {
				return curr.setValue(value);
			}
		} while (curr != null);	
		Entry<K,V> e = new Entry<>(key, value, parent);
		if (cmp < 0)
			parent.left = e;
		else parent.right = e;
		
		while (e != null && e != root) {
			if (e.parent.priority < e.priority){
				if ( e.equals(e.parent.left)) {
					e = e.parent;
					rotateRight(e);
				} 
				else if ( e.equals(e.parent.right)){
					e = e.parent;
					rotateLeft(e);
				}
			} 
			else{
				e = e.parent;
			}
		}
		
		size++;
		modCount++;
		return null;		
	}
			
	/** From CLR */
	private void rotateLeft(Entry<K,V> p) {
		
		Entry<K,V> pivot = p.right;
		p.right = pivot.left;
		if (pivot.left != null) pivot.left.parent = p; 
		pivot.parent = p.parent;
		pivot.left = p; 
		
		if (p.parent == null) root = pivot;
		else if (p.parent.left == p) p.parent.left = pivot;
		else p.parent.right = pivot;
			
		pivot.left = p; 
		p.parent = pivot;
	
	}
			
	/** From CLR */
	private void rotateRight(Entry<K,V> p) {
				
		Entry<K,V> pivot = p.left;
		p.left = pivot.right;
		if (pivot.right != null) pivot.right.parent = p; 
		pivot.parent = p.parent;
		
		if (p.parent == null) root = pivot;
		else if (p.parent.right == p) p.parent.right = pivot;
		else p.parent.left = pivot;
		
		pivot.right = p; 
		p.parent = pivot;
		
	}
	
	
	//For testing purposes
	
	public void printP ( Treap<K, V> t){
		printPHelper(t.root);
	}
	public void printPHelper( Entry<K, V> root2){
		if(root2 == null) return; 
		System.out.print(root2.priority + " ");
		printPHelper(root2.left);
		printPHelper(root2.right);
	}
	
	/**
	 * Returns {@code true} if this map contains a mapping for the specified
	 * key.
	 * 
	 * @param key key whose presence in this map is to be tested
	 * @return {@code true} if this map contains a mapping for the
	 * specified key
	 */
	@Override
	public boolean containsKey(Object key) {
		if (key == null) throw new NullPointerException();
		return getEntry(key) != null;
	}
	
	/**
	 * Returns this map's entry for the given key, or {@code null} if the map
	 * does not contain an entry for the key.
	 * 
	 * @return this map's entry for the given key, or {@code null} if the map
	 * does not contain an entry for the key
	 */
	final Entry<K,V> getEntry(Object key) {
		// Offload comparator-based version for sake of performance
		if (comparator != null)
			return getEntryUsingComparator(key);
		if (key == null)
			throw new NullPointerException();
		Comparable<? super K> k = (Comparable<? super K>) key;
		Entry<K,V> p = root;
		while (p != null) {
			int cmp = k.compareTo(p.key);
			if (cmp < 0)
				p = p.left;
			else if (cmp > 0)
				p = p.right;
			else
				return p;
		}
		return null;
	}
	
	/**
	 * Version of getEntry using comparator. Split off from getEntry
	 * for performance. (This is not worth doing for most methods,
	 * that are less dependent on comparator performance, but is
	 * worthwhile here.)
	 */
	 final Entry<K,V> getEntryUsingComparator(Object key) {
		 K k = (K)key;
		 Comparator<? super K> cpr = comparator;
		 if (cpr != null) {
			 Entry<K,V> p = root;
			 while (p != null) {
				 int cmp = cpr.compare(k, p.key);
				 if (cmp < 0)
					 p = p.left;
				 else if (cmp > 0)
					 p = p.right;
				 else
					 return p;
				 }
			 }
		 return null;
	 }
	 
	 /**
	  * Gets the entry corresponding to the specified key; if no such entry
	  * exists, returns the entry for the least key greater than the specified
	  * key; if no such entry exists (i.e., the greatest key in the Tree is less
	  * than the specified key), returns {@code null}.
	  */
	 final Entry<K,V> getCeilingEntry(K key) {
		 Entry<K,V> p = root;
		 while (p != null) {
			 int cmp = compare(key, p.key);
			 if (cmp < 0) {
				 if (p.left != null) p = p.left;
				 else return p;
			 } 
			 else if (cmp > 0) {
				 if (p.right != null) {
					 p = p.right;
				 } 
				 else {
					 Entry<K,V> parent = p.parent;
					 Entry<K,V> ch = p;
					 while (parent != null && ch == parent.right) {
						 ch = parent;
						 parent = parent.parent;
						 }
					 return parent;
				}
			 } 
			 else return p;
		}
		 return null;
	}
	 
	 /**
	  * Gets the entry corresponding to the specified key; if no such entry
	  * exists, returns the entry for the greatest key less than the specified
	  * key; if no such entry exists, returns {@code null}.
	  */
	 final Entry<K,V> getFloorEntry(K key) {
		 Entry<K,V> p = root;
		 while (p != null) {
			 int cmp = compare(key, p.key);
			 if (cmp > 0) {
				 if (p.right != null) p = p.right;
				 else return p;
			 } 
			 else if (cmp < 0) {
				 if (p.left != null) {
					 p = p.left;
				 }
				 else {
					 Entry<K,V> parent = p.parent;
					 Entry<K,V> ch = p;
					 while (parent != null && ch == parent.left) {
						 ch = parent;
						 parent = parent.parent;
					 }
					 return parent;
				 }
			 } 
			 else return p;
			 
		 }
		 return null;
	}
	 
	 /**
	  * Gets the entry for the least key greater than the specified
	  * key; if no such entry exists, returns the entry for the least
	  * key greater than the specified key; if no such entry exists
	  * returns {@code null}.
	  */
	 final Entry<K,V> getHigherEntry(K key) {
		 Entry<K,V> p = root;
		 while (p != null) {
			 int cmp = compare(key, p.key);
			 if (cmp < 0) {
				 if (p.left != null) p = p.left;
				 else return p;
			 } 
			 else {
				 if (p.right != null) {
					 p = p.right;
				 } 
				 else {
					 Entry<K,V> parent = p.parent;
					 Entry<K,V> ch = p;
					 while (parent != null && ch == parent.right) {
						 ch = parent;
						 parent = parent.parent;
					 }
					 return parent;
				}
			}
		}
		return null;
	}
	
	 /**
	  * Returns the entry for the greatest key less than the specified key; if
	  * no such entry exists (i.e., the least key in the Tree is greater than
	  * the specified key), returns {@code null}.
	  */
	 final Entry<K,V> getLowerEntry(K key) {
		 Entry<K,V> p = root;
		 while (p != null) {
			 int cmp = compare(key, p.key);
			 if (cmp > 0) {
				 if (p.right != null) p = p.right;
				 else return p;
			 } 
			 else {
				 if (p.left != null) {
					 p = p.left;
				 } 
				 else {
					 Entry<K,V> parent = p.parent;
					 Entry<K,V> ch = p;
					 while (parent != null && ch == parent.left) {
						 ch = parent;
						 parent = parent.parent;
					 }
					 return parent;
				}
			}
		 }
		 return null;
	}
	
	/**
	 * Returns {@code true} if this map maps one or more keys to the
	 * specified value.  More formally, returns {@code true} if and only if
	 * this map contains at least one mapping to a value {@code v} such
	 * that {@code (value==null ? v==null : value.equals(v))}.  This
	 * operation will probably require time linear in the map size for
	 * most implementations.
	 * @param value value whose presence in this map is to be tested
	 * @return {@code true} if a mapping to {@code value} exists;
	 * {@code false} otherwise
	 */
	@Override
	public boolean containsValue(Object value) {
		for (Entry<K,V> e = getFirstEntry(); e != null; e = successor(e))
			if (valEquals(value, e.value)) return true;
		return false;
	}
	
	/**
	 * Returns the first Entry in the Treap (according to the Treap's
	 *  key-sort function).  Returns null if the Treap is empty.
	 */
	final Entry<K,V> getFirstEntry() {
		Entry<K,V> p = root;
		if (p != null) while (p.left != null) p = p.left;
		return p;
	}
	
	/**
	 * Returns the last Entry in the Treap (according to the Treap's
	 * key-sort function).  Returns null if the Treap is empty.
	 */
	final Entry<K,V> getLastEntry() {
		Entry<K,V> p = root;
		if (p != null) while (p.right != null) p = p.right;
		return p;
	}
	
	/**
	 * Returns the successor of the specified Entry, or null if no such.
	 */
	static <K,V> Treap.Entry<K,V> successor(Entry<K,V> t) {
		if (t == null)
			return null;
		else if (t.right != null) {
			Entry<K,V> p = t.right;
			while (p.left != null)
				p = p.left;
			return p;
		} 
		else {
			Entry<K,V> p = t.parent;
			Entry<K,V> ch = t;
			while (p != null && ch == p.right) {
				ch = p;
				p = p.parent;
			}
			return p;
		}
	}
	
	/**
	 * Returns the predecessor of the specified Entry, or null if no such.
	 */
	static <K,V> Entry<K,V> predecessor(Entry<K,V> t) {
		if (t == null) return null;
		else if (t.left != null) {
			Entry<K,V> p = t.left;
			while (p.right != null)
				p = p.right;
			return p;
		} 
		else {
			Entry<K,V> p = t.parent;
			Entry<K,V> ch = t;
			while (p != null && ch == p.left) {
				ch = p;
				p = p.parent;
			}
			return p;
		}
	}
	
	/**
	 * Returns the value to which the specified key is mapped,
	 * or null if this map contains no mapping for the key.
	 * More formally, if this map contains a mapping from a key
	 * k to a value v such that key compares
	 * equal to k according to the map's ordering, then this
	 * method returns v; otherwise it returns null.
	 * (There can be at most one such mapping.)
	 * 
	 * A return value of null does not necessarily 
	 * indicate that the map contains no mapping for the key; it's also
	 * possible that the map explicitly maps the key to null.
	 * The containsKey operation may be used to
	 * distinguish these two cases.
	 */
	@Override
	public V get(Object key) {
		 if (key == null) throw new NullPointerException();
		 Entry<K,V> p = getEntry(key);
		 return (p==null ? null : p.value);
	}
	
	
	public V getValue(Map.Entry<K, V> e) {
		 if (e == null) throw new NullPointerException();
		 return ((Entry<K,V>) e).getValue();
	}
	
	public K getKey(Map.Entry<K, V> e) {
		 if (e == null) throw new NullPointerException();
		 return ((Entry<K,V>) e).getKey();
	}
	
	public int getPriority(Map.Entry<K, V> e) {
		 if (e == null) throw new NullPointerException();
		 return ((Entry<K,V>) e).getPriority();
	}
	
	public Map.Entry<K, V> getLeft(Map.Entry<K, V> e) {
		 if (e == null) throw new NullPointerException();
		 return ((Entry<K,V>) e).left;
	}
	
	public Map.Entry<K, V> getRight(Map.Entry<K, V> e) {
		 if (e == null) throw new NullPointerException();
		 return ((Entry<K,V>) e).right;
	}
	/**
	 *  Returns whether the treap is empty.
	 *  
	 *  @return true if empty, false otherwise
	 */
	@Override
	public boolean isEmpty() {
		return (root==null ? true : false);
	}
	
	/**
	 *  Returns the number of key-value mappings in this map.
	 *  
	 *  @return the number of key-value mappings in this map
	 */
	@Override
	public int size() {
		return size;
	}
	
	/**
	 *  Returns the comparator
	 *  
	 *  @return the comparator
	 */
	@Override
	public Comparator<? super K> comparator() {
		return comparator;
	}
	
	/**
	 *  @throws NoSuchElementException {@inheritDoc}
	 */
	@Override
	public K firstKey() {
		return key(getFirstEntry());
	}
	
	/**
	 * @throws NoSuchElementException {@inheritDoc}
	 */
	@Override
	public K lastKey() {
		return key(getLastEntry());
	}
	
	/**
	 * Returns the key corresponding to the specified Entry.
	 * @throws NoSuchElementException if the Entry is null
	 */
	static <K> K key(Entry<K,?> e) {
		if (e==null) throw new NoSuchElementException();
		return e.key;
	}
	
	
	/**
	 * @throws ClassCastException       {@inheritDoc}
	 * @throws NullPointerException if {@code fromKey} or {@code toKey} is
	 * null and this map uses natural ordering, or its comparator
	 * does not permit null keys
	 * @throws IllegalArgumentException {@inheritDoc}
	 */
	@Override
	public SortedMap<K, V> subMap(K fromKey, K toKey) {
		return subMap(fromKey, true, toKey, false);
	}
	
	
	/**
	 * @throws ClassCastException       {@inheritDoc}
	 * @throws NullPointerException if fromKey or toKey is
	 * null and this map uses natural ordering, or its comparator
	 * does not permit null keys
	 * @throws IllegalArgumentException {@inheritDoc}
	 * @since 1.6
	 */
	public NavigableMap<K,V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
		return new AscendingSubMap(this,false, fromKey, fromInclusive, false, toKey,   toInclusive);
	}
	
	
	// SubMaps
	
	/**
	 * Dummy value serving as unmatchable fence key for unbounded
	 * SubMapIterators
	 */
	private static final Object UNBOUNDED = new Object();
	
	/**
	 * @serial include
	 */
	abstract static class NavigableSubMap<K,V> extends AbstractMap<K,V>
	implements NavigableMap<K,V>, java.io.Serializable {
		/**
		 * The backing map.
		 */
		final Treap<K,V> m;
		
		/**
		 * Endpoints are represented as triples (fromStart, lo,
		 * loInclusive) and (toEnd, hi, hiInclusive). If fromStart is
		 * true, then the low (absolute) bound is the start of the
		 * backing map, and the other values are ignored. Otherwise,
		 * if loInclusive is true, lo is the inclusive bound, else lo
		 * is the exclusive bound. Similarly for the upper bound.
		 */
		final K lo, hi;
		final boolean fromStart, toEnd;
		final boolean loInclusive, hiInclusive;
		
		NavigableSubMap(Treap<K,V> m,
				boolean fromStart, K lo, boolean loInclusive,
				boolean toEnd,     K hi, boolean hiInclusive) {
			if (!fromStart && !toEnd) {
				if (m.compare(lo, hi) > 0)
					throw new IllegalArgumentException("fromKey > toKey");
				} 
				else {
					if (!fromStart) // type check
						m.compare(lo, lo);
					if (!toEnd) m.compare(hi, hi);
				}
			
			this.m = m;
			this.fromStart = fromStart;
			this.lo = lo;
			this.loInclusive = loInclusive;
			this.toEnd = toEnd;
			this.hi = hi;
			this.hiInclusive = hiInclusive;
		}
		
		// internal utilities
		
		final boolean tooLow(Object key) {
			if (!fromStart) {
				int c = m.compare(key, lo);
				if (c < 0 || (c == 0 && !loInclusive)) return true;
			}
			return false;
		}
		
		final boolean tooHigh(Object key) {
			if (!toEnd) {
				int c = m.compare(key, hi);
				if (c > 0 || (c == 0 && !hiInclusive))
					return true;
				}
			return false;
		}
		
		final boolean inRange(Object key) {
			return !tooLow(key) && !tooHigh(key);
		}
		
		final boolean inClosedRange(Object key) {
			return (fromStart || m.compare(key, lo) >= 0)
					&& (toEnd || m.compare(hi, key) >= 0);
		}
		
		final boolean inRange(Object key, boolean inclusive) {
			return inclusive ? inRange(key) : inClosedRange(key);
		}
		
		/*
		 * Absolute versions of relation operations.
		 * Subclasses map to these using like-named "sub"
		 * versions that invert senses for descending maps
		 */
		
		final Treap.Entry<K,V> absLowest() {
			Treap.Entry<K,V> e =
					(fromStart ?  m.getFirstEntry() : (loInclusive ? m.getCeilingEntry(lo) :
						m.getHigherEntry(lo)));
			return (e == null || tooHigh(e.key)) ? null : e;
		}
		
		final Treap.Entry<K,V> absHighest() {
			Treap.Entry<K,V> e =
					(toEnd ?  m.getLastEntry() : (hiInclusive ?  m.getFloorEntry(hi) :
						m.getLowerEntry(hi)));
			return (e == null || tooLow(e.key)) ? null : e;
		}
		
		final Treap.Entry<K,V> absCeiling(K key) {
			if (tooLow(key)) return absLowest();
			Treap.Entry<K,V> e = m.getCeilingEntry(key);
			return (e == null || tooHigh(e.key)) ? null : e;
		}
		
		final Treap.Entry<K,V> absHigher(K key) {
			if (tooLow(key))
				return absLowest();
			Treap.Entry<K,V> e = m.getHigherEntry(key);
			return (e == null || tooHigh(e.key)) ? null : e;
		}
		
		final Treap.Entry<K,V> absFloor(K key) {
			if (tooHigh(key)) return absHighest();
			Treap.Entry<K,V> e = m.getFloorEntry(key);
			return (e == null || tooLow(e.key)) ? null : e;
		}
		
		final Treap.Entry<K,V> absLower(K key) {
			if (tooHigh(key)) return absHighest();
			Treap.Entry<K,V> e = m.getLowerEntry(key);
			return (e == null || tooLow(e.key)) ? null : e;
		}
		
		/** Returns the absolute high fence for ascending traversal */
		final Treap.Entry<K,V> absHighFence() {
			return (toEnd ? null : (hiInclusive ? m.getHigherEntry(hi) : m.getCeilingEntry(hi)));
		}
		
		/** Return the absolute low fence for descending traversal  */
		final Treap.Entry<K,V> absLowFence() {
			return (fromStart ? null : (loInclusive ? m.getLowerEntry(lo) : m.getFloorEntry(lo)));
		}
		
		// Abstract methods defined in ascending vs descending classes
		// These relay to the appropriate absolute versions
		
		abstract Treap.Entry<K,V> subLowest();
		abstract Treap.Entry<K,V> subHighest();
		abstract Treap.Entry<K,V> subCeiling(K key);
		abstract Treap.Entry<K,V> subHigher(K key);
		abstract Treap.Entry<K,V> subFloor(K key);
		abstract Treap.Entry<K,V> subLower(K key);
		
		/** Returns ascending iterator from the perspective of this submap */
		abstract Iterator<K> keyIterator();
		
		/** Returns descending iterator from the perspective of this submap */
		abstract Iterator<K> descendingKeyIterator();
		
		// public methods
		public boolean isEmpty() {
			return (fromStart && toEnd) ? m.isEmpty() : entrySet().isEmpty();
		}
		
		public int size() {
			return (fromStart && toEnd) ? m.size() : entrySet().size();
		}
		
		public final boolean containsKey(Object key) {
			return inRange(key) && m.containsKey(key);
		}
		
		public final V put(K key, V value) {
			if (!inRange(key)) throw new IllegalArgumentException("key out of range");
			return m.put(key, value);
		}
		
		public final V get(Object key) {
			return !inRange(key) ? null :  m.get(key);
		}
		
		public final V remove(Object key) {
			return !inRange(key) ? null : m.remove(key);
		}
		
		public final Map.Entry<K,V> ceilingEntry(K key) {
			return exportEntry(subCeiling(key));
		}
		
		public final K ceilingKey(K key) {
			return keyOrNull(subCeiling(key));
		}
		
		public final Map.Entry<K,V> higherEntry(K key) {
			return exportEntry(subHigher(key));
		}
		
		public final K higherKey(K key) {
			return keyOrNull(subHigher(key));
		}
		
		public final Map.Entry<K,V> floorEntry(K key) {
			return exportEntry(subFloor(key));
		}
		
		public final K floorKey(K key) {
			return keyOrNull(subFloor(key));
		}
		
		public final Map.Entry<K,V> lowerEntry(K key) {
			return exportEntry(subLower(key));
		}
		
		public final K lowerKey(K key) {
			return keyOrNull(subLower(key));
		}
		
		public final K firstKey() {
			return key(subLowest());
		}
		
		public final K lastKey() {
			return key(subHighest());
		}
		
		public final Map.Entry<K,V> firstEntry() {
			return exportEntry(subLowest());
		}
		
		public final Map.Entry<K,V> lastEntry() {
			return exportEntry(subHighest());
		}
		
		public final Map.Entry<K,V> pollFirstEntry() {
			Treap.Entry<K,V> e = subLowest();
			Map.Entry<K,V> result = exportEntry(e);
			if (e != null) m.deleteEntry(e);
			return result;
		}
		
		public final Map.Entry<K,V> pollLastEntry() {
			Treap.Entry<K,V> e = subHighest();
			Map.Entry<K,V> result = exportEntry(e);
			if (e != null) m.deleteEntry(e);
			return result;
		}
		
		// Views
		transient NavigableMap<K,V> descendingMapView = null;
		transient EntrySetView entrySetView = null;
		transient KeySet<K> navigableKeySetView = null;
		
		public final NavigableSet<K> navigableKeySet() {
			KeySet<K> nksv = navigableKeySetView;
			return (nksv != null) ? nksv : (navigableKeySetView = new Treap.KeySet(this));
		}
		
		public final Set<K> keySet() {
			return navigableKeySet();
		}
		
		public NavigableSet<K> descendingKeySet() {
			return descendingMap().navigableKeySet();
		}
		
		public final SortedMap<K,V> subMap(K fromKey, K toKey) {
			return subMap(fromKey, true, toKey, false);
		}
		
		public final SortedMap<K,V> headMap(K toKey) {
			return headMap(toKey, false);
		}
		
		public final SortedMap<K,V> tailMap(K fromKey) {
			return tailMap(fromKey, true);
		}
		
		// View classes
		
		abstract class EntrySetView extends AbstractSet<Map.Entry<K,V>> {
			private transient int size = -1, sizeModCount;
			public int size() {
				if (fromStart && toEnd) return m.size();
				if (size == -1 || sizeModCount != m.modCount) {
					sizeModCount = m.modCount;
					size = 0;
					Iterator i = iterator();
					while (i.hasNext()) {
						size++;
						i.next();
					}
				}
				return size;
			}
			
			public boolean isEmpty() {
				Treap.Entry<K,V> n = absLowest();
				return n == null || tooHigh(n.key);
			}
			
			public boolean contains(Object o) {
				if (!(o instanceof Map.Entry))
					return false;
				Map.Entry<K,V> entry = (Map.Entry<K,V>) o;
				K key = entry.getKey();
				if (!inRange(key)) return false;
				Treap.Entry node = m.getEntry(key);
				return node != null && valEquals(node.getValue(), entry.getValue());
			}
			
			public boolean remove(Object o) {
				if (!(o instanceof Map.Entry)) return false;
				Map.Entry<K,V> entry = (Map.Entry<K,V>) o;
				K key = entry.getKey();
				if (!inRange(key)) return false;
				Treap.Entry<K,V> node = m.getEntry(key);
				if (node!=null && valEquals(node.getValue(), entry.getValue())) {
					m.deleteEntry(node); 
					return true;
				}
				return false;
			}
		}
		
		/**
		 * Iterators for SubMaps
		 */
		
		abstract class SubMapIterator<T> implements Iterator<T> {
			Treap.Entry<K,V> lastReturned;
			Treap.Entry<K,V> next;
			final Object fenceKey;
			int expectedModCount;
			
			SubMapIterator(Treap.Entry<K,V> first, Treap.Entry<K,V> fence) {
				expectedModCount = m.modCount;
				lastReturned = null;
				next = first;
				fenceKey = fence == null ? UNBOUNDED : fence.key;
			}
			
			public final boolean hasNext() {
				return next != null && next.key != fenceKey;
			}
			
			final Treap.Entry<K,V> nextEntry() {
				Treap.Entry<K,V> e = next;
				if (e == null || e.key == fenceKey)
					throw new NoSuchElementException();
				if (m.modCount != expectedModCount)
					throw new ConcurrentModificationException();
				next = successor(e);
				lastReturned = e;
				return e;
			}
			
			final Treap.Entry<K,V> prevEntry() {
				Treap.Entry<K,V> e = next;
				if (e == null || e.key == fenceKey) throw new NoSuchElementException();
				if (m.modCount != expectedModCount) throw new ConcurrentModificationException();
				next = predecessor(e);
				lastReturned = e;
				return e;
			}
			
			final void removeAscending() {
				if (lastReturned == null)
					throw new IllegalStateException();
				if (m.modCount != expectedModCount)
					throw new ConcurrentModificationException();
				// deleted entries are replaced by their successors
				if (lastReturned.left != null && lastReturned.right != null)
					next = lastReturned;
				m.deleteEntry(lastReturned);
				lastReturned = null;
				expectedModCount = m.modCount;
			}
			
			final void removeDescending() {
				if (lastReturned == null) throw new IllegalStateException();
				if (m.modCount != expectedModCount) throw new ConcurrentModificationException();
				m.deleteEntry(lastReturned);
				lastReturned = null;
				expectedModCount = m.modCount;
			}
		}
		
		final class SubMapEntryIterator extends SubMapIterator<Map.Entry<K,V>> {
			
			SubMapEntryIterator(Treap.Entry<K,V> first, Treap.Entry<K,V> fence) {
				super(first, fence);
				}
			public Map.Entry<K,V> next() {
				return nextEntry();
			}
			
			public void remove() {
				removeAscending();
			}
		}
		
		final class SubMapKeyIterator extends SubMapIterator<K> {
			
			SubMapKeyIterator(Treap.Entry<K,V> first, Treap.Entry<K,V> fence) {
				super(first, fence);
			}
			
			public K next() {
				return nextEntry().key;
			}
			
			public void remove() {
				removeAscending();
			}
		}
		
		final class DescendingSubMapEntryIterator extends SubMapIterator<Map.Entry<K,V>> {
			
			DescendingSubMapEntryIterator(Treap.Entry<K,V> last, Treap.Entry<K,V> fence) {
				super(last, fence);
			}
			
			public Map.Entry<K,V> next() {
				return prevEntry();
			}
			
			public void remove() {
				removeDescending();
			}
		}
		
		final class DescendingSubMapKeyIterator extends SubMapIterator<K> {
			
			DescendingSubMapKeyIterator(Treap.Entry<K,V> last, Treap.Entry<K,V> fence) {
				super(last, fence);
			}
			
			public K next() {
				return prevEntry().key;
			}
			
			public void remove() {
				removeDescending();
			}
		}
	}
	
	/**
	 * @serial include
	 */
	static final class AscendingSubMap<K,V> extends NavigableSubMap<K,V> {
		private static final long serialVersionUID = 912986545866124060L;
		
		AscendingSubMap(Treap<K,V> m, boolean fromStart, K lo, boolean loInclusive, 
				boolean toEnd, K hi, boolean hiInclusive) {
			super(m, fromStart, lo, loInclusive, toEnd, hi, hiInclusive);
		}
		
		public Comparator<? super K> comparator() {
			return m.comparator();
		}
		
		public NavigableMap<K,V> subMap(K fromKey, boolean fromInclusive, K toKey,   boolean toInclusive) {
			if (!inRange(fromKey, fromInclusive))
				throw new IllegalArgumentException("fromKey out of range");
			if (!inRange(toKey, toInclusive))
				throw new IllegalArgumentException("toKey out of range");
			return new AscendingSubMap(m, false, fromKey, fromInclusive, false, toKey,   toInclusive);
		}
		
		public NavigableMap<K,V> headMap(K toKey, boolean inclusive) {
			if (!inRange(toKey, inclusive))
				throw new IllegalArgumentException("toKey out of range");
			return new AscendingSubMap(m, fromStart, lo, loInclusive, false,toKey, inclusive);
		}
		
		public NavigableMap<K,V> tailMap(K fromKey, boolean inclusive) {
			if (!inRange(fromKey, inclusive))
				throw new IllegalArgumentException("fromKey out of range");
			return new AscendingSubMap(m, false, fromKey, inclusive, toEnd, hi, hiInclusive);
		}
		
		public NavigableMap<K,V> descendingMap() {
			NavigableMap<K,V> mv = descendingMapView;
			return (mv != null) ? mv :
				(descendingMapView =  new DescendingSubMap(m, fromStart, lo, loInclusive, 
						toEnd, hi, hiInclusive));
		}
		
		Iterator<K> keyIterator() {
			return new SubMapKeyIterator(absLowest(), absHighFence());
		}
		
		Iterator<K> descendingKeyIterator() {
			return new DescendingSubMapKeyIterator(absHighest(), absLowFence());
		}
		
		final class AscendingEntrySetView extends EntrySetView {
			public Iterator<Map.Entry<K,V>> iterator() {
				return new SubMapEntryIterator(absLowest(), absHighFence());
			}
		}
		
		public Set<Map.Entry<K,V>> entrySet() {
			EntrySetView es = entrySetView;
			return (es != null) ? es : new AscendingEntrySetView();
		}
		
		Treap.Entry<K,V> subLowest()       { return absLowest(); }
		Treap.Entry<K,V> subHighest()      { return absHighest(); }
		Treap.Entry<K,V> subCeiling(K key) { return absCeiling(key); }
		Treap.Entry<K,V> subHigher(K key)  { return absHigher(key); }
		Treap.Entry<K,V> subFloor(K key)   { return absFloor(key); }
		Treap.Entry<K,V> subLower(K key)   { return absLower(key); }
	}
	 
	/**
	 * @serial include
	 */ 
	static final class DescendingSubMap<K,V>  extends NavigableSubMap<K,V> {
		
		private static final long serialVersionUID = 912986545866120460L;
		
		DescendingSubMap(Treap<K,V> m, boolean fromStart, K lo, boolean loInclusive,
				boolean toEnd, K hi, boolean hiInclusive) {
			super(m, fromStart, lo, loInclusive, toEnd, hi, hiInclusive);
		}
		
		private final Comparator<? super K> reverseComparator = Collections.reverseOrder(m.comparator);
		public Comparator<? super K> comparator() {
			return reverseComparator;
		}
		public NavigableMap<K,V> subMap(K fromKey, boolean fromInclusive, K toKey,   boolean toInclusive) {
			if (!inRange(fromKey, fromInclusive))
				throw new IllegalArgumentException("fromKey out of range");
			if (!inRange(toKey, toInclusive))
				throw new IllegalArgumentException("toKey out of range");
			return new DescendingSubMap(m, false, toKey,   toInclusive, false, fromKey, fromInclusive);
		}
		
		public NavigableMap<K,V> headMap(K toKey, boolean inclusive) {
			if (!inRange(toKey, inclusive)) throw new IllegalArgumentException("toKey out of range");
			return new DescendingSubMap(m, false, toKey, inclusive,toEnd, hi,    hiInclusive);
		}
		
		public NavigableMap<K,V> tailMap(K fromKey, boolean inclusive) {
			if (!inRange(fromKey, inclusive))
				throw new IllegalArgumentException("fromKey out of range");
			return new DescendingSubMap(m, fromStart, lo, loInclusive, false, fromKey, inclusive);
		}
		
		public NavigableMap<K,V> descendingMap() {
			NavigableMap<K,V> mv = descendingMapView;
			return (mv != null) ? mv :
				(descendingMapView = new AscendingSubMap(m, fromStart, lo, loInclusive,
						toEnd, hi, hiInclusive));
		}
		
		Iterator<K> keyIterator() {
			return new DescendingSubMapKeyIterator(absHighest(), absLowFence());
		}
		
		Iterator<K> descendingKeyIterator() {
			return new SubMapKeyIterator(absLowest(), absHighFence());
		}
		
		final class DescendingEntrySetView extends EntrySetView {
			public Iterator<Map.Entry<K,V>> iterator() {
				return new DescendingSubMapEntryIterator(absHighest(), absLowFence());
			}
		}
		
		public Set<Map.Entry<K,V>> entrySet() {
			EntrySetView es = entrySetView;
			return (es != null) ? es : new DescendingEntrySetView();
		}
		Treap.Entry<K,V> subLowest()       { return absHighest(); }
		Treap.Entry<K,V> subHighest()      { return absLowest(); }
		Treap.Entry<K,V> subCeiling(K key) { return absFloor(key); }
		Treap.Entry<K,V> subHigher(K key)  { return absLower(key); }
		Treap.Entry<K,V> subFloor(K key)   { return absCeiling(key); }
		Treap.Entry<K,V> subLower(K key)   { return absHigher(key); }
	}
	
	/**
	 * This class exists solely for the sake of serialization
	 * compatibility with previous releases of Treap that did not
	 * support NavigableMap.  It translates an old-version SubMap into
	 * a new-version AscendingSubMap. This class is never otherwise
	 * used.
	 * 
	 * @serial include
	 */
	private class SubMap extends AbstractMap<K,V> implements SortedMap<K,V>, java.io.Serializable {
		private static final long serialVersionUID = -6520786458950516097L;
		private boolean fromStart = false, toEnd = false;
		private K fromKey, toKey;
		private Object readResolve() {
			return new AscendingSubMap(Treap.this, fromStart, fromKey, true, toEnd, toKey, false);
		}
		public Set<Map.Entry<K,V>> entrySet() { throw new InternalError(); }
		public K lastKey() { throw new InternalError(); }
		public K firstKey() { throw new InternalError(); }
		public SortedMap<K,V> subMap(K fromKey, K toKey) { throw new InternalError(); }
		public SortedMap<K,V> headMap(K toKey) { throw new InternalError(); }
		public SortedMap<K,V> tailMap(K fromKey) { throw new InternalError(); }
		public Comparator<? super K> comparator() { throw new InternalError(); }
	}
	
	/*
	 * Unlike Values and EntrySet, the KeySet class is static,
	 * delegating to a NavigableMap to allow use by SubMaps, which
	 * outweighs the ugliness of needing type-tests for the following
	 * Iterator methods that are defined appropriately in main versus
	 * submap classes.
	 */
	
	Iterator<K> keyIterator() {
		return new KeyIterator(getFirstEntry());
	}
	
	Iterator<K> descendingKeyIterator() {
		return new DescendingKeyIterator(getLastEntry());
	}
	
	static final class KeySet<E> extends AbstractSet<E> implements NavigableSet<E> {
		private final NavigableMap<E, Object> m;
		KeySet(NavigableMap<E,Object> map) { m = map; }
		public Iterator<E> iterator() {
			if (m instanceof Treap) return ((Treap<E,Object>)m).keyIterator();
			else
				return (Iterator<E>)(((Treap.NavigableSubMap)m).keyIterator());
			}
		public Iterator<E> descendingIterator() {
			if (m instanceof Treap) return ((Treap<E,Object>)m).descendingKeyIterator();
			else return (Iterator<E>)(((Treap.NavigableSubMap)m).descendingKeyIterator());
		}
		
		public int size() { return m.size(); }
		public boolean isEmpty() { return m.isEmpty(); }
		public boolean contains(Object o) { return m.containsKey(o); }
		public void clear() { m.clear(); }
		public E lower(E e) { return m.lowerKey(e); }
		public E floor(E e) { return m.floorKey(e); }
		public E ceiling(E e) { return m.ceilingKey(e); }
		public E higher(E e) { return m.higherKey(e); }
		public E first() { return m.firstKey(); }
		public E last() { return m.lastKey(); }
		public Comparator<? super E> comparator() { return m.comparator(); }
		public E pollFirst() {
			Map.Entry<E,Object> e = m.pollFirstEntry();
			return (e == null) ? null : e.getKey();
		}
		public E pollLast() {
			Map.Entry<E,Object> e = m.pollLastEntry();
			return (e == null) ? null : e.getKey();
		}
		public boolean remove(Object o) {
			int oldSize = size();
			m.remove(o);
			return size() != oldSize;
		}
		
		public NavigableSet<E> subSet(E fromElement, boolean fromInclusive,
				E toElement,   boolean toInclusive) {
			return new KeySet<>(m.subMap(fromElement, fromInclusive, toElement, toInclusive));
		}
		
		public NavigableSet<E> headSet(E toElement, boolean inclusive) {
			return new KeySet<>(m.headMap(toElement, inclusive));
		}
		
		public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
			return new KeySet<>(m.tailMap(fromElement, inclusive));
		}
		
		public SortedSet<E> subSet(E fromElement, E toElement) {
			return subSet(fromElement, true, toElement, false);
		}
		
		public SortedSet<E> headSet(E toElement) {
			return headSet(toElement, false);
		}
		
		public SortedSet<E> tailSet(E fromElement) {
			return tailSet(fromElement, true);
		}
		
		public NavigableSet<E> descendingSet() {
			return new KeySet(m.descendingMap());
		}
	}
	
	/**
	 * Base class for Treap Iterators
	 */
	abstract class PrivateEntryIterator<T> implements Iterator<T> {
		Entry<K,V> next;
		Entry<K,V> lastReturned;
		int expectedModCount;

		PrivateEntryIterator(Entry<K,V> first) {
			expectedModCount = modCount;
			lastReturned = null;
			next = first;
		}
		
		public final boolean hasNext() {
			return next != null;
		}
		
		final Entry<K,V> nextEntry() {
			Entry<K,V> e = next;
			if (e == null)
				throw new NoSuchElementException();
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();
			next = successor(e);
			lastReturned = e;
			return e;
			}
		
		final Entry<K,V> prevEntry() {
			Entry<K,V> e = next;
			if (e == null) throw new NoSuchElementException();
			if (modCount != expectedModCount) throw new ConcurrentModificationException();
			next = predecessor(e);
			lastReturned = e;
			return e;
		}
		
		public void remove() {
			if (lastReturned == null)
				throw new IllegalStateException();
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();
			// deleted entries are replaced by their successors
			if (lastReturned.left != null && lastReturned.right != null)
				next = lastReturned;
			deleteEntry(lastReturned);
			expectedModCount = modCount;
			lastReturned = null;
		}
	}
	
	final class EntryIterator extends PrivateEntryIterator<Map.Entry<K,V>> {	
		EntryIterator(Entry<K,V> first) {
			super(first);
		}
		public Map.Entry<K,V> next() {
			if (modCount != expectedModCount) throw new ConcurrentModificationException();
			return nextEntry();
		}
	}
	
	final class ValueIterator extends PrivateEntryIterator<V> {
		ValueIterator(Entry<K,V> first) {
			super(first);
		}
		public V next() {
			if (modCount != expectedModCount) throw new ConcurrentModificationException();
			return nextEntry().value;
		}
	}
	
	final class KeyIterator extends PrivateEntryIterator<K> {
		KeyIterator(Entry<K,V> first) {
			super(first);
		}
		public K next() {
			if (modCount != expectedModCount) throw new ConcurrentModificationException();
			return nextEntry().key;
		}
	}
	
	final class DescendingKeyIterator extends PrivateEntryIterator<K> {
		DescendingKeyIterator(Entry<K,V> first) {
			super(first);
		}
		public K next() {
			if (modCount != expectedModCount) throw new ConcurrentModificationException();
			return prevEntry().key;
		}
	}
	
	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return new EntrySet();
	}
	
	protected class EntrySet  extends AbstractSet implements Set{
		
		@Override
		public void clear() {
			Treap.this.clear();
		}
		
		@Override
		public int size() {
			return Treap.this.size();
		}
		
		@Override
		public boolean isEmpty() {
			return Treap.this.isEmpty();
		}
		
		@Override
		public Iterator<Map.Entry<K,V>> iterator() {
			return new EntryIterator(getFirstEntry());
		}
		
		@Override
		public boolean equals(Object o){
			if (o == this) return true;
			
	        if (!(o instanceof Set)) return false;
	        
	        Set m = (Set)o;
	        if (m.size() != size()) return false;
	        try {
	        	Iterator<Map.Entry<K,V>> i = m.iterator();
	        	while (i.hasNext()) {
	        		Map.Entry<K,V> e = i.next();
	        		if(!contains(e)) return false;
	        	}
	        } 
	        catch (ClassCastException unused) {
	        	return false;
	        } 
	        catch (NullPointerException unused) {
	        	return false;
	        }
	        return true;
	   }
		
		public int hashCode(){
			return Treap.this.hashCode();
		}
		
		@Override
		public boolean remove(Object o) {
			Map.Entry<K,V> me = (Map.Entry<K,V>)o;
			// throws a ClassCastException if this fails,
			// as per the API for Set
			boolean b = Treap.this.containsKey(me.getKey());
			Treap.this.remove(me.getKey());
			return b;
		}
		
		@Override
		public boolean contains(Object o) {
			Map.Entry<K,V> me = (Map.Entry<K,V>) o;
			return Treap.this.containsKey(me.getKey()) &&
					(me.getValue() == null ? Treap.this.get(me.getKey()) == null :
						me.getValue().equals(Treap.this.get(me.getKey())));
		}
		
		@Override
		public boolean add(Object o) {
			/*Map.Entry<K,V> me = (Map.Entry<K,V>)o;
			if(Treap.this.containsKey(me.getKey()) && me.getValue().equals(Treap.this.get(me.getKey()))){
				return false;
			}
			else {
				Treap.this.put(me.getKey(), me.getValue());
				return true;
			}*/
			throw new UnsupportedOperationException();
		}
		
		@Override
		public boolean addAll(Collection collection) {
			/* boolean modified = false;
			Iterator<?> e = collection.iterator();
			while (e.hasNext()) {
				if (add(e.next())) modified = true;
			}
			return modified; */
			throw new UnsupportedOperationException();
		}
		
		@Override
		public boolean containsAll(Collection collection) {
			Iterator<?> e = collection.iterator();
		    while (e.hasNext())
		        if (!contains(e.next()))
		        return false;
		    return true;
		}
		
		@Override
		public boolean removeAll(Collection collection) {
			boolean modified = false;
			Iterator<?> e = iterator();
			while (e.hasNext()) {
				if (collection.contains(e.next())) {
					e.remove();
					modified = true;
				}
			}
			return modified;
		}
		
		@Override
		public boolean retainAll(Collection collection) {
			boolean modified = false;
			Iterator<?> e = iterator();
			while (e.hasNext()) {
				if (!collection.contains(e.next())) {
					e.remove();
					modified = true;
				}
			}
			return modified;
		}
		
		@Override
		public Object[] toArray() {
			// Estimate size of array; be prepared to see more or fewer elements
			Object[] r = new Object[size()];
			Iterator<?> it = iterator();
			for (int i = 0; i < r.length; i++) {
				if (! it.hasNext()) // fewer elements than expected
					return Arrays.copyOf(r, i);
				r[i] = it.next();
			}
			return it.hasNext() ? finishToArray(r, it) : r;
		}
		
		@Override
		public Object[] toArray(Object[] a) {
			// Estimate size of array; be prepared to see more or fewer elements
			int size = size();
			Object[] r = a.length >= size ? a :
				(Object[])java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
			Iterator<?> it = iterator();
			
			for (int i = 0; i < r.length; i++) {
				if (! it.hasNext()) { // fewer elements than expected
					if (a != r) return Arrays.copyOf(r, i);
					r[i] = null; // null-terminate
					return r;
				}
				r[i] = it.next();
			}
			return it.hasNext() ? finishToArray(r, it) : r;
		}

		private <T> T[] finishToArray (T[] r, Iterator<?> it) {
			int i = r.length;
			while (it.hasNext()) {
				int cap = r.length;
				if (i == cap) {
					int newCap = ((cap / 2) + 1) * 3;
					if (newCap <= cap) { // integer overflow
						if (cap == Integer.MAX_VALUE) throw new OutOfMemoryError
						("Required array size too large");
						newCap = Integer.MAX_VALUE;
					}
					r = Arrays.copyOf(r, newCap);
				}
				r[i++] = (T)it.next();
			}
			// trim if overallocated
			return (i == r.length) ? r : Arrays.copyOf(r, i);
		}
		
		public String toString() {
			Iterator<?> i = iterator();
			if (! i.hasNext()) return "[]";
			StringBuilder sb = new StringBuilder();
			sb.append('[');
			for (;;) {
				Object e = i.next();
				sb.append(e);
				if (! i.hasNext()) return sb.append(']').toString();
				sb.append(", ");
			}
		}
		
	}
	
	/**
	 * Copies all of the mappings from the specified map to this map.
	 * These mappings replace any mappings that this map had for any
	 * of the keys currently in the specified map.
	 * 
	 * @param  map mappings to be stored in this map
	 * @throws ClassCastException if the class of a key or value in
	 * the specified map prevents it from being stored in this map
	 * @throws NullPointerException if the specified map is null or
	 * the specified map contains a null key and this map does not
	 * permit null keys
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		if (map == null) throw new NullPointerException();
		Iterator<?> e = map.entrySet().iterator();
		while (e.hasNext()) {
			Map.Entry<K, V> m = (Map.Entry<K, V>)e.next();
		}
		e = map.entrySet().iterator();
		while (e.hasNext()) {
			Map.Entry<K, V> m = (Map.Entry<K, V>)e.next();
			K key= m.getKey();
			V value= m.getValue();
			Treap.this.put(key,value);
		}
	}
	
	
	
	@Override
	public V remove(Object arg0) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Delete node p, and then rebalance the tree.
	 */
	private void deleteEntry(Entry<K,V> p) {
		/* fix this for part 3 
		modCount++;
		size--;
		// If strictly internal, copy successor's element to p and then make p
		// point to successor.
		if (p.left != null && p.right != null) {
			Entry<K,V> s = successor(p);
			p.key = s.key;
			p.value = s.value;
			p = s;
		} // p has 2 children
		// Start fixup at replacement node, if it exists.
		Entry<K,V> replacement = (p.left != null ? p.left : p.right);
		
		if (replacement != null) {
			// Link replacement to parent
			replacement.parent = p.parent;
			if (p.parent == null) root = replacement;
			else if (p == p.parent.left) p.parent.left  = replacement;
			else p.parent.right = replacement;
			
			// Null out links so they are OK to use by fixAfterDeletion.
			p.left = p.right = p.parent = null;
			
			// Fix replacement
			if (p.color == BLACK) fixAfterDeletion(replacement);
		} 
		else if (p.parent == null) { // return if we are the only node.
			root = null;
		}
		else { //  No children. Use self as phantom replacement and unlink.
			if (p.color == BLACK) fixAfterDeletion(p);
			
			if (p.parent != null) {
				if (p == p.parent.left) p.parent.left = null;
				else if (p == p.parent.right) p.parent.right = null;
				p.parent = null;
			}
		} */
		throw new UnsupportedOperationException();
	}
	
	
	/** From CLR */
	/* Fix this for part 3 
	private void fixAfterDeletion(Entry<K,V> x) {
		while (x != root && colorOf(x) == BLACK) {
			if (x == leftOf(parentOf(x))) {
				Entry<K,V> sib = rightOf(parentOf(x));
				
				if (colorOf(sib) == RED) {
					setColor(sib, BLACK);
					setColor(parentOf(x), RED);
					rotateLeft(parentOf(x));
					sib = rightOf(parentOf(x));
				}
				
				if (colorOf(leftOf(sib))  == BLACK && colorOf(rightOf(sib)) == BLACK) {
					setColor(sib, RED);
					x = parentOf(x);
				}
				else {
					if (colorOf(rightOf(sib)) == BLACK) {
						setColor(leftOf(sib), BLACK);
						setColor(sib, RED);
						rotateRight(sib);
						sib = rightOf(parentOf(x));
					}
					setColor(sib, colorOf(parentOf(x)));
					setColor(parentOf(x), BLACK);
					setColor(rightOf(sib), BLACK);
					rotateLeft(parentOf(x));
					x = root;
				}
			} 
			else { // symmetric
				Entry<K,V> sib = leftOf(parentOf(x));
				
				if (colorOf(sib) == RED) {
					setColor(sib, BLACK);
					setColor(parentOf(x), RED);
					rotateRight(parentOf(x));
					sib = leftOf(parentOf(x));
				}
				
				if (colorOf(rightOf(sib)) == BLACK && colorOf(leftOf(sib)) == BLACK) {
					setColor(sib, RED);
					x = parentOf(x);
				}
				else {
					if (colorOf(leftOf(sib)) == BLACK) {
						setColor(rightOf(sib), BLACK);
						setColor(sib, RED);
						rotateLeft(sib);
						sib = leftOf(parentOf(x));
					}
					setColor(sib, colorOf(parentOf(x)));
					setColor(parentOf(x), BLACK);
					setColor(leftOf(sib), BLACK);
					rotateRight(parentOf(x));
					x = root;
				}
			}
		}
		setColor(x, BLACK);
	}
	*/
	
	@Override
	public Set<K> keySet() {
		throw new UnsupportedOperationException();
	}
	@Override
	public SortedMap<K, V> headMap(K toKey) {
		throw new UnsupportedOperationException();
	}
	@Override
	public SortedMap<K, V> tailMap(K fromKey) {
		throw new UnsupportedOperationException();
	}
	@Override
	public Collection<V> values() {
		throw new UnsupportedOperationException();
	}
	
	
}

