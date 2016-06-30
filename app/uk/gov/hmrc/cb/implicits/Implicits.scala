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

import play.api.Logger
import uk.gov.hmrc.cb.forms.ChildBirthCertificateReferenceForm.ChildBirthCertificateReferencePageModel
import uk.gov.hmrc.cb.forms.ChildDateOfBirthForm.ChildDateOfBirthPageModel
import uk.gov.hmrc.cb.forms.ChildNameForm.ChildNamePageModel
import uk.gov.hmrc.cb.forms.ClaimantNameForm.ClaimantNamePageModel
import uk.gov.hmrc.cb.forms.HelloTestForm.HelloTestPageModel
import uk.gov.hmrc.cb.models.payload.submission.child.Child
import uk.gov.hmrc.cb.models.payload.submission.claimant.Claimant

/**
 * Created by adamconder on 06/06/2016.
 */
object Implicits {

  implicit def childToChildNamePageModel(child : Child) : ChildNamePageModel = {
    val (firstName, lastName) = (child.firstname, child.surname)
    (firstName, lastName) match {
      case (Some(x), Some(y)) => ChildNamePageModel(firstName = x, lastName = y)
      case (_, _) => throw new RuntimeException("[Implicits] child does not have first and last name")
    }
  }

  implicit def childToHelloTestPageModel(child : Child) : HelloTestPageModel = {
    val firstName = child.firstname
    (firstName) match {
      case (Some(x)) => HelloTestPageModel(firstName = x)
      case _ => throw new RuntimeException("[Implicits] child does not have first ")
    }
  }

  implicit def childToChildBirthCertificatePageModel(child : Child) : ChildBirthCertificateReferencePageModel = {
    val birthNumber = child.birthCertificateReference
    birthNumber match {
      case Some(x) => ChildBirthCertificateReferencePageModel(x)
      case _ => throw new RuntimeException("[Implicits] child does not have birth certificate number")
    }
  }

  implicit def childToChildDateOfBirthPageModel(child : Child) : ChildDateOfBirthPageModel = {
    val dateOfBirth = child.dob
    dateOfBirth match {
      case Some(x) => ChildDateOfBirthPageModel(x)
      case _ => throw new RuntimeException("[Implicits] child does not have a date of birth")
    }
  }

  implicit def claimantToClaimantNamePageModel(claimant : Claimant) : ClaimantNamePageModel = {
      ClaimantNamePageModel(firstName = claimant.firstName, lastName = claimant.lastName)
  }

}


