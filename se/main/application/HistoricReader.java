package se.main.application;

import java.io.File;
import java.io.IOException;
import java.util.List;
import se.betfair.historicfile.HistoricMarketModeEnum;
import se.betfair.historicfile.HistoricFileProcesser;
import se.betfair.historicfile.HistoricFileSystem;
import se.moneymaker.util.Log;
import se.moneymaker.enums.LogLevelEnum;

public class HistoricReader implements Runnable {

    private static final String CLASSNAME = HistoricReader.class.getName();
    private HistoricFileProcesser historicDataReader;
    private HistoricFileSystem historicFileSystem;
    private final long TIME = 10000;
    private HistoricMarketModeEnum mode;
    private final File matchInfoFile;

    public HistoricReader(File dir, File matchInfoFile, HistoricMarketModeEnum mode) throws IOException {
        this.mode = mode;
        this.matchInfoFile = matchInfoFile;
        if (mode.equals(HistoricMarketModeEnum.PRICE)) {
            historicFileSystem = new HistoricFileSystem(dir);
        }
    }

    @Override
    public void run() {
        final String METHOD = "run";
        List<File> historicFiles;
        if (mode.equals(HistoricMarketModeEnum.PRICE)) {
            while (true) {
                historicFiles = historicFileSystem.getHistoricFiles();
                if (historicFiles.isEmpty()) {
                    try {
                        Thread.sleep(TIME);
                    } catch (InterruptedException e) {
                    }
                } else {
                    int counter = 1;
                    int numberOfFiles = historicFiles.size();
                    for (File historicFile : historicFiles) {
                        Log.logMessage(CLASSNAME, METHOD, "Starting file " + counter + "/" + numberOfFiles + ": " + historicFile.getName(), LogLevelEnum.INFO, false);
                        counter++;
                        historicDataReader = new HistoricFileProcesser(historicFile, matchInfoFile, mode);
                        historicDataReader.processHistoricData();
                        historicFileSystem.writeToFilePointerFile(historicFile);
                    }
                }
            }
        } else if (mode.equals(HistoricMarketModeEnum.MATCHINFO)) {
            historicDataReader = new HistoricFileProcesser(null, matchInfoFile, mode);
            historicDataReader.processHistoricData();
            System.exit(0);
        }
    }
}
