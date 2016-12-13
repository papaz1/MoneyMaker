package se.main.start;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.apache.commons.codec.binary.Base64;
import se.betfair.api.Account;
import se.betfair.dict.EventTypeDictionary;
import se.betfair.filecleaner.FileCleaner;
import se.betfair.historicfile.HistoricMarketModeEnum;
import se.main.application.AccountReader;
import se.main.application.BetUpdater;
import se.main.application.FeedReader;
import se.main.application.BetfairKeepAlive;
import se.main.application.HistoricReader;
import se.main.application.PriceReader;
import se.moneymaker.dict.Config;
import se.moneymaker.enums.ConfigEnum;
import se.moneymaker.enums.LogLevelEnum;
import se.moneymaker.enums.ReadReason;
import se.moneymaker.exception.DBConnectionException;
import se.moneymaker.exception.AccountException;
import se.moneymaker.serviceprovider.ServiceProvider;
import se.moneymaker.util.Log;
import se.moneymaker.util.LogOutputEnum;
import se.moneymaker.util.Utils;

/**
 * TODO - Possibility to turn on logs depending on the service, for
 * serviceprovider see everything - Only make one call to readMatch per match
 * and not per betoffer - All critical logs should result in an email - Does the
 * bet proxy really need to do a readMatch - only make a UPDATE statement. Now
 * one unnecessary INSERT is being made in order to get outcome pks.
 */
public class MoneyMaker {

    private static final String PRICEREADER = "Pricereader";
    private static final String FEEDREADER = "Feedreader";
    private static final String SERVICEPROVIDER = "Serviceprovider";
    private static final String BETUPDATER = "Betupdater";
    private static final String ACCOUNTREADER = "Accountreader";
    private static final String HISTORICREADER = "Historicreader";
    private static final String MATCHINFO = "Matchinfo";
    private static final String FILECLEANER = "Filecleaner";
    private PriceReader priceReader;
    private FeedReader feedReader;
    private String application;
    private Thread thread;
    private String matchinfo;
    private String dirName;
    private String sessionTokenBetfair;
    private String betfairUsername;
    private Config config;

    public static void main(String[] args) {
        MoneyMaker main = new MoneyMaker();

        //Set to false when debugging in Netbeans
        if (true) {
            System.out.println("Version 1.0");
            try {
                main.init(args);
            } catch (DBConnectionException | AccountException e) {
                System.exit(0);
            }
        } else {
            dLogin(PRICEREADER);
        }
    }

    private void login(String[] args) throws AccountException, DBConnectionException {
        application = args[0];
        Log log = new Log();
        log.setOutput(LogOutputEnum.CONSOLE);
        //List<LogLevelEnum> severities = new ArrayList<>();
        //severities.add(LogLevelEnum.CRITICAL);
        //severities.add(LogLevelEnum.ERROR);
        //log.setSeverity(severities);
        if (application.equalsIgnoreCase(HISTORICREADER)) {
            log.setOutput(LogOutputEnum.CONSOLE);
            try {
                File dir = new File(dirName);
                if (dir.exists()) {
                    HistoricReader historicReader = new HistoricReader(dir, new File(matchinfo), HistoricMarketModeEnum.PRICE);
                    startThread(historicReader);
                } else {
                    System.out.println("Dir: " + dir + " could not be found");
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
                System.exit(0);
            }
        } else if (application.equalsIgnoreCase(MATCHINFO)) {
            log.setOutput(LogOutputEnum.CONSOLE);
            try {
                HistoricReader historicReader = new HistoricReader(null, new File(matchinfo), HistoricMarketModeEnum.MATCHINFO);
                startThread(historicReader);
            } catch (IOException e) {
                System.out.println(e.getMessage());
                System.exit(0);
            }
        } else if (application.equalsIgnoreCase(SERVICEPROVIDER)) {
            (new ServiceProvider()).start();
        } else if (application.equalsIgnoreCase(PRICEREADER)) {
            betfairUsername = config.get(ConfigEnum.BF_USER_DJ78351);
            sessionTokenBetfair = Account.getInstance().login(betfairUsername, config.get(ConfigEnum.BF_PASS_DJ78351));
            priceReader = new PriceReader(sessionTokenBetfair, betfairUsername, true, ReadReason.HEARTBEAT, 6);
            startThread(priceReader);
        } else if (application.equalsIgnoreCase(FEEDREADER)) {
            String sessionTokenPinnacle = loginPinnacle();
            feedReader = new FeedReader(sessionTokenPinnacle);
            startThread(feedReader);
        } else if (application.equalsIgnoreCase(BETUPDATER)) {
            betfairUsername = config.get(ConfigEnum.BF_USER_DJ78351);
            sessionTokenBetfair = Account.getInstance().login(betfairUsername, config.get(ConfigEnum.BF_PASS_DJ78351));
            BetUpdater betUpdater = new BetUpdater(betfairUsername, sessionTokenBetfair);
            startThread(betUpdater);
        } else if (application.equalsIgnoreCase(ACCOUNTREADER)) {
            betfairUsername = config.get(ConfigEnum.BF_USER_DJ78351);
            sessionTokenBetfair = Account.getInstance().login(betfairUsername, config.get(ConfigEnum.BF_PASS_DJ78351));
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
            Date today = null;
            try {
                today = new Date(Utils.parseStringToLongDate(df.format(new Date()), df));
            } catch (ParseException ex) {
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(today);
            cal.add(Calendar.DATE, -1);
            AccountReader accountReader = new AccountReader(betfairUsername, sessionTokenBetfair, cal.getTime());
            startThread(accountReader);
        } else if (application.equalsIgnoreCase(FILECLEANER)) {
            log.setOutput(LogOutputEnum.CONSOLE);
            File dir = new File(dirName);
            if (dir.exists()) {
                FileCleaner cleaner = new FileCleaner(dir);
                cleaner.processCleaning();
                System.exit(0);
            } else {
                System.out.println("Dir: " + dir + " could not be found");
            }
        }
        if (sessionTokenBetfair != null) {
            Thread keepAlive = new Thread(new BetfairKeepAlive(sessionTokenBetfair, betfairUsername));
            keepAlive.start();
        }
    }

    private void loginScreen(Console console) throws AccountException, DBConnectionException {
        /**
         * The event types of Betfair are the sports, for example Soccer,
         * Tennis, Golf etc.
         */
        //First ask which mode to run in
        System.out.println("Choose service to run. Enter the number for the service.");
        System.out.println("1 " + HISTORICREADER);
        System.out.println("2 " + MATCHINFO);
        System.out.println("3 " + FILECLEANER);
        boolean chosen = false;
        String option;
        while (!chosen) {
            option = console.readLine();
            if (option.equalsIgnoreCase("1")) {
                System.out.println("Enter the path to the directory where the historic files are located: ");
                dirName = console.readLine();
                System.out.println("Enter filename, including path, to the mathcinfo file (new file will be created if it doesn't exist): ");
                matchinfo = console.readLine();

                File f = new File(matchinfo);
                while (f.isDirectory()) {
                    System.out.println("Invalid filename: " + f);
                    System.out.println("Enter filename, including path, to the mathcinfo file (new file will be created if it doesn't exist): ");
                    matchinfo = console.readLine();
                    f = new File(matchinfo);
                }
                String[] args = {HISTORICREADER};
                login(args);
            } else if (option.equals("2")) {
                System.out.println("Enter filename, including path, to the mathcinfo file: ");
                matchinfo = console.readLine();

                File f = new File(matchinfo);
                while (!f.exists() || !f.isFile()) {
                    System.out.println("Enter filename, including path, to the mathcinfo file: ");
                    matchinfo = console.readLine();
                }

                String[] args = {MATCHINFO};
                login(args);
            } else if (option.equals("3")) {
                System.out.println("Enter the path to the directory where the historic files are located: ");
                dirName = console.readLine();
                String[] args = {FILECLEANER};
                login(args);
            } else {
                System.out.println("Invalid choice, enter an integer from 1 to 5.");
            }
        }
    }

    private void startThread(Runnable target) {
        thread = new Thread(target);
        thread.start();
    }

    private String loginPinnacle() throws DBConnectionException {
        String pinnacleUsername = config.get(ConfigEnum.PS_USER);
        String password = config.get(ConfigEnum.PS_PASS);
        String userPassword = pinnacleUsername + ":" + password;
        byte[] encodedBytes;
        String pinnacleSession = null;
        try {
            encodedBytes = Base64.encodeBase64(userPassword.getBytes("UTF-8"));
            String encodedStr = new String(encodedBytes);
            pinnacleSession = "Basic " + encodedStr;
        } catch (UnsupportedEncodingException e) {
        }
        if (pinnacleSession == null) {
            throw new DBConnectionException("Could not login to Pinnacle");
        }
        return pinnacleSession;
    }

    private void init(String[] args) throws AccountException, DBConnectionException {
        config = Config.getInstance();
        EventTypeDictionary.init();
        if (args.length > 0) {
            login(args);
        }

        if (args.length == 0) {
            Console console = System.console();

            if (console == null) {
                System.out.println("Console is not available");
                System.exit(0);
            } else {
                loginScreen(console);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //FOR DEBUG ONLY
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void dLogin(String application) {
        Log log = new Log();
        log.setOutput(LogOutputEnum.CONSOLE);
        Config config = Config.getInstance();
        if (application.equalsIgnoreCase(PRICEREADER)) {
            try {

                //Prematchreader
                String betfairSession = Account.getInstance().login(config.get(ConfigEnum.BF_USER_DJ78351), config.get(ConfigEnum.BF_PASS_DJ78351));
                PriceReader priceReader = new PriceReader(betfairSession, config.get(ConfigEnum.BF_USER_DJ78351), true, ReadReason.HEARTBEAT, 6);
                Thread t = new Thread(priceReader);
                t.start();
            } catch (DBConnectionException e) {
            }
        } else if (application.equalsIgnoreCase(FEEDREADER)) { //Live scores
            String userPassword = config.get(ConfigEnum.PS_USER) + ":" + config.get(ConfigEnum.PS_PASS);
            byte[] encodedBytes = null;
            try {
                encodedBytes = Base64.encodeBase64(userPassword.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
            }
            String encodedStr = new String(encodedBytes);
            String pinnacleSession = "Basic " + encodedStr;
            FeedReader feedReader = new FeedReader(pinnacleSession);
            Thread t = new Thread(feedReader);
            t.start();
        } else if (application.equalsIgnoreCase(HISTORICREADER)) {
            EventTypeDictionary.init();
            File dir = new File("C:\\Users\\Baran.Solen\\Documents\\Dev\\historic_files\\files\\BFTestFiles");
            File mInfo = new File("C:\\Users\\Baran.Solen\\Documents\\Dev\\historic_files\\matchinfo\\minfo.mm");
            HistoricReader historicReader;
            try {
                historicReader = new HistoricReader(dir, mInfo, HistoricMarketModeEnum.PRICE);
                Thread t = new Thread(historicReader);
                t.start();
            } catch (IOException ex) {
            }
        } else if (application.equalsIgnoreCase(BETUPDATER)) {//Betupdater
            try {
                String betfairSession = Account.getInstance().login(config.get(ConfigEnum.BF_USER_DJ78351), config.get(ConfigEnum.BF_PASS_DJ78351));
                BetUpdater betUpdater = new BetUpdater(Account.getInstance().getUsername(), betfairSession);
                Thread t = new Thread(betUpdater);
                t.start();
            } catch (DBConnectionException ex) {
            }
        } else if (application.equalsIgnoreCase(ACCOUNTREADER)) {//Accountreader
            try {
                String betfairSession = Account.getInstance().login(config.get(ConfigEnum.BF_USER_DJ78351), config.get(ConfigEnum.BF_PASS_DJ78351));
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                String date = "2015-11-11";
                Date from = df.parse(date);
                AccountReader accountReader = new AccountReader(Account.getInstance().getUsername(), betfairSession, from);
                Thread t = new Thread(accountReader);
                t.start();
            } catch (DBConnectionException | ParseException ex) {
            }
        } else {
        }
    }
}
