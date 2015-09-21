package net.wisefam.data;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Entity
@Table(indexes = {
        @Index(name = "NDX_FOUND_FILE_FILENAME", columnList = "fileName", unique = false),
        @Index(name = "NDX_FOUND_FILE_EXTENSION", columnList = "extension", unique = false),
        @Index(name = "UK_FOUND_FILE_FILENAME", columnList = "path, fileName, computerName", unique = true)
})
public class FoundFile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String path;
    private String fileName;
    private String extension;
    private ZonedDateTime modifiedTime;
    private long modifiedTimestamp;
    private String computerName;
    private boolean matchedExists;
    private long matchedByteCount;
    @Column(length = 40)
    private String matchedSha1hash;
    private Boolean needsRestored;

    protected FoundFile() {
    }

    public FoundFile(String path, String fileName, String extension, long modifiedTimestamp, String computerName) {
        this.path = path;
        this.fileName = fileName;
        this.extension = extension;
        this.modifiedTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(modifiedTimestamp), ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC);
        this.modifiedTimestamp = modifiedTimestamp;
        this.computerName = computerName;
        this.needsRestored = null;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("path", path)
                .append("fileName", fileName)
                .append("computerName", computerName)
                .toString();
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

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }

    public boolean isMatchedExists() {
        return matchedExists;
    }

    public void setMatchedExists(boolean matchedExists) {
        this.matchedExists = matchedExists;
    }

    public long getMatchedByteCount() {
        return matchedByteCount;
    }

    public void setMatchedByteCount(long matchedByteCount) {
        this.matchedByteCount = matchedByteCount;
    }

    public String getMatchedSha1hash() {
        return matchedSha1hash;
    }

    public void setMatchedSha1hash(String matchedSha1hash) {
        this.matchedSha1hash = matchedSha1hash;
    }

    public Boolean getNeedsRestored() {
        return needsRestored;
    }

    public void setNeedsRestored(Boolean needsRestored) {
        this.needsRestored = needsRestored;
    }

}
