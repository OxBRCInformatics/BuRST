package ox.softeng.burst.grails.plugin.exception;


import com.google.common.base.Strings;

/**
 * Any exceptions to be handled by BuRST should extend this. Or should throw this.
 *
 * @since 19/02/2016
 */
public class BurstException extends Exception {

    private String errorCode;
    private String messageTitle;

    public BurstException(String errorCode, String message) {
        this(errorCode, message, "");
    }

    public BurstException(String errorCode, String message, String messageTitle) {
        super(message);
        this.errorCode = errorCode;
        this.messageTitle = messageTitle;
    }

    public BurstException(String errorCode, String message, Throwable cause) {
        this(errorCode, message, "", cause);
    }

    public BurstException(String errorCode, String message, String messageTitle, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.messageTitle = messageTitle;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return errorCode + " - " + super.getMessage() + buildCauseMessage(getCause());
    }

    public String getMessageTitle() {
        return messageTitle;
    }

    protected String buildCauseMessage(Throwable cause, int indent) {
        if (cause != null) {
            return "\n" + Strings.repeat(" ", indent) + "- " + cause.toString() +
                   buildCauseMessage(cause.getCause(), indent + 2);
        }
        return "";
    }

    protected String buildCauseMessage(Throwable cause) {
        String msg = buildCauseMessage(cause, 2);
        if (Strings.isNullOrEmpty(msg)) return "";
        return "::" + msg;
    }


}
