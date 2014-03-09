/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.peimari.maastokanta.backend;

import org.peimari.maastokanta.domain.Style;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 */
public interface StyleRepository extends JpaRepository<Style, Long> {
    
}
