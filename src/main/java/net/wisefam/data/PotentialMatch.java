package net.wisefam.data;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;

@Entity
@Table(indexes = {
        @Index(name = "NDX_POTENTIAL_MATCH_BACKUP_FILE", columnList = "backup_file_id", unique = false),
        @Index(name = "NDX_POTENTIAL_MATCH_FOUND_FILE", columnList = "found_file_id", unique = false),
        @Index(name = "UK_POTENTIAL_MATCH_FILES", columnList = "found_file_id, backup_file_id", unique = true)
})
public class PotentialMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    private BackupFile backupFile;

    @ManyToOne
    private FoundFile foundFile;

    private PotentialMatch() {
    }

    public PotentialMatch(BackupFile backupFile, FoundFile foundFile) {
        this.backupFile = backupFile;
        this.foundFile = foundFile;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("backupFile", backupFile.getId())
                .append("foundFile", foundFile.getId())
                .toString();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public BackupFile getBackupFile() {
        return backupFile;
    }

    public void setBackupFile(BackupFile backupFile) {
        this.backupFile = backupFile;
    }

    public FoundFile getFoundFile() {
        return foundFile;
    }

    public void setFoundFile(FoundFile foundFile) {
        this.foundFile = foundFile;
    }

}
