package se.pinnacle.api;

import java.net.MalformedURLException;
import java.net.URL;
import se.pinnacle.enums.ServicesEnum;

public class ServiceURL {

    private ServicesEnum servciceEnum;
    private int sportID;
    private long last;
    private boolean live;

    public ServiceURL(ServicesEnum serviceEnum) {
        this.servciceEnum = serviceEnum;
    }

    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }

    public ServicesEnum getServiceEnum() {
        return servciceEnum;
    }

    public int getSportID() {
        return sportID;
    }

    public void setSportID(int sportID) {
        this.sportID = sportID;
    }

    public long getLast() {
        return last;
    }

    public void setLast(long last) {
        this.last = last;
    }

    public URL createURL() {
        URL url = null;
        StringBuilder sb = new StringBuilder(servciceEnum.getURL());
        if (sportID != 0) {
            sb.append("?sportid=").append(sportID);
            if (live) {
                sb.append("&islive=1");
            }
        }
        if (last != 0) {
            if (sportID != 0) {
                sb.append("&");
            } else {
                sb.append("?");
            }
            sb.append("last=").append(last);
        }
        try {
            url = new URL(sb.toString());
        } catch (MalformedURLException e) {
        }
        return url;
    }
}
