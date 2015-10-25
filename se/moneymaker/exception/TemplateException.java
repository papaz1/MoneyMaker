package se.moneymaker.exception;

import se.moneymaker.util.Utils;

public class TemplateException extends Throwable {

    private ErrorType errorType = ErrorType.DEFAULT;
    private String uuid;
    private String msg;

    public TemplateException(String msg) {
        this.msg = msg;
    }

    public TemplateException(String msg, ErrorType errorType) {
        this.msg = msg;
        this.errorType = errorType;
    }

    @Override
    public String getMessage() {
        return msg;
    }

    public void setMessage(String message) {
        this.msg = message;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }

    @Override
    public String toString() {
        return "errorType: " + errorType + " msg: " + msg + " uuid: " + uuid;
    }

    public String toJSONString() {
        return Utils.toJSONStringErrorMsg(errorType, msg, uuid);
    }
}
