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
package de.fips.util.tinybinding.junit;

import static javax.swing.SwingUtilities.isEventDispatchThread;
import static org.fest.reflect.core.Reflection.staticMethod;

import java.lang.ref.WeakReference;

import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

import lombok.RequiredArgsConstructor;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * <p>
 * This Rule is used to detect Event Dispatch Thread rule violations.
 * </p>
 * <p>
 * Usage:
 * <pre>
 * &#064;ClassRule
 * public static final FailOnThreadViolation checkThreadViolation = new FailOnThreadViolation();
 * </pre>
 * </p>
 */
public class FailOnThreadViolation implements TestRule {

	@Override
	public Statement apply(final Statement base, final Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				Object m = staticMethod("appContextGet").withReturnType(Object.class).withParameterTypes(Object.class).in(SwingUtilities.class).invoke(RepaintManager.class);
				if (!(m instanceof CheckThreadViolationRepaintManager)) {
					RepaintManager.setCurrentManager(new CheckThreadViolationRepaintManager());
				}
				base.evaluate();
			}
		};
	}

	/**
	 * <p>
	 * This Class is used to detect Event Dispatch Thread rule violations<br>
	 * See <a href=
	 * "http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html">How
	 * to Use Threads</a> for more info
	 * </p>
	 * <p>
	 * This is a modification of original idea of Scott Delap.<br>
	 * </p>
	 * 
	 * @author Scott Delap
	 * @author Alexander Potochkin
	 * 
	 *         https://swinghelper.dev.java.net/
	 */
	@RequiredArgsConstructor
	private static class CheckThreadViolationRepaintManager extends RepaintManager {
		private final boolean completeCheck;

		private WeakReference<JComponent> lastComponent;

		public CheckThreadViolationRepaintManager() {
			this(true);
		}

		@Override
		public synchronized void addInvalidComponent(final JComponent component) {
			checkThreadViolations(component);
			super.addInvalidComponent(component);
		}

		@Override
		public void addDirtyRegion(final JComponent component, final int x, final int y, final int w, final int h) {
			checkThreadViolations(component);
			super.addDirtyRegion(component, x, y, w, h);
		}

		private void checkThreadViolations(final JComponent c) {
			if (!isEventDispatchThread() && (completeCheck || c.isShowing())) {
				boolean imageUpdate = false;
				boolean repaint = false;
				boolean fromSwing = false;
				StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
				;
				for (StackTraceElement st : stackTrace) {
					if (repaint && st.getClassName().startsWith("javax.swing.")) {
						fromSwing = true;
					}
					if (repaint && "imageUpdate".equals(st.getMethodName())) {
						imageUpdate = true;
					}
					if ("repaint".equals(st.getMethodName())) {
						repaint = true;
						fromSwing = false;
					}
				}
				if (imageUpdate) {
					// assuming it is
					// java.awt.image.ImageObserver.imageUpdate(...)
					// image was asynchronously updated, that's ok
					return;
				}
				if (repaint && !fromSwing) {
					// no problems here, since repaint() is thread safe
					return;
				}
				// ignore the last processed component
				if (lastComponent != null && c == lastComponent.get()) {
					return;
				}
				lastComponent = new WeakReference<JComponent>(c);
				violationFound(c, stackTrace);
			}
		}

		private void violationFound(final JComponent c, final StackTraceElement[] stackTraceElements) {
			final AssertionError e = new AssertionError("EDT violation detected");
			if (stackTraceElements != null) {
				e.setStackTrace(stackTraceElements);
			}
			throw e;
		}
	}
}