package se.betfair.model;

import se.betfair.model.EventType;

public class EventTypeResult {
	private EventType eventType ; 
	private int marketCount;
	
	public EventType getEventType() {
		return eventType;
	}
	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}
	public int getMarketCount() {
		return marketCount;
	}
	public void setMarketCount(int marketCount) {
		this.marketCount = marketCount;
	}

}
