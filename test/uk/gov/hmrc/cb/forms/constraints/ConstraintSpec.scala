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

package uk.gov.hmrc.cb.forms.constraints

import org.joda.time.DateTime
import org.scalatest.mock.MockitoSugar
import play.api.Logger
import uk.gov.hmrc.cb.CBFakeApplication
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by adamconder on 14/06/2016.
 */
class ConstraintSpec extends UnitSpec with CBFakeApplication with MockitoSugar {

  val formatter = Constraints.dateFormatWithoutTimestamp
  val formatterWithTimestamp = Constraints.dateFormatWithTimestamp

  "Constraints" when {

    "validating dates" should {

      "verify the date is not a future date" in {
        val today = DateTime.parse("2016-06-14T00:00:00+0100", formatterWithTimestamp)
        val yesterday = DateTime.parse("2016-06-13", formatter)
        Constraints.dateIsNotAFutureDate(yesterday, today) shouldBe true
      }

      "verify the date is not a future date - same date" in {
        val today = DateTime.parse("2016-06-14T00:00:00+0100", formatterWithTimestamp)
        Constraints.dateIsNotAFutureDate(today, today) shouldBe true
      }

      "verify the date is a future date" in {
        val today = DateTime.parse("2016-06-14T00:00:00+0100", formatterWithTimestamp)
        val tomorrow = DateTime.parse("2016-06-15", formatter)
        Constraints.dateIsNotAFutureDate(tomorrow, today) shouldBe false
      }

      "verify the child's age is before the limit" in {
        val today = DateTime.parse("2016-06-14", formatter)
        val dateOfBirth = DateTime.parse("1996-06-13", formatter)
        Constraints.dateOfBirthIsEqualToOrAfterChildAgeLimit(dateOfBirth, today) shouldBe false
      }

      "verify the child's age is same date as the limit" in {
        val today = DateTime.parse("2016-06-14", formatter)
        val dateOfBirth = DateTime.parse("1996-06-14", formatter)
        Constraints.dateOfBirthIsEqualToOrAfterChildAgeLimit(dateOfBirth, today) shouldBe true
      }

      "verify the child's age is after the limit" in {
        val today = DateTime.parse("2016-06-14", formatter)
        val dateOfBirth = DateTime.parse("1996-06-15", formatter)
        Constraints.dateOfBirthIsEqualToOrAfterChildAgeLimit(dateOfBirth, today) shouldBe true
      }

    }

  }

}
