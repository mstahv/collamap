
package org.peimari.maastokanta.mobile;

import org.vaadin.touchkit.ui.NavigationView;
import org.vaadin.viritin.label.RichText;

/**
 *
 * @author Matti Tahvonen
 */
public class HelpView extends NavigationView {
    
    public HelpView(String mdResource) {
        setCaption("Help");
        setContent(new RichText().withMarkDownResource(mdResource));
    }

}
