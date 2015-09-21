package net.wisefam.data;

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

    public PotentialMatch(BackupFile backupFile, FoundFile foundFile) {
        this.backupFile = backupFile;
        this.foundFile = foundFile;
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
