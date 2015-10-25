package se.betfair.historicfile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import se.moneymaker.util.Log;
import se.moneymaker.enums.LogLevelEnum;

/*
 *
 *
 * ------------------------------------------------------------------------------
 * Change History
 * ------------------------------------------------------------------------------
 * Version Date Author Comments
 * ------------------------------------------------------------------------------
 * 1.0 Dec 3, 2012 Baran Sölen Initial version
 */
public class HistoricFileSystem {

    public static final String TEMP_FILE_PREFIX = "temp_";
    private static final String CLASSNAME = HistoricFileSystem.class.getName();
    private static final String WORK_DIR_NAME = "workdir";
    private static final String CONFIG_DIR_NAME = "config";
    private static final String ERROR_LOG_DIR_NAME = "errorlog";
    private static final String FILE_POINTER_FILE_NAME = "filepointer.bf";
    private File mainDir;
    private File workDir;
    private File configDir;
    private static File errorLogDir;
    private File filePointerFile;
    private HashMap readFiles;

    public HistoricFileSystem(File mainDir) throws IOException {
        if (!mainDir.isDirectory()) {
            throw new IOException(mainDir.getAbsolutePath() + " is not a folder");
        } else {
            this.mainDir = mainDir;
        }
        init();
    }

    //Setup the folders, config files
    private void init() throws IOException {
        readFiles = new HashMap();
        createFolders();
        initFilePointerFile();
        deleteTempFiles();
    }

    public List<File> getHistoricFiles() {

        // The file needs to be checked so that it is indeed a file 
        List<File> files = filterFilesOnly(mainDir.listFiles());
        List<File> notReadFiles = getNotReadHistoricFiles(files);
        return notReadFiles;
    }

    public void writeToFilePointerFile(File file) {
        final String METHOD = "writeToFilePointerFile";
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(filePointerFile, true);
            fileWriter.append(file.getName() + System.getProperty("line.separator"));
            readFiles.put(file.getName(), "");
        } catch (IOException e) {
            Log.logMessage(CLASSNAME, METHOD, e.getMessage() + " Exiting.", LogLevelEnum.CRITICAL, false);
            System.exit(0);
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                Log.logMessage(CLASSNAME, METHOD, e.getMessage() + " Exiting.", LogLevelEnum.CRITICAL, false);
                System.exit(0);
            }
        }
    }

    private List<File> getNotReadHistoricFiles(List<File> files) {
        final String METHOD = "getNotReadHistoricFiles";
        List<File> historicFiles = new ArrayList<>();
        for (File nextFile : files) {
            if (!readFiles.containsKey(nextFile.getName())) {
                historicFiles.add(nextFile);
            }
        }

        for (File addedFile : historicFiles) {
            Log.logMessage(CLASSNAME, METHOD, "Found new file: " + addedFile.getName(), LogLevelEnum.INFO, false);
        }

        return historicFiles;
    }

    private List<File> filterFilesOnly(File[] dir) {
        ArrayList<File> files = new ArrayList<>();

        for (File file : dir) {
            if (file.isFile()) {
                files.add(file);
            }
        }
        return files;
    }

    private void createFolders() throws IOException {
        String mainDirPath = mainDir.getAbsolutePath() + System.getProperty("file.separator");
        workDir = createSubDir(mainDirPath, WORK_DIR_NAME);
        String workDirPath = workDir.getAbsolutePath() + System.getProperty("file.separator");
        configDir = createSubDir(workDirPath, CONFIG_DIR_NAME);
        errorLogDir = createSubDir(workDirPath, ERROR_LOG_DIR_NAME);
    }

    private File createSubDir(String workDirPath, String dirName) throws IOException {
        File dir;
        String path;
        path = workDirPath + System.getProperty("file.separator") + dirName;
        dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                throw new IOException("Could not create " + dirName + " subfolder");
            }
        }
        return dir;
    }

    /**
     * The file containing the latest read historic file. If the file doesn't
     * exist create it else read the last modified file that was read.
     */
    private void initFilePointerFile() throws IOException {
        String filePointerFilename = configDir.getAbsolutePath() + System.getProperty("file.separator") + FILE_POINTER_FILE_NAME;
        filePointerFile = new File(filePointerFilename);
        if (!filePointerFile.exists()) {
            filePointerFile.createNewFile();
        } else {
            BufferedReader reader = new BufferedReader(new FileReader(filePointerFile));
            String line;
            while ((line = reader.readLine()) != null) {
                readFiles.put(line, ""); //The file names of files that have been read
            }
        }
    }

    public static void writeErrorsToFile(StringBuilder erroneousRows, String fileName) {
        final String METHOD = "writeErrorsToFile";
        FileWriter outFile = null;
        try {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
            outFile = new FileWriter(new File(errorLogDir.getAbsolutePath()
                    + System.getProperty("file.separator") + sdf.format(cal.getTime()) + "_" + fileName + ".mm"));
            outFile.append(erroneousRows);
        } catch (IOException e) {
            Log.logMessage(CLASSNAME, METHOD, e.getMessage() + " Exiting.", LogLevelEnum.CRITICAL, false);
            System.exit(0);
        } finally {
            try {
                if (outFile != null) {
                    outFile.close();
                }
            } catch (IOException e) {
                Log.logMessage(CLASSNAME, METHOD, e.getMessage() + " Exiting.", LogLevelEnum.CRITICAL, false);
                System.exit(0);
            }
        }
    }

    private void deleteTempFiles() {
        File[] files = mainDir.listFiles();
        for (File file : files) {
            if (file.isFile() && file.getName().startsWith(HistoricFileSystem.TEMP_FILE_PREFIX)) {
                file.delete();
            }
        }
    }
}
