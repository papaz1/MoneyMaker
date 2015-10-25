package se.pinnacle.enums;

public enum RspStatusEnum {

    OK("ok"),
    FAIL("fail");
    private final String rspStatus;

    private RspStatusEnum(String rspStatus) {
        this.rspStatus = rspStatus;
    }

    public CharSequence getStatus() {
        return rspStatus;
    }
}
