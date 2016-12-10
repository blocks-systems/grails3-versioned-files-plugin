package tools.blocks

class Annex {

    String createdBy = 'SYSTEM'
    Date createdAt = new Date()
    String editedBy = 'SYSTEM'
    Date editedAt = new Date()
    boolean isDeleted = false
    String fileName
    String extension
    String bucket
    String contentType
    Long fileVersion = 0L
    Long length = 0L
    def file

    static hasMany = [annexableDomains: AnnexableDomain]

    static transients = ['file','size']

    static constraints = {
        createdAt nullable: false
        editedAt nullable: false
        createdBy nullable: false
        editedBy nullable: false
        isDeleted nullable: false
        fileName nullable: false
        extension nullable: true
        bucket nullable: true
        annexableDomains nullable: true
        contentType nullable: true
        length nullable: true
        fileVersion nullable: false
    }

    String getSize() {
        if (length < 1024) {
            return "${length} b"
        } else if (length >= 1024 && length < 1048576) {
            return "${(length / 1024).intValue()} kB"
        } else if (length >= 1048576 && length < 1073741824) {
            return "${(length / 1048576).intValue()} MB"
        } else {
            return "${(length / 1073741824).intValue()} GB"
        }
    }

    String toString() {
        extension ? "$fileName.$extension" : "$fileName"
    }
}
