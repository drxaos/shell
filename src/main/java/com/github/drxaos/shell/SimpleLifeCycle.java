package com.github.drxaos.shell;

import org.crsh.plugin.*;
import org.crsh.vfs.spi.FSMountFactory;
import org.crsh.vfs.spi.url.ClassPathMountFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class SimpleLifeCycle extends Embedded {

    protected Map<String, FSMountFactory<?>> mountContexts = new HashMap<String, FSMountFactory<?>>();
    protected PluginContext pluginContext;
    protected Map<PropertyDescriptor, Object> properties = new HashMap<PropertyDescriptor, Object>();

    @Override
    protected Map<String, FSMountFactory<?>> getMountFactories() {
        return mountContexts;
    }

    protected PluginDiscovery createDiscovery(ClassLoader classLoader) {
        return new ServiceLoaderDiscovery(classLoader);
    }

    public void start() {
        setConfig(System.getProperties());

        // Initialise the registerable drivers
        try {
            mountContexts.put("classpath", new ClassPathMountFactory(this.getClass().getClassLoader()));
            //mountContexts.put("file", new FileMountFactory(Utils.getCurrentDirectory()));
        } catch (Exception e) {
            log.log(Level.SEVERE, "Coult not initialize classpath driver", e);
            return;
        }

        ClassLoader webAppLoader = Thread.currentThread().getContextClassLoader();
        PluginDiscovery discovery = createDiscovery(webAppLoader);
        pluginContext = start(new HashMap<String, Object>(), discovery, this.getClass().getClassLoader());
    }

    @Override
    protected String resolveConfMountPointConfig() {
        return "/";
    }

    @Override
    protected String resolveCmdMountPointConfig() {
        return "/commands";
    }

    protected PluginContext start(Map<String, Object> attributes, PluginDiscovery discovery, ClassLoader loader) {
        PluginContext context = create(attributes, discovery, loader);
        if (context != null) {
            context.refresh();
            for (Map.Entry<PropertyDescriptor, Object> entry : properties.entrySet()) {
                context.setProperty(entry.getKey(), entry.getValue());
            }
            start(context);
        }
        return context;
    }

    public void setProperty(PropertyDescriptor descriptor, Object value) {
        properties.put(descriptor, value);
    }
}
