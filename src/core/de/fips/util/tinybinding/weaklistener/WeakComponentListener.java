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
package de.fips.util.tinybinding.weaklistener;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

class WeakComponentListener extends AbstractWeakListener<ComponentListener, ComponentEvent> implements ComponentListener {
	public WeakComponentListener(final Object source, final ComponentListener listener, final Class<?> listenerType) {
		super(source, listener, listenerType);
	}

	@Override
	public void componentResized(final ComponentEvent event) {
		final ComponentListener listener = get(event);
		if (listener != null) {
			listener.componentResized(event);
		}
	}

	@Override
	public void componentMoved(final ComponentEvent event) {
		final ComponentListener listener = get(event);
		if (listener != null) {
			listener.componentMoved(event);
		}
	}

	@Override
	public void componentShown(final ComponentEvent event) {
		final ComponentListener listener = get(event);
		if (listener != null) {
			listener.componentShown(event);
		}
	}

	@Override
	public void componentHidden(final ComponentEvent event) {
		final ComponentListener listener = get(event);
		if (listener != null) {
			listener.componentHidden(event);
		}
	}
}