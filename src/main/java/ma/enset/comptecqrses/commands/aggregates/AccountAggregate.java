package ma.enset.comptecqrses.commands.aggregates;


import ma.enset.comptecqrses.common_api.commands.CreateAccountCommand;
import ma.enset.comptecqrses.common_api.commands.CreditAccountCommand;
import ma.enset.comptecqrses.common_api.commands.DebitAccountCommand;
import ma.enset.comptecqrses.common_api.enums.AccountStatus;
import ma.enset.comptecqrses.common_api.events.AccountActivatedEvent;
import ma.enset.comptecqrses.common_api.events.AccountCreatedEvent;
import ma.enset.comptecqrses.common_api.events.AccountCreditedEvent;
import ma.enset.comptecqrses.common_api.events.AccountDebitedEvent;
import ma.enset.comptecqrses.common_api.exceptions.AmountNegativeException;
import ma.enset.comptecqrses.common_api.exceptions.InsufficientBalanceException;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class AccountAggregate {
    @AggregateIdentifier
    private String accountId;
    private double balance;
    private String currency;
    private AccountStatus status;

    public AccountAggregate() {
        // Required by AXON
    }

    @CommandHandler
    public AccountAggregate(CreateAccountCommand createAccountCommand) {
        if(createAccountCommand.getInitialBalance()<0) throw new RuntimeException("Impossible");
        //OK
        AggregateLifecycle.apply(new AccountCreatedEvent(
                createAccountCommand.getId(),
                createAccountCommand.getInitialBalance(),
                createAccountCommand.getCurrency(),
                AccountStatus.CREATED));
    }

    @EventSourcingHandler
    public void on(AccountCreatedEvent accountCreatedEvent){
        this.accountId = accountCreatedEvent.getId();
        this.balance = accountCreatedEvent.getInitialBalance();
        this.currency = accountCreatedEvent.getCurrency();
        this.status = AccountStatus.CREATED;
        AggregateLifecycle.apply(new AccountActivatedEvent(
                accountCreatedEvent.getId(),
                AccountStatus.ACTIVATED
        ));
    }
    @EventSourcingHandler
    public void on(AccountActivatedEvent accountActivatedEvent){
        this.status = accountActivatedEvent.getStatus();
    }

    @CommandHandler
    public void handle(CreditAccountCommand creditAccountCommand){
        if(creditAccountCommand.getAmount()<0)
            throw new AmountNegativeException("Amount shouldn't be negative");
        AggregateLifecycle.apply(new AccountCreditedEvent(
                creditAccountCommand.getId(),
                creditAccountCommand.getAmount(),
                creditAccountCommand.getCurrency()
        ));
    }
    @EventSourcingHandler
    public void on(AccountCreditedEvent accountCreditedEvent){
        this.balance += accountCreditedEvent.getAmount();
    }

    @CommandHandler
    public void handle(DebitAccountCommand debitAccountCommand){
        if(debitAccountCommand.getAmount()<0)
            throw new AmountNegativeException("Amount shouldn't be negative");
        if(this.balance<debitAccountCommand.getAmount())
            throw new InsufficientBalanceException("Insufficient balance :\t"+this.balance);
        AggregateLifecycle.apply(new AccountDebitedEvent(
                debitAccountCommand.getId(),
                debitAccountCommand.getAmount(),
                debitAccountCommand.getCurrency()
        ));
    }
    @EventSourcingHandler
    public void on(AccountDebitedEvent accountDebitedEvent){
        this.balance -= accountDebitedEvent.getAmount();
    }

}
