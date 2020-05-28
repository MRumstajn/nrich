package net.croz.nrich.registry.data.controller;

import lombok.RequiredArgsConstructor;
import net.croz.nrich.registry.data.request.CreateRegistryRequest;
import net.croz.nrich.registry.data.request.CreateRegistryServiceRequest;
import net.croz.nrich.registry.data.request.DeleteRegistryRequest;
import net.croz.nrich.registry.data.request.ListRegistryRequest;
import net.croz.nrich.registry.data.request.UpdateRegistryRequest;
import net.croz.nrich.registry.data.request.UpdateRegistryServiceRequest;
import net.croz.nrich.registry.data.service.RegistryDataRequestConversionService;
import net.croz.nrich.registry.data.service.RegistryDataService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;

@RequiredArgsConstructor
@RequestMapping("nrichRegistryData")
@ResponseBody
public class RegistryDataController {

    private final RegistryDataService registryDataService;

    private final RegistryDataRequestConversionService registryDataRequestConversionService;

    @PostMapping("list")
    public <T> Page<T> list(@RequestBody @Valid final ListRegistryRequest request) {
        return registryDataService.registryList(request);
    }

    @PostMapping("delete")
    public boolean delete(@RequestBody @Valid final DeleteRegistryRequest request) {
        return registryDataService.registryDelete(request);
    }

    @PostMapping("create")
    public <T> T create(@RequestBody @Valid final CreateRegistryRequest request) {
        final CreateRegistryServiceRequest serviceRequest = registryDataRequestConversionService.convertToServiceRequest(request);

        return registryDataService.registryCreate(serviceRequest);
    }

    @PostMapping("update")
    public <T> T update(@RequestBody @Valid final UpdateRegistryRequest request) {
        final UpdateRegistryServiceRequest serviceRequest = registryDataRequestConversionService.convertToServiceRequest(request);

        return registryDataService.registryUpdate(serviceRequest);
    }
}
