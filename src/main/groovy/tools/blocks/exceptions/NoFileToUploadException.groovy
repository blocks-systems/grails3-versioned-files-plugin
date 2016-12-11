package tools.blocks.exceptions

/**
 * Created by floyd on 11.12.16.
 */
class NoFileToUploadException {
    NoFileToUploadException() {
        super("No file to upload. Request params should contain uploadFile param.")
    }

    NoFileToUploadException(String msg) {
        super(msg)
    }
}
