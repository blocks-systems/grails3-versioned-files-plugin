package tools.blocks.exceptions

/**
 * Created by fgroch on 08.11.16.
 */
class UnavailableFileSystemException extends Exception {
    UnavailableFileSystemException() {
        super("You access to file or directory.")
    }

    UnavailableFileSystemException(String msg) {
        super(msg)
    }

    UnavailableFileSystemException(String paramName, String originalFileName) {
        super("Cannot access " + + originalFileName + " file or directory.")
    }
}
