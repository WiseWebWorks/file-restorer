package net.wisefam.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PotentialMatchRepository extends CrudRepository<PotentialMatch, Long> {


}
