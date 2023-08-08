package quarkus;

import java.math.BigDecimal;
import java.util.Objects;

public class Account {
    public Long accountNumber;
    public Long customerNumber;
    public String customerName;
    public BigDecimal balance;
    public AccountStatus accountStatus = AccountStatus.OPEN;

    public Account() {
    }

    public Account(Long accountNumber, Long customerNumber, String customerName, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.customerNumber = customerNumber;
        this.customerName = customerName;
        this.balance = balance;
    }

    public void markOverdrawn() {
        this.accountStatus = AccountStatus.OVERDRAWN;
    }

    public void removeOverdrawnStatus() {
        this.accountStatus = AccountStatus.OPEN;
    }

    public void close(){
        this.accountStatus = AccountStatus.CLOSED;
        this.balance = BigDecimal.ZERO;
    }

    public void withdrawFunds(final BigDecimal amount){
        this.balance.subtract(amount);
    }

    public void addFunds(final BigDecimal amount){
        this.balance.add(amount);
    }

    public Long getAccountNumber() {
        return accountNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(accountNumber, account.accountNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountNumber);
    }
}
