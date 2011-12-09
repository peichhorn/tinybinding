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
package de.fips.util.tinybinding.util;

import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Collection of useful reflection methods.
 *
 * @author Philipp Eichhorn
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Reflection {
	private static final Map<Class<?>, Class<?>> PRIMITIVE_TYPE_MAP = new HashMap<Class<?>, Class<?>>();

	static {
		PRIMITIVE_TYPE_MAP.put(Boolean.class, Boolean.TYPE);
		PRIMITIVE_TYPE_MAP.put(Byte.class, Byte.TYPE);
		PRIMITIVE_TYPE_MAP.put(Character.class, Character.TYPE);
		PRIMITIVE_TYPE_MAP.put(Short.class, Short.TYPE);
		PRIMITIVE_TYPE_MAP.put(Integer.class, Integer.TYPE);
		PRIMITIVE_TYPE_MAP.put(Long.class, Long.TYPE);
		PRIMITIVE_TYPE_MAP.put(Float.class, Float.TYPE);
		PRIMITIVE_TYPE_MAP.put(Double.class, Double.TYPE);
		PRIMITIVE_TYPE_MAP.put(Void.class, Void.TYPE);
	}

	public static <T, S> Class<T> getPrimitive(final Class<S> clazz) {
		if (clazz == null) return null;
		return Cast.<Class<T>>uncheckedCast(PRIMITIVE_TYPE_MAP.get(clazz));
	}

	public static boolean hasPrimitive(final Class<?> clazz) {
		if (clazz == null) return false;
		return PRIMITIVE_TYPE_MAP.containsKey(clazz);
	}
}
