package de.fips.util.tinybinding;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class ExpectedException implements MethodRule {

	private final org.junit.rules.ExpectedException delegate = org.junit.rules.ExpectedException.none();

	public static ExpectedException none() {
		return new ExpectedException();
	}

	private ExpectedException() {
	}

	public Statement apply(Statement base, FrameworkMethod method, Object target) {
		return delegate.apply(base, method, target);
	}

	public void expect(Class<? extends Throwable> type, String message) {
		expect(type);
		expectMessage(message);
	}

	public void expectIllegalArgumentException(String message) {
		expect(IllegalArgumentException.class, message);
	}

	public void expectNullPointerException(String message) {
		expect(NullPointerException.class, message);
	}

	public void expect(Throwable error) {
		expect(error.getClass(), error.getMessage());
	}

	public void expect(Class<? extends Throwable> type) {
		delegate.expect(type);
	}

	public void expectMessage(String message) {
		delegate.expectMessage(message);
	}
}