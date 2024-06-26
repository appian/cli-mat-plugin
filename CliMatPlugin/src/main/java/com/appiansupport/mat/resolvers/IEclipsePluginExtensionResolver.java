package com.appiansupport.mat.resolvers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import java.util.ArrayList;
import java.util.List;

abstract class IEclipsePluginExtensionResolver<T> {


    /**
     * @return A list of all the resolved extensions of this class' default extension point.
     * Returns empty if the extension point does not exist, has no extensions configured,
     * or none of the extensions contain configuration elements.
     * Returns null if the Eclipse extension registry could not be found
     * (e.g. when executed outside of an Eclipse Runtime).
     */
    abstract List<T> getExtensions();

    /**
     * @param extensionPoint The Eclipse extension-point, as defined in plugin.xml, to resolve
     * @param attributeToResolve The attribute name of the configuration element to resolve (typically impl).
     * @return A list of the resolved extensions, empty if the extension point does not exist,
     * has no extensions configured, or none of the extensions contain configuration elements,
     * or null if the Eclipse extension registry could not be found (e.g. when executed outside of an Eclipse Runtime).
     */
    List<T> getExtensions(String extensionPoint, String attributeToResolve) {
        //Only populated when executed within a proper Eclipse runtime
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        if (registry == null){
            //todo: DEBUG logging would help here once migrated to logging framework
            return null;
        }
        IConfigurationElement[] extensionElements = registry.getConfigurationElementsFor(extensionPoint);
        List<T> extensions = new ArrayList<>(extensionElements.length);
        for (IConfigurationElement el : extensionElements) {
            try {
                T extension = (T) el.createExecutableExtension(attributeToResolve);
                if (extension != null) {
                    extensions.add(extension);
                }
            } catch (CoreException coreException) {
                System.err.printf("Unable to resolve %s extension %s%n",extensionPoint, el.getName());
                coreException.printStackTrace();
            }
        }
        return extensions;
    }
}
