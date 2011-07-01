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
package de.fips.util.tinybinding.swing;

import static de.fips.util.tinybinding.WeakReferences.weakListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * {@link de.fips.util.tinybinding.impl.ObservableValue ObservableValue} that can wrap the document text of a {@link JTextComponent}.
 * <p>
 * <b>Note:</b> All used listeners are added as a {@link java.lang.ref.WeakReference WeakReferences}, so they gets
 * garbage collected when the time comes.
 *
 * @see DocumentListener
 * @see PropertyChangeListener
 * @author Philipp Eichhorn
 */
class ObservableDocumentValue extends ObservableComponentValue<String, JTextComponent> implements DocumentListener, PropertyChangeListener {
	private Document document;
	private DocumentListener weakDocumentListener;

	public ObservableDocumentValue(final JTextComponent component) {
		super(component);
		getComponent().addPropertyChangeListener("document", weakListener(PropertyChangeListener.class, this).createFor(getComponent()));
		document = getComponent().getDocument();
		weakDocumentListener = weakListener(DocumentListener.class, this).createFor(document);
		document.addDocumentListener(weakDocumentListener);
		guardedUpdateValue();
	}

	@Override
	public void changedUpdate(final DocumentEvent event) {
		guardedUpdateValue();
	}

	@Override
	public void insertUpdate(final DocumentEvent event) {
		guardedUpdateValue();
	}

	@Override
	public void removeUpdate(final DocumentEvent event) {
		guardedUpdateValue();
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		document.removeDocumentListener(weakDocumentListener);
		document = (Document) event.getNewValue();
		weakDocumentListener = weakListener(DocumentListener.class, this).createFor(document);
		document.addDocumentListener(weakDocumentListener);
		guardedUpdateValue();
	}

	@Override
	protected void guardedDoSet(final String value) {
		try {
			document.remove(0, document.getLength());
			document.insertString(0, value, null);
		} catch (BadLocationException e) {
			// ignore
		}
	}

	@Override
	public String getComponentValue() {
		try {
			return document.getText(0, document.getLength());
		} catch (BadLocationException e) {
			return null;
		}
	}
}