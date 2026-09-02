package com.isivi.app.repository;

import com.isivi.app.model.Banner;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BannerRepository extends MongoRepository<Banner, String> {}
