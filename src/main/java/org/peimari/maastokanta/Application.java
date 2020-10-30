package org.peimari.maastokanta;

import com.vaadin.server.VaadinServlet;
import org.peimari.maastokanta.mobile.MobileUI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.vaadin.addon.leaflet.LTileLayer;
import org.vaadin.addon.leaflet.LWmsLayer;
import org.vaadin.addon.leaflet.util.AbstractJTSField;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableConfigurationProperties
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    public Application() {
        System.setProperty("org.geotools.referencing.forceXY", "true");
        AbstractJTSField.setDefaultConfigurator(
                new AbstractJTSField.Configurator() {

            @Override
            public void configure(AbstractJTSField<?> field) {
                LTileLayer basemap = new LTileLayer(MobileUI.peruskarttaosoite);
                basemap.setAttributionString("© MML");
                field.getMap().setMaxZoom(17);
                field.getMap().addBaseLayer(basemap, "Peruskartta");
                field.getMap().setCustomInitOption("editable", true);

                LWmsLayer mapant = new LWmsLayer();
                mapant.setUrl("https://wmts.mapant.fi/wmts_EPSG3857.php?z={z}&x={x}&y={y}");
                mapant.setMaxZoom(19);
                mapant.setMinZoom(7);
                mapant.setAttributionString("<a href=\"http://www.maanmittauslaitos.fi/en/digituotteet/laser-scanning-data\" target=\"_blank\">Laser scanning</a> and <a href=\"http://www.maanmittauslaitos.fi/en/digituotteet/topographic-database\" target=\"_blank\">topographic</a> data provided by the <a href=\"http://www.maanmittauslaitos.fi/en\" target=\"_blank\">National Land Survey of Finland</a> under the <a href=\"https://creativecommons.org/licenses/by/4.0/legalcode\">Creative Commons license</a>.");
                field.getMap().addBaseLayer(mapant, "MapAnt");

            }
        });
    }

    @Bean
    public VaadinServlet vaadinServlet() {
        return new SpringAwareTouchKitServlet();
    }

//    @Bean
//    public Filter gzipFiltering() {
//        return new GzipFilter();
//    }
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }
}
