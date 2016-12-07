package tools.blocks

import grails.converters.JSON

import javax.servlet.http.HttpServletResponse
import java.nio.file.Files

class AnnexableController {

    def annexableService

    def find() {
        String namePart = params.remove('namePart')
        String bucket = params.remove('bucket')
        render annexableService.find(namePart, bucket, params) as JSON
    }

    def uploadAnnex() {
        if (params.uploadFile) { //OK, file uploaded
            Annex annex
            if (params.uploadAnnexId) { //OK, annex id is set, create new version
                annex = Annex.get(params.uploadAnnexId)
            } else {//create new annex
                annex = new Annex()
                if (params.uploadFile.filename?.contains('.')) {
                    int idx = params.uploadFile.filename.lastIndexOf('.')
                    annex.fileName = params.uploadFile.filename.substring(0, idx)
                    annex.extension = params.uploadFile.filename.substring(idx+1, params.uploadFile.filename.length())
                } else {
                    annex.fileName = params.uploadFile.filename
                }
                if (params.uploadBucket) {
                    annex.bucket = params.uploadBucket
                }
            }
            //ADD CONTENT TYPE FROM Apache Tika
            annex.save flush:true//must be saved for new version of domain object
            annex.file = params.uploadFile
            annex.fileVersion++
            annex = annexableService.add(annex)
            if (params.domainName && params.domainId) { //it also should be attach to domain object
                AnnexableDomain annexableDomain = new AnnexableDomain()
                annexableDomain.annex = annex
                annexableDomain.domainName = params.domainName
                annexableDomain.domainId = params.domainId as Long
                annex.addToAnnexableDomains(annexableDomain)
            }
            annex.save flush:true//size and content type could be changed
            render message(code: 'default.annex.uploaded',default: 'Annex uploaded sucefully')
        }
        response.status = HttpServletResponse.SC_NO_CONTENT
    }

    def attachAnnex() {
        if(annexableService.attach(params.domainName, params.domainId as Long, params.annexId as Long)) {
            render message(code: 'default.annex.attached',default: 'Annex attached sucefully')
        } else {
            render message(code: 'error.generic')
        }
    }

    def detachAnnex() {
        if (annexableService.detach(params)) {
            render message(code: 'default.annex.detached',default: 'Annex detached sucefully')
        } else {
            render message(code: 'error.generic')
        }
    }

    def showAnnex() {
        params.inline = params.inline ?: ''
        params.withContentType = params.withContentType?: ''
        forward(action:'downloadAnnex', params:params)
    }

    def downloadAnnex() {
        Annex annex = Annex.get(params.annexId)
        def file = annexableService.downloadAnnexFile(annex, params.version as Long)
        if (file) {

            ['Content-disposition': "${params.containsKey('inline') ? 'inline' : 'attachment'};filename=\"$annex\"",
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
}
