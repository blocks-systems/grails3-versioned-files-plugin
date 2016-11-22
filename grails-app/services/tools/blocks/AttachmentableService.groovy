package tools.blocks

import grails.transaction.Transactional
import org.apache.commons.logging.LogFactory
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest.StandardMultipartFile
import tools.blocks.exceptions.EmptyDomainObjectException

@Transactional
class AttachmentableService {

    private static final log = LogFactory.getLog(AttachmentableService.class)

    def getAttachmentsForDomain(def domainObject, def params = [:]) {
        if (!domainObject) {
            throw new EmptyDomainObjectException()
        }
        if (!domainObject.ident()) {
            throw new EmptyDomainObjectException("No identity for domain object")
        }

        params.order = params.order ?: 'desc'
        params.cache = true

        Object result = LinkAttachment.createCriteria().list(params) {
            eq 'domainName', domainObject.class.name
            eq 'domainId', domainObject.ident()
        }

        if(result) {
           Object ref = result.getAt('attachment')
        } else {
            return null
        }
    }

    def getAttachment(LinkAttachment linkAttachment, def params = [:]) {
        linkAttachment.attachment
    }

    def getAttachmentInfo(Long attachmentId, String fileName, String bucket) {
        Attachment attachment = Attachment.get(attachmentId)
        def result = [:]
        result.id = attachment.id
        result.fileName = attachment.fileName
        result.bucket = attachment.bucket
        result.isDeleted = attachment.isDeleted
        result.createdAt = attachment.createdAt
        result.createdBy = attachment.createdBy
        result.editedAt = attachment.editedAt
        result.editedBy = attachment.editedBy
        result.extension = attachment.extension
        result.size = attachment.length ?: 0
        result.contentType = attachment.contentType ?: 'unknown'
        result.versions = (attachment.version + 1)
        return result
    }

    def getAttachmentsGroupByBucket() {
        def result = Attachment.listOrderByBucket().groupBy({ attachment ->
            attachment.bucket
        })
        log.debug(result)
        result
    }

    def getAllBuckets() {
        def result = Attachment.createCriteria().list() {
            projections {
                distinct('bucket')
            }
        }
        log.info(resultMap)
        result
    }

    int getAttachmentsCount(def domainObject) {
        if (!domainObject) {
            throw new EmptyDomainObjectException()
        }
        if (!domainObject.ident()) {
            throw new EmptyDomainObjectException("No identity for domain object")
        }

        int result = LinkAttachment.createCriteria().get() {
            projections {
                rowCount()
            }
            eq 'domainName', domainObject.class.name
            eq 'domainId', domainObject.ident()
        }
        result
    }

    int getAttachmentsCount() {
        int result = LinkAttachment.createCriteria().get() {
            projections {
                rowCount()
            }
        }
        result
    }

    def downloadAttachmentFile(Long attachmentId, Long versionToDownload) {
        Attachment attachment = Attachment.get(attachmentId)
        downloadAttachmentFile(attachment, versionToDownload)
    }

    def downloadAttachmentFile(Attachment attachment, Long versionToDownload) {
        if (!attachment) {
            throw new EmptyDomainObjectException("Attachment cannot be null")
        }
        if (!versionToDownload) {
            versionToDownload = attachment.version
        }
        def params = [:]
        params.versionToDownload = versionToDownload
        def file = FileRepo.getFile(attachment, params)
        file
    }

    def addAttachment(def domainObject, StandardMultipartFile file) {
        if (!domainObject) {
            throw new EmptyDomainObjectException()
        }
        if (!domainObject.ident()) {
            throw new EmptyDomainObjectException("No identity for domain object")
        }
    }

    def attach(def domainObject, Attachment attachment) {
        if (!domainObject) {
            throw new EmptyDomainObjectException()
        }
        if (!domainObject.ident()) {
            throw new EmptyDomainObjectException("No identity for domain object")
        }
        LinkAttachment linkAttachment = new LinkAttachment()
        linkAttachment.attachment = attachment
        linkAttachment.domainName = domainObject.class.name
        linkAttachment.domainId = domainObject.ident()
        attachment.addToLinkAttachments(linkAttachment)
        attachment.save()
        return attachment
    }

    def add(Attachment attachment) {
        if (attachment.file) {
            attachment = FileRepo.uploadFile(attachment)
        }
        attachment
    }

    def add(def file, String domainName, Long domainId) {
        Attachment attachment = new Attachment()
        attachment.fileName = file.filename
        LinkAttachment linkAttachment = new LinkAttachment()
        linkAttachment.attachment = attachment
        linkAttachment.domainName = domainName
        linkAttachment.domainId = domainId
        attachment.addToLinkAttachments(linkAttachment)
        attachment.save()
        return attachment
    }

    def remove(Map params=[:]) {
        if (!params.attachmentId) {
            return
        }
        Attachment attachment = Attachment.get(attachmentId)
        if (!attachment) {
            return
        }
        FileRepo.remove(params)
        attachment.isDeleted = true
        attachment.save flush:true
        attachment
    }
}
