package se.betfair.dict;

import se.betfair.enums.EventTypeEnum;
import java.util.HashMap;
import java.util.Map;

public class EventTypeDictionary {

    private static Map<Integer, EventTypeEnum> eventTypes;

    //Betfair event types
    public static void init() {
        eventTypes = new HashMap<>();
        for (EventTypeEnum enums : EventTypeEnum.values()) {
            eventTypes.put(enums.getId(), enums);
        }
    }

    public static EventTypeEnum getEventType(int id) {
        return eventTypes.get(id);
    }
}
