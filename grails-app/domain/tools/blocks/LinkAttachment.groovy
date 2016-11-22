package tools.blocks

class LinkAttachment {

    Attachment attachment
    String domainName
    Long domainId

    static belongsTo = [attachment: Attachment]

    static constraints = {
        attachment nullable: false
        domainName nullable: false
        domainId nullable: false
    }

    def getLinkedObject() {
        getClass().classLoader.loadClass(domainName).get(domainId)
    }

}
