package ma.enset.comptecqrses.queries.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.enset.comptecqrses.common_api.enums.OperationType;
import ma.enset.comptecqrses.common_api.events.AccountActivatedEvent;
import ma.enset.comptecqrses.common_api.events.AccountCreatedEvent;
import ma.enset.comptecqrses.common_api.events.AccountCreditedEvent;
import ma.enset.comptecqrses.common_api.events.AccountDebitedEvent;
import ma.enset.comptecqrses.common_api.queries.GetAccountByIdQuery;
import ma.enset.comptecqrses.common_api.queries.GetAllAccountsQuery;
import ma.enset.comptecqrses.queries.entities.Account;
import ma.enset.comptecqrses.queries.entities.Operation;
import ma.enset.comptecqrses.queries.repositories.AccountRepository;
import ma.enset.comptecqrses.queries.repositories.OperationRepository;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class AccountServiceHandler {
    private AccountRepository accountRepository;
    private OperationRepository operationRepository;
    @EventHandler
    private void on(AccountCreatedEvent accountCreatedEvent){
        log.info("***********************");
        log.info("AccountCreatedEvent received");
        Account account = new Account();
        account.setId(accountCreatedEvent.getId());
        account.setBalance(accountCreatedEvent.getInitialBalance());
        account.setStatus(accountCreatedEvent.getStatus());
        account.setCurrency(accountCreatedEvent.getCurrency());
        accountRepository.save(account);
    }
    @EventHandler
    private void on(AccountActivatedEvent accountActivatedEvent){
        log.info("***********************");
        log.info("AccountActivatedEvent received");
        Account account = accountRepository.findById(accountActivatedEvent.getId()).get();
        account.setStatus(accountActivatedEvent.getStatus());
        accountRepository.save(account);
    }
    @EventHandler
    private void on(AccountDebitedEvent accountDebitedEvent){
        log.info("***********************");
        log.info("AccountDebitedEvent received");
        Account account = accountRepository.findById(accountDebitedEvent.getId()).get();
        Operation operation = new Operation();
        operation.setAmount(accountDebitedEvent.getAmount());
        operation.setDate(new Date()); // not to do
        operation.setType(OperationType.DEBIT);
        operation.setAccount(account);
        operationRepository.save(operation);
        account.setBalance(account.getBalance()-accountDebitedEvent.getAmount());
        accountRepository.save(account);
    }
    @EventHandler
    private void on(AccountCreditedEvent accountCreditedEvent){
        log.info("***********************");
        log.info("AccountCreditedEvent received");
        Account account = accountRepository.findById(accountCreditedEvent.getId()).get();
        Operation operation = new Operation();
        operation.setAmount(accountCreditedEvent.getAmount());
        operation.setDate(new Date()); // not to do
        operation.setType(OperationType.CREDIT);
        operation.setAccount(account);
        operationRepository.save(operation);
        account.setBalance(account.getBalance()+accountCreditedEvent.getAmount());
        accountRepository.save(account);
    }
    @QueryHandler
    public List<Account> on(GetAllAccountsQuery getAllAccountsQuery){
        return accountRepository.findAll();
    }
    @QueryHandler
    public Account on(GetAccountByIdQuery getAccountByIdQuery){
        return accountRepository.findById(getAccountByIdQuery.getId()).get();
    }
}
