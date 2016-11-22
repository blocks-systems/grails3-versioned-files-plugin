package tools.blocks.exceptions

/**
 * Created by fgroch on 14.11.16.
 */
class EmptyDomainObjectException extends Exception {

    EmptyDomainObjectException() {
        super("Access to attachment with invalid domain object.")
    }

    EmptyDomainObjectException(String msg) {
        super(msg)
    }

}
