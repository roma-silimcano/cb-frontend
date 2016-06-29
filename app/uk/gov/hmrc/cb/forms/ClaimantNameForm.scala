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
import play.api.data.validation.{Invalid, Valid, Constraint}
import play.api.i18n.Messages
import uk.gov.hmrc.cb.config.FrontendAppConfig
import uk.gov.hmrc.cb.forms.constraints.Constraints

/**
  * Created by chrisianson on 23/06/16.
  */
object ClaimantNameForm {

  case class ClaimantNamePageModel(firstName: String, lastName: String)

  lazy val lengthConstraint : Int = FrontendAppConfig.claimaintLengthMaxContraint

  private def existsConstraint : Constraint[String] = Constraint("cb.claimant.name.invalid.excluded"){
    model =>
      if(model.nonEmpty) Valid
      else Invalid(Messages("cb.claimant.name.invalid.excluded"))
  }

  private def validLength : Constraint[String] = Constraint("cb.claimant.name.invalid.length"){
    model =>
      if(model.length <= lengthConstraint)  Valid
      else Invalid(Messages("cb.claimant.name.invalid.length"))
  }

  private def validCharacters : Constraint[String] = Constraint("cb.claimant.name.invalid.character"){
    model =>
      if(model.matches(Constraints.nameConstraint) || model.isEmpty)  Valid
      else Invalid(Messages("cb.claimant.name.invalid.character"))
  }

  val form : Form[ClaimantNamePageModel] = Form(
    mapping(
      "firstName" -> text.verifying(existsConstraint).verifying(validLength).verifying(validCharacters),
      "lastName" -> text.verifying(existsConstraint).verifying(validLength).verifying(validCharacters)
    )(ClaimantNamePageModel.apply)(ClaimantNamePageModel.unapply)
  )

}
