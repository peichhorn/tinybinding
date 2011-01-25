/*
Copyright © 2010-2011 Philipp Eichhorn.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package de.fips.util.tinybinding;

import static de.fips.util.tinybinding.util.Cast.uncheckedCast;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import de.fips.util.tinybinding.util.Cast;

import lombok.Delegate;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author Philipp Eichhorn
 */
@RequiredArgsConstructor
public class ObservableMap<K, V> extends AbstractMap<K, V> implements IObservableMap<K, V> {
	@Delegate
	private final Map<K, V> map;
	private final List<IMapObserver<K, V>> registeredObservers = new CopyOnWriteArrayList<IMapObserver<K, V>>();
	private Set<Map.Entry<K, V>> entrySet;

	@Override
	public void clear() {
		Iterator<K> iterator = keySet().iterator();
		while (iterator.hasNext()) {
			iterator.next();
			iterator.remove();
		}
	}

	@Override 
	public Set<Map.Entry<K, V>> entrySet() {
		Set<Map.Entry<K, V>> es = entrySet;
		return es != null ? es : (entrySet = new EntrySet());
	}

	@Override
	public V put(final K key, final V value) {
		V lastValue = map.put(key, value);
		if (containsKey(key)) {
			for (IMapObserver<K, V> observer : registeredObservers) {
				observer.valueChanged(this, key, lastValue);
			}
		} else {
			for (IMapObserver<K, V> observer : registeredObservers) {
				observer.valueAdded(this, key);
			}
		}
		return lastValue;
	}

	@Override
	public void putAll(final Map<? extends K, ? extends V> m) {
		for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public V remove(final Object o) {
		if (containsKey(o)) {
			V value = map.remove(o);
			K key = Cast.<K>uncheckedCast(o);
			for (IMapObserver<K, V> observer : registeredObservers) {
				observer.valueRemoved(this, key, value);
			}
			return value;
		}
		return null;
	}

	public void addObserver(final IMapObserver<K, V> observer) {
		if (!registeredObservers.contains(observer)) {
			registeredObservers.add(observer);
		}
	}

	public void removeObserver(final IMapObserver<K, V> observer) {
		registeredObservers.remove(observer);
	}

	private class EntryIterator implements Iterator<Map.Entry<K, V>> {
		private final Iterator<Map.Entry<K, V>> iterator = map.entrySet().iterator();
		private Map.Entry<K, V> last;

		public boolean hasNext() {
			return iterator.hasNext();
		}

		public Map.Entry<K, V> next() {
			last = iterator.next();
			return last;
		}

		public void remove() {
			if (last == null) {
				throw new IllegalStateException();
			}
			Object toRemove = last.getKey();
			last = null;
			ObservableMap.this.remove(toRemove);
		}
	}

	private class EntrySet extends AbstractSet<Map.Entry<K, V>> {
		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			return new EntryIterator();
		}

		@Override
		public boolean contains(final Object o) {
			if (!(o instanceof Map.Entry)) {
				return false;
			}
			Map.Entry<K, V> e = uncheckedCast(o);
			return containsKey(e.getKey());
		}

		@Override
		public boolean remove(final Object o) {
			if (o instanceof Map.Entry) {
				Map.Entry<K, V> entry = uncheckedCast(o);
				K key = entry.getKey();
				if (containsKey(key)) {
					remove(key);
					return true;
				}
			}
			return false;
		}

		@Override
		public int size() {
			return ObservableMap.this.size();
		}

		@Override
		public void clear() {
			ObservableMap.this.clear();
		}
	}
}