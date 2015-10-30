package se.betfair.factory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import se.betfair.model.ClearedOrderSummary;
import se.moneymaker.model.AccountStatement;
import se.moneymaker.model.AccountTransaction;
import se.moneymaker.util.Utils;

public class FactoryAccount {

    private String bookmaker;
    private String accountName;
    private String currency;

    public FactoryAccount(String bookmaker, String accountName, String currency) {
        this.bookmaker = bookmaker;
        this.accountName = accountName;
        this.currency = currency;
    }

    public AccountStatement createAccountStatement(double statement) {
        AccountStatement betAccountStatement = new AccountStatement(bookmaker, accountName, currency);
        Date balanceUTCEncounter;
        balanceUTCEncounter = Utils.getCurrentTimeUTC();
        betAccountStatement.setUTCStatement(balanceUTCEncounter);
        betAccountStatement.setStatementLocal(Double.toString(statement));
        return betAccountStatement;
    }

    private AccountTransaction createAccountTransaction(double transaction, String comment) {
        AccountTransaction accountTransaction = new AccountTransaction(bookmaker, accountName);
        Date transactionUTCEncounter;
        transactionUTCEncounter = Utils.getCurrentTimeUTC();
        accountTransaction.setUtcTransaction(transactionUTCEncounter);
        accountTransaction.setInternalFeeLocal(transaction);
        return accountTransaction;
    }

    public List<AccountTransaction> createAccountTransactions(List<ClearedOrderSummary> clearedOrders) {
        List<AccountTransaction> accountTranactions = new ArrayList<>();
        for (ClearedOrderSummary order : clearedOrders) {
            if (order.getCommission() > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("Commission marketID: ").append(order.getMarketId());
                accountTranactions.add(createAccountTransaction(order.getCommission(), sb.toString()));
            }
        }
        return accountTranactions;
    }
}
