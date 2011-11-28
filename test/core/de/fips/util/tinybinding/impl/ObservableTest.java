/*
 * Copyright © 2010-2011 Philipp Eichhorn.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.fips.util.tinybinding.impl;

import static de.fips.util.tinybinding.Observables.observe;
import static de.fips.util.tinybinding.util.Cast.uncheckedCast;
import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.fips.util.tinybinding.IListObserver;
import de.fips.util.tinybinding.IMapObserver;
import de.fips.util.tinybinding.IObservableList;
import de.fips.util.tinybinding.IObservableMap;
import de.fips.util.tinybinding.IObservableValue;
import de.fips.util.tinybinding.IValueObserver;

/**
 * Tests {@link Observable}.
 */
@RunWith(JUnit4.class)
public class ObservableTest {

	@Test
	public void test_observeMap() throws Exception {
		IObservableMap<String, String> map = observe().map(new HashMap<String, String>());
		assertThat(map).hasSize(0);
		IMapObserver<String, String> observer = uncheckedCast(mock(IMapObserver.class));
		map.addObserver(observer);
		map.put("key", "value");
		map.put("another key", "another value");
		assertThat(map).hasSize(2);
		assertThat(map.get("key")).isEqualTo("value");
		assertThat(map.get("another key")).isEqualTo("another value");
		verify(observer, times(1)).valueAdded(eq(map), eq("key"));
		map.put("key", "new value");
		assertThat(map.get("key")).isEqualTo("new value");
		verify(observer, times(1)).valueChanged(eq(map), eq("key"), eq("value"));
		map.remove("key");
		assertThat(map.get("key")).isNull();
		verify(observer, times(1)).valueRemoved(eq(map), eq("key"), eq("new value"));
		map.clear();
		assertThat(map).isEmpty();
		verify(observer, times(1)).valueRemoved(eq(map), eq("another key"), eq("another value"));
	}

	@Test
	public void test_observeList() throws Exception {
		IObservableList<String> list = observe().list(new ArrayList<String>());
		assertThat(list).hasSize(0);
		IListObserver<String> observer = uncheckedCast(mock(IListObserver.class));
		list.addObserver(observer);
		list.add("element");
		assertThat(list).containsOnly("element");
		verify(observer, times(1)).valuesAdded(eq(list), eq(0), eq(1));
		list.addAll(asList("new element", "another element"));
		assertThat(list).containsOnly("element", "new element", "another element");
		verify(observer, times(1)).valuesAdded(eq(list), eq(1), eq(2));
		list.remove(1);
		assertThat(list).containsOnly("element", "another element");
		verify(observer, times(1)).valuesRemoved(eq(list), eq(1), eq(asList("new element")));
		list.set(0, "yet another element");
		assertThat(list).containsOnly("yet another element", "another element");
		verify(observer, times(1)).valueReplaced(eq(list), eq(0), eq("element"));
		list.clear();
		assertThat(list).isEmpty();
		verify(observer, times(1)).valuesRemoved(eq(list), eq(0), eq(asList("yet another element", "another element")));
	}

	@Test
	public void test_observeValue() throws Exception {
		IObservableValue<String> string = observe().value("value");
		assertThat(string.get()).isEqualTo("value");
		IValueObserver<String> observer = uncheckedCast(mock(IValueObserver.class));
		string.addObserver(observer);
		verify(observer, times(1)).valueChanged(eq("value"), eq((String) null));
		string.set("new value");
		assertThat(string.get()).isEqualTo("new value");
		verify(observer, times(1)).valueChanged(eq("new value"), eq("value"));
	}
	
	@Test
	public void test_observeNil() throws Exception {
		IObservableValue<String> string = observe().nil();
		assertThat(string.get()).isNull();
		IValueObserver<String> observer = uncheckedCast(mock(IValueObserver.class));
		string.addObserver(observer);
		verify(observer, times(1)).valueChanged(eq((String) null), eq((String) null));
		string.set("value");
		assertThat(string.get()).isEqualTo("value");
		verify(observer, times(1)).valueChanged(eq("value"), eq((String) null));
	}
}
