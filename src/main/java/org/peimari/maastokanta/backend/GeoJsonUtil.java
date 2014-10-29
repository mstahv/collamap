/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.peimari.maastokanta.backend;

import java.util.List;
import org.peimari.maastokanta.domain.SpatialFeature;
import org.peimari.maastokanta.domain.UserGroup;

/**
 *
 * @author mattitahvonenitmill
 */
public class GeoJsonUtil {
    
    String toJson(UserGroup group) {
        List<SpatialFeature> features = group.getFeatures();

        
        return "";
    }
    
}
