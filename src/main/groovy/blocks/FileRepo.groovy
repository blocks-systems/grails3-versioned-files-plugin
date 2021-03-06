package blocks

import grails.util.Holders
import org.apache.commons.logging.LogFactory
import blocks.exceptions.UnavailableFileSystemException

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
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
        if (!cfg.bucket) {
            cfg.bucket = 'common'
        }
    }

    private static def getConfig() {
        Holders.config?.annexable
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

    protected static def uploadFile(Annex annex, Map params=[:]) {
        uploadFile(annex, params, false)
    }

    /**
     *
     * @param annex Annex data to upload
     * @param params map of params like version, annex, bucket or file.
     * If not present data will should exists in annex object
     * @return changed annex object
     */
    protected static def uploadFile(Annex annex, Map params=[:], boolean copyFile) {
        if (!isInitialized()) {
            init()
        }
        params.version = params.version ?: annex.fileVersion
        if (!params.bucket) {
            if (annex.bucket) {
                params.bucket = annex.bucket
            } else {
                params.bucket = cfg.bucket
            }
        }

        params.annexId = params.annexId ?: annex.id
        params.file = params.file ?: annex.file
        //params.version = params.version ?: 0
        if (!annex.contentType && params?.file?.contentType) {
            annex.contentType = params.file.contentType
        }
        Long fileSize = 0
        if (copyFile) {
            fileSize = copyFileToRepo(params)
        } else {
            fileSize = saveFileToRepo(params)
        }
        annex.length = fileSize
        annex
    }

    /**
     *  Method for get/download annex file
     * @param annex
     * @param params Additionals parameters like versionToDownload
     * If not set latest version will be downloaded
     * @return File from disk
     */
    protected static def getFile(Annex annex, Map params=[:]) {
        if (!isInitialized()) {
            init()
        }
        Long versionToDownload = params.versionToDownload ?: annex.fileVersion
        Path filePath = Paths.get(repoDir.toString(), annex.bucket)
        if (Files.notExists(filePath)) {
            filePath = Files.createDirectory(filePath)
        }
        String diskFileName = "${annex.id}" + VERSION_SEPARTOR + "${versionToDownload}"
        /////
        Path file = Paths.get(filePath.toString(), diskFileName)
        if (Files.notExists(file)) {
            throw new Exception("File not found")
        }
        annex.file = file
        file
    }

    /**
     *  Method move files to trash directory
     * @param params - bucket, last version and id of annex
     * @return true if succeed, otherwise false
     */
    protected static def moveToTrash(Map params) {
        if (!isInitialized()) {
            init()
        }
        boolean ret = false
        if (params.annexId && params.bucket) {
            Path filePath = Paths.get(repoDir.toString(), params.bucket)
            if (Files.notExists(filePath)) {
                return false
            }
            Long lastVersion = params.version ?: 0
            for (int i = 0; i <= lastVersion.intValue(); i++) {
                ret = ret | moveFileToTrash(params.annexId, i, filePath)
            }
        }
        ret
    }

    /**
     * Methods to writes file to disk
     * @param params like file, bucket, annex id and version
     * param file must be a StandardMultipartFile type
     * @return size of the file
     */
    private static Long saveFileToRepo(Map params) {
        String diskFileName = "${params.annexId}" + VERSION_SEPARTOR + "${params.version}"
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
            throw e
        }
        file.toFile().length()
    }

    private static Long copyFileToRepo(Map params) {
        String diskFileName = "${params.annexId}" + VERSION_SEPARTOR + "${params.version}"
        Path filePath = Paths.get(repoDir.toString(), params.bucket)
        if (Files.notExists(filePath)) {
            filePath = Files.createDirectory(filePath)
        }
        Path file = Paths.get(filePath.toString(), diskFileName)
        def f = params.file
        file = Files.createFile(file)

        try {
            Files.copy(f.toPath(), file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error(e)
            throw e
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

    /**
     * Method to clear the trash bin
     * @return true if operation success, false otherwise
     */
    protected static boolean  emptyTrash() {
        boolean ret = false
        Files.walk(trashDir).forEach(
                {
                    it->
                        if (it != trashDir) {
                            ret = ret & removeFile(it.toAbsolutePath())
                        }
                }
        )
        ret
    }

    /**
     * Method removes file
     * @param filePath
     * @return true if operation success, false otherwise
     */
    private static boolean removeFile(Path filePath) {
        Files.deleteIfExists(filePath)
    }
}
