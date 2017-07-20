package com.ebay.opentracing.basic;

/**
 * Precondition checks.
 */
final class TracerPreconditions
{

	private TracerPreconditions()
	{
		// Prevent construction
	}

	/**
	 * Ensures that an object reference passed as a parameter to the calling method is not null.
	 *
	 * @param reference an object reference
	 * @return the non-null reference that was validated
	 * @throws NullPointerException if {@code reference} is null
	 */
	static <T> T checkNotNull(T reference) {
		if (reference == null) {
			throw new NullPointerException();
		}
		return reference;
	}

	/**
	 * Ensures that an object reference passed as a parameter to the calling method is not null.
	 *
	 * @param reference an object reference
	 * @param errorMessage the exception message to use if the check fails
	 * @return the non-null reference that was validated
	 * @throws NullPointerException if {@code reference} is null
	 */
	static <T> T checkNotNull(T reference, String errorMessage) {
		if (reference == null) {
			throw new NullPointerException(errorMessage);
		}
		return reference;
	}

}
