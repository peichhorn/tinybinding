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

import java.util.List;
import java.util.Map;

import de.fips.util.tinybinding.IObservableList;
import de.fips.util.tinybinding.IObservableMap;
import de.fips.util.tinybinding.IObservableValue;

import lombok.RequiredArgsConstructor;

/**
 * Creates new {@link ObservableValue ObservableValues}, {@link ObservableMap ObservableMaps} and
 * {@link ObservableList ObservableLists} for just about anything.
 *
 * @author Philipp Eichhorn
 */
@RequiredArgsConstructor
public class Observable {
	public <KEY_TYPE, VALUE_TYPE> IObservableMap<KEY_TYPE, VALUE_TYPE> map(final Map<KEY_TYPE, VALUE_TYPE> map) {
		return new ObservableMap<KEY_TYPE, VALUE_TYPE>(map);
	}

	public <ELEMENT_TYPE> IObservableList<ELEMENT_TYPE> list(final List<ELEMENT_TYPE> list) {
		return new ObservableList<ELEMENT_TYPE>(list);
	}
	
	public <TYPE> IObservableValue<TYPE> value(final TYPE value) {
		return new ObservableValue<TYPE>(value);
	}
	
	public <TYPE> IObservableValue<TYPE> nil() {
		return new ObservableValue<TYPE>();
	}
}
