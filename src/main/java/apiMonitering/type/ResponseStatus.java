package apiMonitering.type;

import java.lang.reflect.Array;
import java.util.Arrays;

public enum ResponseStatus {

    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", 500),
    NO_MANDATORY_REQUEST_PARAMETER_ERROR("NO_MANDATORY_REQUEST_PARAMETER_ERROR", 400),
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", 503),
    UNAUTHORIZED("Unauthorized", 401),
    FORBIDDEN("Forbidden", 403),
    NOT_FOUND("Not_Found", 404),
    METHOD_NOT_ALLOWED("Method_Not_Allowed", 405),
    NORMAL_CODE("NORMAL_CODE", 0),
    APPLICATION_ERROR("APPLICATION_ERROR", 1),
    SERVICE_KEY_NOT_REGISTERED("SERVICE_KEY_IS_NOT_REGISTERED_ERROR", 30),
    DEFAULT("DEFAULT", 200);

    private final String errorMessage;
    private final int statusCode;

    ResponseStatus(String errorMessage, int statusCode) {
        this.errorMessage = errorMessage;
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public static int fromBody(String body) {

        return Arrays.stream(values())
                .filter(status -> body.contains(status.errorMessage))
                .findFirst()
                .orElse(DEFAULT)
                .getStatusCode();
    }
}
