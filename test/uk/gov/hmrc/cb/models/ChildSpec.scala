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

package uk.gov.hmrc.cb.models

import org.joda.time.LocalDate
import uk.gov.hmrc.cb.mappings.Genders
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by chrisianson on 27/05/16.
  */
class ChildSpec extends UnitSpec {

  "Child" should {

    "instantiate an instance of child" in {

      val child = new Child(1, Some("123456"), Some("Shane"), Some("Wilson"), Some(LocalDate.now()), Genders.Male, false)

      child shouldBe a[Child]
      child.id shouldBe 1
      child.birthCertificateReference.get shouldBe "123456"
      child.firstname.get shouldBe "Shane"
      child.surname.get shouldBe "Wilson"
      child.dob.get shouldBe LocalDate.now()
      child.gender shouldBe Genders.Male
      child.previousClaim shouldBe false
    }

    "return a new instance of a child when calling editFullName" in {
      val child = Child(1, Some("123456"), Some("Shane"), Some("Wilson"), Some(LocalDate.now()), Genders.Male, false)
      val modified = child.editFullName("Adam", "Conder")
      modified should not be child
      modified.firstname shouldBe Some("Adam")
      modified.surname shouldBe Some("Conder")
    }

    "return a new instance of a child when calling unique reference number" in {
      val child = Child(1, Some("34543"), Some("Chris"), Some("Conder"), Some(LocalDate.now()), Genders.Male)
      val modified = child.editUniqueReferenceNumber("434434434")
      modified should not be child
      modified.birthCertificateReference shouldBe Some("434434434")
    }
  }
}
