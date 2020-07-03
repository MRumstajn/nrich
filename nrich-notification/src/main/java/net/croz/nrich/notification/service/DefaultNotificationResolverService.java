package net.croz.nrich.notification.service;

import lombok.RequiredArgsConstructor;
import net.croz.nrich.notification.api.model.AdditionalNotificationData;
import net.croz.nrich.notification.api.model.Notification;
import net.croz.nrich.notification.api.model.NotificationSeverity;
import net.croz.nrich.notification.api.model.ValidationError;
import net.croz.nrich.notification.api.model.ValidationFailureNotification;
import net.croz.nrich.notification.api.service.NotificationResolverService;
import net.croz.nrich.notification.constant.NotificationConstants;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class DefaultNotificationResolverService implements NotificationResolverService {

    private final MessageSource messageSource;

    private final ConstraintConversionService constraintConversionService;

    @Override
    public ValidationFailureNotification createNotificationForValidationFailure(final Errors errors, final Class<?> validationFailedOwningType, final AdditionalNotificationData additionalNotificationData) {
        final String typeName = validationFailedOwningType == null ? null : validationFailedOwningType.getName();
        final NotificationSeverity severity = Optional.ofNullable(additionalNotificationData.getSeverity()).orElse(NotificationSeverity.WARNING);

        final String title;
        if (typeName == null) {
            title = resolveMessage(NotificationConstants.VALIDATION_FAILED_MESSAGE_TITLE_CODE, NotificationConstants.VALIDATION_FAILED_DEFAULT_TITLE);
        }
        else {
            final String titleCode = String.format(NotificationConstants.PREFIX_MESSAGE_FORMAT, typeName, NotificationConstants.MESSAGE_TITLE_SUFFIX);

            title = resolveMessage(titleCode, NotificationConstants.VALIDATION_FAILED_MESSAGE_TITLE_CODE, NotificationConstants.VALIDATION_FAILED_DEFAULT_TITLE);
        }

        final String content = resolveMessage(NotificationConstants.VALIDATION_FAILED_CONTENT_CODE, null);
        final List<ValidationError> validationErrorList = convertValidationErrorsToMessageList(errors, validationFailedOwningType);
        final List<String> additionalNotificationDataMessageList = resolveMessageListFromNotificationData(additionalNotificationData.getMessageListData());
        final List<String> validationMessageList = validationErrorList.stream().flatMap(value -> value.getErrorMessageList().stream()).collect(Collectors.toList());

        final List<String> messageList = Stream.concat(additionalNotificationDataMessageList.stream(), validationMessageList.stream()).collect(Collectors.toList());

        return new ValidationFailureNotification(title, content, messageList, severity, additionalNotificationData.getUxNotificationOptions(), validationErrorList);
    }

    @Override
    public ValidationFailureNotification createNotificationForValidationFailure(final ConstraintViolationException exception, final AdditionalNotificationData additionalNotificationData) {
        final Object target = constraintConversionService.resolveTarget(exception.getConstraintViolations());
        final Errors errors = constraintConversionService.convertConstraintViolationsToErrors(exception.getConstraintViolations(), target, NotificationConstants.UNKNOWN_VALIDATION_TARGET);

        final Class<?> targetClass = Optional.ofNullable(target).map(Object::getClass).orElse(null);

        return createNotificationForValidationFailure(errors, targetClass, additionalNotificationData);
    }

    @Override
    public Notification createNotificationForException(final Throwable throwable, final AdditionalNotificationData additionalNotificationData, Object... exceptionMessageArgumentList) {
        final String typeName = throwable.getClass().getName();
        final String titleCode = String.format(NotificationConstants.PREFIX_MESSAGE_FORMAT, typeName, NotificationConstants.MESSAGE_TITLE_SUFFIX);

        final String title = resolveMessage(titleCode, NotificationConstants.ERROR_OCCURRED_MESSAGE_TITLE_CODE, NotificationConstants.ERROR_OCCURRED_DEFAULT_TITLE);
        final String messageCode = String.format(NotificationConstants.PREFIX_MESSAGE_FORMAT, typeName, NotificationConstants.MESSAGE_SUFFIX);
        final String severityCode = String.format(NotificationConstants.PREFIX_MESSAGE_FORMAT, typeName, NotificationConstants.MESSAGE_SEVERITY_SUFFIX);

        final String content = resolveMessage(messageCode, NotificationConstants.ERROR_OCCURRED_DEFAULT_CODE, null, exceptionMessageArgumentList);
        final NotificationSeverity severity = Optional.ofNullable(additionalNotificationData.getSeverity()).orElse(resolveExceptionSeverity(severityCode));

        return new Notification(title, content, resolveMessageListFromNotificationData(additionalNotificationData.getMessageListData()), severity, additionalNotificationData.getUxNotificationOptions());
    }

    @Override
    public Notification createNotificationForAction(final String actionName, final AdditionalNotificationData additionalNotificationData) {
        final String titleCode = String.format(NotificationConstants.PREFIX_MESSAGE_FORMAT, actionName, NotificationConstants.MESSAGE_TITLE_SUFFIX);
        final String messageCode = String.format(NotificationConstants.PREFIX_MESSAGE_FORMAT, actionName, NotificationConstants.MESSAGE_SUFFIX);

        final String title = resolveMessage(titleCode, NotificationConstants.SUCCESS_MESSAGE_TITLE_CODE, NotificationConstants.SUCCESS_DEFAULT_TITLE);
        final String content = resolveMessage(messageCode, NotificationConstants.SUCCESS_DEFAULT_CODE, null, (Object[]) null);
        final NotificationSeverity severity = Optional.ofNullable(additionalNotificationData.getSeverity()).orElse(NotificationSeverity.INFO);

        return new Notification(title, content, resolveMessageListFromNotificationData(additionalNotificationData.getMessageListData()), severity, additionalNotificationData.getUxNotificationOptions());
    }

    private List<ValidationError> convertValidationErrorsToMessageList(final Errors errors, final Class<?> validationFailedOwningType) {
        final Map<String, List<String>> resultMap = new LinkedHashMap<>();

        for (final ObjectError objectError : errors.getAllErrors()) {
            final String constraintFieldName = constraintFieldNameOrDefault(objectError, ValidationError.CONTAINING_OBJECT_NAME);
            final String message = resolveMessageForObjectError(validationFailedOwningType, objectError);

            if (!resultMap.containsKey(constraintFieldName)) {
                resultMap.put(constraintFieldName, new ArrayList<>());
            }

            resultMap.get(constraintFieldName).add(message);
        }

        return resultMap.entrySet().stream()
                .map(value -> new ValidationError(value.getKey(), value.getValue()))
                .collect(Collectors.toList());
    }

    private String resolveMessageForObjectError(final Class<?> validationFailedOwningType, final ObjectError objectError) {
        final String constraintName = objectError.getCode();
        final String fieldName = constraintFieldNameOrDefault(objectError, null);
        final String name = validationFailedOwningType == null ? "" : StringUtils.uncapitalize(validationFailedOwningType.getName());
        final String shortName = validationFailedOwningType == null ? "" : StringUtils.uncapitalize(validationFailedOwningType.getSimpleName());

        final List<String> messageCodeList = new ArrayList<>();

        messageCodeList.add(String.format(NotificationConstants.PREFIX_MESSAGE_FORMAT, constraintName, NotificationConstants.INVALID_SUFFIX));

        if (fieldName == null) {
            messageCodeList.add(String.format(NotificationConstants.CONSTRAINT_SHORT_MESSAGE_FORMAT, name, constraintName, NotificationConstants.INVALID_SUFFIX));
            messageCodeList.add(String.format(NotificationConstants.CONSTRAINT_SHORT_MESSAGE_FORMAT, shortName, constraintName, NotificationConstants.INVALID_SUFFIX));
        }
        else {
            messageCodeList.add(String.format(NotificationConstants.CONSTRAINT_SHORT_MESSAGE_FORMAT, fieldName, constraintName, NotificationConstants.INVALID_SUFFIX));
            messageCodeList.add(String.format(NotificationConstants.CONSTRAINT_FULL_MESSAGE_FORMAT, name, fieldName, constraintName, NotificationConstants.INVALID_SUFFIX));
            messageCodeList.add(String.format(NotificationConstants.CONSTRAINT_FULL_MESSAGE_FORMAT, shortName, fieldName, constraintName, NotificationConstants.INVALID_SUFFIX));
        }

        if (objectError.getCodes() != null) {
            messageCodeList.addAll(Arrays.asList(objectError.getCodes()));
        }

        String message = resolveMessage(messageCodeList, argumentsWithoutMessageCodeResolvable(objectError.getArguments()), objectError.getDefaultMessage());

        if (fieldName != null) {
            messageCodeList.clear();

            messageCodeList.add(String.format(NotificationConstants.CONSTRAINT_SHORT_MESSAGE_FORMAT, name, fieldName, NotificationConstants.FIELD_LABEL_SUFFIX));
            messageCodeList.add(String.format(NotificationConstants.CONSTRAINT_SHORT_MESSAGE_FORMAT, shortName, fieldName, NotificationConstants.FIELD_LABEL_SUFFIX));

            final String fieldNameMessage = resolveMessage(messageCodeList, null, fieldName);

            message = fieldNameMessage + ": " + message;
        }

        return message;
    }

    private Object[] argumentsWithoutMessageCodeResolvable(final Object[] arguments) {
        if (arguments == null || arguments.length == 0) {
            return null;
        }

        Object[] filteredArguments = arguments;
        if ((arguments[0] instanceof DefaultMessageSourceResolvable)) {
            filteredArguments = Arrays.copyOfRange(arguments, 1, arguments.length);
        }

        return Arrays.stream(filteredArguments)
                .map(value -> value instanceof Object[] ? convertToString((Object[]) value) : value)
                .toArray();
    }

    private String constraintFieldNameOrDefault(final ObjectError objectError, final String defaultValue) {
        return objectError instanceof FieldError ? ((FieldError) objectError).getField() : defaultValue;
    }

    private NotificationSeverity resolveExceptionSeverity(final String messageCode) {
        return NotificationSeverity.valueOf(resolveMessage(messageCode, NotificationSeverity.ERROR.name()));
    }

    private List<String> resolveMessageListFromNotificationData(final Map<String, ?> additionalNotificationData) {
        if (additionalNotificationData == null) {
            return Collections.emptyList();
        }

        return additionalNotificationData.entrySet().stream()
                .map(this::resolveMessageForAdditionalData)
                .filter(message -> !NotificationConstants.UNDEFINED_MESSAGE_VALUE.equals(message))
                .collect(Collectors.toList());
    }

    private String resolveMessageForAdditionalData(final Map.Entry<String, ?> additionalDataEntry) {
        final String messageCode = String.format(NotificationConstants.ADDITIONAL_EXCEPTION_DATA_MESSAGE_CODE_FORMAT, additionalDataEntry.getKey());

        return resolveMessage(messageCode, NotificationConstants.UNDEFINED_MESSAGE_VALUE, additionalDataEntry.getValue());
    }

    private String resolveMessage(final List<String> messageCodeList, final Object[] argumentList, final String defaultMessage) {
        final DefaultMessageSourceResolvable messageCodeResolvable = new DefaultMessageSourceResolvable(messageCodeList.toArray(new String[0]), argumentsWithoutMessageCodeResolvable(argumentList), defaultMessage);

        return messageSource.getMessage(messageCodeResolvable, LocaleContextHolder.getLocale());
    }

    private String resolveMessage(final String messageCode, final String defaultMessage, final Object... arguments) {
        return resolveMessage(messageCode, null, defaultMessage, arguments);
    }

    private String resolveMessage(final String messageCode, final String defaultMessageCode, final String defaultMessage, final Object... arguments) {
        final DefaultMessageSourceResolvable messageSourceResolvable = new DefaultMessageSourceResolvable(new String[] { messageCode, defaultMessageCode }, arguments, defaultMessage);

        return messageSource.getMessage(messageSourceResolvable, LocaleContextHolder.getLocale());
    }

    private String convertToString(final Object[] value) {
        return Arrays.toString(value).replace('[', ' ').replace(']', ' ').trim();
    }
}
