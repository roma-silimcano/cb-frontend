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
import uk.gov.hmrc.cb.forms.ChildNameForm.ChildNamePageModel
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}

/**
 * Created by adamconder on 27/05/2016.
 */
class ChildNameFormSpec extends UnitSpec with WithFakeApplication {

  // TODO do we allow spaces? such as: 'van helden'
  // TODO is O'brian a valid surname?

  val maxLength = "tZWfHEhJlSIdAuSnaSjGgWTWeYWrPXYrMPmycvLyJXfSmrIcqrvgrDtISXsdhaOPKAKzHAfzUOiKzIIVkyEbEeCSrVtwFYcyUHQAzFfAFMaFckTtycGiGQrEbIsPsuMW"

  "ChildNameForm" should {

    /* valid */

    "accept a valid value for first name and last name" in {
      val data = ChildNamePageModel(firstName = "adam", lastName = "conder")
      ChildNameForm.form.bind(
        Map(
          "firstName" -> "adam",
          "lastName" -> "conder"
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

    "accept a valid max length value for first name and last name" in {
      val data = ChildNamePageModel(
        firstName = maxLength,
        lastName = maxLength
      )
      ChildNameForm.form.bind(
        Map(
          "firstName" -> maxLength,
          "lastName" -> maxLength
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

    "accept a valid UTF-8 character for first name and last name" ignore {
      val data = ChildNamePageModel(
        firstName = "AƉam",
        lastName = "Ἀχαιός"
      )
      ChildNameForm.form.bind(
        Map(
          "firstName" -> "AƉam",
          "lastName" -> "Ἀχαιός"
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

    "accept a valid Logographic character for first name and last name" ignore {
      val data = ChildNamePageModel(
        firstName = "亚当",
        lastName = "亚当"
      )
      ChildNameForm.form.bind(
        Map(
          "firstName" -> "亚当",
          "lastName" -> "亚当"
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

    /* special */

    "throw a ValidationError when provided special characters for last name" in {
      ChildNameForm.form.bind(
        Map(
          "firstName" -> "adam",
          "lastName" -> "@£$^%&&&&"
        )
      ).fold(
         hasErrors =>
           hasErrors.errors.head.message shouldBe Messages("cb.error.child.last.name.invalid"),
          success => {
            success should not be Some(ChildNamePageModel(_, _))
            success shouldBe None
          }
        )
    }

    "throw a ValidationError when provided special characters for first name" in {
      ChildNameForm.form.bind(
        Map(
          "firstName" -> "@%^&$@!",
          "lastName" -> "Conder"
        )
      ).fold(
            hasErrors =>
              hasErrors.errors.head.message shouldBe Messages("cb.error.child.first.name.invalid"),
            success => {
              success should not be Some(ChildNamePageModel(_, _))
              success shouldBe None
            }
        )
    }

    "throw a ValidationError when provided special characters for last name and first name" in {
      ChildNameForm.form.bind(
        Map(
          "firstName" -> "@%^&$@!",
          "lastName" -> "$%^@@@2"
        )
      ).fold(
            hasErrors => {
              hasErrors.errors.head.message shouldBe Messages("cb.error.child.first.name.invalid")
              hasErrors.errors.last.message shouldBe Messages("cb.error.child.last.name.invalid")
            },
            success => {
              success should not be Some(ChildNamePageModel(_, _))
              success shouldBe None
            }
      )
    }

    /* numbers */

    "throw a ValidationError when provided numbers for last name" in {
      ChildNameForm.form.bind(
        Map(
          "firstName" -> "Adam",
          "lastName" -> "12345"
        )
      ).fold(
            hasErrors => {
              hasErrors.errors.head.message shouldBe Messages("cb.error.child.last.name.invalid")
            },
            success => {
              success should not be Some(ChildNamePageModel(_, _))
              success shouldBe None
            }
      )
    }

    "throw a ValidationError when provided numbers for first name" in {
      ChildNameForm.form.bind(
        Map(
          "firstName" -> "12345",
          "lastName" -> "Conder"
        )
      ).fold(
          hasErrors => {
            hasErrors.errors.head.message shouldBe Messages("cb.error.child.first.name.invalid")
          },
          success => {
            success should not be Some(ChildNamePageModel(_, _))
            success shouldBe None
          }
      )
    }

    "throw a ValidationError when provided numbers for last name and first name" in {
      ChildNameForm.form.bind(
        Map(
          "firstName" -> "12345",
          "lastName" -> "12345"
        )
      ).fold(
          hasErrors => {
            hasErrors.errors.head.message shouldBe Messages("cb.error.child.first.name.invalid")
            hasErrors.errors.last.message shouldBe Messages("cb.error.child.last.name.invalid")
          },
          success => {
            success should not be Some(ChildNamePageModel(_, _))
            success shouldBe None
          }
      )
    }


    /* empty */

    "throw a ValidationError for an empty first name" in {
      ChildNameForm.form.bind(
        Map(
          "firstName" -> "",
          "lastName" -> "conder"
        )
      ).fold(
          hasErrors => {
            hasErrors.errors.head.message shouldBe Messages("cb.error.child.first.name.invalid")
          },
          success => {
            success should not be Some(ChildNamePageModel(_, _))
            success shouldBe None
          }
      )
    }

    "throw a ValidationError for an empty last name" in {
      ChildNameForm.form.bind(
        Map(
          "firstName" -> "adam",
          "lastName" -> ""
        )
      ).fold(
          hasErrors => {
            hasErrors.errors.last.message shouldBe Messages("cb.error.child.last.name.invalid")
          },
          success => {
            success should not be Some(ChildNamePageModel(_, _))
            success shouldBe None
          }
      )

    }

    "throw a ValidationError for an empty first name and an empty last name" in {
      ChildNameForm.form.bind(
        Map(
          "firstName" -> "",
          "lastName" -> ""
        )
      ).fold(
          hasErrors => {
            hasErrors.errors.head.message shouldBe Messages("cb.error.child.first.name.invalid")
            hasErrors.errors.last.message shouldBe Messages("cb.error.child.last.name.invalid")
          },
          success => {
            success should not be Some(ChildNamePageModel(_, _))
            success shouldBe None
          }
      )
    }

    /* range */

    "throw a ValidationError for a value larger than max length for first name" in {
      ChildNameForm.form.bind(
        Map(
          "firstName" -> {maxLength + "s"},
          "lastName" -> "conder"
        )).fold(
          hasErrors => {
            hasErrors.errors.head.message shouldBe Messages("cb.error.child.first.name.invalid")
          },
          success => {
            success should not be Some(ChildNamePageModel(_, _))
            success shouldBe None
          }
      )
    }

    "throw a ValidationError for a value larger than max length for last name" in {
      ChildNameForm.form.bind(
        Map(
          "firstName" -> "adam",
          "lastName" -> {maxLength + "s"}
        )).fold(
          hasErrors => {
            hasErrors.errors.last.message shouldBe Messages("cb.error.child.last.name.invalid")
          },
          success => {
            success should not be Some(ChildNamePageModel(_, _))
            success shouldBe None
          }
      )
    }

    "throw a ValidationError for a value larger than max length for first name and last name" in {
      ChildNameForm.form.bind(
        Map(
          "firstName" -> {maxLength + "s"},
          "lastName" -> {maxLength + "s"}
        )).fold(
          hasErrors => {
            hasErrors.errors.head.message shouldBe Messages("cb.error.child.first.name.invalid")
            hasErrors.errors.last.message shouldBe Messages("cb.error.child.last.name.invalid")
          },
          success => {
            success should not be Some(ChildNamePageModel(_, _))
            success shouldBe None
          }
      )
    }

    "return values from the form" in {
      val data = ChildNamePageModel("adam", "conder")
      val form = ChildNameForm.form.fill(data)
      form.get shouldBe data
    }

  }

}
