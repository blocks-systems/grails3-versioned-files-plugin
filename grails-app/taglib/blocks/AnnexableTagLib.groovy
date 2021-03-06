package blocks

import org.apache.commons.logging.LogFactory

/**
 * Tag library for annexes handle
 *
 * @author fgroch
 */
class AnnexableTagLib {
    //static defaultEncodeAs = [taglib:'html']
    private static final log = LogFactory.getLog(this)

    static namespace = "annexable"

    def annexesList = { attrs, body ->
        def bean = attrs.remove('bean')
        def controller = attrs.remove('controller')

        if (bean?.metaClass?.hasProperty(bean, 'annexes') != null) {
            def inputName = attrs.remove('name') ?: 'annex'
            def wrapper = inputName + 'Wrapper'
            // html
            final StringBuilder sb = new StringBuilder()
            sb.append("<div id='$wrapper'>")
            bean?.getAnnexes()?.each { annex ->
                def annexLink = g.createLink(controller: 'annexable', action: 'showAnnex', params:['annexId':annex.id])
                sb.append("<div id='$inputName" + "_$annex.id' class='col-lg-6 col-md-12 col-xs-12 col-sm-12'>")
                        .append("<iframe id='iframeannex_$annex.id'  style='width: 100%; height:425px;' frameborder='0' src='${annexLink}'></iframe>")
                        .append("</div>")
            }
            sb.append("</div>")
            out << sb.toString()
        }
    }

    def annexesDomainMiniPanel = { attrs, body ->
        def bean = attrs.remove('bean')
        def bucket = attrs.remove('bucket') ?: 'default'
        def controller = attrs.remove('controller') ?: 'annexable'
        def redirectController = attrs.remove('redirectController')
        def redirectAction = attrs.remove('redirectAction')
        def redirectId = attrs.remove('redirectId')
        def redirectControllerInput = ''
        def redirectActionInput = ''
        def redirectIdInput = ''
        def uploadAnnexLink = g.createLink(controller: "${controller}", action: 'uploadAnnex')
        def showAnnexSimpleLink = g.createLink(controller: "${controller}", action: 'showAnnex')
        def attachAnnexSimpleLink = g.createLink(controller: "${controller}", action: 'attachAnnex')
        if (redirectController != null) {
            redirectControllerInput = "<input id='redirectController' type='hidden' value='${redirectController}' name='redirectController'>"
        }
        if (redirectAction != null){
            redirectActionInput = "<input id='redirectAction' type='hidden' value='${redirectAction}' name='redirectAction'>"
        }
        if (redirectId != null) {
            redirectIdInput = "<input id='redirectId' type='hidden' value='${redirectId}' name='redirectId'>"
        }

        if (bean?.metaClass?.hasProperty(bean, 'annexes') != null) {
            def inputName = attrs.remove('name') ?: 'annex'
            def wrapper = inputName + 'Wrapper'
            //html
            final StringBuilder sb = new StringBuilder()
            sb.append("<div class='container content' id='$wrapper'>")
            sb.append("<div class='row' style='padding-bottom:20px;'>")
            sb.append("<div class='col-lg-4'>")

            sb.append("<form id='uploadNewForm${bucket}' class='MultiFile-intercepted annex-upload-inline' enctype='multipart/form-data' name='uploadNewForm${bucket}' method='post' action='${uploadAnnexLink}' style='display: initial;'>")
            sb.append("<input id='uploadBucket' type='hidden' value='${bucket}' name='uploadBucket'>")
            sb.append("<input id='domainName' type='hidden' value='${bean.class.name}' name='domainName'>")
            sb.append("<input id='domainId' type='hidden' value='${bean.ident()}' name='domainId'>")
            sb.append(redirectControllerInput)
            sb.append(redirectActionInput)
            sb.append(redirectIdInput)
            sb.append("<label class='btn btn-default btn-file'>")
            sb.append(g.message(code: "addNewAnnex", default: "Add new annex")).append("  ")
            sb.append("<span class='fa fa-plus'>")
            sb.append("<input class='upload-new-input-${bucket}' type='file' name='uploadFile' id='uploadFile' style='display: none;'>")
            sb.append("</span>")
            sb.append("</label>")
            sb.append("</form>")

            sb.append("</div>")
            sb.append("</div>")


            sb.append("<div class='row'>")
            sb.append("<div class='col-lg-6'>")
            bean?.getAnnexes()?.findAll{ it -> it.bucket == bucket }.each { annex ->
                def annexLink = g.createLink(controller: "${controller}", action: 'showAnnex', params: ['annexId': annex.id])

                def detachLink = g.createLink(controller: "${controller}", action: 'detachAnnex', params: [
                        'annexId': annex.id, 'domainName': bean.class.name, 'domainId': bean.ident(),
                        'redirectController':redirectController, 'redirectAction': redirectAction, 'redirectId':redirectId
                ])
                sb.append("<div class='col-lg-6 annex-thumbnail'>")
                sb.append("<div class='annex-domain-panel' style='background-color:#cccccc;'>")
                sb.append("<div style='margin-top:20px;'>")
                sb.append("<div class='media'>")
                sb.append("<div class='media-left' style='padding-left: 20px; padding-right: 20px;'>")
                sb.append("<a href='#'>")
                sb.append("<span class='fa ${getIconByExtension(annex.extension)} fa-5x'>")
                sb.append("</a>")
                sb.append("</div>")
                sb.append("<div class='media-body' style='text-align:right;'>")
                sb.append("<p style='padding-right: 20px;'>${annex.size} <span class='fa fa-balance-scale'></span></p>")
                sb.append("<p style='padding-right: 20px;'>${annex.bucket} <span class='fa fa-folder-o'></span></p>")
                sb.append("<p style='padding-right: 20px;'>${annex.fileVersion} <span class='fa fa-code-fork'></span></p>")
                sb.append("<p style='padding-right: 20px;'>${annex.editedBy} <span class='fa fa-user-o'></span></p>")
                sb.append("</div>")
                sb.append("</div>")
                sb.append("</div>")
                sb.append("<div class='caption'>")
                sb.append("<div class='caption-text'>")
                sb.append("<p style='border-top:2px solid white; border-bottom:2px solid white; padding:10px;'>${annex.fileName}</p>")
                sb.append("<div class='btn-group'>")
                sb.append("<button type='button' class='btn btn-default'><span class='fa fa-download'></span></button>")
                sb.append("<button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>")
                sb.append("<span class='caret'></span>")
                sb.append("<span class='sr-only'>Toggle Dropdown</span>")
                sb.append("</button>")
                sb.append("<ul class='dropdown-menu'>")
                def versionMessage = g.message(code: "version")
                for (int i = 0; i <= annex.fileVersion; i++) {
                    def downloadLink = g.createLink(controller: "${controller}", action: 'downloadAnnex', params: ['annexId': annex.id, 'version': i])
                    sb.append("<li><a href='${downloadLink}'>${versionMessage} ${i}</a></li>")
                }
                sb.append("</ul>")
                sb.append("</div>")
                sb.append("<a class='btn btn-default' href='#' onClick='showAnnex${bucket}(${annex.id})'><span class='fa fa-eye'></span></a>")
                sb.append("<a class='btn btn-default' href='${detachLink}'><span class='fa fa-unlink'></span></a>")

                sb.append("<form id='uploadForm_${annex.id}' class='MultiFile-intercepted annex-upload-inline' enctype='multipart/form-data' name='uploadForm_${annex.id}' method='post' action='/${controller}/uploadAnnex' style='display: initial;'>")
                sb.append("<input id='uploadAnnexId' type='hidden' value='${annex.id}' name='uploadAnnexId'>")
                sb.append("<input id='uploadBucket' type='hidden' value='${bucket}' name='uploadBucket'>")
                sb.append(redirectControllerInput)
                sb.append(redirectActionInput)
                sb.append(redirectIdInput)
                sb.append("<label class='btn btn-default btn-file'>")
                sb.append("<span class='fa fa-upload'>")
                sb.append("<input class='upload-input_${annex.id}' type='file' name='uploadFile' id='uploadFile' style='display: none;'>")
                sb.append("</span>")
                sb.append("</label>")
                sb.append("</form>")

                sb.append("</div>")
                sb.append("</div>")
                sb.append("</div>")
                sb.append("</div>")
                sb.append("<script>")
                sb.append("\$(document).ready(function () {")
                sb.append("\$('.upload-input_${annex.id}').on('change', function () {")
                sb.append("\$('#uploadForm_${annex.id}').submit();")
                sb.append("});")
                sb.append("});")
                sb.append("</script>")

            }
            sb.append("</div>")
            sb.append("<div class='col-lg-6 annex-preview'>")
            sb.append("<iframe src='' style='height:425px; width:100%;' frameborder='0' id='showAnnexiFrame${bucket}' name='showAnnexiFrame${bucket}'></iframe>")
            sb.append("</div>")
            sb.append("</div>")
            sb.append("</div>")

            sb.append("<script>")
            sb.append("\$(document).ready(function () {")
            sb.append("\$('.upload-new-input-${bucket}').on('change', function () {")
            sb.append("\$('#uploadNewForm${bucket}').submit();")
            //sb.append("location.reload();")
            sb.append("});")
            sb.append("});")

            sb.append("function showAnnex${bucket}(id) {")
            //sb.append("\$('#showAnnexiFrame').attr('src', '/${controller}/showAnnex?annexId='+id);};")
            sb.append("\$('#showAnnexiFrame${bucket}').attr('src', '${showAnnexSimpleLink}?annexId='+id);};")
            sb.append("function findAnnex() {")
            sb.append("var query = \$('#annexableFindQuery').val();")
            sb.append("\$.ajax({")
            sb.append("url: \"${g.createLink(controller: "${controller}",action:'find')}\",")
            sb.append("dataType: 'json',")
            sb.append("data: {")
            sb.append("namePart: query")
            sb.append("},")
            sb.append("success: function (data) {")
            sb.append("\$('#annexableFindResult').empty();")
            sb.append("for (var i = 0; i < data.length; i++) {")
            sb.append("\$('#annexableFindResult').append(\"<li><a href='#' onclick='setAttachLink(\" + data[i].id + \", \\\"\" + data[i].fileName +\"\\\")'>\" + data[i].fileName + \"</a></li>\");")
            sb.append("}")
            sb.append("\$('#annexToAttachFoundedCount').text(data.length);")
            sb.append("},")
            sb.append("error: function (request, status, error) {")
            sb.append("alert(error);")
            sb.append("},")
            sb.append("complete: function () {}});};")
            sb.append("function setAttachLink(id, fileName) {")
            sb.append("\$('#annexToAttach').text(fileName);")
            sb.append("\$('#annexToAttach').prepend(\"<span class='fa fa-link'></span> \");")
            //sb.append("\$('#annexToAttach').attr('href', '/${controller}/attachAnnex?domainName=${bean.class.name}&domainId=${bean.id}&annexId='+id);};")
            sb.append("\$('#annexToAttach').attr('href', '${attachAnnexSimpleLink}?domainName=${bean.class.name}&domainId=${bean.id}&annexId='+id);};")
            sb.append("</script>")

            out << sb.toString()
        }
    }

    def annexesDomainPanel = { attrs, body ->
        def bean = attrs.remove('bean')
        def bucket = attrs.remove('bucket')
        def controller = attrs.remove('controller') ?: 'annexable'

        if (bean?.metaClass?.hasProperty(bean, 'annexes') != null) {
            def inputName = attrs.remove('name') ?: 'annex'
            def wrapper = inputName + 'Wrapper'
            //html
            final StringBuilder sb = new StringBuilder()
            sb.append("<div class='container content' id='$wrapper'>")
            sb.append("<div class='row' style='padding-bottom:20px;'>")
            sb.append("<div class='col-lg-4'>")
            sb.append("<a class='btn btn-default' href='#' style='margin-left:15px;'><span class='fa fa-trash'></span></a>")

            sb.append("<form id='uploadNewForm' class='MultiFile-intercepted annex-upload-inline' enctype='multipart/form-data' name='uploadNewForm' method='post' action='/annexable/uploadAnnex'>")
            sb.append("<input id='uploadBucket' type='hidden' value='${bucket}' name='uploadBucket'>")
            sb.append("<input id='domainName' type='hidden' value='${bean.class.name}' name='domainName'>")
            sb.append("<input id='domainId' type='hidden' value='${bean.ident()}' name='domainId'>")
            sb.append("<label class='btn btn-default btn-file'>")
            sb.append("<span class='fa fa-upload'>")
            sb.append("<input class='upload-new-input' type='file' name='uploadFile' id='uploadFile' style='display: none;'>")
            sb.append("</span>")
            sb.append("</label>")
            sb.append("</form>")

            sb.append("</div>")
            sb.append("<div class='col-lg-6 col-lg-offset-2' style='padding-right: 0px;'>")
            sb.append("<div class='input-group'>")
            sb.append("<div class='input-group-btn'>")
            sb.append("<a class='btn btn-default' href='#' id='annexToAttach' name='annexToAttach'><span class='fa fa-link'></span> name of the file</a>")
            sb.append("<button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>")
            sb.append("<span class='caret'></span>")
            sb.append("<span class='badge' id='annexToAttachFoundedCount' name='annexToAttachFoundedCount' style='margin-left: 5px;'>0</span>")
            sb.append("<span class='sr-only'>Toggle Dropdown</span>")
            sb.append("</button>")
            sb.append("<ul class='dropdown-menu' id='annexableFindResult' name='annexableFindResult'>")
            sb.append("<li><a href='#'>Object 1</a></li>")
            sb.append("<li><a href='#'>Object 2</a></li>")
            sb.append("</ul>")
            sb.append("</div>")
            sb.append("<input type='text' class='form-control' id='annexableFindQuery' name='annexableFindQuery'>")
            sb.append("<div class='input-group-btn'>")
            sb.append("<a class='btn btn-default' href='#' onClick='findAnnex()'><span class='fa fa-binoculars'></span></a>")
            sb.append("</div>")
            sb.append("</div>")
            sb.append("</div>")
            sb.append("</div>")


            sb.append("<div class='row'>")
            sb.append("<div class='col-lg-6'>")
            bean?.getAnnexes()?.each { annex ->
                def annexLink = g.createLink(controller: 'annexable', action: 'showAnnex', params: ['annexId': annex.id])

                def detachLink = g.createLink(controller: 'annexable', action: 'detachAnnex', params: ['annexId': annex.id, 'domainName': bean.class.name, 'domainId': bean.ident()])
                sb.append("<div class='col-lg-6 annex-thumbnail'>")
                sb.append("<div class='annex-domain-panel' style='background-color:#cccccc;'>")
                sb.append("<div style='margin-top:20px;'>")
                sb.append("<div class='media'>")
                sb.append("<div class='media-left' style='padding-left: 20px; padding-right: 20px;'>")
                sb.append("<a href='#'>")
                sb.append("<span class='fa ${getIconByExtension(annex.extension)} fa-5x'>")
                sb.append("</a>")
                sb.append("</div>")
                sb.append("<div class='media-body' style='text-align:right;'>")
                sb.append("<p style='padding-right: 20px;'>${annex.size} <span class='fa fa-balance-scale'></span></p>")
                sb.append("<p style='padding-right: 20px;'>${annex.bucket} <span class='fa fa-folder-o'></span></p>")
                sb.append("<p style='padding-right: 20px;'>${annex.fileVersion} <span class='fa fa-code-fork'></span></p>")
                sb.append("<p style='padding-right: 20px;'>${annex.editedBy} <span class='fa fa-user-o'></span></p>")
                sb.append("</div>")
                sb.append("</div>")
                sb.append("</div>")
                sb.append("<div class='caption'>")
                sb.append("<div class='blur'></div>")
                sb.append("<div class='caption-text'>")
                sb.append("<h3 style='border-top:2px solid white; border-bottom:2px solid white; padding:10px;'>${annex.fileName}</h3>")
                sb.append("<p>&nbsp</p>")
                sb.append("<div class='btn-group'>")
                sb.append("<button type='button' class='btn btn-default'><span class='fa fa-download'></span></button>")
                sb.append("<button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>")
                sb.append("<span class='caret'></span>")
                sb.append("<span class='sr-only'>Toggle Dropdown</span>")
                sb.append("</button>")
                sb.append("<ul class='dropdown-menu'>")
                for (int i = 0; i <= annex.fileVersion; i++) {
                    def downloadLink = g.createLink(controller: 'annexable', action: 'downloadAnnex', params: ['annexId': annex.id, 'version': i])
                    sb.append("<li><a href='${downloadLink}'>version ${i}</a></li>")
                }
                sb.append("</ul>")
                sb.append("</div>")
                sb.append("<a class='btn btn-default' href='#' onClick='showAnnex(${annex.id})'><span class='fa fa-eye'></span></a>")
                sb.append("<a class='btn btn-default' href='${detachLink}'><span class='fa fa-unlink'></span></a>")

                sb.append("<form id='uploadForm_${annex.id}' class='MultiFile-intercepted annex-upload-inline' enctype='multipart/form-data' name='uploadForm_${annex.id}' method='post' action='/annexable/uploadAnnex'>")
                sb.append("<input id='uploadAnnexId' type='hidden' value='${annex.id}' name='uploadAnnexId'>")
                sb.append("<input id='uploadBucket' type='hidden' value='${bucket}' name='uploadBucket'>")
                sb.append("<label class='btn btn-default btn-file'>")
                sb.append("<span class='fa fa-upload'>")
                sb.append("<input class='upload-input_${annex.id}' type='file' name='uploadFile' id='uploadFile' style='display: none;'>")
                sb.append("</span>")
                sb.append("</label>")
                sb.append("</form>")

                sb.append("</div>")
                sb.append("</div>")
                sb.append("</div>")
                sb.append("</div>")
                sb.append("<script>")
                sb.append("\$(document).ready(function () {")
                sb.append("\$('.upload-input_${annex.id}').on('change', function () {")
                sb.append("\$('#uploadForm_${annex.id}').submit();")
                sb.append("});")
                sb.append("});")
                sb.append("</script>")

            }
            sb.append("</div>")
            sb.append("<div class='col-lg-6 annex-preview'>")
            sb.append("<iframe src='' style='height:425px; width:100%;' frameborder='0' id='showAnnexiFrame' name='showAnnexiFrame'></iframe>")
            sb.append("</div>")
            sb.append("</div>")
            sb.append("</div>")

            sb.append("<script>")
            sb.append("\$(document).ready(function () {")
            sb.append("\$('.upload-new-input').on('change', function () {")
            sb.append("\$('#uploadNewForm').submit();")
            sb.append("});")
            sb.append("});")

            sb.append("function showAnnex(id) {")
            sb.append("\$('#showAnnexiFrame').attr('src', '/annexable/showAnnex?annexId='+id);};")
            sb.append("function findAnnex() {")
            sb.append("var query = \$('#annexableFindQuery').val();")
            sb.append("\$.ajax({")
            sb.append("url: \"${g.createLink(controller:'annexable',action:'find')}\",")
            sb.append("dataType: 'json',")
            sb.append("data: {")
            sb.append("namePart: query")
            sb.append("},")
            sb.append("success: function (data) {")
            sb.append("\$('#annexableFindResult').empty();")
            sb.append("for (var i = 0; i < data.length; i++) {")
            sb.append("\$('#annexableFindResult').append(\"<li><a href='#' onclick='setAttachLink(\" + data[i].id + \", \\\"\" + data[i].fileName +\"\\\")'>\" + data[i].fileName + \"</a></li>\");")
            sb.append("}")
            sb.append("\$('#annexToAttachFoundedCount').text(data.length);")
            sb.append("},")
            sb.append("error: function (request, status, error) {")
            sb.append("alert(error)")
            sb.append("},")
            sb.append("complete: function () {}});};")
            sb.append("function setAttachLink(id, fileName) {")
            sb.append("\$('#annexToAttach').text(fileName);")
            sb.append("\$('#annexToAttach').prepend(\"<span class='fa fa-link'></span> \");")
            sb.append("\$('#annexToAttach').attr('href', '/annexable/attachAnnex?domainName=${bean.class.name}&domainId=${bean.id}&annexId='+id);};")
            sb.append("</script>")

            out << sb.toString()
        }
    }

    def upload = { attrs, body ->
        def bean = attrs.remove('bean')
        def bucket = attrs.remove('bucket') ?: 'common'
        def controller = attrs.remove('controller') ?: 'annexable'
        def uploadAnnexId = attrs.remove('uploadAnnexId')
        final StringBuilder sb = new StringBuilder()

        sb.append("<form id='uploadAnnexForm' class='MultiFile-intercepted annex-upload-inline' enctype='multipart/form-data' name='uploadAnnexForm' method='post' action='/${controller}/uploadAnnex'>")
        sb.append("<input id='uploadBucket' type='hidden' value='${bucket}' name='uploadBucket'>")
        sb.append("<input id='domainName' type='hidden' value='${bean?.class.name}' name='domainName'>")
        sb.append("<input id='domainId' type='hidden' value='${bean?.ident()}' name='domainId'>")
        sb.append("<input id='uploadAnnexId' type='hidden' value='${uploadAnnexId}' name='uploadAnnexId'>")
        sb.append("<label class='btn btn-default btn-file'>")
        sb.append("<span class='fa fa-upload'>")
        sb.append("<input class='upload-input' type='file' name='uploadFile' id='uploadFile' style='display: none;'>")
        sb.append("</span>")
        sb.append("</label>")
        sb.append("</form>")
        sb.append("<script>")
        sb.append("\$(document).ready(function () {")
        sb.append("\$('.upload-input').on('change', function () {")
        sb.append("\$('#uploadAnnexForm').submit();")
        sb.append("});")
        sb.append("});")
        sb.append("</script>")

        out << sb.toString()
    }

    def download = { attrs, body ->
        def annex = attrs.remove('annex')
        def controller = attrs.remove('controller') ?: 'annexable'
        final StringBuilder sb = new StringBuilder()

        if(annex) {
            sb.append("<div class='btn-group'>")
            sb.append("<button type='button' class='btn btn-default'><span class='fa fa-download'></span></button>")
            sb.append("<button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>")
            sb.append("<span class='caret'></span>")
            sb.append("<span class='sr-only'>Toggle Dropdown</span>")
            sb.append("</button>")
            sb.append("<ul class='dropdown-menu'>")
            for (int i = 0; i <= annex.fileVersion; i++) {
                def downloadLink = g.createLink(controller: controller, action: 'downloadAnnex', params: ['annexId': annex.id, 'version': i])
                sb.append("<li><a href='${downloadLink}'>version ${i}</a></li>")
            }
            sb.append("</ul>")
            sb.append("</div>")
        }

        out << sb.toString()
    }

    def show = { attrs, body ->
        def annexId = attrs.remove('annexId')
        def controller = attrs.remove('controller') ?: 'annexable'
        def iframeId = attrs.remove('iframeId') ?: 'showAnnexiFrame'
        final StringBuilder sb = new StringBuilder()

        sb.append("<a class='btn btn-default' href='#' onClick='showAnnex(${annexId})'><span class='fa fa-eye'></span></a>")
        sb.append("<script>")
        sb.append("function showAnnex(id) {")
        sb.append("\$('#${iframeId}').attr('src', '/${controller}/showAnnex?annexId='+id);};")
        sb.append("</script>")

        out << sb.toString()
    }

    def attach = { attrs, body ->
        def annexId = attrs.remove('annexId')
        def controller = attrs.remove('controller') ?: 'annexable'
        def bean = attrs.remove('bean')
        def fileName = attrs.remove('fileName') ?: ''
        final StringBuilder sb = new StringBuilder()

        sb.append("<a class='btn btn-default' id='annexToAttach' name='annexToAttach' href='/${controller}/attachAnnex?domainName=${bean?.class?.name}&domainId=${bean?.id}&annexId=${annexId}'><span class='fa fa-link'></span> ${fileName}</a>")

        out << sb.toString()
    }

    def detach = { attrs, body ->
        def annexId = attrs.remove('annexId')
        def controller = attrs.remove('controller') ?: 'annexable'
        def bean = attrs.remove('bean')
        final StringBuilder sb = new StringBuilder()

        def detachLink = g.createLink(controller: controller, action: 'detachAnnex', params: ['annexId': annexId, 'domainName': bean.class.name, 'domainId': bean.ident()])
        sb.append("<a class='btn btn-default' href='${detachLink}'><span class='fa fa-unlink'></span></a>")

        out << sb.toString()
    }

    static final Map MIME_ICON_MAP = [
            pdf: 'fa-file-pdf-o',
            doc: 'fa-file-word-o',
            docx: 'fa-file-word-o',
            odt: 'fa-file-word-o',
            xls: 'fa-file-excel-o',
            xlsx: 'fa-file-excel-o',
            ods: 'fa-file-excel-o',
            ppt: 'fa-file-powerpoint-o',
            gif: 'fa-file-image-o',
            png: 'fa-file-image-o',
            jpg: 'fa-file-image-o',
            jpeg: 'fa-file-image-o',
            bmp: 'fa-file-image-o',
            tif: 'fa-file-image-o',
            mov: 'fa-file-video-o',
            wav: 'fa-file-audio-o',
            mp3: 'fa-file-audio-o',
            raw: 'fa-file-video-o',
            txt: 'fa-file-text-o',
            zip: 'fa-file-archive-o',
            xml: 'fa-file-code-o',
            htm: 'fa-file-code-o',
            html: 'fa-file-code-o',
            groovy: 'fa-file-code-o',
            php: 'fa-file-code-o',
            java: 'fa-file-code-o'
    ]

    private String getIconByExtension(String extension) {
        MIME_ICON_MAP[extension?.toLowerCase()] ?: 'fa fa-file-o'
    }
}
