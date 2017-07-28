package de.toem.impulse.other.examples;

import java.io.IOException;
import java.util.List;

import de.toem.impulse.ImpulseBase;
import de.toem.impulse.cells.configuration.RecordConfiguration;
import de.toem.pattern.bundles.Bundles;
import de.toem.pattern.element.ElementHierarchyModifier;
import de.toem.pattern.element.Elements;
import de.toem.pattern.element.ICell;
import de.toem.pattern.element.IElement;
import de.toem.pattern.element.IElementModifier;

public class CreateConfiguration {

    public static void insertDefaultConfig() {

        byte[] wallet = null;
        try {
            wallet = Bundles.getBundleEntryAsByteArray("de.toem.impulse.extension.examples", "integer.walML");
        } catch (IOException e) {
        }
        if (wallet != null) {
            IElement preferences = ImpulseBase.configurationPreferences;
            if (preferences.isBound() && preferences.hasCell()) {
                IElement element = Elements.getElement(wallet);
                if (element.isBound() && element.hasCell()) {
                    List<ICell> configs = element.getCell().getChildren(RecordConfiguration.class);
                    for (ICell config : configs) {
                        if (!preferences.hasChild(config.getName())) {
                            IElementModifier m = ElementHierarchyModifier.add(preferences, config.getElement(), null);
                            m.doIt(null);
                        }

                    }
                }
            }
        }

    }
}
