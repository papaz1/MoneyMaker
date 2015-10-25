package se.moneymaker.db;

import se.moneymaker.enums.ApiServiceName;
import se.moneymaker.enums.ApiConnectionEnum;
import java.util.List;
import se.moneymaker.exception.DBConnectionException;
import se.moneymaker.jsonfactory.JSONFactoryAccount;
import se.moneymaker.model.AccountStatement;
import se.moneymaker.model.AccountTransaction;

public class DBAccount {

    private final DBServices services;

    public DBAccount() {
        services = new DBServices(false);
    }

    public void insertAccountStatementItems(AccountStatement betAccountStatement) throws DBConnectionException {
        services.sendRequest(ApiServiceName.WRITE_BET_ACCOUNT_STATEMENT, JSONFactoryAccount.parseBetAccountStatement(betAccountStatement), ApiConnectionEnum.POST);
    }

    public void insertAccountTransactions(List<AccountTransaction> betAccountTransactions) throws DBConnectionException {
        services.sendRequest(ApiServiceName.WRITE_BET_ACCOUNT_TRANSACTION, JSONFactoryAccount.parseBetAccountTransaction(betAccountTransactions), ApiConnectionEnum.POST);
    }
}
