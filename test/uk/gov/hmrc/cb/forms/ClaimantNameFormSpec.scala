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

import uk.gov.hmrc.cb.forms.ClaimantNameForm.ClaimantNamePageModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

/**
  * Created by chrisianson on 23/06/16.
  */
class ClaimantNameFormSpec extends UnitSpec with WithFakeApplication {

  val invalidLength = "tZWfHEhJlSIdAuSnaSjGgWTWeYWrPXYrrtyy"

  "ClaimantNameForm" should {

    /* valid */

    "Accept a first name and last name value" in {
      val data = ClaimantNamePageModel(firstName = "John", lastName = "Scott")
      ClaimantNameForm.form.bind(
        Map(
          "firstName" -> "John",
          "lastName" -> "Scott"
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

    "accept a valid value for first name and last name which have a space" in {
      val data = ClaimantNamePageModel(firstName = "John David", lastName = "Scott Mark")
      ClaimantNameForm.form.bind(
        Map(
          "firstName" -> "John David",
          "lastName" -> "Scott Mark"
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

    "Accept a first name with a apostrophe" in {
      val data = ClaimantNamePageModel(firstName = "John'Paul", lastName = "Scott")
      ClaimantNameForm.form.bind(
        Map(
          "firstName" -> "John'Paul",
          "lastName" -> "Scott"
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

    "Accept a last name with a apostrophe" in {
      val data = ClaimantNamePageModel(firstName = "John", lastName = "O'brian")
      ClaimantNameForm.form.bind(
        Map(
          "firstName" -> "John",
          "lastName" -> "O'brian"
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

    "Accept a first name with a hyphen" in {
      val data = ClaimantNamePageModel(firstName = "Mary-Louise", lastName = "Jones")
      ClaimantNameForm.form.bind(
        Map(
          "firstName" -> "Mary-Louise",
          "lastName" -> "Jones"
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

    "Accept a last name with a hyphen" in {
      val data = ClaimantNamePageModel(firstName = "Sarah", lastName = "Mary-Anne")
      ClaimantNameForm.form.bind(
        Map(
          "firstName" -> "Sarah",
          "lastName" -> "Mary-Anne"
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

    "Return values from the form" in {
      val data = ClaimantNamePageModel(firstName = "John", lastName = "Scott")
      val form = ClaimantNameForm.form.fill(data)
      form.get shouldBe data
    }

    /* invalid */

    "throw a ValidationError when first name has invalid characters" in {
      ClaimantNameForm.form.bind(
        Map(
          "firstName" -> "@£$^%&&&&",
          "lastName" -> "Scott"
        )
      ).fold(
        formWithErrors => {
          formWithErrors.errors.head.message shouldBe "You've entered a character we don't recognise, please re-enter the name"
          formWithErrors.errors.length shouldBe 1
        },
        success => {
          success should not be Some(ClaimantNamePageModel(_, _))
          success shouldBe None
        }
      )
    }

    "throw a ValidationError when last name has invalid characters" in {
      ClaimantNameForm.form.bind(
        Map(
          "firstName" -> "Chris",
          "lastName" -> "@£$^%&&&&"
        )
      ).fold(
        formWithErrors => {
          formWithErrors.errors.head.message shouldBe "You've entered a character we don't recognise, please re-enter the name"
          formWithErrors.errors.length shouldBe 1
        },
        success => {
          success should not be Some(ClaimantNamePageModel(_, _))
          success shouldBe None
        }
      )
    }

    "throw a ValidationError when first name and last name has invalid characters" in {
      ClaimantNameForm.form.bind(
        Map(
          "firstName" -> "@£$^%&&&&",
          "lastName" -> "@£$^%&&&&"
        )
      ).fold(
        formWithErrors => {
          formWithErrors.errors.head.message shouldBe "You've entered a character we don't recognise, please re-enter the name"
          formWithErrors.errors.length shouldBe 2
        },
        success => {
          success should not be Some(ClaimantNamePageModel(_, _))
          success shouldBe None
        }
      )
    }

    "throw a ValidationError when the first name character count exceeds allowed length" in {
      ClaimantNameForm.form.bind(
        Map(
          "firstName" -> invalidLength,
          "lastName" -> "Jones"
        )
      ).fold(
        formWithErrors => {
          formWithErrors.errors.head.message shouldBe "The name you've entered is too long, please re-enter it"
          formWithErrors.errors.length shouldBe 1
        },
        success => {
          success should not be Some(ClaimantNamePageModel(_, _))
          success shouldBe None
        }
      )
    }

    "throw a ValidationError when the last name character count exceeds allowed length" in {
      ClaimantNameForm.form.bind(
        Map(
          "firstName" -> "Chris",
          "lastName" -> invalidLength
        )
      ).fold(
        formWithErrors => {
          formWithErrors.errors.head.message shouldBe "The name you've entered is too long, please re-enter it"
          formWithErrors.errors.length shouldBe 1
        },
        success => {
          success should not be Some(ClaimantNamePageModel(_, _))
          success shouldBe None
        }
      )
    }

    "throw a ValidationError when the first name and last name character count exceeds allowed length" in {
      ClaimantNameForm.form.bind(
        Map(
          "firstName" -> invalidLength,
          "lastName" -> invalidLength
        )
      ).fold(
        formWithErrors => {
          formWithErrors.errors.head.message shouldBe "The name you've entered is too long, please re-enter it"
          formWithErrors.errors.length shouldBe 2
        },
        success => {
          success should not be Some(ClaimantNamePageModel(_, _))
          success shouldBe None
        }
      )
    }

    "throw a ValidationError when the first name is excluded" in {
      ClaimantNameForm.form.bind(
        Map(
          "firstName" -> "",
          "lastName" -> "Jones"
        )
      ).fold(
        formWithErrors => {
          formWithErrors.errors.head.message shouldBe "Please enter the name of the person claiming"
          formWithErrors.errors.length shouldBe 1
        },
        success => {
          success should not be Some(ClaimantNamePageModel(_, _))
          success shouldBe None
        }
      )
    }

    "throw a ValidationError when the last name is excluded" in {
      ClaimantNameForm.form.bind(
        Map(
          "firstName" -> "Chris",
          "lastName" -> ""
        )
      ).fold(
        formWithErrors => {
          formWithErrors.errors.head.message shouldBe "Please enter the name of the person claiming"
          formWithErrors.errors.length shouldBe 1
        },
        success => {
          success should not be Some(ClaimantNamePageModel(_, _))
          success shouldBe None
        }
      )
    }

    "throw a ValidationError when the first name and last name are excluded" in {
      ClaimantNameForm.form.bind(
        Map(
          "firstName" -> "",
          "lastName" -> ""
        )
      ).fold(
        formWithErrors => {
          formWithErrors.errors.head.message shouldBe "Please enter the name of the person claiming"
          formWithErrors.errors.length shouldBe 2
        },
        success => {
          success should not be Some(ClaimantNamePageModel(_, _))
          success shouldBe None
        }
      )
    }
  }
}
