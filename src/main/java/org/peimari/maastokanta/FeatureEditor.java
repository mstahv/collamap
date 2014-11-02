package org.peimari.maastokanta;

import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.DateField;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.beanutils.BeanUtils;
import org.peimari.maastokanta.backend.AppService;
import org.peimari.maastokanta.backend.Repository;
import org.peimari.maastokanta.domain.AreaFeature;
import org.peimari.maastokanta.domain.LineFeature;
import org.peimari.maastokanta.domain.PointFeature;
import org.peimari.maastokanta.domain.SpatialFeature;
import org.peimari.maastokanta.domain.Style;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.vaadin.addon.leaflet.shared.Point;
import org.vaadin.addon.leaflet.util.AbstractJTSField;
import org.vaadin.addon.leaflet.util.LineStringField;
import org.vaadin.addon.leaflet.util.LinearRingField;
import org.vaadin.addon.leaflet.util.PointField;
import org.vaadin.maddon.BeanBinder;
import org.vaadin.maddon.button.MButton;
import org.vaadin.maddon.button.PrimaryButton;
import org.vaadin.maddon.components.DisclosurePanel;
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
    private SpatialFeature originalState;

    private Button save = new PrimaryButton("Save", this);
    private Button cancel = new Button("Cancel", this);
    private Button simplyfy = new MButton("Simplify geometry", this).withStyleName(ValoTheme.BUTTON_LINK);

    private TextField title = new MTextField("Title").withFullWidth();
    private TextArea description = new MTextArea("Description");
    private TextField contact = new MTextField("Contact").withFullWidth();
    private TextArea privateDescription = new MTextArea("Private Description");
    private DateField validUntil = new DateField("Valid until");
    /* Used for EventWithPoint, field used for bean binding */
    @SuppressWarnings("unused")
    private PointField location;
    /* Used for EventWithRoute, field used for bean binding */
    @SuppressWarnings("unused")
    private LineStringField line;
    @SuppressWarnings("unused")
    private LinearRingField area;
    private AbstractJTSField<? extends Geometry> geometryField;

    private TypedSelect<Style> style = new TypedSelect<>("Style");

    public FeatureEditor init(SpatialFeature spatialFeature) {
        this.feature = spatialFeature;
        try {
            this.originalState = (SpatialFeature) BeanUtils.cloneBean(feature);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException ex) {
            Logger.getLogger(FeatureEditor.class.getName()).
                    log(Level.SEVERE, null, ex);
        }

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
        setCaption("Edit feature");
        setHeight("80%");
        setWidth("80%");
        setModal(true);
        setClosable(false); // only via save/cancel

        /* Build layout */
        setContent(new MHorizontalLayout(
                new MVerticalLayout(title, description,
                        style,
                        new DisclosurePanel("Private details",
                                validUntil,
                                contact,
                                privateDescription
                        ),
                        new MHorizontalLayout(save, cancel)
                ).withMargin(false),
                new MVerticalLayout(
                        geometryField,
                        simplyfy
                ).withMargin(false)
        ).withFullWidth().withMargin(true).withSpacing(true));

        /* Bind data to fields */
        BeanBinder.bind(feature, this);

        if (feature instanceof PointFeature) {
            geometryField.getMap().setZoomLevel(DEFAULT_ZOOM_FOR_POINTS);
        }
        geometryField.setHeight(
                (int) (Page.getCurrent().getBrowserWindowHeight() * 0.7),
                Unit.PIXELS);

        // Show editor
        if (getParent() == null) {
            UI.getCurrent().addWindow(this);
            addCloseListener((Window.CloseListener) UI.getCurrent());
        }
        return this;
    }
    private static final int DEFAULT_ZOOM_FOR_POINTS = 15;

    @Override
    public void buttonClick(ClickEvent event) {
        if (event.getButton() == simplyfy) {
            if (feature instanceof AreaFeature) {
                Geometry geometry = geometryField.getValue();
                LinearRing simplified = (LinearRing) DouglasPeuckerSimplifier.
                        simplify(geometry, 0.00005);
                area.setValue(simplified);
            }
            return;
        }
        if (event.getButton() == save) {
            // NOP, not buffering in this app
        } else {
            // Cancel clicked
            try {
                // Reset the original state
                BeanUtils.copyProperties(feature, originalState);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                Logger.getLogger(FeatureEditor.class.getName()).
                        log(Level.SEVERE, null, ex);
            }

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
