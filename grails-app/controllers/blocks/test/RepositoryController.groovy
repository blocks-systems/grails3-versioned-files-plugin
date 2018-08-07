package blocks.test

import blocks.AnnexableService
import grails.converters.JSON
import blocks.Annex

import javax.servlet.http.HttpServletResponse
import java.nio.file.Files

class RepositoryController {

    AnnexableService annexableService

    def index() {
        def buckets = annexableService.annexesGroupByBucket
        respond Annex.list(params), model:[buckets: buckets]
    }

    def getAnnexInfo(Long annexId, String fileName, String bucket) {
        def data = annexableService.getAnnexInfo(annexId, fileName, bucket)
        render data as JSON
    }

    def downloadAnnexFile() {
        Annex annex = Annex.get(params.annexId)
        def file = annexableService.downloadAnnexFile(annex, params.versionToDownload as Long)
        if (file != null) {
            String filename = annex.fileName
            if (annex.extension) {
                filename += "." + annex.extension
            }

            ['Content-disposition': "${params.containsKey('inline') ? 'inline' : 'annex'};filename=\"$filename\"",
             'Cache-Control': 'private',
             'Pragma': ''].each {k, v ->
                response.setHeader(k, v)
            }

            if (params.containsKey('withContentType')) {
                response.contentType = annex.contentType
            } else {
                response.contentType = 'application/octet-stream'
            }
            response.outputStream << Files.newInputStream(file)
            return
        }
        response.status = HttpServletResponse.SC_NOT_FOUND
    }

    def uploadAnnex() {
        if (params.uploadFile) {//OK, file uploaded
            Annex annex
            //params.bucket = params.bucket ?: 'common'
            if (params.uploadAnnexId) {//OK, annex id is set
                annex = Annex.get(params.uploadAnnexId)
                annex.file = params.uploadFile
                if (params.uploadFile.filename?.contains('.')) {
                    int idx = annex.fileName.lastIndexOf('.')
                    annex.fileName = params.uploadFile.filename.substring(0, idx)
                    annex.extension = params.uploadFile.filename.substring(idx+1, params.uploadFile.filename.length())
                } else {
                    annex.fileName = params.uploadFile.filename
                }
                //ADD CONTENT TYPE FROM Apache Tika

            } /*else {//create new annex
                def attachments =  Annex.findAllByFileName(params.uploadFile.filename)
                if (attachments && !attachments.empty) {
                }
            }*/
            annex.save flush:true//must be saved for new version of domain object
            annex = annexableService.add(annex)
            annex.save flush:true//size and content type could be changed
            redirect action:"index", method:"GET"
        }
        response.status = HttpServletResponse.SC_NO_CONTENT
    }

    def moveToTrash() {
        boolean res = false
        if (params.attachmentId) {
            //Annex annex = Annex.get(params.attachmentId)
            //params.version = annex.version
            //params.bucket = annex.bucket
            res = annexableService.moveToTrash(params)
            /*if (annexableService.remove(params)) {

            }*/
        }
        if (res) {
            request.withFormat {
                form multipartForm {
                    flash.message = message(code: 'default.annex.delete.success', default: 'Annex deleted successfully')
                    redirect action: 'index'
                }
                '*' { redirect action: 'index' }
            }
        } else {
            render message(code: 'error.annex.delete', default:'Error while deleting annex')
        }
    }
}
