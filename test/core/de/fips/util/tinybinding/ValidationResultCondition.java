package de.fips.util.tinybinding;

import lombok.RequiredArgsConstructor;

import org.fest.assertions.Condition;

@RequiredArgsConstructor
public class ValidationResultCondition extends Condition<Object> {
	private final IValidationResult.Type type;

	@Override
	public boolean matches(Object value) {
		if ((value instanceof IValidationResult)) {
			final IValidationResult result = (IValidationResult) value;
			return type.equals(result.getType());
		}
		return false;
	}
	
	public static Condition<Object> ok() {
		return new ValidationResultCondition(IValidationResult.Type.OK);
	}

	public static Condition<Object> warning() {
		return new ValidationResultCondition(IValidationResult.Type.WARNING);
	}
	
	public static Condition<Object> error() {
		return new ValidationResultCondition(IValidationResult.Type.ERROR);
	}
}