package tools.blocks

class AnnexableDomain {

    Annex annex
    String domainName
    Long domainId

    static belongsTo = [annex: Annex]

    static constraints = {
        annex nullable: false
        domainName nullable: false
        domainId nullable: false
    }

    def getLinkedObject() {
        getClass().classLoader.loadClass(domainName).get(domainId)
    }

}
