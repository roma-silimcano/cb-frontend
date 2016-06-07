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

package uk.gov.hmrc.cb.implicits

import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.cb.CBFakeApplication
import uk.gov.hmrc.cb.forms.ChildBirthCertificateReferenceForm.ChildBirthCertificateReferencePageModel
import uk.gov.hmrc.cb.forms.ChildNameForm.ChildNamePageModel
import uk.gov.hmrc.cb.models.Child
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by adamconder on 06/06/2016.
 */
class ImplicitsSpec extends UnitSpec with CBFakeApplication with MockitoSugar {

  "Implicits" should {

    "covert Child to ChildNamePageModel" in {
      import uk.gov.hmrc.cb.implicits.Implicits._

      val child = Child(id = 1, firstname = Some("Adam"), surname = Some("Conder"))
      val pageModel : ChildNamePageModel = child
      pageModel shouldBe ChildNamePageModel(firstName = "Adam", lastName = "Conder")
    }

    "throw an exception when converting to ChildNamePageModel from Child" in {
      import uk.gov.hmrc.cb.implicits.Implicits._

      val child = Child(id = 1, firstname = None, surname = None)

      intercept[RuntimeException] {
        val pageModel: ChildNamePageModel = child
        pageModel.firstName
      }
    }

    "covert Child to ChildBirthCertificatePageModel" in {
      import uk.gov.hmrc.cb.implicits.Implicits._

      val child = Child(id = 1, birthCertificateReference = Some("12345"))
      val pageModel : ChildBirthCertificateReferencePageModel = child
      pageModel shouldBe ChildBirthCertificateReferencePageModel("12345")
    }

    "throw an exception when converting to ChildBirthCertificatePageModel from Child" in {
      import uk.gov.hmrc.cb.implicits.Implicits._

      val child = Child(id = 1, birthCertificateReference = None)
      intercept[RuntimeException] {
        val pageModel : ChildBirthCertificateReferencePageModel = child
        pageModel.birthCertificateReference
      }
    }

  }

}
