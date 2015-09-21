package net.wisefam;

import net.wisefam.data.FoundFile;
import net.wisefam.data.FoundFileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Component
public class FileCopier implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FileCopier.class);

    private static final String ENCRYPTED_FILE_EXT = ".qskkhze";
    private static final FilenameFilter ENCRYPTED_FILES_FILTER = new WildcardFileFilter("*" + ENCRYPTED_FILE_EXT, IOCase.INSENSITIVE);
    private static final String COMPUTER_NAME = getComputerName();
    private static final String BACKUPPC_PC_ROOT = "/wisefam/big/backuppc/pc/";

    @Autowired
    private FoundFileRepository foundFileRepository;

    @Override
    public void run(String... args) throws Exception {
//        LineNumberReader reader = new LineNumberReader(new FileReader(new File("files.html")));

        File[] roots;
        if (args.length > 0) {
            roots = new File[args.length];
            for (int i = 0; i < roots.length; i++) {
                roots[i] = new File(args[i]);
            }
        } else {
            roots = File.listRoots();
        }
        if (roots != null) {
            for (File root : roots) {
                addAllFilesRecursively(root);
                log.info("Done finding files in root {} on {}", root, COMPUTER_NAME);
            }
        }
        log.info("Done finding files on {}", COMPUTER_NAME);

    }

    private void addAllFilesRecursively(File directory) throws IOException {
        try (Stream<Path> stream = Files.find(directory.toPath(), Integer.MAX_VALUE,
                (path, attr) ->
                        attr.isRegularFile() && StringUtils.endsWithIgnoreCase(path.toString(), ENCRYPTED_FILE_EXT)
        )) {
            stream.forEach(path -> {
                File file = path.toFile();
                String directoryPath = path.getParent().toString();
                String currentFileName = path.getFileName().toString();
                String actualFileName = StringUtils.removeEndIgnoreCase(currentFileName, ENCRYPTED_FILE_EXT);
                File unencryptedFile = new File(directoryPath, actualFileName);
                String extension = StringUtils.lowerCase(FilenameUtils.getExtension(actualFileName));
                boolean needsRestored = !unencryptedFile.exists() || unencryptedFile.lastModified() == file.lastModified();

                FoundFile foundFile = foundFileRepository.findByPathAndFileNameAndComputerName(directoryPath, actualFileName, COMPUTER_NAME);
                if (foundFile == null) {
                    foundFile = new FoundFile(directoryPath, actualFileName, extension, file.lastModified(), COMPUTER_NAME, needsRestored);
                }
                foundFile.setByteCount(file.length());
                String sha1hash;
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    sha1hash = DigestUtils.sha1Hex(fileInputStream);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                foundFile.setSha1hash(sha1hash);
                foundFile = foundFileRepository.save(foundFile);
                log.info("Found file {}", foundFile);
            });
        }
    }

    private void addAllBackupFiles() {

    }

//    private BackupFile matchFoundFileToBackupFile(FoundFile foundFile) {
//        String backupHost = getBackupPCHost(foundFile.getComputerName());
//        File hostRoot = new File(BACKUPPC_PC_ROOT + backupHost);
//        for ()
//    }

//    private String(String computerName) {
//    }

    private String getBackupPCHost(String computerName) {
        switch (computerName) {
            case "jacob.wise.home":
                return "localhost";
            case "hook":
            case "wendybird":
                return "hook.wise.home";
            case "peterpan":
                return "peterpan.wise.home";
            case "tinkerbell":
                return "tinkerbell.wise.home";
            default:
                return computerName;
        }
    }

    private static String getComputerName() {
        try {
            return StringUtils.lowerCase(StringUtils.defaultIfBlank(StringUtils.defaultIfBlank(System.getenv("COMPUTERNAME"), System.getenv("HOSTNAME")), InetAddress.getLocalHost().getHostName()));
        } catch (UnknownHostException ignored) {
            return "unknown";
        }
    }

}
