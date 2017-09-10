package blocks.traits

import grails.artefact.Enhances
import grails.converters.JSON
import grails.util.Holders
import grails.web.Action
import org.grails.core.artefact.ControllerArtefactHandler
import blocks.Annex
import blocks.AnnexableDomain
import blocks.AnnexableService
import blocks.exceptions.InsufficientParamsException
import blocks.exceptions.NoFileToUploadException

import javax.servlet.http.HttpServletResponse
import java.nio.file.Files

/**
 * Created by fgroch on 10.12.16.
 */
@Enhances(ControllerArtefactHandler.TYPE)
trait AnnexableControllerTrait {
    AnnexableService annexableService;

    @Action
    def findAnnex() {
        String namePart = params.remove('namePart')
        String bucket = params.remove('bucket')
        render annexableService.find(namePart, bucket, params) as JSON
    }

    @Action
    def uploadAnnex() {
        if (params.uploadFile) { //OK, file uploaded
            Annex annex
            if (params.uploadAnnexId) { //OK, annex id is set, create new version
                annex = Annex.get(params.uploadAnnexId)
            } else {//create new annex
                annex = new Annex()
                annex.createdBy = resolveUserName()
                annex.createdAt = new Date()
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
            annex.editedBy = resolveUserName()
            annex.editedAt = new Date()
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
            def redirectUri = ''
            if (params.redirectController) {
                redirectUri = createLink(controller: params.redirectController, action: params.redirectAction, id: params.redirectId)
            }
            annex.save flush:true//size and content type could be changed
            if (redirectUri != null && redirectUri.size() > 0) {
                redirect uri:redirectUri
            } else {
                render message(code: 'default.annex.uploaded', default: 'Annex uploaded sucefully')
            }
        } else {
            throw new NoFileToUploadException()
        }
        response.status = HttpServletResponse.SC_NO_CONTENT
    }

    @Action
    def attachAnnex() {
        if(annexableService.attach(params.domainName, params.domainId as Long, params.annexId as Long)) {
            render message(code: 'default.annex.attached',default: 'Annex attached sucefully')
        } else {
            render message(code: 'error.generic')
        }
    }

    @Action
    def detachAnnex() {
        if (annexableService.detach(params.domainName, params.domainId as Long, params.annexId as Long)) {
            render message(code: 'default.annex.detached',default: 'Annex detached sucefully')
        } else {
            render message(code: 'error.generic')
        }
    }

    @Action
    def showAnnex() {
        params.inline = params.inline ?: ''
        params.withContentType = params.withContentType?: ''
        forward(action:'downloadAnnex', params:params)
    }

    @Action
    def downloadAnnex() {
        if (!params.annexId) {
            throw new InsufficientParamsException()
        }
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

    private String resolveUserName() {
        String userName = 'SYSTEM'
        def userConfig = Holders.config?.annexable?.userName
        if (userConfig instanceof Closure) {
            userConfig.delegate = this
            userConfig.resolveStrategy = Closure.DELEGATE_ONLY
            userName = userConfig.call()
        }
        if (userConfig instanceof String) {
            userName = userConfig
        }
        userName
    }
}