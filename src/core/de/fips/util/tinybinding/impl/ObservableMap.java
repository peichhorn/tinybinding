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
package de.fips.util.tinybinding.impl;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import de.fips.util.tinybinding.IMapObserver;
import de.fips.util.tinybinding.IObservableMap;
import de.fips.util.tinybinding.util.Cast;

import lombok.RequiredArgsConstructor;

/**
 *
 * @author Philipp Eichhorn
 */
@RequiredArgsConstructor
public class ObservableMap<KEY_TYPE, VALUE_TYPE> extends AbstractMap<KEY_TYPE, VALUE_TYPE> implements IObservableMap<KEY_TYPE, VALUE_TYPE> {
	private final Map<KEY_TYPE, VALUE_TYPE> map;
	private final List<IMapObserver<KEY_TYPE, VALUE_TYPE>> registeredObservers = new CopyOnWriteArrayList<IMapObserver<KEY_TYPE, VALUE_TYPE>>();
	private Set<Map.Entry<KEY_TYPE, VALUE_TYPE>> entrySet;

	@Override
	public void clear() {
		Iterator<KEY_TYPE> iterator = keySet().iterator();
		while (iterator.hasNext()) {
			iterator.next();
			iterator.remove();
		}
	}

	@Override
	public Set<Map.Entry<KEY_TYPE, VALUE_TYPE>> entrySet() {
		Set<Map.Entry<KEY_TYPE, VALUE_TYPE>> es = entrySet;
		return es != null ? es : (entrySet = new EntrySet());
	}

	@Override
	public VALUE_TYPE put(final KEY_TYPE key, final VALUE_TYPE value) {
		boolean alreadyContainsKey = containsKey(key);
		VALUE_TYPE lastValue = map.put(key, value);
		if (alreadyContainsKey) {
			for (IMapObserver<KEY_TYPE, VALUE_TYPE> observer : registeredObservers) {
				observer.valueChanged(this, key, lastValue);
			}
		} else {
			for (IMapObserver<KEY_TYPE, VALUE_TYPE> observer : registeredObservers) {
				observer.valueAdded(this, key);
			}
		}
		return lastValue;
	}

	@Override
	public VALUE_TYPE remove(final Object o) {
		if (containsKey(o)) {
			VALUE_TYPE value = map.remove(o);
			KEY_TYPE key = Cast.<KEY_TYPE>uncheckedCast(o);
			for (IMapObserver<KEY_TYPE, VALUE_TYPE> observer : registeredObservers) {
				observer.valueRemoved(this, key, value);
			}
			return value;
		}
		return null;
	}
	
	public int size() {
		return map.size();
	}

	@Override
	public void addObserver(final IMapObserver<KEY_TYPE, VALUE_TYPE> observer) {
		if (!registeredObservers.contains(observer)) {
			registeredObservers.add(observer);
		}
	}

	@Override
	public void removeObserver(final IMapObserver<KEY_TYPE, VALUE_TYPE> observer) {
		registeredObservers.remove(observer);
	}

	private class EntryIterator implements Iterator<Map.Entry<KEY_TYPE, VALUE_TYPE>> {
		private final Iterator<Map.Entry<KEY_TYPE, VALUE_TYPE>> iterator = map.entrySet().iterator();
		private Map.Entry<KEY_TYPE, VALUE_TYPE> last;

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public Map.Entry<KEY_TYPE, VALUE_TYPE> next() {
			last = iterator.next();
			return last;
		}

		@Override
		public void remove() {
			if (last == null) {
				throw new IllegalStateException();
			}
			Object toRemove = last.getKey();
			last = null;
			ObservableMap.this.remove(toRemove);
		}
	}

	private class EntrySet extends AbstractSet<Map.Entry<KEY_TYPE, VALUE_TYPE>> {
		@Override
		public Iterator<Map.Entry<KEY_TYPE, VALUE_TYPE>> iterator() {
			return new EntryIterator();
		}

		@Override
		public boolean contains(final Object o) {
			if (!(o instanceof Map.Entry)) {
				return false;
			}
			Map.Entry<KEY_TYPE, VALUE_TYPE> e = Cast.uncheckedCast(o);
			return containsKey(e.getKey());
		}

		@Override
		public boolean remove(final Object o) {
			if (o instanceof Map.Entry) {
				Map.Entry<KEY_TYPE, VALUE_TYPE> entry = Cast.uncheckedCast(o);
				KEY_TYPE key = entry.getKey();
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