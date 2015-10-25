package se.moneymaker.dict;

import se.moneymaker.enums.ConfigEnum;
import java.util.HashMap;
import se.moneymaker.util.Utils;

public class Config {

    public static final String CLASSNAME = Config.class.getName();
    public static final int PAYBACK_ROUNDING = 5;
    public static final int MM_DB_DATE_LENGTH = 26;
    private static Config instance;
    private static final String filename = "config.mm";
    private final HashMap<String, String> config;

    private Config() {
        config = Utils.readDictionaryFile(filename);
    }

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    public String get(ConfigEnum configKey) {
        return config.get(configKey.value());
    }

    public String get(String key) {
        return config.get(key);
    }
}
