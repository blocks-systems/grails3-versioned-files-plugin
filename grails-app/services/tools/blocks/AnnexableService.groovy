package tools.blocks

import grails.transaction.Transactional
import org.apache.commons.logging.LogFactory
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest.StandardMultipartFile
import tools.blocks.exceptions.EmptyDomainObjectException

@Transactional
class AnnexableService {

    private static final log = LogFactory.getLog(AnnexableService.class)

    /**
     * Method for get attached annexes for domain object
     * @param domainObject Object to check
     * @param params Additional params like order, max or offset
     * @return Collection of attached annexes
     */
    def getAnnexesForDomain(def domainObject, def params = [:]) {
        if (!domainObject) {
            throw new EmptyDomainObjectException()
        }
        if (!domainObject.ident()) {
            throw new EmptyDomainObjectException("No identity for domain object")
        }

        params.order = params.order ?: 'desc'
        params.cache = true

        Object result = AnnexableDomain.createCriteria().list(params) {
            eq 'domainName', domainObject.class.name
            eq 'domainId', domainObject.ident()
        }

        result?.getAt('annex')
    }

    def getAnnex(AnnexableDomain annexableDomain, def params = [:]) {
        annexableDomain.annex
    }

    def getAnnexInfo(Long annexId, String fileName, String bucket) {
        Annex annex = Annex.get(annexId)
        def result = [:]
        result.id = annex.id
        result.fileName = annex.fileName
        result.bucket = annex.bucket
        result.isDeleted = annex.isDeleted
        result.createdAt = annex.createdAt
        result.createdBy = annex.createdBy
        result.editedAt = annex.editedAt
        result.editedBy = annex.editedBy
        result.extension = annex.extension
        result.size = annex.length ?: 0
        result.contentType = annex.contentType ?: 'unknown'
        result.versions = (annex.version + 1)
        return result
    }

    def getAnnexesGroupByBucket() {
        def result = Annex.listOrderByBucket().groupBy({ annex ->
            annex.bucket
        })
        log.debug(result)
        result
    }

    def getAllBuckets() {
        def result = Annex.createCriteria().list() {
            projections {
                distinct('bucket')
            }
        }
        log.info(resultMap)
        result
    }

    int getAnnexesCount(def domainObject) {
        if (!domainObject) {
            throw new EmptyDomainObjectException()
        }
        if (!domainObject.ident()) {
            throw new EmptyDomainObjectException("No identity for domain object")
        }

        int result = AnnexableDomain.createCriteria().get() {
            projections {
                rowCount()
            }
            eq 'domainName', domainObject.class.name
            eq 'domainId', domainObject.ident()
        }
        result
    }

    int getAnnexesCount() {
        int result = AnnexableDomain.createCriteria().get() {
            projections {
                rowCount()
            }
        }
        result
    }

    def find(String namePart, String bucket, def params= [:]) {
        params.order = params.order ?: 'desc'
        params.cache = true
        def c = Annex.createCriteria()
        c.eq("","").and {}
        def criteria = Annex.createCriteria() {
            like("fileName", namePart)
        }
        if (bucket) {
            criteria = criteria.and {
                eq("bucket", bucket)
            }
        }
        def results = criteria.list(params)
        results
    }

    def downloadAnnexFile(Long annexId, Long versionToDownload) {
        Annex annex = Annex.get(annexId)
        downloadAnnexFile(annex, versionToDownload)
    }

    def downloadAnnexFile(Annex annex, Long versionToDownload) {
        if (!annex) {
            throw new EmptyDomainObjectException("Annex cannot be null")
        }
        if (!versionToDownload) {
            versionToDownload = annex.version
        }
        def params = [:]
        params.versionToDownload = versionToDownload
        def file = FileRepo.getFile(annex, params)
        file
    }

    def addAnnex(def domainObject, StandardMultipartFile file) {
        if (!domainObject) {
            throw new EmptyDomainObjectException()
        }
        if (!domainObject.ident()) {
            throw new EmptyDomainObjectException("No identity for domain object")
        }
        add(file, domainObject)
    }

    def attach(def domainObject, Long annexId) {
        Annex annex = Annex.get(annexId)
        attach(domainObject, annex)
    }

    def attach(def domainObject, Annex annex) {
        if (!domainObject) {
            throw new EmptyDomainObjectException()
        }
        if (!domainObject.ident()) {
            throw new EmptyDomainObjectException("No identity for domain object")
        }
        AnnexableDomain annexableDomain = new AnnexableDomain()
        annexableDomain.annex = annex
        annexableDomain.domainName = domainObject.class.name
        annexableDomain.domainId = domainObject.ident()
        annex.addToAnnexableDomains(annexableDomain)
        annex.save()
        return annex
    }

    def add(Annex annex) {
        if (annex.file) {
            annex = FileRepo.uploadFile(annex)
        }
        annex
    }

    def add(def file, def domainObject) {
        if (!domainObject) {
            throw new EmptyDomainObjectException()
        }
        if (!domainObject.ident()) {
            throw new EmptyDomainObjectException("No identity for domain object")
        }
        add(file, domainObject.class.name, domainObject.ident())
    }

    def add(def file, String domainName, Long domainId) {
        Annex annex = new Annex()
        annex.fileName = file.filename
        AnnexableDomain annexableDomain = new AnnexableDomain()
        annexableDomain.annex = annex
        annexableDomain.domainName = domainName
        annexableDomain.domainId = domainId
        annex.addToAnnexableDomains(annexableDomain)
        annex.save()
        annex.file = file
        add(annex)
    }

    def detach(def domainObject, def params = [:]) {
        detach(domainObject, params.annexId as Long)
    }

    def detach(def domainObject, Long annexId) {
        Annex annex = Annex.get(annexId)
        boolean isDeleted = false
        if (annex) {
            isDeleted = annex.annexableDomains.removeAll { it.domainName == domainObject?.class?.name && it.domainId == domainObject?.ident()}
        }
        annex.save flush:true
        isDeleted
    }

    def moveToTrash(Map params=[:]) {
        if (!params.annexId) {
            return
        }
        Annex annex = Annex.get(params.annexId)
        if (!annex) {
            return
        }
        params.bucket = params.bucket ?: annex.bucket
        params.version = params.version ?: annex.version
        boolean  ret = FileRepo.moveToTrash(params)
        annex.isDeleted = true
        annex.save flush:true
        ret
    }

    def emptyTrash() {
        FileRepo.emptyTrash()
    }
}
