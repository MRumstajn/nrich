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

package net.croz.nrich.registry.history.controller;

import lombok.RequiredArgsConstructor;
import net.croz.nrich.registry.api.history.model.EntityWithRevision;
import net.croz.nrich.registry.api.history.request.ListRegistryHistoryRequest;
import net.croz.nrich.registry.api.history.service.RegistryHistoryService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;

@RequiredArgsConstructor
@RequestMapping("${nrich.registry.history.endpoint-path:nrich/registry/history}")
@ResponseBody
public class RegistryHistoryController {

    private final RegistryHistoryService registryHistoryService;

    @PostMapping("list")
    public <T> Page<EntityWithRevision<T>> historyList(@RequestBody @Valid ListRegistryHistoryRequest request) {
        return registryHistoryService.historyList(request);
    }
}
