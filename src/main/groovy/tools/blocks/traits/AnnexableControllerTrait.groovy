package tools.blocks.traits

import grails.artefact.Enhances
import grails.converters.JSON
import org.grails.core.artefact.ControllerArtefactHandler
import org.grails.web.servlet.mvc.GrailsWebRequest
import groovy.transform.CompileStatic
import tools.blocks.Annex
import tools.blocks.AnnexableService

import javax.servlet.http.HttpServletResponse
import java.nio.file.Files

/**
 * Created by fgroch on 10.12.16.
 */
@Enhances(ControllerArtefactHandler.TYPE)
trait AnnexableControllerTrait {
    AnnexableService annexableService;

    def findAnnex() {
        String namePart = params.remove('namePart')
        String bucket = params.remove('bucket')
        render annexableService.find(namePart, bucket, params) as JSON
    }

    def addAnnex() {
        if (params.uploadFile) {
            annexableService.addAnnex(domainObject, params.uploadFile)
        }
    }

    def detachAnnex() {
        annexableService.detach(domainObject, params)
    }

    def showAnnex() {
        forward(action:'downloadAnnex', params:params)
    }

    def downloadAnnex() {
        Annex annex = Annex.get(params.annexId)
        def file = annexableService.downloadAnnexFile(annex, params.version)
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