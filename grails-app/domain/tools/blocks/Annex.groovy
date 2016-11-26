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
    Long length = 0
    def file

    static hasMany = [annexableDomains: AnnexableDomain]

    static transients = ['file']

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
    }
}
