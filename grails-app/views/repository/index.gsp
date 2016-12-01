<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Welcome to Grails</title>

    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
    <script type="text/javascript" src="/assets/jquery-2.2.0.min.js?compile=false"></script>
    <link href="https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css" rel="stylesheet" integrity="sha384-wvfXpqpZZVQGK6TAh5PVlGOfQNHSoD2xbE+QkPxCAFlNEevoEH3Sl0sibVcOQVnN" crossorigin="anonymous">
    <style>
    .tree, .tree ul {
        margin:0;
        padding:0;
        list-style:none
    }
    .tree ul {
        margin-left:1em;
        position:relative
    }
    .tree ul ul {
        margin-left:.5em
    }
    .tree ul:before {
        content:"";
        display:block;
        width:0;
        position:absolute;
        top:0;
        bottom:0;
        left:0;
        border-left:1px solid
    }
    .tree li {
        margin:0;
        padding:0 1em;
        line-height:2em;
        color:#369;
        font-weight:700;
        position:relative
    }
    .tree ul li:before {
        content:"";
        display:block;
        width:10px;
        height:0;
        border-top:1px solid;
        margin-top:-1px;
        position:absolute;
        top:1em;
        left:0
    }
    .tree ul li:last-child:before {
        background:#fff;
        height:auto;
        top:1em;
        bottom:0
    }
    .indicator {
        margin-right:5px;
    }
    .tree li a {
        text-decoration: none;
        color:#369;
    }
    .tree li button, .tree li button:active, .tree li button:focus {
        text-decoration: none;
        color:#369;
        border:none;
        background:transparent;
        margin:0px 0px 0px 0px;
        padding:0px 0px 0px 0px;
        outline: 0;
    }

    /*dt, dd {
        font-family:sans-serif;
    }*/
    dt {
        float:left;
        clear:left;
        text-align:right;
        width:25%;
        /*color:#bbb;*/
    }
    dd {
        float:left;
        margin-left:3em;
        /*color:#999*/
    }
    </style>
</head>
<body>
<script>
    function fileClicked(annexId) {
        $.ajax({
            url: "${g.createLink(controller:'repository',action:'getAnnexInfo')}",
            dataType: 'json',
            data: {
                annexId: annexId,
                fileName: '',
                bucket: ''
            },
            success: function (data) {
                //alert(data.fileName)
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
                $('#moveToTrash').attr("href","/repository/moveToTrash?annexId=" + data.id)
                $('#downloadAnnexDropdown').empty();
                for (var i = 0; i < data.versions; i++) {
                    $('#downloadAnnexDropdown').append('<li><a href="/repository/downloadAnnexFile?annexId=' + data.id + '&amp;versionToDownload='+i + '">Version '+ (i+1) + '</a></li>');
                }
                $('#uploadAnnexId').val(data.id)
                $('#uploadBucket').val(data.bucket)

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
                <li><a href="#">REPOSITORY ROOT</a>
                    <ul>
                        <g:each var="bucket" in="${buckets}">
                            <li><a href="#">${bucket.key}</a>
                                <ul>
                                    <g:each var="annex" in="${bucket.value}">
                                        <li><a href="#" onclick="fileClicked(${annex.id})">${annex.fileName}</a></li>
                                    </g:each>
                                </ul>
                            </li>
                        </g:each>
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
                                <ul id="downloadAnnexDropdown" class="dropdown-menu" >
                                    %{--<li>
                                        <a href="/repository/downloadAnnexFile?annexId=1&amp;versionToDownload=0">
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
                                <form id="uploadForm" class="MultiFile-intercepted" enctype="multipart/form-data" name="uploadForm" method="post" action="/repository/uploadAnnex">
                                    <input id="uploadAnnexId" type="hidden" value="" name="uploadAnnexId">
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
%{--<asset:javascript src="main.js"></asset:javascript>--}%
<script>
    $.fn.extend({
        treed: function (o) {

            var openedClass = 'fa-folder-open-o';
            var closedClass = 'fa-folder-o';

            if (typeof o != 'undefined'){
                if (typeof o.openedClass != 'undefined'){
                    openedClass = o.openedClass;
                }
                if (typeof o.closedClass != 'undefined'){
                    closedClass = o.closedClass;
                }
            };

            //initialize each of the top levels
            var tree = $(this);
            tree.addClass("tree");
            tree.find('li').has("ul").each(function () {
                var branch = $(this); //li with children ul
                branch.prepend("<i class='indicator fa " + closedClass + "'></i>");
                branch.addClass('branch');
                branch.on('click', function (e) {
                    if (this == e.target) {
                        var icon = $(this).children('i:first');
                        icon.toggleClass(openedClass + " " + closedClass);
                        $(this).children().children().toggle();
                    }
                })
                branch.children().children().toggle();
            });
            //fire event from the dynamically added icon
            tree.find('.branch .indicator').each(function(){
                $(this).on('click', function () {
                    $(this).closest('li').click();
                });
            });
            //fire event to open branch if the li contains an anchor instead of text
            tree.find('.branch>a').each(function () {
                $(this).on('click', function (e) {
                    $(this).closest('li').click();
                    e.preventDefault();
                });
            });
            //fire event to open branch if the li contains a button instead of text
            tree.find('.branch>button').each(function () {
                $(this).on('click', function (e) {
                    $(this).closest('li').click();
                    e.preventDefault();
                });
            });
        }
    });

    /*$.fn.extend({
        fileClicked: function (o) {
            $.ajax({
                url:"${g.createLink(controller:'repository',action:'getAnnexInfo')}",
                dataType: 'json',
                data: {
                    annexId: 1,
                    fileName: '',
                    bucket: ''
                },
                success: function(data) {
                    alert(data)
                },
                error: function(request, status, error) {
                    alert(error)
                },
                complete: function() {
                }
            });

        }
    });*/

    $('#tree').treed({openedClass: 'fa-folder-open-o', closedClass: 'fa-folder-o'});

    $(document).ready(function () {
        $('#uploadFile').on('change', function () {
            $('#uploadForm').submit();
        });
    });
</script>
</body>

</html>
