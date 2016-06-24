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

import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import uk.gov.hmrc.cb.config.FrontendAppConfig
import uk.gov.hmrc.cb.forms.constraints.Constraints

/**
  * Created by chrisianson on 23/06/16.
  */
object ClaimantNameForm {

  case class ClaimantNamePageModel(firstName: String, lastName: String)

  lazy val lengthConstraint : Int = FrontendAppConfig.claimaintLengthMaxContraint

  private def validate(t: play.api.data.Mapping[scala.Predef.String]) : play.api.data.Mapping[scala.Predef.String] = {
    t.verifying(Messages("cb.claimant.name.invalid.excluded"), x => x.nonEmpty)
      .verifying(Messages("cb.claimant.name.invalid.length"), x => x.length <= lengthConstraint)
        .verifying(Messages("cb.claimant.name.invalid.character"), x => x.matches(Constraints.nameConstraint))
  }

  val form : Form[ClaimantNamePageModel] = Form(
    mapping(
      "firstName" -> validate(text),
      "lastName" -> validate(text)
    )(ClaimantNamePageModel.apply)(ClaimantNamePageModel.unapply)
  )

}
