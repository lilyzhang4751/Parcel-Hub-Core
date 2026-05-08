package com.lily.parcelhubcore.shared.converter;

import jakarta.annotation.Resource;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

@Component
@Converter(autoApply = false)
public class MobileEncryptConverter implements AttributeConverter<String, String> {

    @Resource
    private TextEncryptor textEncryptor;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isBlank()) {
            return attribute;
        }
        return textEncryptor.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return dbData;
        }
        return textEncryptor.decrypt(dbData);
    }
}
