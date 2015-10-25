package se.pinnacle.api;

import java.io.IOException;
import se.pinnacle.enums.RspStatusEnum;
import se.pinnacle.enums.ServicesEnum;
import se.pinnacle.enums.SportsEnum;

public class Services {

    private Connection connection;

    public Services(String sessionToken) {
        connection = new Connection(sessionToken);
    }

    public String getFeed(ServicesEnum url, SportsEnum sports, long lastFetched) throws PinnacleAPIException {
        String result = null;
        String status;
        ServiceURL serviceURL = new ServiceURL(url);
        serviceURL.setSportID(sports.getID());
        serviceURL.setLive(true);
        serviceURL.setLast(lastFetched);
        try {
            result = connection.sendGetRequest(serviceURL.createURL());
            if (result.length() >= 39) {
                status = result.substring(22, 40);
                if (!status.contains(RspStatusEnum.OK.getStatus())) {
                    throw new PinnacleAPIException("Status tag did not contain ok: " + result);
                }
            } else {
                throw new PinnacleAPIException("Status tag did not contain ok: " + result);
            }
        } catch (IOException ex) {
        }
        return result;
    }
}
