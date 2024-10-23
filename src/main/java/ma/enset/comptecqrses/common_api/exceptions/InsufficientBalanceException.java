package ma.enset.comptecqrses.common_api.exceptions;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String insufficientBalance) {
        super(insufficientBalance);
    }
}
