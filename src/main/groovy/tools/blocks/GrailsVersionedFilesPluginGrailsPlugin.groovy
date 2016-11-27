package tools.blocks

import grails.plugins.*
import grails.util.GrailsClassUtils
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest

import javax.servlet.http.HttpServletResponse
import java.nio.file.Files

class GrailsVersionedFilesPluginGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.2.2 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "Grails3 Versioned Files Plugin" // Headline display name of the plugin
    def author = "Filip Grochowski"
    def authorEmail = "fgroch@gmail.com"
    def description = '''\
Grails 3 plugin for manage files as attachments with versions.
'''
    def profiles = ['web']

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/grails-versioned-files-plugin"

    // Extra (optional) plugin metadata
    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
    def developers = [ [ name: "Filip Grochowski", email: "filip.grochowski@blocks.tools"],[name: "Emil Wesołowski", email:"emil.wesolowski@blocks.tools"]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/fgroch/grails3-versioned-files-plugin" ]

    Closure doWithSpring() { {->
            // TODO Implement runtime spring config (optional)
        }
    }

    void doWithDynamicMethods() {
        AnnexableService annexableService = applicationContext.getBean("annexableService")

        grailsApplication.domainClasses?.each {d ->
            if (Annexable.class.isAssignableFrom(d.clazz) || GrailsClassUtils.getStaticPropertyValue(d.clazz, "annexable")) {
                addDomainMethods d.clazz.metaClass, annexableService
            }
        }

        grailsApplication.controllerClasses?.each {c ->
            addControllerMethods c.clazz.metaClass, service
        }
    }

    void doWithApplicationContext() {
        // TODO Implement post initialization spring config (optional)
    }

    void onChange(Map<String, Object> event) {
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    void onConfigChange(Map<String, Object> event) {
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    void onShutdown(Map<String, Object> event) {
        // TODO Implement code that is executed when the application shuts down (optional)
    }

    private void addControllerMethods(MetaClass metaClass, AnnexableService annexableService) {
        mc.find = { params = [:] ->
            String namePart = ""
            String bucket = ""
            if (params.namePart) {
                namePart = params.namePart
                params.remove('namePart')
            }
            if (params.bucket) {
                bucket = params.bucket
                params.remove('bucket')
            }
            annexableService.find(partName, bucket, params)
        }

        mc.addAnnex = { domainObject, params= [:] ->
            if (params.uploadFile) {
                annexableService.addAnnex(domainObject, params.uploadFile)
            }
        }

        mc.detachAnnex = { domainObject, params= [:] ->
            annexableService.detach(domainObject, params)
        }

        mc.downloadAnnex = { params = [:] ->
            Annex annex = Annex.get(params.annexId)
            def file = annexableService.downloadAnnexFile(annex, params.version)
            if (file) {
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
    }

    private void addDomainMethods(MetaClass metaClass, AnnexableService annexableService) {
        metaClass.getAnnexes = { params = [:] ->
            annexableService.getAnnexesForDomain(delegate, params)
        }

        metaClass.attachAnnex = { params = [:] ->
            annexableService.attach(delegate, params.annexId)

        }

        metaClass.addAnnex = { StandardMultipartHttpServletRequest.StandardMultipartFile file
            annexableService.addAnnex(delegate, file)
        }

        metaClass.detachAnnex = { params = [:] ->
            annexableService.detach(delegate, params)
        }
    }
}
