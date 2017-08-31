package blocks.exceptions

/**
 * Created by fgroch on 11.12.16.
 */
class InsufficientParamsException extends Exception {
    InsufficientParamsException() {
        super("Insufficient params for operation handling")
    }

    InsufficientParamsException(String msg) {
        super(msg)
    }
}
