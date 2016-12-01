package tools.blocks

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

    def annexesDomainPanel = { attrs, body ->
        def bean = attrs.remove('bean')
        def controller = attrs.remove('controller')
        controller = controller ?: 'annexable'

        if (bean?.metaClass?.hasProperty(bean, 'annexes') != null) {
            def inputName = attrs.remove('name') ?: 'annex'
            def wrapper = inputName + 'Wrapper'
            //html
            final StringBuilder sb = new StringBuilder()
            //def findLink = g.createLink(controller: 'annexable', action: 'showAnnex', params:['annexId':annex.id])
            sb.append("<div class='container content' id='$wrapper'>")
            sb.append("<div class='row' style='padding-bottom:20px;'>")
            sb.append("<div class='col-lg-4'>")
            sb.append("<a class='btn btn-default' href='#' style='margin-left:15px;'><span class='fa fa-trash'></span></a>")
            sb.append("<a class='btn btn-default' href='#'><span class='fa fa-upload'></span></a>")
            sb.append("</div>")
            sb.append("<div class='col-lg-6 col-lg-offset-2' style='padding-right: 0px;'>")
            sb.append("<div class='input-group'>")
            sb.append("<div class='input-group-btn'>")
            sb.append("<a class='btn btn-default' href='#' id='annexToAttach' name = 'annexToAttach'><span class='fa fa-link'></span> name of the file</a>")
            sb.append("<button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>")
            sb.append("<span class='caret'></span>")
            sb.append("<span class='badge'>42</span>")
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
                def annexLink = g.createLink(controller: 'annexable', action: 'showAnnex', params:['annexId':annex.id])

                def detachLink = g.createLink(controller: 'annexable', action: 'detachAnnex', params:['annexId':annex.id, 'domainName':bean.class.name, 'domainId':bean.ident()])
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
                sb.append("<p style='padding-right: 20px;'>${annex.length} <span class='fa fa-balance-scale'></span></p>")
                sb.append("<p style='padding-right: 20px;'>${annex.bucket} <span class='fa fa-folder-o'></span></p>")
                sb.append("<p style='padding-right: 20px;'>${annex.version} <span class='fa fa-code-fork'></span></p>")
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
                for (int i = 0; i <= annex.version; i++) {
                    def downloadLink = g.createLink(controller: 'annexable', action: 'downloadAnnex', params:['annexId':annex.id, 'version':i])
                    sb.append("<li><a href='${downloadLink}'>version ${i}</a></li>")
                }
                sb.append("</ul>")
                sb.append("</div>")
                sb.append("<a class='btn btn-default' href='#'><span class='fa fa-eye'></span></a>")
                sb.append("<a class='btn btn-default' href='${detachLink}'><span class='fa fa-unlink'></span></a>")
                sb.append("<a class='btn btn-default' href='#'><span class='fa fa-upload'></span></a>")
                sb.append("</div>")
                sb.append("</div>")
                sb.append("</div>")
                sb.append("</div>")
                sb.append("</div>")
            }
            sb.append("<div class='col-lg-6 annex-preview'>")
            sb.append("<iframe src='/annexable/showAnnex?annexId=22' style='height:425px; width:100%;' frameborder='0'></iframe>")
            sb.append("</div>")
            sb.append("</div>")
            sb.append("</div>")

            out << sb.toString()
        }
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
