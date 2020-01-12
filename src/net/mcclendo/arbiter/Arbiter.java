package net.mcclendo.arbiter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

@SuppressWarnings("WeakerAccess")
public final class Arbiter implements AutoCloseable {

    private final List<Object> container;
    private final Properties properties;

    public Arbiter() {
        this.container = new ArrayList<>();
        this.properties = new Properties();

        final String stage = System.getProperty("STAGE", "default");
        try (final InputStream inputStream = this.getConfigFile(stage)) {
            if (inputStream != null) {
                this.properties.load(inputStream);
            }
        } catch (final IOException e) {
            throw new ArbiterException("Loading config failed", e);
        }
    }

    private InputStream getConfigFile(
            final String stage) {
        String path;
        InputStream inputStream;

        path = String.format(
                "/config-%s.properties",
                stage);
        inputStream = Arbiter.class.getResourceAsStream(path);
        if (inputStream != null) {
            return inputStream;
        }

        path = String.format(
                "/config-%s.properties",
                stage);
        inputStream = Arbiter.class.getClassLoader().getResourceAsStream(path);

        return inputStream;
    }

    public <T> T getComponent(
            final Class<T> clazz) {
        return this.createComponent(clazz, new HashSet<>());
    }

    @Override
    public void close() {
        this.container.stream()
                .filter(o -> ComponentLifecycleDestroy.class.isAssignableFrom(o.getClass()))
                .map(o -> ((ComponentLifecycleDestroy) o))
                .forEach(ComponentLifecycleDestroy::onComponentDestroy);
    }

    private <T> T createComponent(
            final Class<T> clazz,
            final Set<Class<?>> depth) {
        final Set<Class<?>> currentDepth = new HashSet<>(depth);

        /*
         * Validate assignable and get component class
         */
        final Component component = clazz.getAnnotation(Component.class);
        if (component == null) {
            throw new ArbiterException("Component not annotated: " + clazz.getCanonicalName());
        } else if (component.implementedBy() == Void.class && clazz.isInterface()) {
            throw new ArbiterException("Component annotation on an interface must have 'annotatedBy' field: " + clazz.getCanonicalName());
        } else if (component.implementedBy() != Void.class && !clazz.isAssignableFrom(component.implementedBy())) {
            throw new ArbiterException(clazz.getCanonicalName() + " is not assignable from " + component.implementedBy().getCanonicalName());
        }

        @SuppressWarnings("unchecked") final Class<T> componentClass = component.implementedBy() == Void.class ? clazz : (Class<T>) component.implementedBy();

        /*
         * Check for circular dependencies.
         */
        if (currentDepth.contains(componentClass)) {
            throw new ArbiterException("Circular reference detected at: " + componentClass.getCanonicalName());
        }
        currentDepth.add(componentClass);

        /*
         * Check to see if the container has one, and only one, component to satisfy the dependency.
         */
        final List<T> components = new ArrayList<>();
        for (final Object o : this.container) {
            if (componentClass.isAssignableFrom(o.getClass())) {
                //noinspection unchecked
                components.add((T) o);
            }
        }

        if (components.size() > 1) {
            throw new ArbiterException("Ambiguous injection for: " + clazz.getCanonicalName());
        } else if (components.size() == 1) {
            return components.get(0);
        }

        /*
         * Create the class.
         */
        if (componentClass.getConstructors().length != 1) {
            throw new ArbiterException("No public constructor for: " + componentClass.getCanonicalName());
        }

        @SuppressWarnings("unchecked") final Constructor<T> constructor = (Constructor<T>) componentClass.getConstructors()[0];
        final Class<?>[] parameterTypes = constructor.getParameterTypes();
        final Object[] parameterInstances = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            final ConfigValue configValue = constructor.getParameters()[i].getAnnotation(ConfigValue.class);
            if (configValue != null) {
                parameterInstances[i] = this.castConfigParam(
                        (String) this.properties.get(configValue.value()),
                        parameterTypes[i]);
            } else {
                parameterInstances[i] = this.createComponent(parameterTypes[i], new HashSet<>(currentDepth));
            }
        }

        final T instance;
        try {
            instance = constructor.newInstance(parameterInstances);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ArbiterException("Unable to create an instance of " + componentClass.getCanonicalName(), e);
        }

        this.container.add(instance);

        return instance;
    }

    private Object castConfigParam(
            final String property,
            final Class<?> parameterInstanceType) {
        try {
            if (String.class.isAssignableFrom(parameterInstanceType)) {
                return property;
            } else if ("int".equals(parameterInstanceType.getName())
                    || Integer.class.isAssignableFrom(parameterInstanceType)) {
                return Integer.parseInt(property);
            } else if ("short".equals(parameterInstanceType.getName())
                    || Short.class.isAssignableFrom(parameterInstanceType)) {
                return Short.parseShort(property);
            } else {
                throw new ArbiterException(String.format("Unknown cast from property '%s' to parameter '%s'.", property, parameterInstanceType.getName()));
            }
        } catch (final NumberFormatException e) {
            throw new ArbiterException(
                    String.format("Unable to cast property '%s' to parameter '%s'.", property, parameterInstanceType.getName()),
                    e);
        }

    }
}
