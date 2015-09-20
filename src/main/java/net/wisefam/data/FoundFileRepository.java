package net.wisefam.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoundFileRepository extends CrudRepository<FoundFile, Long> {

    List<FoundFile> findByFileName(String fileName);

    FoundFile findByPathAndFileNameAndComputerName(String path, String fileName, String computerName);

}
