package se.betfair.filecleaner;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import se.betfair.util.BetfairUtility;
import se.moneymaker.util.Utils;

public class RecordCleaner {

    private char delimiter;
    private List<String> records;

    public void set(List<String> records, char delimiter) {
        this.records = records;
        this.delimiter = delimiter;
    }

    public List<String> clean() {
        List<String> cleanRecords = new ArrayList<>();
        Record r;
        for (String record : records) {
            r = parseRecord(record);
            if (isCorrectInPlayFlag(r)) {
                cleanRecords.add(record);
            }
        }
        return cleanRecords;
    }

    /**
     * If both first and latest taken is after dtActualOff then this record
     * needs to be inplay.
     */
    private boolean isCorrectInPlayFlag(Record record) {
        if (record.getFirstTaken() != null
                && record.getLatestTaken() != null
                && record.getDtActualOff() != null) {
            if ((record.getFirstTaken().equals(record.getDtActualOff())
                    || record.getFirstTaken().after(record.getDtActualOff()))
                    && (record.getLatestTaken().equals(record.getDtActualOff())
                    || record.getLatestTaken().after(record.getDtActualOff()))) {
                if (!record.isInPlay()) {
                    return false;
                }
            } else if (record.getFirstTaken().before(record.getDtActualOff())
                    && record.getLatestTaken().before(record.getDtActualOff())) {
                if (record.isInPlay()) {
                    return false;
                }
            }
        }
        return true;
    }

    private Record parseRecord(String record) {
        StringReader reader = new StringReader(record);

        String scheduledOff = null;
        String dtActualOff = null;
        String firstTaken = null;
        String latestTaken = null;
        String inPlay = null;

        for (int i = 0; i < RecordInfo.NUMBER_OF_FIELDS; i++) {
            if (i == RecordInfo.INDEX_SCHEDULED_OFF) {
                scheduledOff = BetfairUtility.readString(reader, delimiter);
            } else if (i == RecordInfo.INDEX_DT_ACTUAL_OFF) {
                dtActualOff = BetfairUtility.readString(reader, delimiter);
            } else if (i == RecordInfo.INDEX_FIRST_TAKEN) {
                firstTaken = BetfairUtility.readString(reader, delimiter);
            } else if (i == RecordInfo.INDEX_LATEST_TAKEN) {
                latestTaken = BetfairUtility.readString(reader, delimiter);
            } else if (i == RecordInfo.INDEX_IN_PLAY) {
                inPlay = BetfairUtility.readString(reader, delimiter);
            } else {
                BetfairUtility.readString(reader, delimiter);
            }
        }

        if (dtActualOff == null) {
            dtActualOff = scheduledOff;
        } else if (dtActualOff.length() < scheduledOff.length()) {
            dtActualOff = scheduledOff;
        }

        return new Record(dtActualOff, firstTaken, latestTaken, inPlay);
    }

    private class Record {

        private String firstTaken;
        private String latestTaken;
        private String inPlay;
        private String dtActualOff;

        public Record(String dtActualOff,
                String firstTaken,
                String latestTaken,
                String inPlay) {

            this.dtActualOff = Utils.trimQuotationMarks(dtActualOff);
            this.firstTaken = Utils.trimQuotationMarks(firstTaken);
            this.latestTaken = Utils.trimQuotationMarks(latestTaken);
            this.inPlay = Utils.trimQuotationMarks(inPlay);
        }

        private Date getFirstTaken() {
            return Utils.stringToDate(firstTaken);
        }

        private Date getLatestTaken() {
            return Utils.stringToDate(latestTaken);
        }

        private Date getDtActualOff() {
            return Utils.stringToDate(dtActualOff);
        }

        private boolean isInPlay() {
            return Utils.stringToBoolean(inPlay);
        }
    }
}
