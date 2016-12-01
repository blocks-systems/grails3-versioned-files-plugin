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

    def addAnnex() {
        String domainName = params.domainName
        Long domainId = params.domainId as Long
        render annexableService.add(params.uploadFile, domainName, domainId)
    }

    def attachAnnex() {
        annexableService.attach(params.domainName, params.domainId as Long, params.annexId as Long)
    }

    def detachAnnex() {
        render annexableService.detach(params) ? 'OK' : 'ERROR'
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
            String filename = annex.fileName
            if (annex.extension) {
                filename += "." + annex.extension
            }

            ['Content-disposition': "${params.containsKey('inline') ? 'inline' : 'attachment'};filename=\"$filename\"",
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
