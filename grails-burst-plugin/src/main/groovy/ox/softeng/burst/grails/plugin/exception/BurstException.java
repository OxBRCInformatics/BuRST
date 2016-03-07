package ox.softeng.burst.grails.plugin.exception;


import com.google.common.base.Strings;

/**
 * Any exceptions to be handled by BuRST should extend this. Or should throw this.
 *
 * @since 19/02/2016
 */
public class BurstException extends Exception {

    private String errorCode;

    public BurstException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BurstException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return errorCode + " - " + super.getMessage() + buildCauseMessage(getCause());
    }

    protected String buildCauseMessage(Throwable cause, int indent) {
        if (cause != null) {
            return "\n" + Strings.repeat(" ", indent) + cause.toString() +
                   buildCauseMessage(cause.getCause(), indent + 2);
        }

        return "";
    }

    protected String buildCauseMessage(Throwable cause) {
        return buildCauseMessage(cause, 2);
    }


}
