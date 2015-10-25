package se.betfair.filecleaner;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import se.moneymaker.util.Log;
import se.moneymaker.enums.LogLevelEnum;

public class FileCleaner {

    private static final String DIR_CLEAN_FILES_NAME = "BFCleanFiles";
    private static final String CLASSNAME = FileCleaner.class.getName();
    private File dir;
    private File cleanFilesDir;
    private String header;

    public FileCleaner(File dir) {
        this.dir = dir;
        createDir(dir);
    }

    private void createDir(File dir) {
        String cleanFilesDirName = dir.getPath() + System.getProperty("file.separator") + DIR_CLEAN_FILES_NAME;
        cleanFilesDir = new File(cleanFilesDirName);
        if (!cleanFilesDir.exists() || !cleanFilesDir.isDirectory()) {
            cleanFilesDir.mkdir();
        }
    }

    public void processCleaning() {
        final String METHOD = "processCleaning";
        try {
            File[] files = dir.listFiles();
            String record;
            char delimiter = 0;
            FileSort fileSort = new FileSort();
            RecordCleaner recordCleaner = new RecordCleaner();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    Log.logMessage(CLASSNAME, METHOD, "Processing file " + files[i].getName() + ". Number of files " + i + "/" + (files.length - 1), LogLevelEnum.INFO, false);
                    BufferedReader reader = new BufferedReader(new FileReader(files[i]));
                    header = reader.readLine();//Skip the header in the file

                    List<String> records = new ArrayList<>();
                    boolean first = true;
                    while ((record = reader.readLine()) != null) {
                        if (first) {
                            delimiter = parseDeliminiter(record);
                            first = false;
                        }

                        //TODO: Remove isTempSoccerMethod when you have enough RAM memory
                        if (isTempSoccer(record)) {
                            records.add(record);
                        }
                    }
                    if (!records.isEmpty()) {

                        //The file has been read into a list. Now sort the list.
                        fileSort.set(records, delimiter);
                        records = fileSort.sort();
                        recordCleaner.set(records, delimiter);
                        records = recordCleaner.clean();

                        //File cleaning done, now save it
                        save(records, files[i].getName());
                    }
                }
            }
            Log.logMessage(CLASSNAME, METHOD, "Cleaned files saved in: " + cleanFilesDir, LogLevelEnum.INFO, false);
        } catch (IOException e) {
            Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.CRITICAL, false);
        }
    }

    private void save(List<String> records, String fileName) {
        final String METHOD = "save";
        FileWriter outFile = null;
        try {
            StringBuilder builderCleanFile = new StringBuilder();
            builderCleanFile.append(header).append(System.getProperty("line.separator"));
            for (String cleanRecord : records) {
                builderCleanFile.append(cleanRecord).append(System.getProperty("line.separator"));
            }
            outFile = new FileWriter(new File(cleanFilesDir + System.getProperty("file.separator") + fileName));
            outFile.append(builderCleanFile);
            outFile.flush();
        } catch (IOException e) {
            Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.CRITICAL, false);
        } finally {
            try {
                if (outFile != null) {
                    outFile.close();
                }
            } catch (IOException e1) {
                Log.logMessage(CLASSNAME, METHOD, e1.getMessage(), LogLevelEnum.CRITICAL, false);
            }
        }
    }

    private boolean isTempSoccer(String record) {
        return record.substring(0, 1).equals("1") || record.substring(1, 2).equals("1");
    }

    private char parseDeliminiter(String str) {
        StringTokenizer tokenizerSemicolon = new StringTokenizer(str, ";");
        StringTokenizer tokenizercomma = new StringTokenizer(str, ",");

        int numberOfSemiColons = tokenizerSemicolon.countTokens();
        int numberOfComma = tokenizercomma.countTokens();

        if (numberOfSemiColons >= numberOfComma) {
            return ';';
        } else {
            return ',';
        }
    }
}
