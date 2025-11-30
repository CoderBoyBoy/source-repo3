package com.gitserver.repository;

import com.gitserver.entity.SshKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for SshKey entity.
 */
@Repository
public interface SshKeyRepository extends JpaRepository<SshKey, Long> {

    List<SshKey> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<SshKey> findByFingerprint(String fingerprint);

    boolean existsByFingerprint(String fingerprint);

    boolean existsByUserIdAndTitle(Long userId, String title);
}
