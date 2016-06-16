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

import org.joda.time.DateTime

import org.joda.time.format.DateTimeFormat
import play.api.i18n.Messages
import uk.gov.hmrc.cb.CBFakeApplication
import uk.gov.hmrc.cb.forms.ChildDateOfBirthForm.ChildDateOfBirthPageModel
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by adamconder on 13/06/2016.
 */
class ChildDateOfBirthFormSpec extends UnitSpec with CBFakeApplication {

  val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")

  "ChildDateOfBirthForm" should {

    "accept a valid date - now" in {
      val date = DateTime.now()

      val day = date.getDayOfMonth
      val month = date.getMonthOfYear
      val year = date.getYear

      val outputModel = ChildDateOfBirthPageModel(dateOfBirth = date)

      ChildDateOfBirthForm.form.bind(
        Map(
          "dateOfBirth.day" ->  s"$day",
          "dateOfBirth.month" -> s"$month",
          "dateOfBirth.year" -> s"$year"
        )
      ).fold(
        errors =>
          errors.errors shouldBe empty,
        success => {

          val expected = formatter.print(outputModel.dateOfBirth)
          val actual = formatter.print(success.dateOfBirth)

          expected shouldBe actual
        }
      )
    }

    "accept a valid date - 20 years in the past" in {
      val date = DateTime.now() // with timestamp and zone
      val dateOfBirth = date.minusYears(20)

      val day = dateOfBirth.getDayOfMonth
      val month = dateOfBirth.getMonthOfYear
      val year = dateOfBirth.getYear

      val outputModel = ChildDateOfBirthPageModel(dateOfBirth = dateOfBirth)

      ChildDateOfBirthForm.form.bind(
        Map(
          "dateOfBirth.day" ->  s"$day",
          "dateOfBirth.month" -> s"$month",
          "dateOfBirth.year" -> s"$year"
        )
      ).fold(
          errors =>
            errors.errors shouldBe empty,
          success => {
            val expected = formatter.print(outputModel.dateOfBirth)
            val actual = formatter.print(success.dateOfBirth)

            expected shouldBe actual
          }
        )
    }

    "throw a ValidationError - 20 years and 1 day in the past" in {
      val date = DateTime.now() // with timestamp and zone
      val dateOfBirth = date.minusYears(20).minusDays(1)

      val day = dateOfBirth.getDayOfMonth
      val month = dateOfBirth.getMonthOfYear
      val year = dateOfBirth.getYear

      val outputModel = ChildDateOfBirthPageModel(dateOfBirth = dateOfBirth)

      ChildDateOfBirthForm.form.bind(
        Map(
          "dateOfBirth.day" ->  s"$day",
          "dateOfBirth.month" -> s"$month",
          "dateOfBirth.year" -> s"$year"
        )
      ).fold(
          errors =>
            errors.errors.head.message shouldBe "You've told us a date that is too far in the past, please re-enter the child's date of birth",
          success =>
            success should not be outputModel
        )
    }

    "throw a ValidationError - tomorrow" in {
      val date = DateTime.now() // with timestamp and zone
      val dateOfBirth = date.plusDays(1)

      val day = dateOfBirth.getDayOfMonth
      val month = dateOfBirth.getMonthOfYear
      val year = dateOfBirth.getYear

      val outputModel = ChildDateOfBirthPageModel(dateOfBirth = dateOfBirth)

      ChildDateOfBirthForm.form.bind(
        Map(
          "dateOfBirth.day" ->  s"$day",
          "dateOfBirth.month" -> s"$month",
          "dateOfBirth.year" -> s"$year"
        )
      ).fold(
          errors =>
            errors.errors.head.message shouldBe "You've entered a date in the future, please re-enter the child's date of birth",
          success =>
            success should not be outputModel
        )
    }

    "throw a ValidationError for an incorrect day" in {
      val dateOfBirth = DateTime.now() // with timestamp and zone

      val month = dateOfBirth.getMonthOfYear
      val year = dateOfBirth.getYear

      val outputModel = ChildDateOfBirthPageModel(dateOfBirth = dateOfBirth)

      ChildDateOfBirthForm.form.bind(
        Map(
          "dateOfBirth.day" ->  s"32",
          "dateOfBirth.month" -> s"$month",
          "dateOfBirth.year" -> s"$year"
        )
      ).fold(
          errors =>
            Messages(errors.errors.head.message) shouldBe "You've told us a date that does not exist, please re-enter the child's date of birth",
          success =>
            success should not be outputModel
        )
    }

    "throw a ValidationError for an incorrect month" in {
      val dateOfBirth = DateTime.now() // with timestamp and zone

      val day = dateOfBirth.getDayOfMonth
      val year = dateOfBirth.getYear

      val outputModel = ChildDateOfBirthPageModel(dateOfBirth = dateOfBirth)

      ChildDateOfBirthForm.form.bind(
        Map(
          "dateOfBirth.day" ->  s"$day",
          "dateOfBirth.month" -> s"13",
          "dateOfBirth.year" -> s"$year"
        )
      ).fold(
          errors =>
            Messages(errors.errors.head.message) shouldBe "You've told us a date that does not exist, please re-enter the child's date of birth",
          success =>
            success should not be outputModel
        )
    }

    "throw a ValidationError for an incorrect year" in {
      val dateOfBirth = DateTime.now() // with timestamp and zone

      val day = dateOfBirth.getDayOfMonth
      val month = dateOfBirth.getMonthOfYear

      val outputModel = ChildDateOfBirthPageModel(dateOfBirth = dateOfBirth)

      ChildDateOfBirthForm.form.bind(
        Map(
          "dateOfBirth.day" ->  s"$day",
          "dateOfBirth.month" -> s"$month",
          "dateOfBirth.year" -> s"19999"
        )
      ).fold(
          errors =>
            Messages(errors.errors.head.message) shouldBe "You've entered a date in the future, please re-enter the child's date of birth",
          success =>
            success should not be outputModel
        )
    }

    "throw a ValidationError for special characters" in {
      val dateOfBirth = DateTime.now() // with timestamp and zone

      val outputModel = ChildDateOfBirthPageModel(dateOfBirth = dateOfBirth)

      ChildDateOfBirthForm.form.bind(
        Map(
          "dateOfBirth.day" ->  s"@%",
          "dateOfBirth.month" -> s"&%",
          "dateOfBirth.year" -> s"*£"
        )
      ).fold(
          errors =>
            Messages(errors.errors.head.message) shouldBe "The child's date of birth must only contain numbers, please re-enter the child's date of birth",
          success =>
            success should not be outputModel
        )
    }

    "throw a ValidationError when value is negative" in {
      val dateOfBirth = DateTime.now() // with timestamp and zone

      val outputModel = ChildDateOfBirthPageModel(dateOfBirth = dateOfBirth)

      ChildDateOfBirthForm.form.bind(
        Map(
          "dateOfBirth.day" ->  s"-10",
          "dateOfBirth.month" -> s"-12",
          "dateOfBirth.year" -> s"-2010"
        )
      ).fold(
          errors =>
            Messages(errors.errors.head.message) shouldBe "The child's date of birth must only contain numbers, please re-enter the child's date of birth",
          success =>
            success should not be outputModel
        )
    }

    "throw a ValidationError when empty" in {
      val dateOfBirth = DateTime.now() // with timestamp and zone

      val outputModel = ChildDateOfBirthPageModel(dateOfBirth = dateOfBirth)

      ChildDateOfBirthForm.form.bind(
        Map(
          "dateOfBirth.day" ->  "",
          "dateOfBirth.month" -> "",
          "dateOfBirth.year" -> ""
        )
      ).fold(
          errors =>
            Messages(errors.errors.head.message) shouldBe "Please enter the child’s date of birth",
          success =>
            success should not be outputModel
        )
    }

    "pre-populate the form with a value" in {
      val dateOfBirth = DateTime.now() // with timestamp and zone

      val outputModel = ChildDateOfBirthPageModel(dateOfBirth = dateOfBirth)
      val form = ChildDateOfBirthForm.form.fill(outputModel)
      form.get shouldBe outputModel
    }

  }

}
