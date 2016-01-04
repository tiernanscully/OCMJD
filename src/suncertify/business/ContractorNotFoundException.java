package suncertify.business;

public class ContractorNotFoundException extends ServiceException {

	/**
	 * Instantiates a new ContractorNotFound exception.
	 */
	public ContractorNotFoundException() {
		super();
	}

	/**
	 * Instantiates a new ContractorNotFound exception.
	 *
	 * @param message
	 *            the message
	 */
	public ContractorNotFoundException(String message) {
		super(message);
	}
}
