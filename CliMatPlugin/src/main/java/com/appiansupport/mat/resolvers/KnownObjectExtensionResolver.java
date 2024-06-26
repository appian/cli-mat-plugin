package com.appiansupport.mat.resolvers;

import java.util.List;

public class KnownObjectExtensionResolver extends IEclipsePluginExtensionResolver<KnownObjectResolver> {

    /**
     * @return A List of all KnownObjectResolver extensions as defined in plugin.xml
     */
    public List<KnownObjectResolver> getExtensions() {
        final String KNOWNOBJECTRESOLVER_EXTENSION_POINT="CliMatPlugin.knownObjectResolver";
        final String KNOWNOBJECTRESOLVER_EXTENSION_ATTRIBUTE="impl";
        return getExtensions(KNOWNOBJECTRESOLVER_EXTENSION_POINT,KNOWNOBJECTRESOLVER_EXTENSION_ATTRIBUTE);
    }
}
