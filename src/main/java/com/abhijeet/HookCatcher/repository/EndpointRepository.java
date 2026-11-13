package com.abhijeet.HookCatcher.repository;

import com.abhijeet.HookCatcher.entity.EndpointEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EndpointRepository extends JpaRepository<EndpointEntity,Long> {
    List<EndpointEntity> findByOwnerUsername(String username);

    Optional<EndpointEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
