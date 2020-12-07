package org.peimari.maastokanta.gwt;

import java.util.HashSet;
import java.util.Set;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.vaadin.server.widgetsetutils.ConnectorBundleLoaderFactory;
import com.vaadin.shared.ui.Connect.LoadStyle;

public class OptimizedConnectorBundleLoaderFactory extends
            ConnectorBundleLoaderFactory {
    private Set<String> eagerConnectors = new HashSet<String>();
    {
            eagerConnectors.add(com.vaadin.client.ui.ui.UIConnector.class.getName());
            eagerConnectors.add(com.vaadin.client.ui.passwordfield.PasswordFieldConnector.class.getName());
            eagerConnectors.add(com.vaadin.client.ui.textfield.TextFieldConnector.class.getName());
            eagerConnectors.add(com.vaadin.client.ui.textfield.TextFieldConnector.class.getName());
            eagerConnectors.add(org.vaadin.touchkit.gwt.client.vcom.navigation.NavigationBarConnector.class.getName());
            eagerConnectors.add(com.vaadin.client.ui.csslayout.CssLayoutConnector.class.getName());
            eagerConnectors.add(org.vaadin.touchkit.gwt.client.vcom.LocalStorageConnector.class.getName());
            eagerConnectors.add(org.vaadin.touchkit.gwt.client.vcom.navigation.NavigationButtonConnector.class.getName());
            eagerConnectors.add(com.vaadin.client.ui.button.ButtonConnector.class.getName());
            eagerConnectors.add(org.vaadin.touchkit.gwt.client.vcom.VerticalComponentGroupConnector.class.getName());
            eagerConnectors.add(org.vaadin.touchkit.gwt.client.vcom.navigation.NavigationManagerConnector.class.getName());
            eagerConnectors.add(org.vaadin.addon.leaflet.client.LeafletTileLayerConnector.class.getName());
            eagerConnectors.add(com.vaadin.client.ui.label.LabelConnector.class.getName());
            eagerConnectors.add(org.vaadin.touchkit.gwt.client.vcom.navigation.NavigationViewConnector.class.getName());
            eagerConnectors.add(org.vaadin.addon.leaflet.client.LeafletMapConnector.class.getName());
            eagerConnectors.add(org.vaadin.addon.leaflet.client.LeafletPolygonConnector.class.getName());
            eagerConnectors.add(org.vaadin.addon.leaflet.client.LeafletLayerGroupConnector.class.getName());
            eagerConnectors.add(org.vaadin.addon.leaflet.client.LeafletMarkerConnector.class.getName());
            eagerConnectors.add(org.vaadin.addon.leaflet.client.LeafletPolylineConnector.class.getName());
    }

    @Override
    protected LoadStyle getLoadStyle(JClassType connectorType) {
            if (eagerConnectors.contains(connectorType.getQualifiedBinaryName())) {
                    return LoadStyle.EAGER;
            } else {
                    // Loads all other connectors immediately after the initial view has
                    // been rendered
                    return LoadStyle.DEFERRED;
            }
    }
}
