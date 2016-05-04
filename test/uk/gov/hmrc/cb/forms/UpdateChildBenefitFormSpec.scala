/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.cb.forms

import play.api.i18n.Messages
import uk.gov.hmrc.cb.forms.UpdateChildBenefitForm.UpdateChildBenefitPageModel
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}

/**
 * Created by adamconder on 04/05/2016.
 */
class UpdateChildBenefitFormSpec extends UnitSpec with WithFakeApplication {

  "UpdateChildBenefitForm" should {

    "accept a valid value (false)" in {
      val result = UpdateChildBenefitPageModel(updateChildBenefit = Some(false))
      UpdateChildBenefitForm.form.bind(
        Map(
          "updateChildBenefit" -> "false"
        )
      ).fold(
          errors => {
            errors.errors shouldBe empty
          },
          success => {
            success shouldBe result
          }
        )
    }

    "accept a valid value (true)" in {
      val result = UpdateChildBenefitPageModel(updateChildBenefit = Some(true))
      UpdateChildBenefitForm.form.bind(
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
      UpdateChildBenefitForm.form.bind(
        Map(
          "updateChildBenefit" -> ""
        )
      ).fold(
          hasErrors =>
            hasErrors.errors.head.message shouldBe Messages("cb.error.update.child.benefit.required"),
          success => {
            success should not be Some(UpdateChildBenefitPageModel(_))
            success shouldBe None
          }
        )
    }

    "throw ValidationError when characters are provided" in {
      UpdateChildBenefitForm.form.bind(
        Map(
          "updateChildBenefit" -> "adam"
        )
      ).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("error.boolean"),
          success =>
            success should not be Some(UpdateChildBenefitPageModel(_))
        )
    }

    "throw validation error when numbers are provided" in {
      UpdateChildBenefitForm.form.bind(
      Map(
        "updateChildBenefit" -> "1234"
        )
      ).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("error.boolean"),
          success =>
            success should not be Some(UpdateChildBenefitPageModel(_))
      )
    }

    "throw a validation error when special characters are provided" in {
      UpdateChildBenefitForm.form.bind(
        Map(
          "updateChildBenefit" -> "!@£$%^&*()_"
        )
      ).fold(
        errors =>
          errors.errors.head.message shouldBe Messages("error.boolean"),
        success =>
          success should not be Some(UpdateChildBenefitPageModel(_))
        )
    }

    "return a value when unbinding the form" in {
      val result = UpdateChildBenefitPageModel(updateChildBenefit = Some(true))
      val form = UpdateChildBenefitForm.form.fill(result)
      form.get shouldBe result
    }

  }

}
