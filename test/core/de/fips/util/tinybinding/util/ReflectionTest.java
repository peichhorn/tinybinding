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
package de.fips.util.tinybinding.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class ReflectionTest {

	@Test
	public void test_getPrimitiveReturnsNullOnNull() {
		assertNull(Reflection.getPrimitive(null));
	}
	
	@Test
	public void test_hasPrimitiveReturnsFalseOnNull() {
		assertFalse(Reflection.hasPrimitive(null));
	}
	
	@Test
	public void test_getPrimitive() {	
		assertEquals(Boolean.TYPE, Reflection.getPrimitive(Boolean.class));
		assertEquals(Byte.TYPE, Reflection.getPrimitive(Byte.class));
		assertEquals(Character.TYPE, Reflection.getPrimitive(Character.class));
		assertEquals(Short.TYPE, Reflection.getPrimitive(Short.class));
		assertEquals(Integer.TYPE, Reflection.getPrimitive(Integer.class));
		assertEquals(Long.TYPE, Reflection.getPrimitive(Long.class));
		assertEquals(Float.TYPE, Reflection.getPrimitive(Float.class));
		assertEquals(Double.TYPE, Reflection.getPrimitive(Double.class));
		assertEquals(Void.TYPE, Reflection.getPrimitive(Void.class));
	}
}
