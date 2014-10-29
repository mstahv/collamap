package org.peimari.maastokanta;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import org.peimari.maastokanta.backend.AppService;
import org.peimari.maastokanta.domain.Style;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.maddon.button.MButton;
import org.vaadin.maddon.fields.MTable;
import org.vaadin.maddon.layouts.MVerticalLayout;
import org.vaadin.spring.UIScope;
import org.vaadin.spring.VaadinComponent;

@UIScope
@VaadinComponent
public class StyleEditor extends Window {

    @Autowired
    AppService appService;

    public StyleEditor() {
        setModal(true);
    }

    @Override
    public void attach() {
        super.attach();

        final MTable<Style> table = new MTable<>(
                appService.getGroup().getStyles()).
                withProperties("name", "color");
        table.setEditable(true);
        table.addGeneratedColumn("actions", new Table.ColumnGenerator() {

            @Override
            public Object generateCell(Table source, final Object itemId,
                    Object columnId) {
                return new MButton(FontAwesome.MINUS).withListener(
                        new Button.ClickListener() {

                            @Override
                            public void buttonClick(Button.ClickEvent event) {
                                table.removeItem(itemId);
                            }
                        });
            }
        });

        MButton add = new MButton(FontAwesome.PLUS);
        add.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                Style style = new Style();
                table.addBeans(style);
            }
        });

        setContent(new MVerticalLayout(add, table));
    }

    void activate() {
        UI.getCurrent().addWindow(this);
    }

}
