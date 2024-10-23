package ma.enset.comptecqrses.commands.controllers;

import lombok.AllArgsConstructor;
import ma.enset.comptecqrses.common_api.commands.CreateAccountCommand;
import ma.enset.comptecqrses.common_api.commands.CreditAccountCommand;
import ma.enset.comptecqrses.common_api.commands.DebitAccountCommand;
import ma.enset.comptecqrses.common_api.dtos.CreateAccountRequestDTO;
import ma.enset.comptecqrses.common_api.dtos.CreditAccountRequestDTO;
import ma.enset.comptecqrses.common_api.dtos.DebitAccountRequestDTO;
import ma.enset.comptecqrses.common_api.events.AccountDebitedEvent;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@RestController
@RequestMapping(path = "/commands/account")
@AllArgsConstructor
public class AccountCommandController {
    private CommandGateway commandGateway;
    private EventStore eventStore;
    @PostMapping(path = "/create")
    public CompletableFuture<String> createAccount(@RequestBody CreateAccountRequestDTO request){
        return commandGateway.send(new CreateAccountCommand(
                UUID.randomUUID().toString(),
                request.getInitialBalance(),
                request.getCurrency()
        ));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> exceptionHandler(Exception exception){
        return new ResponseEntity<>(
                exception.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
    @GetMapping(path = "/eventStore/{id}")
    public Stream eventStore(@PathVariable String id){
        return eventStore.readEvents(id).asStream();
    }

    @PutMapping(path = "/credit")
    public CompletableFuture<String> creditAccount(@RequestBody CreditAccountRequestDTO request){
        return commandGateway.send(new CreditAccountCommand(
                request.getAccountId(),
                request.getAmount(),
                request.getCurrency()
        ));
    }

    @PutMapping(path = "/debit")
    public CompletableFuture<String> debitAccount(@RequestBody DebitAccountRequestDTO debitAccountRequestDTO){
        return commandGateway.send(new DebitAccountCommand(
                debitAccountRequestDTO.getAccountId(),
                debitAccountRequestDTO.getAmount(),
                debitAccountRequestDTO.getCurrency()
        ));
    }
}
