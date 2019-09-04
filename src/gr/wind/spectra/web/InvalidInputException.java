package gr.wind.spectra.web;

public class InvalidInputException extends Exception
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String errorDetails;

	public InvalidInputException(String reason, String errorDetails)
	{
		super(reason);
		this.errorDetails = errorDetails;
	}

	public String getFaultInfo()
	{
		return errorDetails;
	}
}
