package net.wisefam;

import net.wisefam.data.*;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.net.URLCodec;
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

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class FileCopier implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FileCopier.class);

    private static final String ENCRYPTED_FILE_EXT = ".qskkhze";
    private static final FilenameFilter ENCRYPTED_FILES_FILTER = new WildcardFileFilter("*" + ENCRYPTED_FILE_EXT, IOCase.INSENSITIVE);
    private static final String COMPUTER_NAME = getComputerName();
    //    private static final String BACKUPPC_PC_ROOT = "/wisefam/big/backuppc/pc/";
    private static final String BACKUPPC_PC_ROOT = "K:\\backuppc\\pc";
    private static final URLCodec URL_CODEC = new URLCodec("UTF8");

    @Autowired
    private FoundFileRepository foundFileRepository;

    @Autowired
    private BackupFileRepository backupFileRepository;

    @Autowired
    private PotentialMatchRepository potentialMatchRepository;

    private static String getComputerName() {
        try {
            return StringUtils.lowerCase(StringUtils.defaultIfBlank(StringUtils.defaultIfBlank(System.getenv("COMPUTERNAME"), System.getenv("HOSTNAME")), InetAddress.getLocalHost().getHostName()));
        } catch (UnknownHostException ignored) {
            return "unknown";
        }
    }

    @Override
    public void run(String... args) throws Exception {
//        LineNumberReader reader = new LineNumberReader(new FileReader(new File("files.html")));

        if (args.length == 1 && StringUtils.equalsIgnoreCase(args[0], "backuppc")) {
            findBackupPCFiles();
        } else {
            File[] roots;
            if (args.length > 0) {
                roots = new File[args.length];
                for (int i = 0; i < roots.length; i++) {
                    roots[i] = new File(args[i]);
                }
            } else {
                roots = File.listRoots();
            }
            findEncryptedFiles(roots);
        }

    }

    private void findEncryptedFiles(File[] roots) {
        if (roots != null) {
            for (File root : roots) {
                addEncryptedFilesRecursively(root);
                log.info("Done finding files in root {} on {}", root, COMPUTER_NAME);
            }
        }
        log.info("Done finding files on {}", COMPUTER_NAME);
    }

    private void addEncryptedFilesRecursively(File directory) {
        File[] files = directory.listFiles(ENCRYPTED_FILES_FILTER);
        if (files != null) {
            log.info("Checking directory {}", directory.getPath());
            for (File file : files) {
                String currentFileName = file.getName();
                String actualFileName = StringUtils.removeEndIgnoreCase(currentFileName, ENCRYPTED_FILE_EXT);
                File unencryptedFile = new File(directory, actualFileName);
                String extension = StringUtils.lowerCase(FilenameUtils.getExtension(actualFileName));
                boolean matchedExists = unencryptedFile.exists();

                String filePath = file.getParent();
                FoundFile foundFile = foundFileRepository.findByPathAndFileNameAndComputerName(filePath, actualFileName, COMPUTER_NAME);
                if (foundFile == null) {
                    foundFile = new FoundFile(filePath, actualFileName, extension, file.lastModified(), COMPUTER_NAME);
                }
                foundFile.setMatchedExists(matchedExists);
                if (matchedExists) {
                    foundFile.setMatchedByteCount(unencryptedFile.length());
                    if (foundFile.getMatchedSha1hash() == null) {
                        String sha1hash = getSha1Hash(unencryptedFile);
                        foundFile.setMatchedSha1hash(sha1hash);
                    }
                }
                foundFile = foundFileRepository.save(foundFile);
                log.info("Found file {}", foundFile);
            }
        }
        File[] subdirectories = directory.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
        if (subdirectories != null) {
            for (File subDirectory : subdirectories) {
                addEncryptedFilesRecursively(subDirectory);
            }
        }
    }

    private String getSha1Hash(File unencryptedFile) {
        String sha1hash;
        try (FileInputStream fileInputStream = new FileInputStream(unencryptedFile)) {
            sha1hash = DigestUtils.sha1Hex(fileInputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sha1hash;
    }

    private void findBackupPCFiles() {
        File pcDir = new File(BACKUPPC_PC_ROOT);
        for (File hostRoot : pcDir.listFiles((FileFilter) DirectoryFileFilter.INSTANCE)) {
            String backupHost = hostRoot.getName();
            String computerName = getComputerNameFromBackupPCHost(backupHost);

            if (computerName != null) {
                List<File> backupDirsList;
                try (Stream<File> stream = Arrays.asList(hostRoot.listFiles((FileFilter) DirectoryFileFilter.INSTANCE)).stream();) {
                    // Reverse sort
                    backupDirsList = stream.sorted((file1, file2) -> {
                        return new Integer(file2.getName()).compareTo(new Integer(file1.getName()));
                    }).collect(Collectors.toList());
                }

                List<BackupPCShare> shares;
                try (Stream<File> stream = Arrays.asList(backupDirsList.get(0).listFiles((FileFilter) DirectoryFileFilter.INSTANCE)).stream();) {
                    shares = stream.map((file) -> {
                        String encodedName = file.getName();
                        return new BackupPCShare(encodedName, decodeBackupPCFileName(encodedName));
                    }).collect(Collectors.toList());
                }

                for (BackupPCShare share : shares) {
                    String pathSeparator = StringUtils.contains(share.srcPath, "\\") ? "\\" : "/";
                    for (FoundFile foundFile : foundFileRepository.findByComputerNameAndPathLike(computerName, share.srcPath + pathSeparator + "%")) {
                        matchFoundFileToBackupFile(computerName, backupDirsList, share, foundFile, pathSeparator);
                    }
                }
            }
        }
    }

    private BackupFile matchFoundFileToBackupFile(String computerName, List<File> backupDirsList, BackupPCShare share, FoundFile foundFile, String pathSeparator) {
        String relativePath = StringUtils.removeStart(foundFile.getPath(), share.srcPath);
        for (File backupDir : backupDirsList) {
            String fullPath = backupDir.getPath() + File.separator + share.encodedName + encodeBackupPCFilePath(relativePath, pathSeparator) + File.separator + "f" + foundFile.getFileName();
            File backupFile = new File(fullPath);
            if (!backupFile.exists()) {
                // Try lowercase extension
                String extension = StringUtils.lowerCase(foundFile.getExtension());
                fullPath = StringUtils.removeEndIgnoreCase(fullPath, extension) + extension;
                backupFile = new File(fullPath);
            }
            if (backupFile.exists()) {
                try {
                    backupFile = backupFile.getCanonicalFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                log.info("Found backup file {} for {}", backupFile, foundFile);

                String backupFileName = backupFile.getName();
                String newExtension = FilenameUtils.getExtension(backupFileName);
                String existingExtension = FilenameUtils.getExtension(foundFile.getFileName());
                if (!StringUtils.equals(newExtension, existingExtension)) {
                    // Update foundFile to match
                    foundFile.setFileName(StringUtils.removeEndIgnoreCase(foundFile.getFileName(), existingExtension) + newExtension);
                    foundFile.setExtension(newExtension);
                    foundFile = foundFileRepository.save(foundFile);
                }

                String backupFilePath = backupFile.getPath();
                BackupFile backupFileDb = backupFileRepository.findByPathAndFileNameAndComputerNameAndBackupSource(backupFilePath, backupFileName, computerName, BackupFile.BackupSource.BackupPC);
                if (backupFileDb == null) {
                    backupFileDb = new BackupFile(backupFilePath, backupFileName, newExtension, backupFile.lastModified(), computerName, BackupFile.BackupSource.BackupPC);
                }
                if (backupFileDb.getSha1hash() == null) {
                    backupFileDb.setSha1hash(getSha1Hash(backupFile));
                }
                backupFileDb = backupFileRepository.save(backupFileDb);

                potentialMatchRepository.save(new PotentialMatch(backupFileDb, foundFile));
                return backupFileDb;
            }
        }
        log.info("Backup file NOT FOUND for {}", foundFile);
        return null;
    }

    private String decodeBackupPCFileName(String backupPCFileName) {
        // Example: f%2fcygdrive%2fc%2fProgramData
        // Remove leading "f"
        String decodedFileName = backupPCFileName.substring(1);
        try {
            decodedFileName = URL_CODEC.decode(decodedFileName);
        } catch (DecoderException e) {
            throw new RuntimeException(e);
        }
        if (StringUtils.startsWith(decodedFileName, "/cygdrive/")) {
            String driveLetter = StringUtils.lowerCase(decodedFileName.substring(10, 11));
            decodedFileName = driveLetter + ":\\" + decodedFileName.substring(12).replaceAll("/", "\\");
        }
        return decodedFileName;
    }

    private String encodeBackupPCFilePath(String relativeFilePath, String pathSeparator) {
        return String.join(File.separator + "f", relativeFilePath.split(pathSeparator));
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

    private String getComputerNameFromBackupPCHost(String backupPCHost) {
        switch (backupPCHost) {
            case "localhost":
                return "jacob.wise.home";
            case "hook.wise.home":
                return "wendybird";
            case "peterpan.wise.home":
                return "peterpan";
            case "tinkerbell.wise.home":
                return "tinkerbell";
            default:
                return null;
        }
    }

    private class BackupPCShare {
        String encodedName;
        String srcPath;

        public BackupPCShare(String encodedName, String srcPath) {
            this.encodedName = encodedName;
            this.srcPath = srcPath;
        }
    }

}
