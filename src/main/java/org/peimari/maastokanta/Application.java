package org.peimari.maastokanta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.vaadin.addon.leaflet.LTileLayer;
import org.vaadin.addon.leaflet.shared.Crs;
import org.vaadin.addon.leaflet.util.AbstractJTSField;
import org.vaadin.spring.boot.EnableTouchKitServlet;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableConfigurationProperties
@EnableTouchKitServlet
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    public Application() {
        System.setProperty("org.geotools.referencing.forceXY", "true");
        AbstractJTSField.setDefaultConfigurator(new AbstractJTSField.Configurator() {

                    @Override
                    public void configure(AbstractJTSField<?> field) {
                        LTileLayer basemap = new LTileLayer(
                                "http://v4.tahvonen.fi/mvm75/tiles/peruskartta_3067/{z}/{x}/{y}.png");
                        basemap.setAttributionString("© MML");
                        field.getMap().setCrs(new Crs("EPSG3067"));
                        field.getMap().setMaxZoom(17);
                        field.getMap().addLayer(basemap);
                        field.getMap().setCustomInitOption("editable", true);
                    }
                });
    }

//    @Bean
//    public Filter gzipFiltering() {
//        return new GzipFilter();
//    }

    @Override
    protected SpringApplicationBuilder configure(
            SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    
    
}
