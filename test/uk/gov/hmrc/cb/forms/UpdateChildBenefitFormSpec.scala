package uk.gov.hmrc.cb.forms

import play.api.i18n.Messages
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}

/**
 * Created by adamconder on 04/05/2016.
 */
class UpdateChildBenefitFormSpec extends UnitSpec with WithFakeApplication {

  "UpdateChildBenefitForm" should {

    "accept a valid value (false)" in {
      val result = UpdateChildBenefitPageModel(updateChildBenefit = Some(false))
      UpdateChildBenefitForm.form.bing(
        Map(
          "updateChildBenefit" -> "false"
        )
      ).fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe result
        )
    }

    "accept a valid value (true)" in {
      val result = UpdateChildBenefitPageModel(updateChildBenefit = Some(true))
      UpdateChildBenefitForm.form.bing(
        Map(
          "updateChildBenefit" -> "true"
        )
      ).fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe result
        )
    }

    "throw ValidationError when value is not provided" in {
      UpdateChildBenefitForm.form.bing(
        Map(
          "updateChildBenefit" -> ""
        )
      ).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cb.error.update.child.benefit.required"),
          success =>
            success should not be Some(_)
        )
    }

    "throw ValidationError when characters are provided" in {
      UpdateChildBenefitForm.form.bing(
        Map(
          "updateChildBenefit" -> "adam"
        )
      ).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cb.error.update.child.benefit.required"),
          success =>
            success should not be Some(_)
        )
    }

    "throw ValidationError when special characters are provided" in {
      UpdateChildBenefitForm.form.bing(
        Map(
          "updateChildBenefit" -> "$%%&!@;"
        )
      ).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cb.error.update.child.benefit.required"),
          success =>
            success should not be Some(_)
        )
    }

    "return a value when unbinding the form" in {
      val result = UpdateChildBenefitPageModel(updateChildBenefit = Some(true))
      val form = UpdateChildBenefitForm.form.fill(result)
      form.get shouldBe result
    }

  }

}
