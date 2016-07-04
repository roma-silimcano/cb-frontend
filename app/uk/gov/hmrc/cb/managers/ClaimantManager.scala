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

package uk.gov.hmrc.cb.managers

import uk.gov.hmrc.cb.forms.ClaimantNameForm.ClaimantNamePageModel
import uk.gov.hmrc.cb.models.payload.submission.claimant.Claimant

/**
  * Created by chrisianson on 28/06/16.
  */
object ClaimantManager {

  val claimantService = new ClaimantService

  class ClaimantService {

    def editClaimantName(model: ClaimantNamePageModel, storedClaimant: Option[Claimant]): Claimant = storedClaimant match {
      case Some(x) => x.copy(model.firstName, model.lastName)
      case _ => Claimant(firstName = model.firstName, lastName = model.lastName)
    }
  }

}
