package net.croz.nrich.encrypt.api.service;

import net.croz.nrich.encrypt.api.model.EncryptionContext;

import java.util.List;

public interface DataEncryptionService {

    <T> T encryptData(T data, List<String> pathToEncryptDecryptList, EncryptionContext encryptionContext);

    <T> T decryptData(T data, List<String> pathToEncryptDecryptList, EncryptionContext encryptionContext);
}