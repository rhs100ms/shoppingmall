package com.dsapkl.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cluster_item_preference")
@Getter
@Setter
@NoArgsConstructor
public class ClusterItemPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cluster_item_id")
    private Long clusterItemId;

    @Column(name = "cluster_number")
    private Integer clusterNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(name = "preference_score")
    private Integer preferenceScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cluster_number", referencedColumnName = "cluster_id", insertable = false, updatable = false)
    private Cluster cluster;
} 