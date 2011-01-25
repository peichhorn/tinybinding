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

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests the {@link DefaultConverter}.
 * 
 * @author Philipp Eichhorn
 */
public class DefaultConverterTest {
	@Rule public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void test_convert() {
		DefaultConverter<Integer, Number> converter = new DefaultConverter<Integer, Number>();
		Number num = converter.convert(Integer.valueOf(10));
		assertThat(num).isNotNull();
	}

	@Test
	public void test_convert_invalidType() {
		DefaultConverter<Integer, String> converter = new DefaultConverter<Integer, String>();
		thrown.expect(ClassCastException.class);
		System.out.println(converter.convert(Integer.valueOf(10)));
	}
}
