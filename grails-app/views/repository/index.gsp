<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Welcome to Grails</title>

    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
    %{--<script src="//code.jquery.com/jquery-1.11.0.min.js"></script>
    <script src="//netdna.bootstrapcdn.com/bootstrap/3.3.2/js/bootstrap.min.js"></script>--}%

    <asset:stylesheet src="style.css"></asset:stylesheet>
    <script type="text/javascript" src="/assets/jquery-2.2.0.min.js?compile=false"></script>
    <link href="https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css" rel="stylesheet" integrity="sha384-wvfXpqpZZVQGK6TAh5PVlGOfQNHSoD2xbE+QkPxCAFlNEevoEH3Sl0sibVcOQVnN" crossorigin="anonymous">

</head>
<body>
<script>
    function fileClicked(attachmentId) {
        $.ajax({
            url: "${g.createLink(controller:'repository',action:'getAttachmentInfo')}",
            dataType: 'json',
            data: {
                attachmentId: attachmentId,
                fileName: '',
                bucket: ''
            },
            success: function (data) {
                //alert(data.fileName)
                var newDiv = '<div class="well"><div class="row" style="padding: 0px;"><div class="col-md-6">';
                $('#fileName').text(data.fileName);
                $('#bucket').text(data.bucket);
                $('#size').text(data.size);

                $('#extension').text(data.extension);
                $('#contentType').text(data.contentType);
                $('#createdBy').text(data.createdBy);
                $('#createdAt').text(data.createdAt);
                $('#editedBy').text(data.editedBy);
                $('#editedAt').text(data.editedAt);
                $('#isDeleted').text(data.isDeleted);
                $('#moveToTrash').attr("href","/repository/moveToTrash?attachmentId=" + data.id)
                $('#downloadAttachmentDropdown').empty();
                for (var i = 0; i < data.versions; i++) {
                    $('#downloadAttachmentDropdown').append('<li><a href="/repository/downloadAttachmentFile?attachmentId=' + data.id + '&amp;versionToDownload='+i + '">Version '+ (i+1) + '</a></li>');
                }
                $('#uploadAttachmentId').val(data.id)
                $('#uploadBucket').val(data.bucket)

                //newDiv += '<p><strong>Name: </strong>' + data.fileName + '</p>';
                newDiv += '<p><strong>Bucket: </strong>' + data.bucket + '</p>';
                newDiv += '<p><strong>Size: </strong>' + data.size + '</p>';
                newDiv += '<p><strong>Extension: </strong>' + data.extension + '</p>';
                newDiv += '<p><strong>Type: </strong>' + data.contentType + '</p>';
                newDiv += '<p><strong>Created by: </strong>' + data.createdBy + '</p>';
                newDiv += '<p><strong>Created at: </strong>' + data.createdAt + '</p>';
                newDiv += '<p><strong>Edited by: </strong>' + data.editedBy + '</p>';
                newDiv += '<p><strong>Edited at: </strong>' + data.editedAt + '</p>';
                newDiv += '<p><strong>Deleted: </strong>' + data.isDeleted + '</p></div>';
                newDiv += '<div class="col-md-6">';
                newDiv += '<p><strong>Versions to download</strong></p>';
                newDiv += '<div class="btn-group">';
                newDiv += '<button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">';
                newDiv += '<span class="fa fa-download" style="margin-right: 5px;"></span><span class="caret"></span></button>';
                newDiv += '<ul class="dropdown-menu">';
                for (var i = 0; i < data.versions; i++) {//, params:[attachmentId: data.id, versionToDownload:i]
                    var attrs = {
                        'controller': 'repository',
                        'action': 'downloadAttachmentFile',
                        'params': {
                            'attachmentId': data.id,
                            'versionToDownload':i
                        }
                    };
                    newDiv += '<li><a href="/repository/downloadAttachmentFile?attachmentId=' + data.id + '&amp;versionToDownload='+i + '">Version '+ (i+1) + '</a></li>';
                }
                newDiv += '</ul></div>';
                newDiv += '<p><strong>Actions on file</strong></p>';
                newDiv += '<p>';
                newDiv += '<a href="/repository/moveToTrash?attachmentId=' + data.id + '" class="btn btn-danger" role="button" style="margin: 5px;"><span class="fa fa-trash"></a>';//move to trash
                newDiv += '<a href="#" class="btn btn-primary" role="button" style="margin: 5px;"><span class="fa fa-upload"></a>';//upload
                newDiv += '<label class="btn btn-default btn-file"><span class="fa fa-upload"><input type="file" name="file" id="file" style="display: none;"></label>';
                newDiv += '<a href="#" class="btn btn-primary" role="button" style="margin: 5px;"><span class="fa fa-folder-open"></a>';//move to other bucket
                newDiv += '<a href="#" class="btn btn-primary" role="button" style="margin: 5px;"><span class="fa fa-paperclip"></a>';//edit attachment domain object
                newDiv += '<a href="#" class="btn btn-primary" role="button" style="margin: 5px;"><span class="fa fa-link"></a>';//link to domain
                //newDiv += '';
                newDiv += '</p>';
                newDiv += '</div></div></div>';
                //$('#file-description').html(newDiv);

            },
            error: function (request, status, error) {
                alert(error)
            },
            complete: function () {
            }
        });
    }
</script>

<content tag="nav">
    <li class="dropdown">
        <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Application Status <span class="caret"></span></a>
        <ul class="dropdown-menu">
            <li><a href="#">Environment: ${grails.util.Environment.current.name}</a></li>
            <li><a href="#">App profile: ${grailsApplication.config.grails?.profile}</a></li>
            <li><a href="#">App version:
                <g:meta name="info.app.version"/></a>
            </li>
            <li role="separator" class="divider"></li>
            <li><a href="#">Grails version:
                <g:meta name="info.app.grailsVersion"/></a>
            </li>
            <li><a href="#">Groovy version: ${GroovySystem.getVersion()}</a></li>
            <li><a href="#">JVM version: ${System.getProperty('java.version')}</a></li>
            <li role="separator" class="divider"></li>
            <li><a href="#">Reloading active: ${grails.util.Environment.reloadingAgentEnabled}</a></li>
        </ul>
    </li>
    <li class="dropdown">
        <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Artefacts <span class="caret"></span></a>
        <ul class="dropdown-menu">
            <li><a href="#">Controllers: ${grailsApplication.controllerClasses.size()}</a></li>
            <li><a href="#">Domains: ${grailsApplication.domainClasses.size()}</a></li>
            <li><a href="#">Services: ${grailsApplication.serviceClasses.size()}</a></li>
            <li><a href="#">Tag Libraries: ${grailsApplication.tagLibClasses.size()}</a></li>
        </ul>
    </li>
    <li class="dropdown">
        <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Installed Plugins <span class="caret"></span></a>
        <ul class="dropdown-menu">
            <g:each var="plugin" in="${applicationContext.getBean('pluginManager').allPlugins}">
                <li><a href="#">${plugin.name} - ${plugin.version}</a></li>
            </g:each>
        </ul>
    </li>
</content>

<div class="svg" role="presentation">
    <div class="grails-logo-container">
        <asset:image src="grails-cupsonly-logo-white.svg" class="grails-logo"/>
    </div>
</div>

<div id="content" role="main">
    <section class="row colset-2-its">
        <h1>Welcome to Grails</h1>

        <p>
            Congratulations, you have successfully started your first Grails application! At the moment
            this is the default page, feel free to modify it to either redirect to a controller or display
            whatever content you may choose. Below is a list of controllers that are currently deployed in
            this application, click on each to execute its default action:
        </p>

        <div id="controllers" role="navigation">
            <h2>Available Controllers:</h2>
            <ul>
                <g:each var="c" in="${grailsApplication.controllerClasses.sort { it.fullName } }">
                    <li class="controller">
                        <g:link controller="${c.logicalPropertyName}">${c.fullName}</g:link>
                    </li>
                </g:each>
            </ul>
        </div>
    </section>

</div>

<div class="container" style="margin-top:30px;">
    <div class="row">
        <div class="col-md-5 col-sm-12">
            <p class="well" style="height:135px;"><strong>File: </strong>

                <br /> <code>$('#tree').treed({openedClass : 'fa fa-folder-open-o', closedClass : 'fa fa-folder-o'});</code>

            </p>
            <ul id="tree">
                <li><a href="#">REPOSITORY ROOT <span class="badge">${attachmentList.size()}</span></a>

                    <ul>
                        <g:each var="bucket" in="${buckets}">
                            <li><a href="#">${bucket.key}  <span class="badge">${bucket.value.size()}</span></a>
                                <ul>
                                    <g:each var="attachment" in="${bucket.value}">
                                        <li><a href="#" onclick="fileClicked(${attachment.id})">${attachment.fileName}</a></li>
                                    </g:each>
                                </ul>
                            </li>
                        </g:each>
                    </ul>
                </li>
                <li>XRP
                    <ul>
                        <li>Company Maintenance</li>
                        <li>Employees
                            <ul>
                                <li>Reports
                                    <ul>
                                        <li>Report1</li>
                                        <li>Report2</li>
                                        <li>Report3</li>
                                        <li><g:link controller="repository" action="downloadAttachmentFile" params="[sort: 'title', order: 'asc']">Test linku</g:link></li>
                                    </ul>
                                </li>
                                <li>Employee Maint.</li>
                            </ul>
                        </li>
                        <li>Human Resources</li>
                    </ul>
                </li>
            </ul>
        </div>
        <div class="col-md-7 col-sm-12">
            <div id="file-description" class="file-description affix">
                <div class="well">
                    <div class="row" style="padding: 0px;">
                        <div class="col-md-8">
                            <dl>
                                <dt>Name</dt>
                                <dd id="fileName"></dd>
                                <dt>Bucket</dt>
                                <dd id="bucket"></dd>
                                <dt>Size</dt>
                                <dd id="size"></dd>
                                <dt>Extension</dt>
                                <dd id="extension"></dd>
                                <dt>Type</dt>
                                <dd id="contentType"></dd>
                                <dt>Created by</dt>
                                <dd id="createdBy"></dd>
                                <dt>Created at</dt>
                                <dd id="createdAt"></dd>
                                <dt>Edited by</dt>
                                <dd id="editedBy"></dd>
                                <dt>Edited at</dt>
                                <dd id="editedAt"></dd>
                                <dt>Deleted</dt>
                                <dd id="isDeleted"></dd>
                            </dl>
                        </div>
                        <div class="col-md-4">
                            <p>
                                <strong>Versions to download
                                </strong>
                            </p>
                            <div class="btn-group">
                                <button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                    <span class="fa fa-download" style="margin-right: 5px;">
                                    </span>
                                    <span class="caret">
                                    </span>
                                </button>
                                <ul id="downloadAttachmentDropdown" class="dropdown-menu" >
                                    %{--<li>
                                        <a href="/repository/downloadAttachmentFile?attachmentId=1&amp;versionToDownload=0">
                                            Version 1
                                        </a>
                                    </li>--}%
                                </ul>
                            </div>
                            <p>
                                <strong>
                                    Upload new version
                                </strong>
                            </p>
                            <p>
                                <form id="uploadForm" class="MultiFile-intercepted" enctype="multipart/form-data" name="uploadForm" method="post" action="/repository/uploadAttachment">
                                    <input id="uploadAttachmentId" type="hidden" value="" name="uploadAttachmentId">
                                    <input id="uploadBucket" type="hidden" value="" name="uploadBucket">
                                    <label class="btn btn-default btn-file">
                                        <span class="fa fa-upload">
                                            <input type="file" name="uploadFile" id="uploadFile" style="display: none;">
                                        </span>
                                    </label>
                                </form>
                            </p>
                            <p>
                                <strong>
                                    Actions on file
                                </strong>
                            </p>
                            <p>
                                <a id="moveToTrash" href="#" class="btn btn-danger" role="button" style="margin: 5px;">
                                    <span class="fa fa-trash">
                                    </span>
                                </a>
                                <a href="#" class="btn btn-primary" role="button" style="margin: 5px;">
                                    <span class="fa fa-upload">
                                    </span>
                                </a>
                                <label class="btn btn-default btn-file">
                                    <span class="fa fa-upload">
                                        <input type="file" name="file" id="file" style="display: none;">
                                    </span>
                                </label>
                                <a href="#" class="btn btn-primary" role="button" style="margin: 5px;">
                                    <span class="fa fa-folder-open">
                                    </span>
                                </a>
                                <a href="#" class="btn btn-primary" role="button" style="margin: 5px;">
                                    <span class="fa fa-paperclip">
                                    </span>
                                </a>
                                <a href="#" class="btn btn-primary" role="button" style="margin: 5px;">
                                    <span class="fa fa-link">
                                    </span>
                                </a>
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<asset:javascript src="main.js"></asset:javascript>
<script>
    $(document).ready(function () {\
        $('#uploadFile').on('change', function () {
            console.log('#uploadFile changed');
            $('#uploadForm').submit();
        });
    });
</script>
</body>

</html>
