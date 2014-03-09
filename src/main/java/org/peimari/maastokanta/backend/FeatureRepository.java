/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.peimari.maastokanta.backend;

import org.peimari.maastokanta.domain.SpatialFeature;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 */
public interface FeatureRepository extends JpaRepository<SpatialFeature, Long> {
    
}
