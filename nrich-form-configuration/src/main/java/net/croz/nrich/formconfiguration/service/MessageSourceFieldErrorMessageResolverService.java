package net.croz.nrich.formconfiguration.service;

import lombok.RequiredArgsConstructor;
import net.croz.nrich.formconfiguration.api.model.ConstrainedProperty;
import net.croz.nrich.formconfiguration.constants.FormConfigurationConstants;
import org.springframework.context.MessageSource;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
public class MessageSourceFieldErrorMessageResolverService implements FieldErrorMessageResolverService {

    private final MessageSource messageSource;

    @Override
    public String resolveErrorMessage(final ConstrainedProperty constrainedProperty, final Locale locale) {
        final List<String> messageCodeList = resolveConstraintMessageCodeList(constrainedProperty);
        final Object[] argumentList = convertArraysInArgumentList(constrainedProperty.getConstraintArgumentList());
        final DefaultMessageSourceResolvable defaultMessageSourceResolvable = new DefaultMessageSourceResolvable(messageCodeList.toArray(new String[0]), argumentList, constrainedProperty.getConstraintMessage());

        return messageSource.getMessage(defaultMessageSourceResolvable, locale);
    }

    private List<String> resolveConstraintMessageCodeList(final ConstrainedProperty constrainedProperty) {
        final String constraintOwningClassName = StringUtils.uncapitalize(constrainedProperty.getParentType().getName());
        final String constraintOwningClassShortName = StringUtils.uncapitalize(constrainedProperty.getParentType().getSimpleName());
        final String constraintPropertyName = constrainedProperty.getName();
        final String constraintName = constrainedProperty.getConstraintName();

        final List<String> codeList = new ArrayList<>();

        codeList.add(resolveMessageCode(FormConfigurationConstants.CONSTRAINT_FULL_CLIENT_MESSAGE_FORMAT, constraintOwningClassName, constraintPropertyName, constraintName));
        codeList.add(resolveMessageCode(FormConfigurationConstants.CONSTRAINT_FULL_CLIENT_MESSAGE_FORMAT, constraintOwningClassShortName, constraintPropertyName, constraintName));

        codeList.add(resolveMessageCode(FormConfigurationConstants.CONSTRAINT_FULL_MESSAGE_FORMAT, constraintOwningClassName, constraintPropertyName, constraintName));
        codeList.add(resolveMessageCode(FormConfigurationConstants.CONSTRAINT_FULL_MESSAGE_FORMAT, constraintOwningClassShortName, constraintPropertyName, constraintName));

        codeList.add(resolveMessageCode(FormConfigurationConstants.CONSTRAINT_SHORT_CLIENT_MESSAGE_FORMAT, constraintName));

        codeList.add(resolveMessageCode(FormConfigurationConstants.CONSTRAINT_SHORT_MESSAGE_FORMAT, constraintName));

        return codeList;
    }

    private String resolveMessageCode(final String messageFormat, final String... argumentList) {
        @SuppressWarnings("RedundantCast")
        final String messageCode = String.format(messageFormat, (Object[]) argumentList);

        return messageCode;
    }

    private Object[] convertArraysInArgumentList(final Object[] argumentList) {
        if (argumentList == null) {
            return null;
        }

        return Arrays.stream(argumentList)
                .map(value -> value instanceof Object[] ? convertToString((Object[]) value) : value)
                .toArray();
    }

    private String convertToString(final Object[] value) {
        return Arrays.toString(value).replace('[', ' ').replace(']', ' ').trim();
    }
}
