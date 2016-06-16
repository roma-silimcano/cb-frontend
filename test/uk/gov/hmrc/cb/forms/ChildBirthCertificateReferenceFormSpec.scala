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
import uk.gov.hmrc.cb.forms.ChildBirthCertificateReferenceForm.ChildBirthCertificateReferencePageModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by chrisianson on 06/06/16.
  */
class ChildBirthCertificateReferenceFormSpec extends UnitSpec with WithFakeApplication {

  "ChildBirthCertificateReferenceForm" should {

    "accept a valid value for birth-certificate-ref" in {

      val data = ChildBirthCertificateReferencePageModel(birthCertificateReference = "123456789")
      ChildBirthCertificateReferenceForm.form.bind(
        Map(
          "birthCertificateReference" -> "123456789"
        )
      ).fold(
        formWithErrors => {
          formWithErrors.errors shouldBe empty
        },
        success => {
          success shouldBe data
        }
      )
    }

    "throw a ValidationError when birthCertificateReference is less than 9 digits in length" in {
      ChildBirthCertificateReferenceForm.form.bind(
        Map(
          "birthCertificateReference" -> "123"
        )
      ).fold(
        hasErrors =>
          hasErrors.errors.head.message shouldBe "Please check your Child Benefit reference number and re-enter it",
        success => {
          success should not be Some(ChildBirthCertificateReferencePageModel(_))
          success shouldBe None
        }
      )
    }

    "throw a ValidationError when birthCertificateReference is more than 9 digits in length" in {
      ChildBirthCertificateReferenceForm.form.bind(
        Map(
          "birthCertificateReference" -> "1234567891"
        )
      ).fold(
        hasErrors =>
          hasErrors.errors.head.message shouldBe "Please check your Child Benefit reference number and re-enter it",
        success => {
          success should not be Some(ChildBirthCertificateReferencePageModel(_))
          success shouldBe None
        }
      )
    }

    "throw a ValidationError when birthCertificateReference is less than 000000001" in {
      ChildBirthCertificateReferenceForm.form.bind(
        Map(
          "birthCertificateReference" -> "000000000"
        )
      ).fold(
        hasErrors =>
          hasErrors.errors.head.message shouldBe "Please check your Child Benefit reference number and re-enter it",
        success => {
          success should not be Some(ChildBirthCertificateReferencePageModel(_))
          success shouldBe None
        }
      )
    }

    "throw a ValidationError when birthCertificateReference is characters only" in {
      ChildBirthCertificateReferenceForm.form.bind(
        Map(
          "birthCertificateReference" -> "abcdefghi"
        )
      ).fold(
        hasErrors =>
          hasErrors.errors.head.message shouldBe "Please check your Child Benefit reference number and re-enter it",
        success => {
          success should not be Some(ChildBirthCertificateReferencePageModel(_))
          success shouldBe None
        }
      )
    }
  }
}
