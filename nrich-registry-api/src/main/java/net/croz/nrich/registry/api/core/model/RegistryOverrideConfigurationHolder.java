/*
 *  Copyright 2020-2022 CROZ d.o.o, the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package net.croz.nrich.registry.api.core.model;

import lombok.Builder;
import lombok.Getter;
import net.croz.nrich.search.api.model.SearchConfiguration;

import java.util.Map;

/**
 * Holder that holds {@link RegistryOverrideConfiguration} and {@link SearchConfiguration} for specific entity.
 */
@Getter
@Builder
public class RegistryOverrideConfigurationHolder {

    /**
     * Entity type.
     */
    private final Class<?> type;

    /**
     * Registry override configuration.
     */
    private final RegistryOverrideConfiguration overrideConfiguration;

    /**
     * Search override configuration.
     */
    private final SearchConfiguration<Object, Object, Map<String, Object>> overrideSearchConfiguration;

}
