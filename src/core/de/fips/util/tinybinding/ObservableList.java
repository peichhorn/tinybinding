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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.Delegate;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Philipp Eichhorn
 */
@RequiredArgsConstructor
public class ObservableList<E> extends AbstractList<E> implements IObservableList<E> {
	@Delegate
	private final List<E> list;
	private final List<IListObserver<E>> registeredObservers = new CopyOnWriteArrayList<IListObserver<E>>();

	@Override
	public E set(final int index, final E element) {
		E oldValue = list.set(index, element);
		for (IListObserver<E> observer : registeredObservers) {
			observer.valueReplaced(this, index, oldValue);
		}
		return oldValue;
	}

	@Override
	public void add(final int index, final E element) {
		list.add(index, element);
		modCount++;
		for (IListObserver<E> observer : registeredObservers) {
			observer.valuesAdded(this, index, 1);
		}
	}

	@Override
	public E remove(final int index) {
		E oldValue = list.remove(index);
		modCount++;
		for (IListObserver<E> observer : registeredObservers) {
			observer.valuesRemoved(this, index, Collections.singletonList(oldValue));
		}
		return oldValue;
	}

	@Override
	public boolean addAll(final Collection<? extends E> c) {
		return addAll(size(), c);
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends E> c) {
		if (list.addAll(index, c)) {
			modCount++;
			for (IListObserver<E> observer : registeredObservers) {
				observer.valuesAdded(this, index, c.size());
			}
		}
		return false;
	}

	@Override
	public void clear() {
		List<E> dup = new ArrayList<E>(list);
		list.clear();
		modCount++;
		if (!dup.isEmpty()) for (IListObserver<E> observer : registeredObservers) {
			observer.valuesRemoved(this, 0, dup);
		}
	}

	@Override
	public void addObserver(final IListObserver<E> observer) {
		if (!registeredObservers.contains(observer)) {
			registeredObservers.add(observer);
		}
	}

	@Override
	public void removeObserver(final IListObserver<E> observer) {
		registeredObservers.remove(observer);
	}
}