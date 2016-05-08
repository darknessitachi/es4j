/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Provides a command set from a given list of packages
 */
public class PackageCommandSetProvider implements CommandSetProvider {

    private final String[] packages;
    private final ClassLoader[] classLoaders;

    public PackageCommandSetProvider(Package[] packages) {
        this(packages, new ClassLoader[]{});
    }

    public PackageCommandSetProvider(Package[] packages, ClassLoader[] classLoaders) {
        this.packages = Arrays.asList(packages).stream().map(Package::getName).toArray(String[]::new);
        this.classLoaders = classLoaders;
    }

    @Override
    public Set<Class<? extends Command>> getCommands() {
        Configuration configuration = ConfigurationBuilder.build((Object[])packages).addClassLoaders(classLoaders);
        Reflections reflections = new Reflections(configuration);
        Predicate<Class<? extends Entity>> classPredicate = klass ->
                Modifier.isPublic(klass.getModifiers()) &&
                        (!klass.isMemberClass() || (klass.isMemberClass() && Modifier.isStatic(klass.getModifiers()))) &&
                        !Modifier.isAbstract(klass.getModifiers());
        return reflections.getSubTypesOf(Command.class).stream().filter(classPredicate).collect(Collectors.toSet());
    }
}