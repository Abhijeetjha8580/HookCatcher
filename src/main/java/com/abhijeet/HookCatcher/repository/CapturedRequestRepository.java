package com.abhijeet.HookCatcher.repository;

import com.abhijeet.HookCatcher.entity.CapturedRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CapturedRequestRepository extends MongoRepository<CapturedRequest, String> {

    List<CapturedRequest> findByEndpointSlugOrderByReceivedAtDesc(String endpointSlug);

    void deleteByEndpointSlug(String endpointSlug);
}