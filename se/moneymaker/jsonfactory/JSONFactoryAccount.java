package se.moneymaker.jsonfactory;

import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import se.moneymaker.model.AccountStatement;
import se.moneymaker.model.AccountTransaction;
import se.moneymaker.util.Utils;

public class JSONFactoryAccount {

    private static final String TAG_UTC_TRANSACTION = "utcTransaction";
    private static final String TAG_DEPOSITED_LOCAL = "depositedLocal";
    private static final String TAG_WITHDRAWN_LOCAL = "withdrawnLocal";
    private static final String TAG_INTERNALFEE_LOCAL = "internalFeeLocal";
    private static final String TAG_EXTERNALFEE_LOCAL = "externalFeeLocal";
    private static final String TAG_UTC_STATEMENT = "utcStatement";
    private static final String TAG_STATEMENT_LOCAL = "statementLocal";
    private static final String TAG_ACCOUNT_NAME = "accountName";
    private static final String TAG_NAME = "name";

    public static JSONObject parseAccount(String bookmaker, String accountName) {
        JSONObject accountJSON = new JSONObject();
        JSONObject bookmakerJSON = new JSONObject();
        bookmakerJSON.put(JSONKeyNames.KEY_NAME, bookmaker);
        accountJSON.put(JSONKeyNames.KEY_BOOKMAKER, bookmakerJSON);
        accountJSON.put(TAG_ACCOUNT_NAME, accountName);
        return accountJSON;
    }

    public static JSONObject parseBookmaker(String bookmaker) {
        JSONObject bookmakerJSON = new JSONObject();
        bookmakerJSON.put(JSONKeyNames.KEY_NAME, bookmaker);
        return bookmakerJSON;
    }

    public static JSONArray parseBetAccountTransaction(List<AccountTransaction> accountTransactions) {
        JSONArray transactionsJSON = new JSONArray();
        JSONObject transactionJSON;
        for (AccountTransaction transaction : accountTransactions) {
            transactionJSON = new JSONObject();
            transactionJSON.put(JSONKeyNames.KEY_EXTERNAL_KEY, transaction.getId());

            JSONObject accountJSON = JSONFactoryAccount.parseAccount(transaction.getBookmaker(), transaction.getAccountName());
            transactionJSON.put(JSONKeyNames.KEY_ACCOUNT, accountJSON);
            transactionJSON.put(TAG_UTC_TRANSACTION, Utils.dateToString(transaction.getUTCTransaction()));
            transactionJSON.put(TAG_DEPOSITED_LOCAL, transaction.getDepositedLocal());
            transactionJSON.put(TAG_WITHDRAWN_LOCAL, transaction.getWithdrawnLocal());
            transactionJSON.put(TAG_INTERNALFEE_LOCAL, transaction.getInternalFeeLocal());
            transactionJSON.put(TAG_EXTERNALFEE_LOCAL, transaction.getExternalFeeLocal());

            transactionsJSON.add(transactionJSON);
        }
        return transactionsJSON;
    }

    public static JSONArray parseBetAccountStatement(AccountStatement accountStatement) {
        JSONArray accountStatementsJSON = new JSONArray();
        JSONObject accountStatementJSON = new JSONObject();

        accountStatementJSON.put(TAG_UTC_STATEMENT, Utils.dateToString(accountStatement.getUTCStatement()));
        accountStatementJSON.put(TAG_STATEMENT_LOCAL, accountStatement.getStatementLocal());
        accountStatementJSON.put(JSONKeyNames.KEY_ACCOUNT, JSONFactoryAccount.parseAccount(accountStatement.getBookmaker(), accountStatement.getAccountName()));
        accountStatementJSON.put(JSONKeyNames.KEY_CURRENCY, accountStatement.getCurrency());
        accountStatementsJSON.add(accountStatementJSON);

        return accountStatementsJSON;
    }

    public static JSONObject createCurrencyCodeReadCall(String accountName) {
        JSONObject currencyCode = new JSONObject();
        currencyCode.put(TAG_NAME, accountName);
        return currencyCode;
    }
}
