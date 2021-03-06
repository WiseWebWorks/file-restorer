package net.wisefam.data;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Entity
@Table(indexes = {
        @Index(name = "NDX_BACKUP_FILE_FILENAME", columnList = "fileName", unique = false),
        @Index(name = "NDX_BACKUP_FILE_EXTENSION", columnList = "extension", unique = false),
        @Index(name = "UK_BACKUP_FILE_FILENAME", columnList = "path, fileName, computerName", unique = true)
})
public class BackupFile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(length = 1000)
    private String path;
    private String fileName;
    private String extension;
    private String computerName;
    @Enumerated(EnumType.STRING)
    private BackupSource backupSource;
    private ZonedDateTime modifiedTime;
    private long modifiedTimestamp;
    private long byteCount;
    @Column(length = 40)
    private String sha1hash;

    private BackupFile() {
    }

    public BackupFile(String path, String fileName, String extension, long modifiedTimestamp, String computerName, BackupSource backupSource) {
        this.path = path;
        this.fileName = fileName;
        this.extension = extension;
        this.modifiedTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(modifiedTimestamp), ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC);
        this.modifiedTimestamp = modifiedTimestamp;
        this.computerName = computerName;
        this.backupSource = backupSource;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("path", path)
                .append("fileName", fileName)
                .append("computerName", computerName)
                .append("backupSource", backupSource)
                .toString();
    }

    public static enum BackupSource {
        BackupPC,
        ExternalHD
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }

    public BackupSource getBackupSource() {
        return backupSource;
    }

    public void setBackupSource(BackupSource backupSource) {
        this.backupSource = backupSource;
    }

    public ZonedDateTime getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(ZonedDateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public long getModifiedTimestamp() {
        return modifiedTimestamp;
    }

    public void setModifiedTimestamp(long modifiedTimestamp) {
        this.modifiedTimestamp = modifiedTimestamp;
    }

    public long getByteCount() {
        return byteCount;
    }

    public void setByteCount(long byteCount) {
        this.byteCount = byteCount;
    }

    public String getSha1hash() {
        return sha1hash;
    }

    public void setSha1hash(String sha1hash) {
        this.sha1hash = sha1hash;
    }
}
