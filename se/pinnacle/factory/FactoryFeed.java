package se.pinnacle.factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import se.moneymaker.enums.LogLevelEnum;
import se.moneymaker.util.Log;
import se.pinnacle.model.Feed;

public class FactoryFeed {

    private static final String CLASSNAME = FactoryFeed.class.getName();
    private String rawFeed;
    private Date utcEncounter;

    public FactoryFeed(String rawFeed, Date utcEncounter) {
        this.rawFeed = rawFeed;
        this.utcEncounter = utcEncounter;
    }

    public Feed createFeed() throws SAXException, IOException {
        final String METHOD = "createFeed";
        FactoryFeedObjects feedObjects = null;
        try {
            feedObjects = new FactoryFeedObjects(utcEncounter);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            File file = new File("feed.xml");

            try {
                FileWriter fwrite = new FileWriter(file);
                fwrite.write(rawFeed);
                fwrite.close();
                InputStream xmlInput
                        = new FileInputStream(file);
                saxParser.parse(xmlInput, feedObjects);
                //file.delete();
            } catch (NullPointerException e) {
                //file.delete();
                throw new IOException(e.getMessage());
            }
        } catch (ParserConfigurationException | IOException e) {
            Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
        }
        if (feedObjects
                != null) {
            return feedObjects.getFeed();
        } else {
            return null;
        }
    }
}
