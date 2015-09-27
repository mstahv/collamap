
package org.peimari.maastokanta.mobile;

import com.vaadin.addon.touchkit.ui.NavigationView;
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
