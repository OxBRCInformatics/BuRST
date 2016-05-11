package ox.softeng.burst.grails.plugin.utils

import org.grails.datastore.mapping.validation.ValidationException
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Errors
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @since 10/05/2016
 */
class Utils {
    final static Pattern EXCEPTION_MESSAGE = ~/Validation error whilst flushing entity \[(?<entity>.+)\]/

    final static Pattern FIELD_ERROR_START =
            ~/Field error in object '(?<objectName>.+?)' on field '(?<field>.+?)': rejected value \[(?<rejectedValue>.+?)\]; /
    final static Pattern OBJECT_ERROR_START =
            ~/\s*- Error in object '(?<objectName>.+?)': /
    final static Pattern RESOLVED_ERROR_STRING =
            ~/codes \[(?<codes>.+?)\]; arguments \[(?<arguments>.+?)\]; default message \[(?<defaultMessage>.+)\]/

    static Errors extractErrorsFromException(ValidationException exception) {
        extractErrorsFromExceptionMessage(exception.getMessage())
    }

    static Errors extractErrorsFromExceptionMessage(String exceptionMessage) {

        Errors errors
        Class entity
        Matcher matcher = EXCEPTION_MESSAGE.matcher(exceptionMessage)
        if (matcher.find()) {
            entity = Class.forName(matcher.group('entity'))
        }
        else entity = ValidationException

        errors = new BeanPropertyBindingResult(entity.newInstance(), matcher.group('entity'))

        splitErrorsString(errors, exceptionMessage)
    }

    static Errors splitErrorsString(BeanPropertyBindingResult errors, String errorsString) {

        Matcher matcher
        errorsString.findAll(FIELD_ERROR_START.toString() + RESOLVED_ERROR_STRING.toString()).each {fes ->
            matcher = FIELD_ERROR_START.matcher(fes)
            matcher.find()

            String objName = matcher.group('objectName')
            String field = matcher.group('field')
            String rejValue = matcher.group('rejectedValue')

            matcher = RESOLVED_ERROR_STRING.matcher(fes)
            matcher.find()

            String[] codes = matcher.group('codes').split(',')
            String[] arguments = matcher.group('arguments').split(',')

            errors.addError(
                    new FieldError(objName, field, rejValue, false, codes, arguments,
                                   matcher.group('defaultMessage')
                    )
            )
        }

        errorsString.findAll(OBJECT_ERROR_START.toString() + RESOLVED_ERROR_STRING.toString()).each {fes ->
            matcher = OBJECT_ERROR_START.matcher(fes)
            matcher.find()

            String objName = matcher.group('objectName')

            matcher = RESOLVED_ERROR_STRING.matcher(fes)
            matcher.find()

            errors.addError(
                    new ObjectError(objName,
                                    matcher.group('codes'),
                                    matcher.group('arguments'),
                                    matcher.group('defaultMessage')
                    )
            )
        }
        errors
    }


}
