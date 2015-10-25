package se.pinnacle.enums;

public enum SportsEnum {

    SOCCER(29);
    final int id;

    private SportsEnum(int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }
}
