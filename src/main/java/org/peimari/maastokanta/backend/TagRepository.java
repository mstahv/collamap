/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.peimari.maastokanta.backend;

import org.peimari.maastokanta.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 */
public interface TagRepository extends JpaRepository<Tag, Long> {
    
}
