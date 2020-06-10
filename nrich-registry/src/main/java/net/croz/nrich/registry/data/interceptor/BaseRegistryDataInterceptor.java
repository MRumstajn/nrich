package net.croz.nrich.registry.data.interceptor;

import net.croz.nrich.registry.data.request.CreateRegistryServiceRequest;
import net.croz.nrich.registry.data.request.DeleteRegistryRequest;
import net.croz.nrich.registry.data.request.ListRegistryRequest;
import net.croz.nrich.registry.data.request.UpdateRegistryServiceRequest;

public abstract class BaseRegistryDataInterceptor implements RegistryDataInterceptor {

    @Override
    public void beforeRegistryList(final ListRegistryRequest request) {

    }

    @Override
    public void beforeRegistryCreate(final CreateRegistryServiceRequest request) {

    }

    @Override
    public void beforeRegistryUpdate(final UpdateRegistryServiceRequest request) {

    }

    @Override
    public void beforeRegistryDelete(final DeleteRegistryRequest request) {

    }
}