package tools.blocks.test

import grails.converters.JSON

import tools.blocks.Attachment

import javax.servlet.http.HttpServletResponse
import java.nio.file.Files
import java.nio.file.Path

class RepositoryController {

    def attachmentableService

    def index() {
        def buckets = attachmentableService.attachmentsGroupByBucket
        respond Attachment.list(params), model:[buckets: buckets]
    }

    def getAttachmentInfo(Long attachmentId, String fileName, String bucket) {
        def data = attachmentableService.getAttachmentInfo(attachmentId, fileName, bucket)
        render data as JSON
    }

    def downloadAttachmentFile() {
        Attachment attachment = Attachment.get(params.attachmentId)
        def file = attachmentableService.downloadAttachmentFile(attachment, params.versionToDownload as Long)
        if (file != null) {
            String filename = attachment.fileName
            if (attachment.extension) {
                filename += "." + attachment.extension
            }

            ['Content-disposition': "${params.containsKey('inline') ? 'inline' : 'attachment'};filename=\"$filename\"",
             'Cache-Control': 'private',
             'Pragma': ''].each {k, v ->
                response.setHeader(k, v)
            }

            if (params.containsKey('withContentType')) {
                response.contentType = attachment.contentType
            } else {
                response.contentType = 'application/octet-stream'
            }
            response.outputStream << Files.newInputStream(file)
            //file.toFile().withInputStream{fis->
            //    response.outputStream << fis
            //}

            // response.contentLength = file.length()
            // response.outputStream << file.readBytes()
            // response.outputStream.flush()
            // response.outputStream.close()
            return
        }
        response.status = HttpServletResponse.SC_NOT_FOUND
    }

    def uploadAttachment() {
        if (params.uploadFile) {//OK, file uploaded
            Attachment attachment
            //params.bucket = params.bucket ?: 'common'
            if (params.uploadAttachmentId) {//OK, attachment id is set
                attachment = Attachment.get(params.uploadAttachmentId)
                attachment.file = params.uploadFile
                attachment.fileName = params.uploadFile.filename
                if (attachment.fileName.contains('.')) {
                    int idx = attachment.fileName.lastIndexOf('.')
                    attachment.extension = attachment.fileName.substring(idx+1, attachment.fileName.length())
                }
                //ADD CONTENT TYPE FROM Apache Tika

            } /*else {//create new attachment
                def attachments =  Attachment.findAllByFileName(params.uploadFile.filename)
                if (attachments && !attachments.empty) {
                }
            }*/
            attachment.save flush:true//must be saved for new version of domain object
            attachment = attachmentableService.add(attachment)
            attachment.save flush:true//size and content type could be changed
            redirect action:"index", method:"GET"
        }
        response.status = HttpServletResponse.SC_NO_CONTENT
    }

    def moveToTrash() {
        if (params.attachmentId) {
            Attachment attachment = Attachment.get(params.attachmentId)
            params.version = attachment.version
            params.bucket = attachment.bucket
            if (attachmentableService.remove(params)) {

            }
        }

        redirect action:"index", method:"GET"
    }
}
