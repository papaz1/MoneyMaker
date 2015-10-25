package se.betfair.filecleaner;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import se.betfair.util.BetfairUtility;

public class FileSort {

    private char delimiter;
    private String customDelimiter = "£$€";
    private List<String> records;

    public FileSort() {
    }

    public List<String> sort() {
        String sortString;
        List<String> sortableRecords = new ArrayList<>(records.size());

        for (String record : records) {
            sortString = transformToSortableString(record);//String contains a prefix with the sorting parameters
            sortableRecords.add(sortString);
        }

        String[] historicRecordArray = new String[sortableRecords.size()];
        historicRecordArray = sortableRecords.toArray(historicRecordArray);
        sortableRecords.clear();
        Arrays.sort(historicRecordArray);
        historicRecordArray = removeSortingPrefix(historicRecordArray);

        sortableRecords.addAll(Arrays.asList(historicRecordArray));
        return sortableRecords;
    }

    public void set(List<String> records, char delimiter) {
        this.records = records;
        this.delimiter = delimiter;
    }

    private String[] removeSortingPrefix(String[] historicRecordArray) {
        for (int i = 0; i < historicRecordArray.length; i++) {
            historicRecordArray[i] = historicRecordArray[i].substring(historicRecordArray[i].lastIndexOf(customDelimiter) + 3, historicRecordArray[i].length());
        }
        return historicRecordArray;
    }

    private String transformToSortableString(String record) {
        StringReader reader = new StringReader(record);
        String hierarchy = null;
        String selectionId = null;
        String event = null;
        String eventId = null;
        String sortString;

        for (int i = 0; i < RecordInfo.INDEX_SELECTION_ID + 1; i++) {
            if (i == RecordInfo.INDEX_FULL_DESCRIPTION) {
                hierarchy = BetfairUtility.readString(reader, delimiter);
            } else if (i == RecordInfo.INDEX_EVENT_ID) {
                eventId = BetfairUtility.readString(reader, delimiter);
            } else if (i == RecordInfo.INDEX_EVENT) {
                event = BetfairUtility.readString(reader, delimiter);
            } else if (i == RecordInfo.INDEX_SELECTION_ID) {
                selectionId = BetfairUtility.readString(reader, delimiter);
            } else {
                BetfairUtility.readString(reader, delimiter);
            }
        }

        sortString = hierarchy + customDelimiter + event + customDelimiter + eventId + customDelimiter + selectionId + customDelimiter + record;
        return sortString;
    }
}
