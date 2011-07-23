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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.fips.util.tinybinding.IListObserver;
import de.fips.util.tinybinding.IObservableList;

import lombok.RequiredArgsConstructor;

/**
 *
 * @author Philipp Eichhorn
 */
@RequiredArgsConstructor
public class ObservableList<ELEMENT> extends AbstractList<ELEMENT> implements IObservableList<ELEMENT> {
	private final List<ELEMENT> list;
	private final List<IListObserver<ELEMENT>> registeredObservers = new CopyOnWriteArrayList<IListObserver<ELEMENT>>();

	@Override
	public ELEMENT set(final int index, final ELEMENT element) {
		ELEMENT oldValue = list.set(index, element);
		for (IListObserver<ELEMENT> observer : registeredObservers) {
			observer.valueReplaced(this, index, oldValue);
		}
		return oldValue;
	}

	@Override
	public void add(final int index, final ELEMENT element) {
		list.add(index, element);
		modCount++;
		for (IListObserver<ELEMENT> observer : registeredObservers) {
			observer.valuesAdded(this, index, 1);
		}
	}

	@Override
	public ELEMENT remove(final int index) {
		ELEMENT oldValue = list.remove(index);
		modCount++;
		for (IListObserver<ELEMENT> observer : registeredObservers) {
			observer.valuesRemoved(this, index, Collections.singletonList(oldValue));
		}
		return oldValue;
	}

	@Override
	public boolean addAll(final Collection<? extends ELEMENT> c) {
		return addAll(size(), c);
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends ELEMENT> c) {
		if (list.addAll(index, c)) {
			modCount++;
			for (IListObserver<ELEMENT> observer : registeredObservers) {
				observer.valuesAdded(this, index, c.size());
			}
		}
		return false;
	}

	public ELEMENT get(int index) {
		return list.get(index);
	}

	public int size() {
		return list.size();
	}

	@Override
	public void clear() {
		List<ELEMENT> dup = new ArrayList<ELEMENT>(list);
		list.clear();
		modCount++;
		if (!dup.isEmpty()) for (IListObserver<ELEMENT> observer : registeredObservers) {
			observer.valuesRemoved(this, 0, dup);
		}
	}

	@Override
	public void addObserver(final IListObserver<ELEMENT> observer) {
		if (!registeredObservers.contains(observer)) {
			registeredObservers.add(observer);
		}
	}

	@Override
	public void removeObserver(final IListObserver<ELEMENT> observer) {
		registeredObservers.remove(observer);
	}
}