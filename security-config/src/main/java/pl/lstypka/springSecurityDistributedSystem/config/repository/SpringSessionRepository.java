package pl.lstypka.springSecurityDistributedSystem.config.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

public interface SpringSessionRepository extends CrudRepository<SessionEntity, String>{
}