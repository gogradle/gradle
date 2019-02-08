/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.plugins.internal;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.component.ConfigurationVariantDetails;
import org.gradle.api.ecosystem.Ecosystem;
import org.gradle.api.internal.component.SoftwareComponentInternal;
import org.gradle.api.internal.component.UsageContext;
import org.gradle.api.internal.java.usagecontext.ConfigurationVariantMapping;
import org.gradle.internal.Factory;
import org.gradle.internal.component.ImmutableEcosystem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class DefaultAdhocSoftwareComponent implements AdhocComponentWithVariants, SoftwareComponentInternal {
    private final String componentName;
    private final Factory<List<Ecosystem>> defaultEcosystems;
    private final List<ConfigurationVariantMapping> variants = Lists.newArrayListWithExpectedSize(4);
    private List<Ecosystem> explicitEcosystems;

    public DefaultAdhocSoftwareComponent(String componentName, Factory<List<Ecosystem>> defaultEcosystems) {
        this.componentName = componentName;
        this.defaultEcosystems = defaultEcosystems;
    }

    @Override
    public String getName() {
        return componentName;
    }

    @Override
    public void addVariantsFromConfiguration(Configuration outgoingConfiguration, Action<? super ConfigurationVariantDetails> spec) {
        variants.add(new ConfigurationVariantMapping(outgoingConfiguration, spec));
    }

    @Override
    public void registerEcosystem(String name, @Nullable String description) {
        if (explicitEcosystems == null) {
            explicitEcosystems = Lists.newArrayListWithExpectedSize(2);
        }
        explicitEcosystems.add(new ImmutableEcosystem(name, description));
    }

    @Override
    public Set<? extends UsageContext> getUsages() {
        ImmutableSet.Builder<UsageContext> builder = new ImmutableSet.Builder<UsageContext>();
        for (ConfigurationVariantMapping variant : variants) {
            variant.collectUsageContexts(builder);
        }
        return builder.build();
    }

    @Override
    public List<Ecosystem> getEcosystems() {
        return explicitEcosystems != null ? explicitEcosystems : defaultEcosystems.create();
    }
}
