package org.peimari.maastokanta;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.peimari.maastokanta.auth.LoginView;
import org.peimari.maastokanta.backend.StyleRepository;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.vaadin.addon.leaflet.LTileLayer;
import org.vaadin.addon.leaflet.util.AbstractJTSField;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableConfigurationProperties
@EntityScan
@EnableJpaRepositories
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
        
        try {
            org.h2.tools.Server.createWebServer().start();
        } catch (SQLException ex) {
            Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Application() {
        AbstractJTSField.setDefaultConfigurator(new AbstractJTSField.Configurator() {

            @Override
            public void configure(AbstractJTSField<?> field) {
                LTileLayer basemap = new LTileLayer(
                        "http://v3.tahvonen.fi/mvm71/tiles/peruskartta/{z}/{x}/{y}.png");
                field.getMap().addLayer(basemap);
            }
        });
    }
    
}
