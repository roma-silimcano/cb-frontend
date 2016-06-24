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

package uk.gov.hmrc.cb.models.payload.submission.child

import org.joda.time.DateTime
import uk.gov.hmrc.cb.helpers.DateHelpers
import uk.gov.hmrc.cb.mappings.Genders
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by chrisianson on 27/05/16.
  */
class ChildSpec extends UnitSpec {

  "Child" should {

    "instantiate an instance of child" in {
      val dob = DateHelpers.dateWithoutTimeZone(DateTime.now)
      val child = new Child(1, Some("123456"), Some("Shane"), Some("Wilson"), Some(dob), Genders.Male, false)

      child shouldBe a[Child]
      child.id shouldBe 1
      child.birthCertificateReference.get shouldBe "123456"
      child.firstname.get shouldBe "Shane"
      child.surname.get shouldBe "Wilson"
      child.dob.get shouldBe dob
      child.gender shouldBe Genders.Male
      child.previousClaim shouldBe false
    }

    "return a new instance of a child when calling edit full name" in {
      val dob = DateHelpers.dateWithoutTimeZone(DateTime.now)
      val child = Child(1, Some("123456"), Some("Shane"), Some("Wilson"), Some(dob), Genders.Male, false)
      val modified = child.edit("Adam", "Conder")
      modified should not be child
      modified.firstname shouldBe Some("Adam")
      modified.surname shouldBe Some("Conder")
    }

    "return a new instance of a child when calling edit unique reference number" in {
      val dob = DateHelpers.dateWithoutTimeZone(DateTime.now)
      val child = Child(1, Some("34543"), Some("Chris"), Some("Conder"), Some(dob), Genders.Male)
      val modified = child.edit("434434434")
      modified should not be child
      modified.birthCertificateReference shouldBe Some("434434434")
    }

    "return a new instance of a child when calling edit date of birth" in {
      val dob = DateHelpers.dateWithoutTimeZone(DateTime.now)
      val datePlus2Days = dob.plusDays(2)
      val child = Child(1, Some("34543"), Some("Chris"), Some("Conder"), Some(dob), Genders.Male)
      val modified = child.edit(datePlus2Days)
      modified should not be child
      modified.dob shouldBe Some(datePlus2Days)
    }

    "check if the child has a birth certificate reference number - true" in {
      val today = DateHelpers.dateWithoutTimeZone(DateTime.now)
      val child = Child(1, Some("34543"), Some("Chris"), Some("Conder"), Some(today), Genders.Male)
      child.hasBirthCertificateReferenceNumber shouldBe true
    }

    "check if the child has a birth certificate reference number - false" in {
      val today = DateHelpers.dateWithoutTimeZone(DateTime.now)
      val child = Child(1, None, Some("Chris"), Some("Conder"), Some(today), Genders.Male)
      child.hasBirthCertificateReferenceNumber shouldBe false
    }

    "check if the child has a name - true" in {
      val today = DateHelpers.dateWithoutTimeZone(DateTime.now)
      val child = Child(1, Some("34543"), Some("Chris"), Some("Conder"), Some(today), Genders.Male)
      child.hasName shouldBe true
    }

    "check if the child has a name - false" in {
      val today = DateHelpers.dateWithoutTimeZone(DateTime.now)
      val child = Child(1, Some("34543"), None, None, Some(today), Genders.Male)
      child.hasName shouldBe false
    }

    "check if the child has a date of birth - true" in {
      val today = DateHelpers.dateWithoutTimeZone(DateTime.now)
      val child = Child(1, Some("34543"), None, None, Some(today), Genders.Male)
      child.hasDateOfBirth shouldBe true
    }

    "check if the child has a date of birth - false" in {
      val child = Child(1, Some("34543"), None, None, None, Genders.Male)
      child.hasDateOfBirth shouldBe false
    }

  }
}
