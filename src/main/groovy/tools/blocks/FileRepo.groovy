package tools.blocks

import grails.util.Holders
import org.apache.commons.logging.LogFactory
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest
import tools.blocks.exceptions.UnavailableFileSystemException

import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

/**
 * Created by fgroch on 08.11.16.
 */
class FileRepo {
    private static final log = LogFactory.getLog(FileRepo.class)
    private static def cfg = [:]
    protected static Path repoDir
    protected static Path trashDir
    protected  static final String VERSION_SEPARTOR = "#"

    private static def init() {
        cfg = config
        log.info(cfg)
        initRepoStructure()
    }

    private static def getConfig() {
        Holders.config?.gvfp
    }

    protected static isInitialized() {
        repoDir != null && trashDir != null && Files.exists(repoDir) && Files.exists(trashDir)
    }

    protected static def initRepoStructure() {
        String repoPath = cfg.repositoryPath
        Path rootPath = Paths.get(repoPath)
        if (Files.notExists(rootPath)) {
            try {
                rootPath = Files.createDirectories(rootPath)
            } catch (IOException e) {
                log.error(e)
                throw new UnavailableFileSystemException()
            }
        }
        repoDir = rootPath
        Path trashPath = Paths.get(repoPath, "trash")
        if (Files.notExists(trashPath)) {
            try {
                trashPath = Files.createDirectory(trashPath)
            } catch (IOException e) {
                log.error(e)
                throw new UnavailableFileSystemException('Cannot create Thrash directory')
            }
        }
        trashDir = trashPath
    }

    /**
     *
     * @param attachment Attachment data to upload
     * @param params map of params like version, attachment, bucket or file.
     * If not present data will should exists in attachment object
     * @return changed attachment object
     */
    protected static def uploadFile(Attachment attachment, Map params=[:]) {
        if (!isInitialized()) {
            init()
        }
        params.version = params.version ?: attachment.version
        params.bucket = params.bucket ?: attachment.bucket
        params.attachmentId = params.attachmentId ?: attachment.id
        params.file = params.file ?: attachment.file
        attachment.contentType = params.file.contentType
        Long fileSize = saveFileToRepo(params)
        attachment.length = fileSize
        attachment
    }

    /**
     *  Method for get/download attachment file
     * @param attachment
     * @param params Additionals parameters like versionToDownload
     * If not set latest version will be downloaded
     * @return File from disk
     */
    protected static def getFile(Attachment attachment, Map params=[:]) {
        if (!isInitialized()) {
            init()
        }
        Long versionToDownload = params.versionToDownload ?: attachment.version
        Path filePath = Paths.get(repoDir.toString(), attachment.bucket)
        if (Files.notExists(filePath)) {
            filePath = Files.createDirectory(filePath)
        }
        String diskFileName = "${attachment.id}" + VERSION_SEPARTOR + "${versionToDownload}"
        /////
        Path file = Paths.get(filePath.toString(), diskFileName)
        if (Files.notExists(file)) {
            throw new Exception("File not found")
        }
        attachment.file = file
        file
    }

    /**
     *  Method move files to trash directory
     * @param params - bucket, last version and id of attachment
     * @return true if succeed, otherwise false
     */
    protected static def moveToTrash(Map params) {
        if (!isInitialized()) {
            init()
        }
        boolean ret = false
        if (params.attachmentId && params.bucket) {
            Path filePath = Paths.get(repoDir.toString(), params.bucket)
            if (Files.notExists(filePath)) {
                return false
            }
            Long lastVersion = params.version ?: 0
            for (int i = 0; i <= lastVersion.intValue(); i++) {
                ret = ret | moveFileToTrash(params.attachmentId, i, filePath)
            }
        }
        ret
    }

    /**
     * Methods to writes file to disk
     * @param params like file, bucket, attachment id and version
     * param file must be a StandardMultipartFile type
     * @return size of the file
     */
    private static Long saveFileToRepo(Map params) {
        String diskFileName = "${params.attachmentId}" + VERSION_SEPARTOR + "${params.version}"
        Path filePath = Paths.get(repoDir.toString(), params.bucket)
        if (Files.notExists(filePath)) {
            filePath = Files.createDirectory(filePath)
        }
        Path file = Paths.get(filePath.toString(), diskFileName)
        def f = params.file //as StandardMultipartHttpServletRequest.StandardMultipartFile
        file = Files.createFile(file)
        try {
            OutputStream out = new BufferedOutputStream(
                Files.newOutputStream(file, StandardOpenOption.CREATE, StandardOpenOption.APPEND))
            out.write(f.bytes, 0, f.bytes.length);
        } catch (IOException e) {
            log.error(e)
        }
        file.toFile().length()
    }

    private static boolean moveFileToTrash(def id, def version, Path filePath) {
        moveFile(id, version, filePath, trashDir)
    }


    /**
     * Method for move files to trash directory
     * @param id
     * @param version
     * @param filePath
     * @param targetPath
     * @return
     */
    private static boolean moveFile(def id, def version, Path filePath, Path targetPath) {
        boolean moved = false
        String diskFileName = "${id}" + VERSION_SEPARTOR + "${version}"
        Path file = Paths.get(filePath.toString(), diskFileName)
        Path destinationFile  = Paths.get(targetPath.toString(), diskFileName)
        try {
            Files.move(file, destinationFile)
            moved = true
        } catch (IOException e) {
            log.info(e.stackTrace)
        }
        moved
    }
}
