package ox.softeng.burst.grails.plugin.utils

import org.springframework.validation.Errors
import spock.lang.Specification

/**
 * @since 10/05/2016
 */
class UtilsTest extends Specification {

    void 'split errors string'() {

        when:
        String fullString =
"- org.grails.datastore.mapping.validation.ValidationException: Validation error whilst flushing entity [ox.softeng.burst.grails.plugin.utils" +
".UtilsTest] : \n\n"+

 "- Field error in object 'ox.softeng.burst.grails.plugin.utils.UtilsTest' on field 'opcs4': rejected value [null]; codes [uk.ac.ox.ndm.mercury.gel"+
 ".rare.diseases.v1_3.intervention.PrimaryTreatment.opcs4.validator.error.uk.ac.ox.ndm.mercury.gel.rare.diseases.v1_3.intervention.PrimaryTreatment"+
".opcs4,uk.ac.ox.ndm.mercury.gel.rare.diseases.v1_3.intervention.PrimaryTreatment.opcs4.validator.error.opcs4,uk.ac.ox.ndm.mercury.gel.rare"+
".diseases.v1_3.intervention.PrimaryTreatment.opcs4.validator.error.java.lang.String,uk.ac.ox.ndm.mercury.gel.rare.diseases.v1_3.intervention"+
".PrimaryTreatment.opcs4.validator.error,primaryTreatment.opcs4.validator.error.uk.ac.ox.ndm.mercury.gel.rare.diseases.v1_3.intervention"+
".PrimaryTreatment.opcs4,primaryTreatment.opcs4.validator.error.opcs4,primaryTreatment.opcs4.validator.error.java.lang.String,primaryTreatment"+
".opcs4.validator.error,uk.ac.ox.ndm.mercury.gel.rare.diseases.v1_3.intervention.PrimaryTreatment.opcs4.validation.choice.atleastone.uk.ac.ox.ndm"+
".mercury.gel.rare.diseases.v1_3.intervention.PrimaryTreatment.opcs4,uk.ac.ox.ndm.mercury.gel.rare.diseases.v1_3.intervention.PrimaryTreatment"+
".opcs4.validation.choice.atleastone.opcs4,uk.ac.ox.ndm.mercury.gel.rare.diseases.v1_3.intervention.PrimaryTreatment.opcs4.validation.choice"+
".atleastone.java.lang.String,uk.ac.ox.ndm.mercury.gel.rare.diseases.v1_3.intervention.PrimaryTreatment.opcs4.validation.choice.atleastone,"+
"primaryTreatment.opcs4.validation.choice.atleastone.uk.ac.ox.ndm.mercury.gel.rare.diseases.v1_3.intervention.PrimaryTreatment.opcs4,"+
"primaryTreatment.opcs4.validation.choice.atleastone.opcs4,primaryTreatment.opcs4.validation.choice.atleastone.java.lang.String,primaryTreatment"+
".opcs4.validation.choice.atleastone,validation.choice.atleastone.uk.ac.ox.ndm.mercury.gel.rare.diseases.v1_3.intervention.PrimaryTreatment.opcs4,"+
"validation.choice.atleastone.opcs4,validation.choice.atleastone.java.lang.String,validation.choice.atleastone]; arguments [opcs4,class uk.ac.ox"+
".ndm.mercury.gel.rare.diseases.v1_3.intervention.PrimaryTreatment,null,treatment]; default message [Property [{0}] of class [{1}] with value [{2}]"+
" does not pass custom validation]\n\n"+

"- Field error in object 'ox.softeng.burst.grails.plugin.utils.UtilsTest' on field 'snomedCt': rejected value [null]; codes [uk.ac.ox.ndm.mercury"+
".gel.rare.diseases.v1_3.intervention.PrimaryTreatment.snomedCt.validator.error.uk.ac.ox.ndm.mercury.gel.rare.diseases.v1_3.intervention"+
".PrimaryTreatment.snomedCt,uk.ac.ox.ndm.mercury.gel.rare.diseases.v1_3.intervention.PrimaryTreatment.snomedCt.validator.error.snomedCt,uk.ac.ox"+
".ndm.mercury.gel.rare.diseases.v1_3.intervention.PrimaryTreatment.snomedCt.validator.error.java.lang.String,uk.ac.ox.ndm.mercury.gel.rare.diseases"+
".v1_3.intervention.PrimaryTreatment.snomedCt.validator.error,primaryTreatment.snomedCt.validator.error.uk.ac.ox.ndm.mercury.gel.rare.diseases.v1_3"+
".intervention.PrimaryTreatment.snomedCt,primaryTreatment.snomedCt.validator.error.snomedCt,primaryTreatment.snomedCt.validator.error.java.lang"+
".String,primaryTreatment.snomedCt.validator.error,uk.ac.ox.ndm.mercury.gel.rare.diseases.v1_3.intervention.PrimaryTreatment.snomedCt.validation"+
".choice.atleastone.uk.ac.ox.ndm.mercury.gel.rare.diseases.v1_3.intervention.PrimaryTreatment.snomedCt,uk.ac.ox.ndm.mercury.gel.rare.diseases.v1_3"+
".PrimaryTreatment.snomedCt.validation.choice.atleastone.java.lang.String,uk.ac.ox.ndm.mercury.gel.rare.diseases.v1_3.intervention.PrimaryTreatment"+
".intervention.PrimaryTreatment.snomedCt.validation.choice.atleastone.snomedCt,uk.ac.ox.ndm.mercury.gel.rare.diseases.v1_3.intervention"+
".snomedCt.validation.choice.atleastone,primaryTreatment.snomedCt.validation.choice.atleastone.uk.ac.ox.ndm.mercury.gel.rare.diseases.v1_3"+
".intervention.PrimaryTreatment.snomedCt,primaryTreatment.snomedCt.validation.choice.atleastone.snomedCt,primaryTreatment.snomedCt.validation"+
".choice.atleastone.java.lang.String,primaryTreatment.snomedCt.validation.choice.atleastone,validation.choice.atleastone.uk.ac.ox.ndm.mercury.gel"+
".rare.diseases.v1_3.intervention.PrimaryTreatment.snomedCt,validation.choice.atleastone.snomedCt,validation.choice.atleastone.java.lang.String,"+
"validation.choice.atleastone]; arguments [snomedCt,class uk.ac.ox.ndm.mercury.gel.rare.diseases.v1_3.intervention.PrimaryTreatment,null,"+
 "treatment]; default message [Property [{0}] of class [{1}] with value [{2}] does not pass custom validation]"

        Errors errors = Utils.extractErrorsFromExceptionMessage(fullString)

        then:
        errors.allErrors.size() == 2

    }
}
