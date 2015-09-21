package net.wisefam.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BackupFileRepository extends CrudRepository<BackupFile, Long> {

    BackupFile findByPathAndFileNameAndComputerNameAndBackupSource(String path, String fileName, String computerName, BackupFile.BackupSource backupSource);

}
