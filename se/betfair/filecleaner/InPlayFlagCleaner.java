package se.betfair.filecleaner;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import se.betfair.util.BetfairUtility;
import se.moneymaker.util.Utils;
import se.moneymaker.util.Log;
import se.moneymaker.enums.LogLevelEnum;

/*
 * Prerequisite: Sorted list All records where last taken date for the pre event
 * records > earliest found first taken date for inplay records are going to be
 * exlucded
 *
 * ------------------------------------------------------------------------------
 * Change History
 * ------------------------------------------------------------------------------
 * Version Date Author Comments
 * ------------------------------------------------------------------------------
 * 1.0 Dec 7, 2013 Baran Sölen Initial version
 */
public class InPlayFlagCleaner {

    private String CLASSNAME = InPlayFlagCleaner.class.getName();
    private List<String> records;
    private char delimiter;
    private int numberOfExcludedRecords;

    public void set(List<String> records, char delimiter) {
        this.records = records;
        this.delimiter = delimiter;
        numberOfExcludedRecords = 0;
    }

    public List<String> clean() {
        final String METHOD = "clean";
        RecordFieldsModel previousRecordFieldsModel = null;
        RecordFieldsModel currentRecordFieldsModel;
        List<RecordFieldsModel> group = new ArrayList<>();
        List<String> cleanRecords = new ArrayList<>(records.size());

        /**
         * Group the records by the hierarchy which in practice means that all
         * markets are grouped into a match. Find the first inPlay time. Then
         * clean the records by comparing the lastTaken to the first inPlay
         * time. If the lastTaken date is >= first inPlay time then that record
         * is also inPlay.
         */
        int numberOfRecords = records.size();

        int recordNo = 0;
        if (numberOfRecords > 1) {
            for (String record : records) {
                recordNo++;
                if (recordNo == 1) {
                    previousRecordFieldsModel = parseRecordFields(record);
                    group.add(previousRecordFieldsModel);
                } else {
                    currentRecordFieldsModel = parseRecordFields(record);

                    //Does this belong to the same match?
                    if (currentRecordFieldsModel.equals(previousRecordFieldsModel)) {
                        group.add(currentRecordFieldsModel);
                    } else {

                        //The current record is a new match, then the old match needs to be cleaned
                        cleanRecords.addAll(cleanRecords(group));
                        group.clear();
                        previousRecordFieldsModel = currentRecordFieldsModel;
                        group.add(previousRecordFieldsModel);
                    }

                    //Was this the last record?
                    if (recordNo == numberOfRecords) {
                        cleanRecords.addAll(cleanRecords(group));
                    }
                }
            }
        } else {
            cleanRecords.addAll(records);
        }

        Log.logMessage(CLASSNAME, METHOD, "Number of excluded records due to inplay flag: " + numberOfExcludedRecords + ". Total number of records: " + numberOfRecords, LogLevelEnum.INFO, true);
        return cleanRecords;
    }

    private List<String> cleanRecords(List<RecordFieldsModel> records) {
        Date earliestInPlayDate = parseEarliestInPlayDate(records);
        return cleanInPlayFlag(earliestInPlayDate, records);
    }

    private List<String> cleanInPlayFlag(Date earliestInPlayDate, List<RecordFieldsModel> records) {
        List<String> cleanRecords = new ArrayList<>(records.size());
        for (RecordFieldsModel record : records) {
            if (!record.isInPlay() && earliestInPlayDate != null && record.getParsedLatestTaken() != null) {
                if (record.getParsedLatestTaken().equals(earliestInPlayDate)
                        || record.getParsedLatestTaken().after(earliestInPlayDate)) {
                    numberOfExcludedRecords++;
                } else {
                    cleanRecords.add(record.getRecord());
                }
            } else {
                cleanRecords.add(record.getRecord());
            }
        }
        return cleanRecords;
    }

    private Date parseEarliestInPlayDate(List<RecordFieldsModel> records) {
        Date earliestDate = null;
        Date currentDate;
        boolean first = true;
        for (RecordFieldsModel record : records) {
            if (record.isInPlay()) {
                if (first) {
                    earliestDate = record.getParsedFirstTaken();
                    first = false;
                } else {
                    currentDate = record.getParsedFirstTaken();
                    if (currentDate.before(earliestDate)) {
                        earliestDate = currentDate;

                    }
                }
            }
        }
        return earliestDate;
    }

    private RecordFieldsModel parseRecordFields(String record) {
        StringReader reader = new StringReader(record);
        String hierarchy = null;
        String firstTaken = null;
        String latestTaken = null;
        String inPlay = null;

        for (int i = 0; i < RecordInfo.NUMBER_OF_FIELDS; i++) {
            if (i == RecordInfo.INDEX_FULL_DESCRIPTION) {
                hierarchy = BetfairUtility.readString(reader, delimiter);
            } else if (i == RecordInfo.INDEX_LATEST_TAKEN) {
                latestTaken = BetfairUtility.readString(reader, delimiter);
            } else if (i == RecordInfo.INDEX_FIRST_TAKEN) {
                firstTaken = BetfairUtility.readString(reader, delimiter);
            } else if (i == RecordInfo.INDEX_IN_PLAY) {
                inPlay = BetfairUtility.readString(reader, delimiter);
            } else {
                BetfairUtility.readString(reader, delimiter);
            }
        }
        return new RecordFieldsModel(record, hierarchy, firstTaken, latestTaken, inPlay, delimiter);
    }

    private class RecordFieldsModel {

        private String record;
        private String hierarchy;
        private String firstTaken;
        private String latestTaken;
        private String inPlay;
        private char delimiter;

        public RecordFieldsModel(String record, String hierarchy, String firstTaken, String latestTaken, String inPlay, char delimiter) {
            this.record = record;
            this.hierarchy = hierarchy;
            this.firstTaken = firstTaken;
            this.latestTaken = latestTaken;
            this.inPlay = inPlay;
            this.delimiter = delimiter;
        }

        public void writeInPlay() {
            inPlay = RecordInfo.NAME_IN_PLAY;
            String finalRecord;
            int lastIndexDelimiter = record.lastIndexOf(delimiter);
            String ip = record.substring(lastIndexDelimiter + 1, record.length());
            String substr = record.substring(0, lastIndexDelimiter);

            if (ip.contains("\"")) {
                finalRecord = substr + delimiter + "\"" + RecordInfo.NAME_IN_PLAY + "\"";
            } else {
                finalRecord = substr + delimiter + RecordInfo.NAME_IN_PLAY;
            }
            record = finalRecord;
        }

        public boolean equals(RecordFieldsModel model) {
            return model.getHierarchy().equalsIgnoreCase(hierarchy);
        }

        public String getRecord() {
            return record;
        }

        public String getHierarchy() {
            return hierarchy;
        }

        public Date getParsedFirstTaken() {
            String str = removeQuotationMark(firstTaken);
            return Utils.stringToDate(str);
        }

        public Date getParsedLatestTaken() {
            String str = removeQuotationMark(latestTaken);
            return Utils.stringToDate(str);
        }

        public boolean isInPlay() {
            String str = removeQuotationMark(inPlay);
            return Utils.stringToBoolean(str);
        }

        private String removeQuotationMark(String str) {
            if (str.contains("\"")) {
                str = str.substring(1, str.length() - 1);
            }
            return str;
        }
    }
}
