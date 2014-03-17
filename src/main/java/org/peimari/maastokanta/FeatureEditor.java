package org.peimari.maastokanta;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vividsolutions.jts.geom.Geometry;
import org.peimari.maastokanta.backend.AppService;
import org.peimari.maastokanta.backend.Repository;
import org.peimari.maastokanta.domain.AreaFeature;
import org.peimari.maastokanta.domain.LineFeature;
import org.peimari.maastokanta.domain.PointFeature;
import org.peimari.maastokanta.domain.SpatialFeature;
import org.peimari.maastokanta.domain.Style;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.addon.leaflet.shared.Point;
import org.vaadin.addon.leaflet.util.AbstractJTSField;
import org.vaadin.addon.leaflet.util.LineStringField;
import org.vaadin.addon.leaflet.util.LinearRingField;
import org.vaadin.addon.leaflet.util.PointField;
import org.vaadin.maddon.BeanBinder;
import org.vaadin.maddon.button.PrimaryButton;
import org.vaadin.maddon.fields.CaptionGenerator;
import org.vaadin.maddon.fields.MTextArea;
import org.vaadin.maddon.fields.MTextField;
import org.vaadin.maddon.fields.TypedSelect;
import org.vaadin.maddon.layouts.MHorizontalLayout;
import org.vaadin.maddon.layouts.MVerticalLayout;
import org.vaadin.spring.UIScope;
import org.vaadin.spring.VaadinComponent;

@UIScope
@VaadinComponent
public class FeatureEditor extends Window implements ClickListener {

    @Autowired
    AppService service;
    @Autowired
    Repository groups;

    private SpatialFeature feature;

    private Button save = new PrimaryButton("Save", this);
    private Button cancel = new Button("Cancel", this);

    private TextField title = new MTextField("Title");
    private TextArea description = new MTextArea("Description");
    /* Used for EventWithPoint, field used for bean binding */
    @SuppressWarnings("unused")
    private PointField location;
    /* Used for EventWithRoute, field used for bean binding */
    @SuppressWarnings("unused")
    private LineStringField line;
    @SuppressWarnings("unused")
    private LinearRingField area;
    private AbstractJTSField<?> geometryField;

    private TypedSelect<Style> style = new TypedSelect<>("Style");

    void init(SpatialFeature spatialFeature) {
        this.feature = spatialFeature;

        style.setCaptionGenerator(new CaptionGenerator<Style>() {
            @Override
            public String getCaption(Style option) {
                return option.getName();
            }
        });
        style.setOptions(service.getGroup().getStyles());

        /* Choose suitable custom field for geometry */
        if (feature instanceof PointFeature) {
            geometryField = location = new PointField();
        } else if (feature instanceof AreaFeature) {
            geometryField = area = new LinearRingField();
        } else if (feature instanceof LineFeature) {
            geometryField = line = new LineStringField();
        }

        /* Configure the sub window editing the pojo */
        setCaption("Edit event");
        setHeight("80%");
        setWidth("80%");
        setModal(true);
        setClosable(false); // only via save/cancel

        /* Build layout */
        HorizontalLayout content = new HorizontalLayout(
                new MVerticalLayout(title, description, style,
                        new MHorizontalLayout(save, cancel)), geometryField);
        content.setSizeFull();
        setContent(content);

        /* Bind data to fields */
        BeanBinder.bind(feature, this);

        if (feature instanceof PointFeature) {
            geometryField.getMap().setZoomLevel(DEFAULT_ZOOM_FOR_POINTS);
        }

        // Show editor
        if (getParent() == null) {
            UI.getCurrent().addWindow(this);
        }
    }
    private static final int DEFAULT_ZOOM_FOR_POINTS = 15;

    @Override
    public void buttonClick(ClickEvent event) {
        if (event.getButton() == save) {
            // TODO commit!? Need Buffering!?, or remove totally??
            
            
        } else {
            // TODO refresh!? Need Buffering!?
            
        }
        close();
    }

    void zoomToExtent(Geometry bounds) {
        geometryField.getMap().zoomToExtent(bounds);
    }

    void setCenterAndZoom(Point center, Integer zoomLevel) {
        geometryField.getMap().setCenter(center);
        geometryField.getMap().setZoomLevel(zoomLevel);
    }

}
