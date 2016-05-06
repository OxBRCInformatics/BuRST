package ox.softeng.burst.grails.plugin.rabbitmq.databinding

import grails.core.GrailsApplication
import grails.core.GrailsDomainClass
import grails.databinding.DataBinder
import grails.databinding.DataBindingSource
import grails.util.Environment
import grails.util.Holders
import grails.validation.ValidationErrors
import grails.web.databinding.DataBindingUtils
import grails.web.databinding.GrailsWebDataBinder
import grails.web.mime.MimeType
import groovy.transform.CompileStatic
import org.grails.core.artefact.DomainClassArtefactHandler
import org.grails.web.databinding.DefaultASTDatabindingHelper
import org.grails.web.databinding.bindingsource.DataBindingSourceRegistry
import org.grails.web.databinding.bindingsource.InvalidRequestBodyException
import org.springframework.context.ApplicationContext
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError

import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap

import static grails.web.databinding.DataBindingUtils.DATA_BINDER_BEAN_NAME

/**
 * @since 19/02/2016
 */
@CompileStatic
public class RabbitDataBindingUtils {

    private static final String BLANK = "";
    private static final Map<Class, List> CLASS_TO_BINDING_INCLUDE_LIST = new ConcurrentHashMap<>();

    /**
     * Binds the given source object to the given target object performing type conversion if necessary
     *
     * @param object The object to bind to
     * @param source The source object
     * @param include The list of properties to include
     * @param exclude The list of properties to exclude
     * @param filter The prefix to filter by
     *
     * @return A BindingResult or null if it wasn't successful
     */
    public static BindingResult bindObjectToInstance(Object object, Object source, MimeType mimeType, List include, List exclude, String filter) {
        if (include == null && exclude == null) {
            include = getBindingIncludeList(object);
        }
        GrailsApplication application = Holders.findApplication();
        GrailsDomainClass domain = null;
        if (application != null) {
            domain = (GrailsDomainClass) application.getArtefact(DomainClassArtefactHandler.TYPE, object.getClass().getName());
        }
        return bindObjectToDomainInstance(domain, object, source, mimeType, include, exclude, filter);
    }

    /**
     * Binds the given source object to the given target object performing type conversion if necessary
     *
     * @param domain The GrailsDomainClass instance
     * @param object The object to bind to
     * @param source The source object
     * @param include The list of properties to include
     * @param exclude The list of properties to exclude
     * @param filter The prefix to filter by
     *
     * @see grails.core.GrailsDomainClass
     *
     * @return A BindingResult or null if it wasn't successful
     */
    @SuppressWarnings("unchecked")
    public static BindingResult bindObjectToDomainInstance(GrailsDomainClass domain, Object object,
                                                           Object source, MimeType mimeType, List include, List exclude, String filter) {
        BindingResult bindingResult = null;
        GrailsApplication grailsApplication = null;
        if (domain != null) {
            grailsApplication = domain.getApplication();
        }
        if (grailsApplication == null) {
            grailsApplication = Holders.findApplication();
        }
        try {
            final DataBindingSource bindingSource = createDataBindingSource(grailsApplication, object.getClass(), source, mimeType);
            final DataBinder grailsWebDataBinder = getGrailsWebDataBinder(grailsApplication);
            grailsWebDataBinder.bind(object, bindingSource, filter, include, exclude);
        } catch (InvalidRequestBodyException invalid) {
            String messageCode = "invalidRequestBody${mimeType.getExtension() ? ".${mimeType.getExtension()}" : ''}";
            Class objectType = object.getClass();
            String defaultMessage = "An error occurred parsing the body of the request: {0}";
            String[] codes = getMessageCodes(messageCode, objectType);
            bindingResult = new BeanPropertyBindingResult(object, objectType.getName());
            bindingResult.addError(new ObjectError(bindingResult.getObjectName(), codes,
                                                   [invalid.cause.message] as Object[],
                                                   defaultMessage));
        } catch (Exception e) {
            bindingResult = new BeanPropertyBindingResult(object, object.getClass().getName());
            bindingResult.addError(new ObjectError(bindingResult.getObjectName(), e.getMessage()));
        }

        if (domain != null && bindingResult != null) {
            BindingResult newResult = new ValidationErrors(object);
            for (Object error : bindingResult.getAllErrors()) {
                if (error instanceof FieldError) {
                    FieldError fieldError = (FieldError) error;
                    final boolean isBlank = BLANK.equals(fieldError.getRejectedValue());
                    if (!isBlank) {
                        newResult.addError(fieldError);
                    }
                    else if (domain.hasPersistentProperty(fieldError.getField())) {
                        final boolean isOptional = domain.getPropertyByName(fieldError.getField()).isOptional();
                        if (!isOptional) {
                            newResult.addError(fieldError);
                        }
                    }
                    else {
                        newResult.addError(fieldError);
                    }
                }
                else {
                    newResult.addError((ObjectError) error);
                }
            }
            bindingResult = newResult;
        }
        MetaClass mc = GroovySystem.getMetaClassRegistry().getMetaClass(object.getClass());
        if (mc.hasProperty(object, "errors") != null && bindingResult != null) {
            ValidationErrors errors = new ValidationErrors(object);
            errors.addAllErrors(bindingResult);
            mc.setProperty(object, "errors", errors);
        }
        return bindingResult;
    }

    public static DataBindingSource createDataBindingSource(GrailsApplication grailsApplication, Class bindingTargetType, Object bindingSource,
                                                            MimeType mimeType) {
        final DataBindingSourceRegistry registry = DataBindingUtils.getDataBindingSourceRegistry(grailsApplication);
        return registry.createDataBindingSource(mimeType, bindingTargetType, bindingSource);
    }

    private static String[] getMessageCodes(String messageCode, Class objectType) {
        return [objectType.getName() + "." + messageCode, messageCode] as String[];
    }

    private static List getBindingIncludeList(final Object object) {
        List includeList = Collections.EMPTY_LIST;
        try {
            final Class<?> objectClass = object.getClass();
            if (CLASS_TO_BINDING_INCLUDE_LIST.containsKey(objectClass)) {
                includeList = CLASS_TO_BINDING_INCLUDE_LIST.get(objectClass);
            }
            else {
                final Field whiteListField = objectClass.getDeclaredField(DefaultASTDatabindingHelper.DEFAULT_DATABINDING_WHITELIST);
                if (whiteListField != null) {
                    if ((whiteListField.getModifiers() & Modifier.STATIC) != 0) {
                        final Object whiteListValue = whiteListField.get(objectClass);
                        if (whiteListValue instanceof List) {
                            includeList = (List) whiteListValue;
                        }
                    }
                }
                if (!Environment.getCurrent().isReloadEnabled()) {
                    CLASS_TO_BINDING_INCLUDE_LIST.put(objectClass, includeList);
                }
            }
        } catch (Exception ignored) {
        }
        return includeList;
    }

    private static DataBinder getGrailsWebDataBinder(final GrailsApplication grailsApplication) {
        DataBinder dataBinder = null;
        if (grailsApplication != null) {
            final ApplicationContext mainContext = grailsApplication.getMainContext();
            if (mainContext != null && mainContext.containsBean(DATA_BINDER_BEAN_NAME)) {
                dataBinder = mainContext.getBean(DATA_BINDER_BEAN_NAME, DataBinder.class);
            }
        }
        if (dataBinder == null) {
            // this should really never happen in the running app as the binder
            // should always be found in the context
            dataBinder = new GrailsWebDataBinder(grailsApplication);
        }
        return dataBinder;
    }
}
