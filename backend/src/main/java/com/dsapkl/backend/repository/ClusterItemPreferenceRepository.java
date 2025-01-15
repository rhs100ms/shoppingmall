package com.dsapkl.backend.repository;

import com.dsapkl.backend.entity.Cluster;
import com.dsapkl.backend.entity.ClusterItemPreference;
import com.dsapkl.backend.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClusterItemPreferenceRepository extends JpaRepository<ClusterItemPreference, Long> {
    Optional<ClusterItemPreference> findByClusterAndItem(Cluster cluster, Item item);
} 