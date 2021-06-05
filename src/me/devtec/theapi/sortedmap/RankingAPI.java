package me.devtec.theapi.sortedmap;


import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class RankingAPI<K, V> {
	private Map<K, V> s;

	public RankingAPI(Map<K, V> map) {
		setMap(map);
	}

	public K getObject(int position) {
		if (position <= 0)
			position = 1;
		int i = 0;
		for (Entry<K, V> e : s.entrySet())
			if (++i >= position)
				return e.getKey();
		return null;
	}

	public int size() {
		return s.size();
	}

	public void clear() {
		s.clear();
	}

	public Set<K> getKeySet() {
		return s.keySet();
	}

	public Map<K, V> getMap() {
		return s;
	}

	public void setMap(Map<K, V> map) {
		s = SortedMap.sortNonComparableByValue(map);
	}

	public Set<Entry<K, V>> entrySet() {
		return s.entrySet();
	}

	public boolean containsKey(K o) {
		return s.containsKey(o);
	}

	public V getValue(K o) {
		return s.get(o);
	}

	public int getPosition(K o) {
		int i = 0;
		for (Entry<K, V> e : s.entrySet()) {
			++i;
			if (e.getKey().equals(o))
				return i;
		}
		return 0;
	}
	
	public List<Entry<K, V>> getTop(int size) {
		List<Entry<K, V>> list = new LinkedList<>();
		int slot = 1;
		for (Entry<K, V> e : s.entrySet()) {
			list.add(e);
			if(++slot==size)break;
		}
		return list;
	}
}