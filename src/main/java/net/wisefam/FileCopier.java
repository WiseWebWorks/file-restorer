package net.wisefam;

import net.wisefam.data.BackupFile;
import net.wisefam.data.FoundFile;
import net.wisefam.data.FoundFileRepository;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

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

    private void addAllFilesRecursively(File directory) {
        File[] files = directory.listFiles(ENCRYPTED_FILES_FILTER);
        if (files != null) {
            log.info("Checking directory {}", directory.getPath());
            for (File file : files) {
                String currentFileName = file.getName();
                String actualFileName = StringUtils.removeEndIgnoreCase(currentFileName, ENCRYPTED_FILE_EXT);
                File unencryptedFile = new File(directory, actualFileName);
                String extension = StringUtils.lowerCase(FilenameUtils.getExtension(actualFileName));
                boolean needsRestored = !unencryptedFile.exists() || unencryptedFile.lastModified() == file.lastModified();

                String filePath = file.getParent();
                FoundFile foundFile = foundFileRepository.findByPathAndFileNameAndComputerName(filePath, actualFileName, COMPUTER_NAME);
                if (foundFile == null) {
                    foundFile = new FoundFile(filePath, actualFileName, extension, file.lastModified(), COMPUTER_NAME, needsRestored);
                }
                foundFile = foundFileRepository.save(foundFile);
                log.info("Found file {}", foundFile);
            }
        }
        File[] subdirectories = directory.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
        if (subdirectories != null) {
            for (File subDirectory : subdirectories) {
                addAllFilesRecursively(subDirectory);
            }
        }
    }

    private void addAllBackupFiles() {

    }

    private BackupFile matchFoundFileToBackupFile(FoundFile foundFile) {
        String backupHost = getBackupPCHost(foundFile.getComputerName());
        File hostRoot = new File(BACKUPPC_PC_ROOT + backupHost);
        for ()
    }

    private String (String computerName) {
    }

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
