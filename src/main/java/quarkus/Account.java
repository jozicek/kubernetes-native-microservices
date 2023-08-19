package quarkus;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@NamedQuery(name = "Accounts.findAll",
        query = "SELECT a FROM Account a ORDER BY a.accountNumber")
@NamedQuery(name = "Accounts.findByAccountNumber",
        query = "SELECT a FROM Account a WHERE a.accountNumber = :accountNumber ORDER BY a.accountNumber")
public class Account {

    @Id
    @SequenceGenerator(name = "accountsSequence", sequenceName = "accounts_id_seq", initialValue = 10, allocationSize = 1)
    @GeneratedValue(generator = "accountsSequence", strategy = GenerationType.SEQUENCE)
    private Long id;
    public Long accountNumber;
    public Long customerNumber;
    public String customerName;
    public BigDecimal balance;
    public AccountStatus accountStatus = AccountStatus.OPEN;

    public Account(Long accountNumber, Long customerNumber, String customerName, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.customerNumber = customerNumber;
        this.customerName = customerName;
        this.balance = balance;
    }

    public Account() {
    }

    public void markOverdrawn() {
        this.accountStatus = AccountStatus.OVERDRAWN;
    }

    public void removeOverdrawnStatus() {
        this.accountStatus = AccountStatus.OPEN;
    }

    public void close() {
        this.accountStatus = AccountStatus.CLOSED;
        this.balance = BigDecimal.ZERO;
    }

    public void withdrawFunds(final BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }

    public void addFunds(final BigDecimal amount) {
        this.balance = this.balance.add(amount);
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
