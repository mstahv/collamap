package org.peimari.maastokanta;

import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.fieldgroup.BeanFieldGroup;
import com.vaadin.v7.data.util.converter.Converter;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.v7.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.peimari.maastokanta.backend.AppService;
import org.peimari.maastokanta.backend.Repository;
import org.peimari.maastokanta.domain.Style;
import org.peimari.maastokanta.domain.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritinv7.fields.MTable;
import org.vaadin.viritinv7.fields.MTextArea;
import org.vaadin.viritinv7.fields.MTextField;
import org.vaadin.viritin.layouts.MVerticalLayout;

@UIScope
@SpringComponent
public class PropertiesEditor extends Window {

    private MTextField name_ = new MTextField("Name");
    private MTextField readOnlyPassword = new MTextField("Read only password");
    private MTextArea editorEmails = new MTextArea(
            "Editors (one email per line)");

    @Autowired
    AppService appService;

    @Autowired
    Repository repo;
    private ArrayList<String> oldEditors;

    public PropertiesEditor() {
        setHeight("90%");
    }

    @Override
    public void attach() {
        super.attach();

        editorEmails.setConverter(new Converter<String, List>() {

            @Override
            public List<String> convertToModel(String value,
                    Class<? extends List> targetType, Locale locale) throws Converter.ConversionException {
                
                final List<String> newEditorList = Arrays.asList(StringUtils.
                        split(value, "\n"));
                return newEditorList;
            }

            @Override
            public String convertToPresentation(List value,
                    Class<? extends String> targetType, Locale locale) throws Converter.ConversionException {
                return StringUtils.join(value, "\n");
            }

            @Override
            public Class<List> getModelType() {
                return List.class;
            }

            @Override
            public Class<String> getPresentationType() {
                return String.class;
            }
        });

        name_.addValueChangeListener(e -> {
            repo.updateName(appService.getGroup(), e.getProperty().toString());
            appService.getPerson().getIdToGroup().put(appService.getGroup().
                    getId(), e.getProperty().toString());
        });

        BeanFieldGroup.bindFieldsUnbuffered(appService.getGroup(), this);

        editorEmails.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                final UserGroup group = appService.getGroup();
                repo.updateEditors(group, oldEditors);
            }
        });

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
        final MVerticalLayout styles = new MVerticalLayout(add, table).
                withCaption("Styles");
        final MVerticalLayout details = new MVerticalLayout(name_,
                readOnlyPassword, editorEmails).withCaption("Generic details");

        setContent(new MVerticalLayout(details, styles));
    }

    void activate() {
        UI.getCurrent().addWindow(this);
        oldEditors = new ArrayList<>(appService.getGroup().getEditorEmails());
    }

}
