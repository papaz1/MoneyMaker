package se.moneymaker.serviceprovider;

import java.io.BufferedReader;
import java.io.IOException;

public class ServiceProviderUtility {
    public static StringBuilder readRequest(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb;
    }    
}
